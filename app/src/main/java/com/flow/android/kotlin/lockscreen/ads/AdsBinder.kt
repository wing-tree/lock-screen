package com.flow.android.kotlin.lockscreen.ads

import android.view.LayoutInflater
import android.view.ViewGroup
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.NativeAdvancedBinding
import com.flow.android.kotlin.lockscreen.util.fadeIn
import com.flow.android.kotlin.lockscreen.util.fadeOut
import com.flow.android.kotlin.lockscreen.util.hide
import com.flow.android.kotlin.lockscreen.util.show
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import timber.log.Timber

class AdsBinder {
    private val duration = 150L

    private var _viewBinding: NativeAdvancedBinding? = null
    private val viewBinding: NativeAdvancedBinding
        get() = _viewBinding!!

    fun loadAds(container: ViewGroup) {
        val context = container.context
        val adLoader = AdLoader.Builder(context, context.getString(R.string.admob_app_id))
                .forNativeAd { nativeAd ->
                    _viewBinding = NativeAdvancedBinding.inflate(LayoutInflater.from(context))

                    viewBinding.closeWindow.setOnClickListener {
                        container.fadeOut(duration) {
                            container.removeAllViews()
                        }

                        nativeAd.destroy()
                    }

                    populateNativeAdView(nativeAd)

                    container.removeAllViews()
                    container.addView(viewBinding.root)

                    container.fadeIn(duration)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Timber.e(loadAdError.message)
                    }
                })
                .withNativeAdOptions(NativeAdOptions
                        .Builder()
                        .setRequestCustomMuteThisAd(true)
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build()
                ).build()
        val adRequest = AdRequest.Builder().build()

        adLoader.loadAd(adRequest)
    }

    private fun populateNativeAdView(nativeAd: NativeAd) {
        nativeAd.icon?.let {
            viewBinding.icon.show()
            viewBinding.icon.setImageDrawable(it.drawable)
        } ?: let { viewBinding.icon.hide() }

        nativeAd.mediaContent?.let {
            viewBinding.mediaContent.show()
            viewBinding.mediaContent.setMediaContent(it)
        } ?: let { viewBinding.mediaContent.hide() }

        nativeAd.advertiser?.let {
            viewBinding.advertiser.text = it
            viewBinding.advertiser.show()
        } ?: let { viewBinding.advertiser.hide(true) }

        nativeAd.body?.let {
            viewBinding.body.text = it
            viewBinding.body.show()
        } ?: let { viewBinding.body.hide(true) }

        nativeAd.callToAction?.let {
            viewBinding.callToAction.text = it
            viewBinding.callToAction.show()
        } ?: let { viewBinding.callToAction.hide(true) }

        viewBinding.headline.text = nativeAd.headline

        nativeAd.price?.let {
            viewBinding.price.text = nativeAd.price
            viewBinding.price.show()
        } ?: let { viewBinding.price.hide(true) }

        nativeAd.starRating?.let {
            viewBinding.starRating.show()
            viewBinding.starRating.rating = it.toFloat()
        } ?: let { viewBinding.starRating.hide(true) }

        nativeAd.store?.let {
            viewBinding.store.text = it
            viewBinding.store.show()
        } ?: let { viewBinding.store.hide(true) }

        viewBinding.root.callToActionView = viewBinding.callToAction
        viewBinding.root.setNativeAd(nativeAd)
    }

    fun clear() {
        _viewBinding = null
    }
}