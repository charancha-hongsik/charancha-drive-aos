package com.milelog.activity

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.CarListFilter
import com.milelog.CarViews
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.room.entity.MyCarsEntity
import com.nex3z.flowlayout.FlowLayout

class ExcelActivity:BaseRefreshActivity() {
    lateinit var layout_flow: FlowLayout
    val filterList: MutableList<CarListFilter> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excel)

        layout_flow = findViewById(R.id.layout_flow)


        PreferenceUtil.getPref(this, PreferenceUtil.MY_CAR_ENTITIES, "")?.let {
            if (it != "") {
                val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                val myCarsListOnDevice: MutableList<MyCarsEntity> = mutableListOf()
                myCarsListOnDevice.addAll(
                    GsonBuilder().serializeNulls().create().fromJson(it, type)
                )

                filterList.add(CarListFilter(null,null, "전체", null))
                filterList.add(CarListFilter(null, null, "미확정", true))
                filterList.add(CarListFilter(null, null, "내 차가 아니에요", false))

                for (car in myCarsListOnDevice) {
                    filterList.add(CarListFilter(car.id, car.number?.takeLast(4), car.name, car.isActive))
                }


                val carViews = mutableListOf<CarViews>()


                for (filter in filterList) {
                    // Inflate the ConstraintLayout view
                    val constraintLayoutView =
                        layoutInflater.inflate(R.layout.item_drive_history_car, layout_flow, false)


                    // Find the TextView within the newly inflated ConstraintLayout
                    val tv_car_name = constraintLayoutView.findViewById<TextView>(R.id.tv_car_name)
                    tv_car_name.text = filter.name
                    val tv_car_number = constraintLayoutView.findViewById<TextView>(R.id.tv_car_number)
                    val divider = constraintLayoutView.findViewById<View>(R.id.divider)
                    val view_parent = constraintLayoutView.findViewById<ConstraintLayout>(R.id.view_parent)

                    // Add the TextView reference to the list
                    carViews.add(CarViews(view_parent, tv_car_name, tv_car_number, divider))

                    if(filter.carNum.isNullOrEmpty()){
                        tv_car_number.visibility = GONE
                        divider.visibility = GONE
                    }else{
                        tv_car_number.visibility = VISIBLE
                        tv_car_number.text = filter.carNum

                        divider.visibility = VISIBLE
                    }

                    if (filter.name.equals("전체")) {
                        ((tv_car_name.parent as LinearLayout).parent as ConstraintLayout).isSelected = true
                        TextViewCompat.setTextAppearance(tv_car_name, R.style.car_filter_selected)
                    }

                    (tv_car_name.parent as LinearLayout).setOnClickListener {
                        // Iterate over the list and update the background of all TextViews
                        for (view in carViews) {
                            if (view.tv_car_name == tv_car_name) {
                                // Change background of the clicked TextView
                                view.view_parent.isSelected = true

                                TextViewCompat.setTextAppearance(
                                    view.tv_car_name,
                                    R.style.car_filter_selected
                                )

                                TextViewCompat.setTextAppearance(
                                    view.tv_car_num,
                                    R.style.car_filter_selected
                                )

                                val matchingFilter = filterList.find { it.name == tv_car_name.text }

                            } else {
                                // Reset the background of other TextViews
                                view.view_parent.isSelected = false
                                TextViewCompat.setTextAppearance(
                                    view.tv_car_name,
                                    R.style.car_filter_unselected
                                )

                                TextViewCompat.setTextAppearance(
                                    view.tv_car_num,
                                    R.style.car_filter_unselected
                                )
                            }
                        }
                    }


                    // Add the inflated view to the parent layout
                    layout_flow.addView(constraintLayoutView)
                }

            }
        }
    }
}