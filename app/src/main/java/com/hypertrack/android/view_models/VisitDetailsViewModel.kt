package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatus
import com.hypertrack.android.repository.VisitsRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class VisitDetailsViewModel(
    private val visitsRepository: VisitsRepository,
    private val id: String
) : ViewModel() {

    val visit: LiveData<Visit> = visitsRepository.visitForId(id)
    private val _takePictureButton =  MediatorLiveData<Boolean>()
    private val _pickUpButton =  MediatorLiveData<Boolean>()
    private val _checkInButton =  MediatorLiveData<Boolean>()
    private val _checkOutButton =  MediatorLiveData<Boolean>()
    private val _cancelButton =  MediatorLiveData<Boolean>()
    private var _visitNote = MediatorLiveData<Pair<String, Boolean>>() //
    private var _showToast = MutableLiveData(false)
    private var updatedNote: String? = null

    init {
        _visitNote.addSource(visitsRepository.visitForId(id)) {
            _visitNote.postValue(it.visitNote to visitsRepository.canEdit(id))
        }

        for ((model, targetState) in listOf(
            _takePictureButton to VisitStatus.COMPLETED,
            _pickUpButton to VisitStatus.PICKED_UP,
            _checkInButton to VisitStatus.VISITED,
            _checkOutButton to VisitStatus.COMPLETED,
            _cancelButton to VisitStatus.CANCELLED
        )) {
            model.addSource(visit) {
                model.postValue(visitsRepository.transitionAllowed(targetState, id))
            }
            model.addSource(visitsRepository.isTracking) {
                model.postValue(visitsRepository.transitionAllowed(targetState, id))
            }
        }

    }

    val visitNote: LiveData<Pair<String, Boolean>>
        get() = _visitNote
    val showToast: LiveData<Boolean>
        get() = _showToast
    val takePictureButton: LiveData<Boolean>
        get() = _takePictureButton
    val pickUpButton: LiveData<Boolean>
        get() = _pickUpButton
    val checkInButton: LiveData<Boolean>
        get() =  _checkInButton
    val checkOutButton: LiveData<Boolean>
        get() = _checkOutButton
    val cancelButton: LiveData<Boolean>
        get() = _cancelButton

    fun onVisitNoteChanged(newNote : String) {
        // Log.d(TAG, "onVisitNoteChanged $newNote")
        updatedNote = newNote
    }

    fun onPickUpClicked() = visitsRepository.setPickedUp(id, updatedNote)

    fun onCheckInClicked() = visitsRepository.setVisited(id, updatedNote)

    fun onCheckOutClicked() = visitsRepository.setCompleted(id, updatedNote)

    fun onCancelClicked() = visitsRepository.setCancelled(id, updatedNote)

    fun getLatLng(): LatLng?  {
        visit.value?.latitude?.let { lat -> visit.value?.longitude?.let { lng -> return LatLng(lat, lng) } }
        return null
    }

    fun getLabel() : String = "Parcel ${visit.value?._id?:"unknown"}"

    fun onBackPressed() = updateNote()

    private fun updateNote() {
        // Log.v(TAG, "updateNote")
        updatedNote?.let {
            if (visitsRepository.updateVisitNote(id, it))
                _showToast.postValue(true)
        }
    }

    fun onPictureResult(path: String) {
        // Log.d(TAG, "onPicResult $path")
        MainScope().launch { visitsRepository.setImage(id, path) }
    }

    companion object {const val TAG = "VisitDetailsVM"}
}
