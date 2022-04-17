package com.hsicen.extension.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Checkable
import androidx.constraintlayout.widget.ConstraintLayout
import com.hsicen.extension.widget.helper.RCHelper
import java.util.*

/**
 * @author: hsicen
 * @date: 4/17/22 19:45
 * @email: codinghuang@163.com
 * description: 圆角 ConstraintLayout
 */
class RCConstraintLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Checkable, RCHelper.RCAttrs {
  private val mRCHelper = RCHelper().apply {
    initAttrs(context, attrs)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    mRCHelper.onSizeChanged(this, w, h)
  }

  override fun invalidate() {
    kotlin.runCatching {
      mRCHelper.refreshRegion(this)
    }
    super.invalidate()
  }

  override fun dispatchDraw(canvas: Canvas) {
    canvas.saveLayer(mRCHelper.mLayer, null, Canvas.ALL_SAVE_FLAG)
    super.dispatchDraw(canvas)

    mRCHelper.onClipDraw(canvas)
    canvas.restore()
  }

  override fun draw(canvas: Canvas) {
    if (mRCHelper.mClipBackground) {
      canvas.save()
      canvas.clipPath(mRCHelper.mClipPath)
      super.draw(canvas)

      canvas.restore()
    } else super.draw(canvas)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    val action = ev.action
    if (action == MotionEvent.ACTION_DOWN
      && !mRCHelper.mAreaRegion.contains(ev.x.toInt(), ev.y.toInt())
    ) return false

    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
      refreshDrawableState()
    } else if (action == MotionEvent.ACTION_CANCEL) {
      isPressed = false
      refreshDrawableState()
    }

    return super.dispatchTouchEvent(ev)
  }

  /***=========== 公开接口 ========== */
  override fun setRadius(radius: Float) {
    Arrays.fill(mRCHelper.radii, radius)
    invalidate()
  }

  override var isClipBackground: Boolean
    get() = mRCHelper.mClipBackground
    set(clipBackground) {
      mRCHelper.mClipBackground = clipBackground
      invalidate()
    }

  override var isRoundAsCircle: Boolean
    get() = mRCHelper.mRoundAsCircle
    set(roundAsCircle) {
      mRCHelper.mRoundAsCircle = roundAsCircle
      invalidate()
    }

  override var topLeftRadius: Float
    get() = mRCHelper.radii[0]
    set(value) {
      mRCHelper.radii[0] = value
      mRCHelper.radii[1] = value
      invalidate()
    }

  override var topRightRadius: Float
    get() = mRCHelper.radii[2]
    set(value) {
      mRCHelper.radii[2] = value
      mRCHelper.radii[3] = value
      invalidate()
    }

  override var bottomLeftRadius: Float
    get() = mRCHelper.radii[6]
    set(value) {
      mRCHelper.radii[6] = value
      mRCHelper.radii[7] = value
      invalidate()
    }

  override var bottomRightRadius: Float
    get() = mRCHelper.radii[4]
    set(value) {
      mRCHelper.radii[4] = value
      mRCHelper.radii[5] = value
      invalidate()
    }

  override var strokeWidth: Int
    get() = mRCHelper.mStrokeWidth
    set(strokeWidth) {
      mRCHelper.mStrokeWidth = strokeWidth
      invalidate()
    }

  override var strokeColor: Int
    get() = mRCHelper.mStrokeColor
    set(strokeColor) {
      mRCHelper.mStrokeColor = strokeColor
      invalidate()
    }

  /*** ======== Selector 支持 ========***/
  override fun drawableStateChanged() {
    super.drawableStateChanged()
    mRCHelper.drawableStateChanged(this)
  }

  override fun setChecked(checked: Boolean) {
    if (mRCHelper.mChecked != checked) {
      mRCHelper.mChecked = checked
      refreshDrawableState()
      mRCHelper.onCheckedChange?.onChange(this, mRCHelper.mChecked)
    }
  }

  override fun isChecked() = mRCHelper.mChecked

  override fun toggle() {
    isChecked = !mRCHelper.mChecked
  }

  fun addCheckChange(listener: RCHelper.OnCheckedChange) {
    mRCHelper.onCheckedChange = listener
  }
}
