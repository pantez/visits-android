package com.hypertrack.android.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.util.LinkProperties
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

interface DeeplinkProcessor {
    fun appOnCreate(application: Application)
    fun activityOnStart(activity: Activity, intent: Intent?, resultListener: DeeplinkResultListener)
    fun activityOnNewIntent(
        activity: Activity,
        intent: Intent?,
        resultListener: DeeplinkResultListener
    )
}

interface DeeplinkResultListener {
    fun onDeeplinkResult(parameters: Map<String, Any>)
}

class BranchIoDeepLinkProcessor(
    private val crashReportsProvider: CrashReportsProvider
) : DeeplinkProcessor {

    override fun appOnCreate(application: Application) {
        //        Branch.enableLogging()
        Branch.getAutoInstance(application)
    }

    override fun activityOnStart(
        activity: Activity,
        intent: Intent?,
        resultListener: DeeplinkResultListener
    ) {
        intent?.let {
            try {
                Branch.sessionBuilder(activity)
                    .withCallback(Branch2ResultListenerAdapter(resultListener))
                    .withData(intent.data)
                    .init()
            } catch (e: Throwable) {
                crashReportsProvider.logException(e)
                resultListener.onDeeplinkResult(mapOf("error" to e))
            }
        } ?: resultListener.onDeeplinkResult(emptyMap())
    }

    override fun activityOnNewIntent(
        activity: Activity,
        intent: Intent?,
        resultListener: DeeplinkResultListener
    ) {
        intent?.let {
            intent.putExtra("branch_force_new_session", true)
            activity.intent = intent
            try {
                Branch.sessionBuilder(activity)
                    .withCallback(Branch2ResultListenerAdapter(resultListener))
                    .withData(intent.data)
                    .reInit()
            } catch (e: Throwable) {
                crashReportsProvider.logException(e)
                resultListener.onDeeplinkResult(mapOf("error" to e))
            }
        } ?: resultListener.onDeeplinkResult(emptyMap())
    }

}

private class Branch2ResultListenerAdapter(
    val deeplinkResultListener: DeeplinkResultListener
) : Branch.BranchUniversalReferralInitListener {
    override fun onInitFinished(
        branchUniversalObject: BranchUniversalObject?,
        linkProperties: LinkProperties?,
        error: BranchError?
    ) =
        deeplinkResultListener.onDeeplinkResult(
             branchUniversalObject?.contentMetadata?.customMetadata?: emptyMap()
         )
}

class MockDeeplinkProcessor: DeeplinkProcessor {

    private val key = "your_publishable_key"
    private val driverId = "your_driver_id"

    override fun appOnCreate(application: Application) { }

    override fun activityOnStart(
        activity: Activity,
        intent: Intent?,
        resultListener: DeeplinkResultListener
    ) {
        GlobalScope.launch {
            delay(TimeUnit.SECONDS.toMillis(2L))
            Log.d(TAG, "Injecting fake deeplink payload")
            resultListener.onDeeplinkResult(mapOf("publishable_key" to key, "driver_id" to driverId))
        }
    }

    override fun activityOnNewIntent(
        activity: Activity,
        intent: Intent?,
        resultListener: DeeplinkResultListener
    ) { }
    companion object {const val TAG = "MockDeeplink"}
}