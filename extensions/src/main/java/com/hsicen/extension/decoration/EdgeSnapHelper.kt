package com.hsicen.extension.decoration

import android.content.Context
import android.graphics.PointF
import android.util.DisplayMetrics
import android.view.View
import android.widget.OverScroller
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * EdgeSnapHelper (可配置版)
 *
 * 可通过构造参数或 setter 调整核心行为：
 *  - flingVelocityThreshold: 小于该速度视为拖拽（非强 fling）
 *  - minVelocityForOne: 当 itemsToScroll 计算为 0 且速度超过该阈值时强制滚动 1 个
 *  - maxFlingDistance: OverScroller 估算最终位移时的最大像素范围
 *  - millisecondsPerInch: LinearSmoothScroller 的速度控制参数
 *
 * 仅针对 LinearLayoutManager（水平或垂直）设计。
 */
class EdgeSnapHelper @JvmOverloads constructor(
  /**
   * 小于该速度视为拖拽（非强 fling），单位为像素/秒（来自 RecyclerView 回调的 velocity 参数）
   */
  var flingVelocityThreshold: Int = 600,

  /**
   * 当 itemsToScroll 计算为 0 时且速度超过该阈值，强制滚动 1 个
   */
  var minVelocityForOne: Int = 800,

  /**
   * OverScroller 在估算 fling 最终位移时使用的最大像素范围（避免极端 final 值）
   */
  var maxFlingDistance: Int = 20000,

  /**
   * LinearSmoothScroller 的速度参数（ms/inch）
   */
  var millisecondsPerInch: Float = 100f
) : SnapHelper() {

  private var mHorizontalHelper: OrientationHelper? = null
  private var mVerticalHelper: OrientationHelper? = null
  private var mRecyclerView: RecyclerView? = null

  override fun attachToRecyclerView(recyclerView: RecyclerView?) {
    super.attachToRecyclerView(recyclerView)
    mRecyclerView = recyclerView
  }

  override fun calculateDistanceToFinalSnap(
    layoutManager: RecyclerView.LayoutManager,
    targetView: View
  ): IntArray? {
    val out = IntArray(2)
    if (layoutManager.canScrollHorizontally()) {
      val helper = getHorizontalHelper(layoutManager)
      var dx = helper.getDecoratedStart(targetView) - helper.startAfterPadding
      dx = capScrollDeltaIfNeeded(layoutManager, helper, dx, true)
      out[0] = dx
    } else {
      out[0] = 0
    }
    if (layoutManager.canScrollVertically()) {
      val helper = getVerticalHelper(layoutManager)
      var dy = helper.getDecoratedStart(targetView) - helper.startAfterPadding
      dy = capScrollDeltaIfNeeded(layoutManager, helper, dy, false)
      out[1] = dy
    } else {
      out[1] = 0
    }

    return out
  }

  private fun capScrollDeltaIfNeeded(
    layoutManager: RecyclerView.LayoutManager,
    helper: OrientationHelper,
    delta: Int,
    horizontal: Boolean
  ): Int {
    if (layoutManager !is LinearLayoutManager) return delta
    val itemCount = layoutManager.itemCount
    if (itemCount == 0) return delta
    if (delta == 0) return 0

    val firstVisible = layoutManager.findFirstVisibleItemPosition()
    val lastVisible = layoutManager.findLastVisibleItemPosition()
    if (firstVisible == RecyclerView.NO_POSITION || lastVisible == RecyclerView.NO_POSITION) return delta

    return try {
      val lastChild = layoutManager.findViewByPosition(lastVisible)
      if (lastChild != null) {
        val lastChildEnd = helper.getDecoratedEnd(lastChild)
        val availableToEnd = lastChildEnd - helper.endAfterPadding
        // 到达末尾且没有可滚动空间时，避免返回会把内容推出导致回弹
        if (lastVisible == itemCount - 1 && availableToEnd <= 0) {
          // 如果尝试往末尾方向滚动，取消滚动
          if (delta < 0) return 0
        }
        // 当 delta 超过实际可用范围时裁剪
        if (lastVisible == itemCount - 1 && availableToEnd < 0) {
          val maxAllowed = availableToEnd
          if (delta > 0 && delta > maxAllowed) return maxAllowed
          if (delta < 0 && -delta > -maxAllowed) return maxAllowed
        }
      }
      delta
    } catch (t: Throwable) {
      delta
    }
  }

  override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
    if (layoutManager !is LinearLayoutManager) return null
    return if (layoutManager.canScrollHorizontally()) {
      findStartView(layoutManager, getHorizontalHelper(layoutManager))
    } else {
      findStartView(layoutManager, getVerticalHelper(layoutManager))
    }
  }

  private fun findStartView(
    layoutManager: LinearLayoutManager,
    helper: OrientationHelper
  ): View? {
    val childCount = layoutManager.childCount
    if (childCount == 0) return null

    var closestChild: View? = null
    var closest = Int.MAX_VALUE
    val start = helper.startAfterPadding

    for (i in 0 until childCount) {
      val child = layoutManager.getChildAt(i) ?: continue
      val childStart = helper.getDecoratedStart(child)
      val absDistance = abs(childStart - start)
      if (absDistance < closest) {
        closest = absDistance
        closestChild = child
      }
    }
    return closestChild
  }

  private fun estimateFlingDistance(context: Context, velocityX: Int, velocityY: Int): Int {
    val scroller = OverScroller(context)
    scroller.fling(
      0, 0,
      velocityX, velocityY,
      -maxFlingDistance, maxFlingDistance,
      -maxFlingDistance, maxFlingDistance
    )
    val finalX = scroller.finalX
    val finalY = scroller.finalY
    return max(abs(finalX), abs(finalY))
  }

  private fun computeAverageChildSize(
    layoutManager: LinearLayoutManager,
    helper: OrientationHelper
  ): Int {
    val childCount = layoutManager.childCount
    if (childCount == 0) return 0
    var totalSize = 0
    for (i in 0 until childCount) {
      val child = layoutManager.getChildAt(i) ?: continue
      totalSize += helper.getDecoratedMeasurement(child)
    }
    return if (childCount > 0) totalSize / childCount else 0
  }

  override fun findTargetSnapPosition(
    layoutManager: RecyclerView.LayoutManager,
    velocityX: Int,
    velocityY: Int
  ): Int {
    if (layoutManager !is LinearLayoutManager) return RecyclerView.NO_POSITION
    val itemCount = layoutManager.itemCount
    if (itemCount == 0) return RecyclerView.NO_POSITION

    val helper = if (layoutManager.canScrollHorizontally()) getHorizontalHelper(layoutManager)
    else getVerticalHelper(layoutManager)

    val startView = findStartView(layoutManager, helper) ?: return RecyclerView.NO_POSITION
    val startPosition = layoutManager.getPosition(startView)
    if (startPosition == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION

    val rv = mRecyclerView ?: return RecyclerView.NO_POSITION

    val firstVisible = layoutManager.findFirstVisibleItemPosition()
    val lastVisible = layoutManager.findLastVisibleItemPosition()

    // 如果 fling 朝向末尾且已经在末尾，直接返回末尾，避免 overscroll 回弹
    val flingForwardByVelocity =
      if (layoutManager.canScrollHorizontally()) velocityX > 0 else velocityY > 0
    if (flingForwardByVelocity && lastVisible == itemCount - 1) return itemCount - 1
    if (!flingForwardByVelocity && firstVisible == 0) return 0

    // 拖拽/轻滑判定使用 flingVelocityThreshold（可配置）
    val velocity = if (layoutManager.canScrollHorizontally()) velocityX else velocityY
    if (abs(velocity) <= flingVelocityThreshold) {
      // 计算当前 startView 相对于 startAfterPadding 的偏移
      val displacement = helper.getDecoratedStart(startView) - helper.startAfterPadding
      if (displacement == 0) return startPosition

      // 用户要求：当滑动不到 item 一半时也往下一个（即拖动只要方向是前进就前进）
      var forward = displacement < 0
      if (layoutManager.reverseLayout) forward = !forward

      val target = if (forward) startPosition + 1 else startPosition - 1
      var targetPos = target
      if (targetPos < 0) targetPos = 0
      if (targetPos >= itemCount) targetPos = itemCount - 1
      return targetPos
    }

    // fling：使用 OverScroller 估算最终位移并转换为 itemsToScroll（使用可配置 minVelocityForOne）
    val flingDistance = estimateFlingDistance(rv.context, velocityX, velocityY)

    var avgItemSize = computeAverageChildSize(layoutManager, helper)
    if (avgItemSize <= 0) avgItemSize = helper.getDecoratedMeasurement(startView)
    if (avgItemSize <= 0) {
      return simpleTargetPositionByVelocity(layoutManager, startPosition, velocityX, velocityY)
    }

    var itemsToScroll = (flingDistance.toFloat() / avgItemSize.toFloat()).roundToInt()
    if (itemsToScroll == 0 && abs(velocity) > minVelocityForOne) itemsToScroll = 1

    var forward = if (layoutManager.canScrollHorizontally()) velocityX > 0 else velocityY > 0
    if (layoutManager.reverseLayout) forward = !forward

    val delta = if (forward) itemsToScroll else -itemsToScroll
    var targetPos = startPosition + delta
    if (targetPos < 0) targetPos = 0
    if (targetPos >= itemCount) targetPos = itemCount - 1

    return targetPos
  }

  private fun simpleTargetPositionByVelocity(
    layoutManager: LinearLayoutManager,
    startPosition: Int,
    velocityX: Int,
    velocityY: Int
  ): Int {
    var forward = if (layoutManager.canScrollHorizontally()) velocityX > 0 else velocityY > 0
    if (layoutManager.reverseLayout) forward = !forward

    val itemCount = layoutManager.itemCount
    var targetPos = if (forward) startPosition + 1 else startPosition - 1
    if (targetPos < 0) targetPos = 0
    if (targetPos >= itemCount) targetPos = itemCount - 1
    return targetPos
  }

  override fun createScroller(layoutManager: RecyclerView.LayoutManager): SmoothScroller? {
    val rv = mRecyclerView ?: return null
    if (layoutManager !is ScrollVectorProvider) {
      return null
    }
    return object : LinearSmoothScroller(rv.context) {
      override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val out = calculateDistanceToFinalSnap(layoutManager, targetView)
        val dx = out?.get(0) ?: 0
        val dy = out?.get(1) ?: 0
        val distance = max(abs(dx), abs(dy))
        val time = calculateTimeForDeceleration(distance)
        if (time > 0) {
          action.update(dx, dy, time, mDecelerateInterpolator)
        }
      }

      override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return millisecondsPerInch / displayMetrics.densityDpi
      }

      override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        val provider = layoutManager as ScrollVectorProvider
        return provider.computeScrollVectorForPosition(targetPosition)
      }
    }
  }

  private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
    if (mHorizontalHelper == null || mHorizontalHelper?.layoutManager !== layoutManager) {
      mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
    }
    return mHorizontalHelper!!
  }

  private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
    if (mVerticalHelper == null || mVerticalHelper?.layoutManager !== layoutManager) {
      mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
    }
    return mVerticalHelper!!
  }
}