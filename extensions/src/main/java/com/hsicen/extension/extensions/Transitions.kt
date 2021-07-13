package com.hsicen.extension.extensions

import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.transition.Transition
import androidx.transition.TransitionSet

/**
 * 作者：hsicen  7/13/21 17:25
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：Transitions扩展
 */

inline fun TransitionSet.forEach(action: (transition: Transition) -> Unit) {
    for (i in 0 until transitionCount) {
        action(getTransitionAt(i) ?: throw IndexOutOfBoundsException())
    }
}

inline fun TransitionSet.forEachIndexed(action: (index: Int, transition: Transition) -> Unit) {
    for (i in 0 until transitionCount) {
        action(i, getTransitionAt(i) ?: throw IndexOutOfBoundsException())
    }
}

operator fun TransitionSet.plusAssign(transition: Transition) {
    addTransition(transition)
}

fun View.bottomAlphaIn(
    endAction: (() -> Unit)? = null,
    startAction: (() -> Unit)? = null,
    startPercent: Float = 1f,
    animTime: Long = 300L
) {
    this.post {
        this.translationY = this.height.toFloat() / startPercent
        this.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(animTime)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { endAction?.invoke() }
            .withStartAction {
                this.isVisible = true
                startAction?.invoke()
            }
            .start()
    }
}

fun View.topAlphaOut(
    endAction: (() -> Unit)? = null,
    startAction: (() -> Unit)? = null,
    endPercent: Float = 1f,
    animTime: Long = 300L
) {
    this.post {
        this.animate()
            .translationY(this.height.toFloat() / endPercent)
            .alpha(0f)
            .setDuration(animTime)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                this.isVisible = false
                endAction?.invoke()
            }
            .withStartAction { startAction?.invoke() }
            .start()
    }
}

fun View.alphaIn(
    endAction: (() -> Unit)? = null,
    startAction: (() -> Unit)? = null,
    animTime: Long = 300L
) {
    this.post {
        this.animate()
            .alpha(1f)
            .setDuration(animTime)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { endAction?.invoke() }
            .withStartAction {
                this.isVisible = true
                startAction?.invoke()
            }
            .start()
    }
}

fun View.alphaOut(
    endAction: (() -> Unit)? = null,
    startAction: (() -> Unit)? = null,
    animTime: Long = 300L
) {
    this.post {
        this.animate()
            .alpha(0f)
            .setDuration(animTime)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                this.isVisible = false
                endAction?.invoke()
            }
            .withStartAction { startAction?.invoke() }
            .start()
    }
}
