package com.milelog

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(context: Context, private val colorResId: Int, private val dividerHeight: Int) : RecyclerView.ItemDecoration() {

    private val paint = Paint()

    init {
        paint.color = ContextCompat.getColor(context, colorResId)
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + dividerHeight

            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // 마지막 아이템이 아닌 경우에만 아래쪽에 공간 추가
        if (parent.getChildAdapterPosition(view) != state.itemCount - 1) {
            outRect.bottom = dividerHeight
        }
    }
}