package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.*
import com.hypertrack.android.repository.VisitsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class VisitsManagementViewModel(private val visitsRepository: VisitsRepository) : ViewModel() {

    private val _clockinButtonText = MediatorLiveData<CharSequence>()
    init {
        _clockinButtonText.addSource(visitsRepository.isTracking) { tracking ->
            _clockinButtonText.postValue(if (tracking) "Clock Out" else "Clock In")
        }
    }
    val clockinButtonText: LiveData<CharSequence>
        get() = _clockinButtonText

    private val _checkinButtonText = MediatorLiveData<CharSequence>()
    init {
        _checkinButtonText.addSource(visitsRepository.hasOngoingLocalVisit) { hasVisit ->
            _checkinButtonText.postValue(if (hasVisit) "CheckOut" else "CheckIn")
        }
    }
    val checkinButtonText: LiveData<CharSequence>
        get() = _checkinButtonText

    private val _showSpinner = MutableLiveData(false)
    val showSpinner: LiveData<Boolean>
        get() = _showSpinner

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String>
        get() = _showToast

    private val _enableCheckin = MediatorLiveData<Boolean>()
    init {
        _enableCheckin.addSource(visitsRepository.isTracking) { _enableCheckin.postValue(it) }
    }
    val enableCheckin: LiveData<Boolean>
        get() = _enableCheckin

    fun refreshVisits() {
        if (_showSpinner.value == true) return

        _showSpinner.postValue(true)

         val coroutineExceptionHandler = CoroutineExceptionHandler{_ , throwable ->
            Log.e(TAG, "Got error $throwable in coroutine")
        }
        try {
            MainScope().launch(Dispatchers.IO + coroutineExceptionHandler) {
                     visitsRepository.refreshVisits()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Got error $e refreshing visits")
            _showToast.postValue("Got error refreshing visits $e")
        } finally {
            _showSpinner.postValue(false)
        }
    }

    fun switchTracking() {
        Log.v(TAG, "switchTracking")
        _showSpinner.postValue(true)
        viewModelScope.launch {
            visitsRepository.switchTracking()
            _showSpinner.postValue(false)

        }
    }

    fun checkin() {
        Log.v(TAG, "checkin")
        visitsRepository.processLocalVisit()
    }

    fun possibleLocalVisitCompletion(): Unit {
        // Local visit change affects Check In/ Check Out state
        visitsRepository.checkLocalVisitCompleted()
    }

    val visits = visitsRepository.visitListItems
    val statusLabel = visitsRepository.statusLabel

    companion object {
        const val TAG = "VisitsManagementVM"
    }

}

