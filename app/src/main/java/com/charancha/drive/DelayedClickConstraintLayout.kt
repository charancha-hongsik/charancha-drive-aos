package com.charancha.drive

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout

class DelayedClickConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var isClickable = false
    private val handler = Handler(Looper.getMainLooper())

    init {
        // ConstraintLayout이 클릭 가능하도록 설정
        this.isClickable = false
    }



//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        if(isClickable){
//            Log.d("testeststset","testestestes :: onTouchEvent")
//            isClickable = false
//            handler.postDelayed({
//                isClickable = true
//            }, 1000) // 1초 후 다시 클릭 가능하게 설정
//            return super.onTouchEvent(event)
//        }else{
//            return true
//
//        }
//
//    }
}