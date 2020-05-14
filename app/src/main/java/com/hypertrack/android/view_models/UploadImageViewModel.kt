package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.Delivery
import com.hypertrack.android.repository.Driver
import com.hypertrack.android.repository.UpdateDeliveryRepo
import okhttp3.RequestBody

class UploadImageViewModel(application : Application) : AndroidViewModel(application) {

    var updateRepo: UpdateDeliveryRepo? = null


    var updateImageModel: LiveData<Delivery>? = null

    private var changeMediator: MediatorLiveData<Driver>? = null

    init {

        updateRepo = UpdateDeliveryRepo(application)

        changeMediator = MediatorLiveData()

        changePasswordApiResponse()
    }

    // call repo method for init API
    fun callUpdateImage(deliveryId : String,body: RequestBody) {

        updateRepo?.callUuploadImage(deliveryId,body)
    }

    // add response here for getting
    private fun changePasswordApiResponse() {


        updateImageModel = updateRepo?.getImageResponse()


        changeMediator?.addSource(updateImageModel!!) {

            print("Check in repo")
        }
    }
}