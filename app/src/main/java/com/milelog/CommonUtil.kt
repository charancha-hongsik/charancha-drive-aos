package com.milelog

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

object CommonUtil {
    fun getSpannableString(context: Context, originalText:String, spanText:String, color:Int):SpannableString{

        val spannableString = SpannableString(originalText)

        val start = originalText.indexOf(spanText)
        val end = start + spanText.length

        val colorSpan = ForegroundColorSpan(color) // 원하는 색상으로 변경

        spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }
}