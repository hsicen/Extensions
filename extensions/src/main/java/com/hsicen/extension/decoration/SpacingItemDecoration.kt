package com.hsicen.extension.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * 作者：hsicen  7/1/21 09:19
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：GridLayoutManager 设置Item之间的Margin
 */
class SpacingItemDecoration(private val horizontalSpacing: Int, private val verticalSpacing: Int) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = horizontalSpacing / 2
        outRect.right = horizontalSpacing / 2
        outRect.top = verticalSpacing / 2
        outRect.bottom = verticalSpacing / 2
    }
}