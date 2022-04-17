package com.hsicen.extension.widget.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import com.hsicen.extension.R

/**
 * @author: hsicen
 * @date: 4/17/22 14:43
 * @email: codinghuang@163.com
 * description: 圆角辅助工具
 */
class RCHelper {
  private val mPaint = Paint().apply {
    color = Color.WHITE
    isAntiAlias = true
  }

  private var mDefaultStrokeColor = Color.WHITE // 默认描边颜色
  private var mStrokeColorStateList: ColorStateList? = null // 描边颜色的状态

  val radii = FloatArray(8) // top-left, top-right, bottom-right, bottom-left
  val mClipPath = Path() // 剪裁区域路径
  val mAreaRegion = Region() // 内容区域
  val mLayer = RectF() // 画布图层大小
  var mRoundAsCircle = false // 圆形
  var mClipBackground = false // 是否剪裁背景
  var mStrokeColor = Color.WHITE // 描边颜色
  var mStrokeWidth = 0 // 描边半径

  var mChecked = false
  var onCheckedChange: OnCheckedChange? = null

  fun initAttrs(context: Context, attrs: AttributeSet?) {
    val ta = context.obtainStyledAttributes(attrs, R.styleable.RCAttrs)
    mRoundAsCircle = ta.getBoolean(R.styleable.RCAttrs_roundAsCircle, false)
    mStrokeColorStateList = ta.getColorStateList(R.styleable.RCAttrs_strokeColor)?.apply {
      mStrokeColor = defaultColor
      mDefaultStrokeColor = defaultColor
    }
    mStrokeWidth = ta.getDimensionPixelSize(R.styleable.RCAttrs_strokeWidth, 0)
    mClipBackground = ta.getBoolean(R.styleable.RCAttrs_clipBackground, false)

    val roundCorner = ta.getDimensionPixelSize(R.styleable.RCAttrs_roundCorner, 0)
    val roundCornerTopLeft = ta.getDimensionPixelSize(R.styleable.RCAttrs_roundCornerTopLeft, roundCorner)
    val roundCornerTopRight = ta.getDimensionPixelSize(R.styleable.RCAttrs_roundCornerTopRight, roundCorner)
    val roundCornerBottomLeft = ta.getDimensionPixelSize(R.styleable.RCAttrs_roundCornerBottomLeft, roundCorner)
    val roundCornerBottomRight = ta.getDimensionPixelSize(R.styleable.RCAttrs_roundCornerBottomRight, roundCorner)
    ta.recycle()

    radii[0] = roundCornerTopLeft * 1f
    radii[1] = roundCornerTopLeft * 1f
    radii[2] = roundCornerTopRight * 1f
    radii[3] = roundCornerTopRight * 1f
    radii[4] = roundCornerBottomRight * 1f
    radii[5] = roundCornerBottomRight * 1f
    radii[6] = roundCornerBottomLeft * 1f
    radii[7] = roundCornerBottomLeft * 1f
  }

  fun onSizeChanged(view: View, w: Int, h: Int) {
    mLayer.set(0f, 0f, w.toFloat(), h.toFloat())
    refreshRegion(view)
  }

  fun refreshRegion(view: View) {
    val w = mLayer.width()
    val h = mLayer.height()

    val areas = RectF(
      view.paddingLeft * 1f,
      view.paddingTop * 1f,
      w - view.paddingRight,
      h - view.paddingBottom
    )
    mClipPath.reset()
    if (mRoundAsCircle) {
      val d = areas.width().coerceAtMost(areas.height())
      val r = d / 2f

      val center = PointF(w / 2f, h / 2f)
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
        mClipPath.addCircle(center.x, center.y, r, Path.Direction.CW)

        // 通过空操作让Path区域占满画布
        mClipPath.moveTo(0f, 0f)
        mClipPath.moveTo(w, h)
      } else {
        val y = h / 2f - r
        mClipPath.moveTo(areas.left, y)
        mClipPath.addCircle(center.x, y + r, r, Path.Direction.CW)
      }
    } else {
      mClipPath.addRoundRect(areas, radii, Path.Direction.CW)
    }

    mAreaRegion.setPath(
      mClipPath,
      Region(areas.left.toInt(), areas.top.toInt(), areas.right.toInt(), areas.bottom.toInt())
    )
  }

  fun onClipDraw(canvas: Canvas) {
    if (mStrokeWidth > 0) {
      // 支持半透明描边，将与描边区域重叠的内容裁剪掉
      mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
      mPaint.color = Color.WHITE
      mPaint.strokeWidth = (mStrokeWidth * 2).toFloat()
      mPaint.style = Paint.Style.STROKE
      canvas.drawPath(mClipPath, mPaint)
      // 绘制描边
      mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
      mPaint.color = mStrokeColor
      mPaint.style = Paint.Style.STROKE
      canvas.drawPath(mClipPath, mPaint)
    }

    mPaint.color = Color.WHITE
    mPaint.style = Paint.Style.FILL
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
      mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
      canvas.drawPath(mClipPath, mPaint)
    } else {
      mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
      val path = Path()
      path.addRect(0f, 0f, mLayer.width(), mLayer.height(), Path.Direction.CW)
      path.op(mClipPath, Path.Op.DIFFERENCE)
      canvas.drawPath(path, mPaint)
    }
  }

  /*** ======== Selector 支持 ======== */
  fun drawableStateChanged(view: View) {
    if (view is RCAttrs) {
      val stateListArray = ArrayList<Int>()
      if (view is Checkable) {
        stateListArray.add(android.R.attr.state_checkable)
        if ((view as Checkable).isChecked) stateListArray.add(android.R.attr.state_checked)
      }
      if (view.isEnabled) stateListArray.add(android.R.attr.state_enabled)
      if (view.isFocused) stateListArray.add(android.R.attr.state_focused)
      if (view.isPressed) stateListArray.add(android.R.attr.state_pressed)
      if (view.isHovered) stateListArray.add(android.R.attr.state_hovered)
      if (view.isSelected) stateListArray.add(android.R.attr.state_selected)
      if (view.isActivated) stateListArray.add(android.R.attr.state_activated)
      if (view.hasWindowFocus()) stateListArray.add(android.R.attr.state_window_focused)

      mStrokeColorStateList?.let {
        if (it.isStateful.not()) return@let

        val stateList = IntArray(stateListArray.size)
        for (i in stateListArray.indices) stateList[i] = stateListArray[i]
        val stateColor = it.getColorForState(stateList, mDefaultStrokeColor)
        (view as RCAttrs).strokeColor = stateColor
      }
    }
  }

  interface OnCheckedChange {
    fun onChange(view: View?, isChecked: Boolean)
  }

  interface RCAttrs {
    var isClipBackground: Boolean
    var isRoundAsCircle: Boolean
    var strokeWidth: Int
    var strokeColor: Int

    fun setRadius(radius: Float)
    var topLeftRadius: Float
    var topRightRadius: Float
    var bottomLeftRadius: Float
    var bottomRightRadius: Float
  }
}
