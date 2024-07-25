package com.charancha.drive.activity

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.charancha.drive.BuildConfig
import com.charancha.drive.CustomDialogNoCancel
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.GetLatestResponse
import com.charancha.drive.retrofit.response.TermsSummaryResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class SettingActivity:BaseRefreshActivity(){
    lateinit var layout_select_distance_unit: CoordinatorLayout
    lateinit var persistent_bottom_sheet_distance: LinearLayout
    lateinit var behavior: BottomSheetBehavior<LinearLayout>
    lateinit var btn_set_distance_unit: TextView
    lateinit var btn_km:LinearLayout
    lateinit var btn_mile:LinearLayout
    lateinit var btn_open_set_distance_unit:LinearLayout
    lateinit var tv_unit:TextView
    lateinit var tv_version:TextView
    lateinit var btn_back:ImageView
    lateinit var btn_update:TextView
    lateinit var tv_latest:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        init()

    }

    fun init(){
        layout_select_distance_unit = findViewById(R.id.layout_select_distance_unit)
        persistent_bottom_sheet_distance = findViewById(R.id.persistent_bottom_sheet_distance)
        btn_set_distance_unit = findViewById(R.id.btn_set_distance_unit)
        btn_km = findViewById(R.id.btn_km)
        btn_mile = findViewById(R.id.btn_mile)
        btn_open_set_distance_unit = findViewById(R.id.btn_open_set_distance_unit)
        tv_unit = findViewById(R.id.tv_unit)
        tv_version = findViewById(R.id.tv_version)
        tv_latest = findViewById(R.id.tv_latest)
        btn_back = findViewById(R.id.btn_back)
        btn_update = findViewById(R.id.btn_update)
        btn_update.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                goUpdate()
            }

        })

        btn_open_set_distance_unit.setOnClickListener {
            layout_select_distance_unit.visibility = VISIBLE
        }

        layout_select_distance_unit.setOnClickListener {
            layout_select_distance_unit.visibility = GONE
        }

        btn_set_distance_unit.setOnClickListener {
            if(btn_km.isSelected){
                tv_unit.text = "km"
                PreferenceUtil.putPref(this@SettingActivity, PreferenceUtil.KM_MILE, "km")
            }else{
                tv_unit.text = "mile"
                PreferenceUtil.putPref(this@SettingActivity, PreferenceUtil.KM_MILE,"mile")
            }

            layout_select_distance_unit.visibility = GONE
        }

        btn_km.setOnClickListener {
            btn_km.isSelected = true
            btn_mile.isSelected = false
        }

        btn_mile.setOnClickListener {
            btn_km.isSelected = false
            btn_mile.isSelected = true
        }

        persistentBottomSheetEvent()

        if(PreferenceUtil.getPref(this@SettingActivity, PreferenceUtil.KM_MILE, "km").equals("km")){
            btn_km.isSelected = true
            btn_mile.isSelected = false

            tv_unit.text = "km"
        }else{
            btn_km.isSelected = false
            btn_mile.isSelected = true

            tv_unit.text = "mile"

        }

        btn_back.setOnClickListener {
            finish()
        }

        apiService().getLatest("AOS","PHONE").enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201) {
                    val getLatestResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetLatestResponse::class.java
                    )

                    tv_version.text = "V" + getLatestResponse.version

                    val currentAppVersion = BuildConfig.VERSION_NAME
                    val majorFromApi =
                        getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0]
                    val minorFromApi =
                        getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[1]
                    val patchFromApi =
                        getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[2]
                    val major = currentAppVersion.split("\\.".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                    val minor = currentAppVersion.split("\\.".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                    val patch = currentAppVersion.split("\\.".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[2]


                    if (patchFromApi.toInt() > patch.toInt()) {
                        tv_version.text = "V " + BuildConfig.VERSION_NAME
                        tv_latest.visibility = GONE
                        btn_update.visibility = VISIBLE

                        return
                    }
                    if (minorFromApi.toInt() > minor.toInt()) {
                        tv_version.text = "V " + BuildConfig.VERSION_NAME
                        tv_latest.visibility = GONE
                        btn_update.visibility = VISIBLE

                        return
                    }
                    if (majorFromApi.toInt() > major.toInt()) {
                        tv_version.text = "V " + BuildConfig.VERSION_NAME
                        tv_latest.visibility = GONE
                        btn_update.visibility = VISIBLE

                        return
                    }


                    tv_latest.visibility = VISIBLE
                    btn_update.visibility = GONE


                }else{
                    tv_version.text = "V " + BuildConfig.VERSION_NAME

                    tv_latest.visibility = GONE
                    btn_update.visibility = VISIBLE

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_version.text = "V " + BuildConfig.VERSION_NAME

                tv_latest.visibility = GONE
                btn_update.visibility = VISIBLE
            }

        })
    }

    private fun persistentBottomSheetEvent() {
        behavior = BottomSheetBehavior.from(persistent_bottom_sheet_distance)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 되는 도중 계속 호출
                // called continuously while dragging
                Log.d("testset", "onStateChanged: 드래그 중")
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED-> {
                        Log.d("testset", "onStateChanged: 접음")
//                        layout_choose_date.visibility = GONE
                    }
                    BottomSheetBehavior.STATE_DRAGGING-> {
                        Log.d("testset", "onStateChanged: 드래그")
                    }
                    BottomSheetBehavior.STATE_EXPANDED-> {
                        Log.d("testset", "onStateChanged: 펼침")
                    }
                    BottomSheetBehavior.STATE_HIDDEN-> {
                        Log.d("testset", "onStateChanged: 숨기기")

                    }
                    BottomSheetBehavior.STATE_SETTLING-> {
                        Log.d("testset", "onStateChanged: 고정됨")
                    }
                }
            }
        })
    }

    private fun goUpdate(){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(
            "https://play.google.com/store/apps/details?id=com.charancha"
        )
        intent.setPackage("com.android.vending")
        startActivity(intent)
    }

}