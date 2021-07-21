package com.flow.android.kotlin.lockscreen.review

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.util.goToPlayStore
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
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

    // todo 아래 참고, 최초 요청시 인포 들고 있기. 필요 없을 수도?
//    private var alreadyAskedForReview = false
//    private var reviewInfo: Deferred<ReviewInfo>? = null
//
//    /**
//     * Start requesting the review info that will be needed later in advance.
//     */
//    @MainThread
//    fun preWarmReview() {
//        if (!alreadyAskedForReview && reviewInfo == null) {
//            reviewInfo = viewModelScope.async { reviewManager.requestReview() }
//        }
//    }
//
//    /**
//     * Only return ReviewInfo object if the prewarming has already completed,
//     * i.e. if the review can be launched immediately.
//     */
//    @OptIn(ExperimentalCoroutinesApi::class)
//    suspend fun obtainReviewInfo(): ReviewInfo? = withContext(Dispatchers.Main.immediate) {
//        if (reviewInfo?.isCompleted == true && reviewInfo?.isCancelled == false) {
//            reviewInfo?.getCompleted().also {
//                reviewInfo = null
//            }
//        } else null
//    }
//
//    /**
//     * The view should call this to let the ViewModel know that an attemt to show the review dialog
//     * was made.
//     *
//     * A real app could record the time when this request was made to implement a back-off strategy.
//     *
//     * @see alreadyAskedForReview
//     */
//    fun notifyAskedForReview() {
//        alreadyAskedForReview = true
//    }
}