package com.charancha.drive

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout

class DelayedClickConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var isClickable = true
    private val handler = Handler(Looper.getMainLooper())

    init {
        setOnClickListener {
            if (isClickable) {
                isClickable = false
                handler.postDelayed({
                    isClickable = true
                }, 1000) // 1초 후 다시 클릭 가능하게 설정

                // 여기에 클릭 이벤트 처리 코드를 넣습니다.
                performClickAction()
            }
        }
    }

    private fun performClickAction() {
        // 클릭 시 수행할 작업
    }
}