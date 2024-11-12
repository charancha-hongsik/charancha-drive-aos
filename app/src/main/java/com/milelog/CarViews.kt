package com.milelog

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

data class CarViews (
    val view_parent: ConstraintLayout,
    val tv_car_name: TextView,
    val tv_car_num:TextView,
    val divider: View
)

