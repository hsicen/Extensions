@file:Suppress("MemberVisibilityCanBePrivate")

package com.hsicen.extension.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import com.hsicen.extension.log.KLog

/**
 * 作者：hsicen  3/21/21 10:50 AM
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：自定义ViewGroup基类
 *
 * measuredWidth：是在measure中赋值的
 * width：是在layout或onLayout中赋值的
 * 在onDraw中尽量使用width参数
 */
abstract class CustomLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

  protected abstract fun onMeasureChildren(widthMeasureSpec: Int, heightMeasureSpec: Int)

  class LayoutParams(width: Int, height: Int) : MarginLayoutParams(width, height)

  override fun generateDefaultLayoutParams(): LayoutParams {
    return LayoutParams(matchParent, wrapContent)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    this.onMeasureChildren(widthMeasureSpec, heightMeasureSpec)
  }

  protected fun View.autoMeasure() {
    if (isGone) return
    measure(
      this.defaultWidthMeasureSpec(this@CustomLayout),
      this.defaultHeightMeasureSpec(this@CustomLayout)
    )
  }

  protected fun View.forEachAutoMeasure() {
    forEach { it.autoMeasure() }
  }

  protected fun View.layout(x: Int, y: Int, fromRight: Boolean = false) {
    if (isGone) return
    if (fromRight) {
      layout(this@CustomLayout.measuredWidth - measuredWidth - x, y)
    } else {
      layout(x, y, x + measuredWidth, y + measuredHeight)
    }
  }

  protected val View.measuredWidthWithMargins
    get() = (measuredWidth + marginLeft + marginRight)

  protected val View.measuredHeightWithMargins
    get() = (measuredHeight + marginTop + marginBottom)

  protected fun View.defaultWidthMeasureSpec(parentView: ViewGroup): Int {
    return when (layoutParams.width) {
      ViewGroup.LayoutParams.MATCH_PARENT -> parentView.measuredWidth.toExactlyMeasureSpec()
      ViewGroup.LayoutParams.WRAP_CONTENT -> Int.MAX_VALUE.toAtMostMeasureSpec()
      0 -> throw IllegalAccessException("Need special treatment for $this")
      else -> layoutParams.width.toExactlyMeasureSpec()
    }
  }

  protected fun View.defaultHeightMeasureSpec(parentView: ViewGroup): Int {
    return when (layoutParams.height) {
      ViewGroup.LayoutParams.MATCH_PARENT -> parentView.measuredHeight.toExactlyMeasureSpec()
      ViewGroup.LayoutParams.WRAP_CONTENT -> Int.MAX_VALUE.toAtMostMeasureSpec()
      0 -> throw IllegalAccessException("Need special treatment for $this")
      else -> layoutParams.height.toExactlyMeasureSpec()
    }
  }

  protected fun Int.toExactlyMeasureSpec(): Int {
    return MeasureSpec.makeMeasureSpec(this, MeasureSpec.EXACTLY)
  }

  protected fun Int.toAtMostMeasureSpec(): Int {
    return MeasureSpec.makeMeasureSpec(this, MeasureSpec.AT_MOST)
  }

  fun addView(child: View, width: Int, height: Int, apply: (LayoutParams.() -> Unit)) {
    val params = generateDefaultLayoutParams()
    params.apply { apply.invoke(this) }
    super.addView(child, params)
  }

  fun View.overScrollNever() {
    overScrollMode = View.OVER_SCROLL_NEVER
  }

  fun ViewGroup.horizontalCenterX(child: View): Int {
    return (measuredWidth - child.measuredWidth) / 2
  }

  fun ViewGroup.verticalCenterTop(child: View): Int {
    return (measuredHeight - child.measuredHeight) / 2
  }
}

const val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
const val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT

fun View.transparentBackground() {
  setBackgroundColor(Color.TRANSPARENT)
}

val View.parentView get() = parent as ViewGroup

fun View?.performHapticFeedbackSafely() {
  try {
    this?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
  } catch (t: Throwable) {
    KLog.e(t)
  }
}
