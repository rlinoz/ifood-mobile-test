package com.rlino.ifoodtwitterchallenge

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

fun ViewGroup.showLoadingOverlay() {
    val loadingView = LayoutInflater.from(context).inflate(R.layout.loading_overlay, this, false)
    loadingView.tag = context.getText(R.string.loading_view_tag)
    addView(loadingView)
    loadingView.setOnClickListener { }
    loadingView.bringToFront()
    invalidate()
}

fun ViewGroup.hideLoadingOverlay() {
    removeView(findViewWithTag(context.getText(R.string.loading_view_tag)))
}

fun ViewGroup.fadeIn(duration: Long = 250) {
    visibility = View.VISIBLE
    startAnimation(AlphaAnimation(0.0f, 1.0f).apply {
        this.duration = duration
        this.fillAfter = true
    })
}

fun ViewGroup.fadeOut(duration: Long = 250) {
    startAnimation(AlphaAnimation(1.0f, 0.0f).apply {
        this.duration = duration
        this.fillAfter = true
    })
    visibility = View.GONE
}

fun ViewGroup.blink(delay: Long = 700) {
    fadeIn()
    Handler().postDelayed({
        fadeOut()
    }, delay)
}

fun Int.toEmojiString(): String = String(Character.toChars(this))


fun <T> Single<T>.defaultSchedulers(): Single<T> = subscribeOn(Schedulers.io()).
        observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.logErrors(): Single<T> = doOnError { Timber.e(it) }

fun <R, T> Single<T>.retryWhenWithLimit(times: Int = 3, condition: (Throwable) -> Flowable<R> = { Flowable.error(it)} ): Single<T> {
    return retryWhen { attempts ->
        attempts.zipWith(Flowable.range(1, times), BiFunction<Throwable, Int, Throwable> { t1, _ ->  t1 })
                .flatMap {
                    condition(it)
                }
    }
}