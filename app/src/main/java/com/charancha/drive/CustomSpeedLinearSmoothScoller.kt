package com.charancha.drive

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller

class CustomSpeedLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
        return 0.4f / displayMetrics.density
    }
}