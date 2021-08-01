package com.flow.android.kotlin.lockscreen.review

import android.app.Activity
import android.widget.Toast
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.util.goToPlayStore
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

object Review {
    fun launchReviewFlow(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        reviewManager.requestReviewFlow().apply {
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reviewManager.launchReviewFlow(activity, task.result).apply {
                        addOnCompleteListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                activity.getString(R.string.review_000).run {
                                    Toast.makeText(activity, this, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                } else {
                    Timber.e(task.exception)
                    goToPlayStore(activity)
                }
            }
        }
    }
}