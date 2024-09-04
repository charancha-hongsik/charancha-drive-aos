package com.milelog

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class TouchInterceptorFrameLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // 지도 상에서 스크롤 제스처를 처리하기 위해 터치 이벤트를 가로챕니다.
        when (ev.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // ScrollView 스크롤을 막습니다.
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP -> {
                // ScrollView 스크롤을 허용합니다.
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return false
    }
}