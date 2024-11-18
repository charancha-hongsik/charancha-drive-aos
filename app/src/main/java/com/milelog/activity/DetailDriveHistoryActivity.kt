package com.milelog.activity

import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.milelog.R
import com.milelog.viewmodel.DetailDriveHistoryViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.StrokeStyle
import com.google.android.gms.maps.model.StyleSpan
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.CustomDialog
import com.milelog.DividerItemDecoration
import com.milelog.PreferenceUtil
import com.milelog.activity.LoadCarMoreInfoActivity.Companion.CORPORATE
import com.milelog.activity.LoadCarMoreInfoActivity.Companion.PERSONAL
import com.milelog.retrofit.request.PatchCorpType
import com.milelog.retrofit.response.UserCar
import com.milelog.retrofit.response.VWorldDetailResponse
import com.milelog.retrofit.response.VWorldResponse
import com.milelog.room.entity.MyCarsEntity
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.state.GetDrivingInfoState
import com.milelog.viewmodel.state.PatchCorpTypeState
import com.milelog.viewmodel.state.PatchDrivingInfoState
import com.milelog.viewmodel.state.PatchMemoState
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


class DetailDriveHistoryActivity: BaseRefreshActivity() {
    val keywords = listOf("주유소", "정비소", "세차장", "폐차장", "중고차", "매매단지", "도이치오토월드", "캠핑","글램핑")


    val polylines:MutableList<LatLng> = mutableListOf()

    private var currentMarker: Marker? = null

    private val detailDriveHistoryViewModel: DetailDriveHistoryViewModel by viewModels()

    lateinit var tv_date:TextView
    lateinit var tv_distance:TextView
    lateinit var tv_start_time:TextView
    lateinit var tv_end_time:TextView
    lateinit var tv_start_time_info:TextView
    lateinit var tv_end_time_info:TextView
    lateinit var tv_drive_time_info:TextView
    lateinit var tv_drive_distance_info:TextView
    lateinit var tv_drive_verification_info:TextView
    lateinit var tv_high_speed_driving_percent_info:TextView
    lateinit var tv_low_speed_driving_percent_info:TextView
    lateinit var tv_max_speed_info:TextView
    lateinit var tv_high_speed_average_info:TextView
    lateinit var tv_low_speed_average_info:TextView
    lateinit var tv_rapid_start_count:TextView
    lateinit var tv_rapid_acc_count_info:TextView
    lateinit var tv_rapid_stop_count_info:TextView
    lateinit var tv_rapid_desc_count_info:TextView
    lateinit var btn_back: ImageView
    lateinit var tv_scope_date_mycar:TextView
    lateinit var tv_mycar:TextView
    lateinit var tv_type:TextView

    lateinit var btn_choose_mycar: LinearLayout
    lateinit var tv_mycar_scope_info:LinearLayout

    lateinit var iv_tooltip_verification:ImageView
    lateinit var iv_tooltip_rapid_desc:ImageView
    lateinit var iv_tooltip_rapid_stop:ImageView
    lateinit var iv_tooltip_high_speed_average:ImageView
    lateinit var iv_tooltip_high_speed:ImageView
    lateinit var iv_tooltip_low_speed:ImageView
    lateinit var iv_tooltip_low_speed_average:ImageView
    lateinit var iv_tooltip_rapid_start:ImageView
    lateinit var iv_tooltip_rapid_acc:ImageView
    lateinit var iv_map:ImageView

    lateinit var tv_start_address:TextView
    lateinit var tv_end_address:TextView
    lateinit var tv_end_address_detail:TextView

    lateinit var view_map:CardView
    lateinit var layout_drive_image:LinearLayout

    lateinit var iv_corp:ImageView

    lateinit var btn_choose_corp: ConstraintLayout

    private var isCameraMoving = false
    private var currentAnimator: ValueAnimator? = null
    private var bluetoothNameExpected:String? = null

    lateinit var et_memo: EditText

    var isActive = true
    var userCarId:String? = null
    var userCar: UserCar? = null
    lateinit var tracking_id:String
    var pastMemo:String? = null

    enum class CorpType(val description: String) {
        WORK("출/퇴근"),
        NON_WORK("비업무"),
        COMMUTE("일반업무");

        override fun toString(): String {
            return description
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_drive_history)

        detailDriveHistoryViewModel.init(applicationContext)

        init()
        setObserver()
        setResources()
    }

    private fun init(){
        tracking_id = intent.getStringExtra("trackingId").toString()
        isActive = intent.getBooleanExtra("isActive",true)
        intent.getStringExtra("userCar")?.let{
            userCar = Gson().fromJson(it, UserCar::class.java)
        }

        iv_map = findViewById(R.id.iv_map)
        tv_date = findViewById(R.id.tv_date)
        tv_distance = findViewById(R.id.tv_distance)
        tv_start_time = findViewById(R.id.tv_start_time)
        tv_end_time = findViewById(R.id.tv_end_time)
        tv_start_time_info = findViewById(R.id.tv_start_time_info)
        tv_end_time_info = findViewById(R.id.tv_end_time_info)
        tv_drive_time_info = findViewById(R.id.tv_drive_time_info)
        tv_drive_distance_info = findViewById(R.id.tv_drive_distance_info)
        tv_drive_verification_info = findViewById(R.id.tv_drive_verification_info)
        tv_high_speed_driving_percent_info = findViewById(R.id.tv_high_speed_driving_percent_info)
        tv_low_speed_driving_percent_info = findViewById(R.id.tv_low_speed_driving_percent_info)
        tv_max_speed_info = findViewById(R.id.tv_max_speed_info)
        tv_low_speed_average_info = findViewById(R.id.tv_low_speed_average_info)
        tv_high_speed_average_info = findViewById(R.id.tv_high_speed_average_info)
        tv_rapid_start_count = findViewById(R.id.tv_rapid_start_count)
        tv_rapid_acc_count_info = findViewById(R.id.tv_rapid_acc_count_info)
        tv_rapid_stop_count_info = findViewById(R.id.tv_rapid_stop_count_info)
        tv_rapid_desc_count_info = findViewById(R.id.tv_rapid_desc_count_info)
        btn_back = findViewById(R.id.btn_back)
        btn_choose_mycar = findViewById(R.id.btn_choose_mycar)
        tv_scope_date_mycar = findViewById(R.id.tv_scope_date_mycar)
        tv_mycar_scope_info = findViewById(R.id.tv_mycar_scope_info)
        tv_type = findViewById(R.id.tv_type)

        iv_tooltip_verification = findViewById(R.id.iv_tooltip_verification)
        iv_tooltip_low_speed = findViewById(R.id.iv_tooltip_low_speed)
        iv_tooltip_high_speed = findViewById(R.id.iv_tooltip_high_speed)
        iv_tooltip_rapid_desc = findViewById(R.id.iv_tooltip_rapid_desc)
        iv_tooltip_rapid_stop = findViewById(R.id.iv_tooltip_rapid_stop)
        iv_tooltip_high_speed_average = findViewById(R.id.iv_tooltip_high_speed_average)
        iv_tooltip_low_speed_average = findViewById(R.id.iv_tooltip_low_speed_average)
        iv_tooltip_rapid_start = findViewById(R.id.iv_tooltip_rapid_start)
        iv_tooltip_rapid_acc = findViewById(R.id.iv_tooltip_rapid_acc)
        tv_mycar = findViewById(R.id.tv_mycar)
        layout_drive_image = findViewById(R.id.layout_drive_image)

        tv_start_address = findViewById(R.id.tv_start_address)
        tv_end_address = findViewById(R.id.tv_end_address)
        tv_end_address_detail = findViewById(R.id.tv_end_address_detail)

        btn_choose_corp = findViewById(R.id.btn_choose_corp)
        btn_choose_corp.setOnClickListener {
//            showBottomSheetForChooseCorp(userCar!!.id)
        }

        et_memo = findViewById(R.id.et_memo)

        iv_corp = findViewById(R.id.iv_corp)

        view_map = findViewById(R.id.view_map)


        repeat(5) {
            // Inflate the ConstraintLayout view
            val constraintLayoutView = layoutInflater.inflate(R.layout.item_drive_image, layout_drive_image, false)

            // Find the ImageView within the newly inflated ConstraintLayout
            val iv_drive_image = constraintLayoutView.findViewById<ImageView>(R.id.iv_drive_image)

            // Load the image into the ImageView with Glide
            Glide.with(this)
                .asBitmap()
                .load("https://charancha.com/uploads/carimg/xxlarge/2024/86410fad-5eaa-4467-88b0-758f5a8691a8.jpg?w=1200&h=675&f=webp")
                .transform(RoundedCornersTransformation(5, 0))  // 5dp의 반경
                .into(iv_drive_image)

            // Add the inflated ConstraintLayout to the parent LinearLayout
            layout_drive_image.addView(constraintLayoutView)
        }

    }

    private fun setObserver(){
        detailDriveHistoryViewModel.patchMemo.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver { state ->
            when (state) {
                is PatchMemoState.Loading -> {

                }
                is PatchMemoState.Success -> {
                    val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                    intent.putExtra("isActive",isActive)
                    intent.putExtra("userCarId",userCarId)
                    intent.putExtra("trackingId",tracking_id)
                    intent.putExtra("userCar",Gson().toJson(userCar))
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is PatchMemoState.Error -> {
                    val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                    intent.putExtra("isActive",isActive)
                    intent.putExtra("userCarId",userCarId)
                    intent.putExtra("trackingId",tracking_id)
                    intent.putExtra("userCar",Gson().toJson(userCar))
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is PatchMemoState.Empty -> {
                    val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                    intent.putExtra("isActive",isActive)
                    intent.putExtra("userCarId",userCarId)
                    intent.putExtra("trackingId",tracking_id)
                    intent.putExtra("userCar",Gson().toJson(userCar))
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        })

        detailDriveHistoryViewModel.patchCorpType.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver { state ->
            when (state) {
                is PatchCorpTypeState.Loading -> {

                }
                is PatchCorpTypeState.Success -> {
                    isActive = state.data.isActive
                    userCarId = state.data.userCarId
                    userCar = state.data.userCar

                    tv_type.text = CorpType.valueOf(state.data.type).description
                    btn_choose_corp.visibility = VISIBLE

                    tv_mycar.text = state.data.userCar.carName
                    iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star1))
                    iv_corp.visibility = VISIBLE
                }
                is PatchCorpTypeState.Error -> {

                }
                is PatchCorpTypeState.Empty -> {

                }
            }
        })


        detailDriveHistoryViewModel.getMapData.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver {
            it?.let{
                for(raw in it.gpses){
                    polylines.add(LatLng(raw.latitude,raw.longtitude))
                }

                bluetoothNameExpected = it.bluetooth_name

                if(polylines.size != 0){
                    view_map.visibility = VISIBLE
                    tv_mycar_scope_info.visibility = GONE
                    setMapData()
                }
            }
        })

        detailDriveHistoryViewModel.patchDrivingInfo.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is PatchDrivingInfoState.Loading -> {

                }
                is PatchDrivingInfoState.Success -> {
                    isActive = state.data.isActive
                    userCarId = state.data.userCarId
                    userCar = state.data.userCar


                    if(state.data.isActive){
                        if(!state.data.userCarId.isNullOrEmpty()){
                            tv_mycar.text = userCar?.carName
                            iv_corp.visibility = VISIBLE

                            if(userCar?.type == PERSONAL){
                                iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star2))
                                btn_choose_corp.visibility = GONE
                            }else{
                                iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star1))
                                btn_choose_corp.visibility = VISIBLE
                            }

                        }else{
                            // 미확정
                            tv_mycar.text = getString(R.string.pending)
                            userCar = null
                            iv_corp.visibility = GONE
                            btn_choose_corp.visibility = GONE

                        }
                    }else{
                        // 내 차가 아니에요
                        userCar = null
                        tv_mycar.text = getString(R.string.not_my_car)
                        iv_corp.visibility = GONE
                        btn_choose_corp.visibility = GONE

                    }
                }
                is PatchDrivingInfoState.Error -> {


                    if(state.code == 401){
                        logout()

                    }
                }
                is PatchDrivingInfoState.Empty -> {

                }
            }
        })

        detailDriveHistoryViewModel.getDrivingInfo.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetDrivingInfoState.Loading -> {

                }
                is GetDrivingInfoState.Success -> {
                    val getDrivingInfoResponse = state.data

                    tv_date.text = transformTimeToDate(getDrivingInfoResponse.endTime)
                    tv_distance.text = transferDistanceWithUnit(getDrivingInfoResponse.totalDistance)
                    tv_start_time_info.text = transformTimeToDateWithTime(getDrivingInfoResponse.startTime)
                    tv_end_time_info.text = transformTimeToDateWithTime(getDrivingInfoResponse.endTime)
                    tv_drive_time_info.text = transformSecondsToHHMMSS(getDrivingInfoResponse.totalTime)
                    tv_drive_distance_info.text = transferDistanceWithUnit(getDrivingInfoResponse.totalDistance)
                    tv_drive_verification_info.text = getDrivingInfoResponse.verification
                    tv_high_speed_driving_percent_info.text = transferNumWithRounds(getDrivingInfoResponse.highSpeedDrivingDistancePercentage).toString() + "%"
                    tv_low_speed_driving_percent_info.text = transferNumWithRounds(getDrivingInfoResponse.lowSpeedDrivingDistancePercentage).toString() + "%"
                    tv_max_speed_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.maxSpeed)
                    tv_high_speed_average_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.highSpeedDrivingAverageSpeed)
                    tv_low_speed_average_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.lowSpeedDrivingAverageSpeed)
                    tv_rapid_start_count.text = getDrivingInfoResponse.rapidStartCount.toInt().toString() + "회"
                    tv_rapid_acc_count_info.text = getDrivingInfoResponse.rapidAccelerationCount.toInt().toString() + "회"
                    tv_rapid_stop_count_info.text = getDrivingInfoResponse.rapidStopCount.toInt().toString() + "회"
                    tv_rapid_desc_count_info.text = getDrivingInfoResponse.rapidDecelerationCount.toInt().toString() + "회"
                    et_memo.setText(getDrivingInfoResponse.memo)
                    pastMemo = getDrivingInfoResponse.memo
                    state.data.type?.let{
                        tv_type.text = CorpType.valueOf(it).description
                    }

                    getDrivingInfoResponse.userCar?.let{
                        if(it.type.equals(CORPORATE)){
                            btn_choose_mycar.isClickable = false
                        }else{
                            btn_choose_mycar.isClickable = true
                        }
                    }


                    if(getDrivingInfoResponse.startAddress != null){
                        tv_start_time.text = getDrivingInfoResponse.startAddress.road?.name?:getDrivingInfoResponse.startAddress.parcel?.name
                        tv_start_address.text = getDrivingInfoResponse.startAddress.road?.name?:getDrivingInfoResponse.startAddress.parcel?.name
                    }
                    if(getDrivingInfoResponse.endAddress != null){
                        tv_end_time.text = getDrivingInfoResponse.endAddress.road?.name?:getDrivingInfoResponse.endAddress.parcel?.name
                        tv_end_address.text = getDrivingInfoResponse.endAddress.road?.name?:getDrivingInfoResponse.endAddress.parcel?.name

                        if(!getDrivingInfoResponse.endAddress.places.isNullOrEmpty()){
                            tv_end_address_detail.text = getDrivingInfoResponse.endAddress.places?.get(0)?.name

                            tv_end_address_detail.setOnClickListener {
                                startActivity(Intent(this@DetailDriveHistoryActivity, AllAddressActivity::class.java).putExtra("places",Gson().toJson(getDrivingInfoResponse.endAddress.places)))
                            }
                        }
                    }

                    isActive = getDrivingInfoResponse.isActive
                    userCarId = getDrivingInfoResponse.userCarId

                    if(isActive){
                        if(!getDrivingInfoResponse.userCarId.isNullOrEmpty()){
                            // CarID

                            PreferenceUtil.getPref(this, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
                                if(it != ""){


                                    val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                                    val myCarsList: MutableList<MyCarsEntity> = GsonBuilder().serializeNulls().create().fromJson(it, type)

                                    val myCar = myCarsList.find { getDrivingInfoResponse.userCarId == it.id }
                                    tv_mycar.text = myCar?.name

                                    if(myCar?.type == PERSONAL){
                                        iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star2))
                                        btn_choose_corp.visibility = GONE
                                    }else{
                                        iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star1))
                                        btn_choose_corp.visibility = VISIBLE


                                    }
                                }
                            }

                        }else{
                            // 미확정
                            tv_mycar.text = getString(R.string.pending)
                            iv_corp.visibility = GONE
                            btn_choose_corp.visibility = GONE

                        }
                    }else{
                        // 내 차가 아니에요
                        tv_mycar.text = getString(R.string.not_my_car)
                        iv_corp.visibility = GONE
                        btn_choose_corp.visibility = GONE


                    }

                    if(isMyCarScope(getDrivingInfoResponse.endTime)){
                        tv_scope_date_mycar.text = transformDateTo30Dayslater(getDrivingInfoResponse.endTime)
                    } else{
                        tv_scope_date_mycar.text = "변경 가능 기간이 지났어요."
                    }

                }
                is GetDrivingInfoState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is GetDrivingInfoState.Empty -> {

                }
            }
        })
    }

    private fun setResources(){
        iv_tooltip_verification.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "데이터 인증이란?","주행 이력을 쌓는 방법에 따라 부여돼요.\n" +
                        "L3 = 안드로이드 오토, 애플 카플레이\n" +
                        "L2 = 블루투스\n" +
                        "L1 = 사용자 활동 자동 감지")
            }
        })

        iv_tooltip_low_speed.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "저속 주행이란?","저속 주행이란 0km/h 초과 40km/h 미만 속력으로 주행한 거리에요. 높을수록 좋아요!")
            }
        })

        iv_tooltip_high_speed.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "고속 주행이란?","80km/h 이상 150km/h 미만 사이의 속력으로 주행한 거리에요. 높을수록 좋아요!")
            }
        })

        iv_tooltip_rapid_desc.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "급감속이란?","초당 14km/h이상 감속 주행하고 속도가 6.0km/h 이상인 경우에요. 낮을수록 좋아요!")
            }
        })

        iv_tooltip_high_speed_average.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "고속 주행 평균 속력이란?","고속 주행 평균 속력은 80km/h 이상 150km/h 이하 속력으로 주행한 거리의 평균 속력이에요")
            }
        })

        iv_tooltip_low_speed_average.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "저속 주행 평균 속력이란?","저속 주행 평균 속력은 0km/h 초과 40km/h 미만 속력으로 주행한 거리의 평균 속력이에요")
            }
        })

        iv_tooltip_rapid_acc.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "급가속이란?","10km/h 초과 속도에서 초당 10km/h 이상 가속 주행한 경우에요. 낮을수록 좋아요!")
            }
        })

        iv_tooltip_rapid_start.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "급출발이란?","5.0km/h 이하 속도에서 출발하여 초당 10km/h이상 가속 주행한 경우에요. 낮을수록 좋아요!")
            }
        })

        iv_tooltip_rapid_stop.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "급정지란?","초당 14km/h이상 감속 주행하고 속도가 5.0km/h 이하인 경우에요. 낮을수록 좋아요!")
            }
        })


        btn_back.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                if(pastMemo != et_memo.text.toString())
                    detailDriveHistoryViewModel.patchMemo(et_memo.text.toString(), tracking_id)
                else{
                    val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                    intent.putExtra("isActive",isActive)
                    intent.putExtra("userCarId",userCarId)
                    intent.putExtra("trackingId",tracking_id)
                    intent.putExtra("userCar",Gson().toJson(userCar))
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        })

        btn_choose_mycar.setOnClickListener {
            showBottomSheetForEditCar()
        }

        detailDriveHistoryViewModel.getMapData(tracking_id)
        detailDriveHistoryViewModel.getDrivingInfo(tracking_id)
    }


    private fun setMapData() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->

            /**
             * 폴리라인 추가
             */
            val polylineOptions = PolylineOptions()
                .clickable(true)
                .addAll(polylines)
                .addSpan(
                    StyleSpan(
                        StrokeStyle.gradientBuilder(
                            resources.getColor(R.color.map_start),
                            resources.getColor(R.color.map_end)
                        ).build()
                    )
                )
            googleMap.addPolyline(polylineOptions)

            /**
             * 지도의 Zoom 정도 설정
             */
            val boundsBuilder = LatLngBounds.Builder()
            for (point in polylines) {
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()
            val padding = 100  // 지도의 가장자리에 여유 공간을 주기 위한 패딩 (px 단위)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))


            googleMap.setOnMapLoadedCallback {
                try {
                    googleMap.snapshot { bitmap ->
                        iv_map.setImageBitmap(bitmap)
                        iv_map.visibility = GONE
                    }

                    /**
                     * 마커 추가
                     */
                    val markerPosition = LatLng(polylines[0].latitude, polylines[0].longitude)
                    currentMarker = googleMap.addMarker(
                        MarkerOptions().position(markerPosition).title("marker")
                    )

                    /**
                     * 마커 애니메이션 추가 및 애니메이션
                     */
                    moveMarkerAlongPolyline(googleMap, 0)


                    googleMap.setOnCameraMoveStartedListener { reason ->
                        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            isCameraMoving = true
                            currentAnimator?.pause() // 애니메이션 일시 중지
                        }
                    }

                    googleMap.setOnCameraIdleListener {
                        if (isCameraMoving) {
                            isCameraMoving = false
                            currentAnimator?.start() // 애니메이션 재개
                        }
                    }
                }catch (e:Exception){

                }
            }
        }
    }
    private fun moveMarkerAlongPolyline(googleMap: GoogleMap, index: Int) {
        val startPosition = polylines[index]
        val endPosition = polylines[(index + 1) % polylines.size]
        val zoomLevel = googleMap.cameraPosition.zoom

        /**
         * 줌 레벨에 따라 애니메이션 지속 시간 설정
         */
        var duration = 10L
        if (zoomLevel > 15) {
            duration = 120L
        } else if (zoomLevel < 15) {
            duration = 10L
        }

        /**
         * 이전 애니메이션 취소
         */
        currentAnimator?.cancel()


        /**
         * 마커 애니메이션 추가
         */
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = duration
        valueAnimator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            val newPosition = LatLng(
                startPosition.latitude + (endPosition.latitude - startPosition.latitude) * fraction,
                startPosition.longitude + (endPosition.longitude - startPosition.longitude) * fraction
            )
            // 마커의 위치만 업데이트
            currentMarker?.position = newPosition
        }
        valueAnimator.start()
        currentAnimator = valueAnimator


        /**
         * 마커 애니메이션 종료 리스너
         */
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // 애니메이터 초기화
                currentAnimator = null
            }
        })

        /**
         * 다음 지점으로 이동
         */
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val nextIndex = (index + 1) % polylines.size
            if (nextIndex < polylines.size - 1) {
                moveMarkerAlongPolyline(googleMap, nextIndex)
            }
        }, duration)
    }

    override fun onDestroy() {
        super.onDestroy()

        // 현재 진행 중인 애니메이터가 있으면 취소
        currentAnimator?.cancel()
        currentAnimator = null

        // 현재 마커가 있으면 제거
        currentMarker?.remove()
        currentMarker = null
    }

    // 두 지점 사이의 회전 각도를 계산하는 함수
    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizedEndDegrees = end - start
        val direction = if (normalizedEndDegrees > 180 || normalizedEndDegrees < -180) -1 else 1
        return start + direction * fraction * 360
    }

    // LatLng 객체 사이의 보간을 담당하는 인터페이스
    interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng

        class Linear : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                val lng = (b.longitude - a.longitude) * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private fun transformSecondsToHHMMSS(seconds:Double):String{
        val hours = seconds.toInt() / 3600
        val minutes = (seconds.toInt() % 3600) / 60
        val secs = seconds.toInt() % 60

        return "${hours}시간 ${minutes}분 ${secs}초"
    }


    fun transformDateTo30Dayslater(isoDate: String): String {
        // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환
        val zonedDateTime = ZonedDateTime.parse(isoDate)

        // 30일 후의 날짜 계산
        val newZonedDateTime = zonedDateTime.plusDays(30)

        // 원하는 형식의 DateTimeFormatter 생성
        val formatter = DateTimeFormatter.ofPattern("MM월 dd일", Locale.KOREAN)

        // 포맷된 문자열 반환
        val formattedDate = newZonedDateTime.format(formatter)
        return "$formattedDate" + "까지만 변경 가능해요"
    }

    fun isMyCarScope(isoDate: String):Boolean{
        // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환
        val zonedDateTime = ZonedDateTime.parse(isoDate)

        // 현재 날짜와 시간
        val now = ZonedDateTime.now()

        // 주어진 날짜로부터 30일 후의 날짜 계산
        val dateAfter30Days = zonedDateTime.plusDays(30)

        // 현재 날짜가 주어진 날짜와 30일 후의 날짜 사이에 있는지 확인
        return now.isAfter(zonedDateTime) && now.isBefore(dateAfter30Days)
    }


    override fun onBackPressed() {
        if(pastMemo != et_memo.text.toString())
            detailDriveHistoryViewModel.patchMemo(et_memo.text.toString(), tracking_id)
        else{
            val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
            intent.putExtra("isActive",isActive)
            intent.putExtra("userCarId",userCarId)
            intent.putExtra("trackingId",tracking_id)
            intent.putExtra("userCar",Gson().toJson(userCar))
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    fun showBottomSheetForEditCar() {
        PreferenceUtil.getPref(this, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
            if(it != ""){
                // Create a BottomSheetDialog
                val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)

                // Inflate the layout
                val bottomSheetView = this.layoutInflater.inflate(R.layout.dialog_connected_bluetooth, null)
                val tv_connected_bluetooth:TextView = bottomSheetView.findViewById(R.id.tv_connected_bluetooth)

                if(bluetoothNameExpected.isNullOrEmpty()){
                    tv_connected_bluetooth.visibility = GONE
                }else{
                    tv_connected_bluetooth.visibility = VISIBLE
                    tv_connected_bluetooth.text = "연결했던 블루투스: " + bluetoothNameExpected

                    if(tv_mycar.text.equals(getString(R.string.pending))){
                        tv_connected_bluetooth.visibility = VISIBLE
                    }else{
                        tv_connected_bluetooth.visibility = GONE
                    }
                }

                val rv_registered_car = bottomSheetView.findViewById<RecyclerView>(R.id.rv_registered_car)

                rv_registered_car.layoutManager = LinearLayoutManager(this)
                val dividerItemDecoration = DividerItemDecoration(this, R.color.white_op_100, this.dpToPx(8f)) // 색상 리소스와 구분선 높이 설정
                rv_registered_car.addItemDecoration(dividerItemDecoration)

                val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                val myCarsList: MutableList<MyCarsEntity> = GsonBuilder().serializeNulls().create().fromJson(it, type)

                for(car in myCarsList){
                    car.isActive = true
                }

                myCarsList.add(MyCarsEntity(null,null,null,null,null,false))
                myCarsList.add(MyCarsEntity(null,null,null,null,null,true))


                rv_registered_car.adapter = MyCarEntitiesAdapter(context = this, mycarEntities = myCarsList, tracking_id = tracking_id, viewModel = detailDriveHistoryViewModel, bottomSheetDialog, isActive, userCarId )

                // Set the content view of the dialog
                bottomSheetDialog.setContentView(bottomSheetView)

                // Show the dialog
                bottomSheetDialog.show()
            }
        }
    }

    fun showBottomSheetForChooseCorp(userCarId: String) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)

        // Inflate the layout
        val bottomSheetView = this.layoutInflater.inflate(R.layout.dialog_choose_corp, null)

        val rv_choose_corp = bottomSheetView.findViewById<RecyclerView>(R.id.rv_choose_corp)

        rv_choose_corp.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, R.color.white_op_100, this.dpToPx(8f)) // 색상 리소스와 구분선 높이 설정
        rv_choose_corp.addItemDecoration(dividerItemDecoration)

        rv_choose_corp.adapter = ChooseCorpAdapter(context = this, corpList = mutableListOf(CorpType.WORK.description, CorpType.NON_WORK.description,CorpType.COMMUTE.description), bottomSheetDialog, detailDriveHistoryViewModel, tracking_id, userCarId,tv_type.text.toString())

        // Set the content view of the dialog
        bottomSheetDialog.setContentView(bottomSheetView)

        // Show the dialog
        bottomSheetDialog.show()
    }

    class MyCarEntitiesAdapter(
        private val context: Context,
        private val mycarEntities: MutableList<MyCarsEntity>,
        private val tracking_id:String,
        private val viewModel: DetailDriveHistoryViewModel,
        private val bottomSheetDialog:BottomSheetDialog,
        private val isActive:Boolean,
        private val userCarId:String?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_connected_bluetooth, parent, false)
            return MyCarEntitiesHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MyCarEntitiesHolder) {
                val myCarsEntity = mycarEntities[position]

                if(myCarsEntity.isActive!!){
                    if(!myCarsEntity.name.isNullOrEmpty()){
                        if(isActive){
                            if(!userCarId.isNullOrEmpty()) {
                                if(myCarsEntity.id.equals(userCarId)){
                                    holder.layout_car.isSelected = true
                                    TextViewCompat.setTextAppearance(holder.tv_car_name, R.style.car_selected)
                                    TextViewCompat.setTextAppearance(holder.tv_car_no, R.style.car_no_selected)
                                }
                            }
                        }

                        myCarsEntity.type?.let{
                            if(it.equals(CORPORATE)){
                                holder.iv_corp.visibility = VISIBLE
                            }else{
                                holder.iv_corp.visibility = GONE
                            }
                        }

                        holder.layout_name.visibility = VISIBLE
                        holder.tv_car_no.visibility = VISIBLE
                        holder.tv_no_mycar.visibility = GONE

                        holder.tv_car_name.text = myCarsEntity.name

                        holder.layout_car.setOnClickListener {
                            myCarsEntity.type?.let{
                                if(it.equals(CORPORATE)){
                                    (context as DetailDriveHistoryActivity).showBottomSheetForChooseCorp(myCarsEntity.id!!)
                                    bottomSheetDialog.dismiss()
                                }else{
                                    viewModel.patchDrivingInfo(true, myCarsEntity.id, tracking_id)
                                    bottomSheetDialog.dismiss()
                                }
                            }
                        }

                        holder.tv_car_no.text = myCarsEntity.number
                    }else{
                        if(isActive) {
                            if (userCarId.isNullOrEmpty()) {
                                holder.layout_car.isSelected = true
                                TextViewCompat.setTextAppearance(holder.tv_no_mycar, R.style.car_selected)

                            }
                        }
                        // 미확정
                        holder.layout_name.visibility = GONE
                        holder.tv_car_no.visibility = GONE
                        holder.tv_no_mycar.visibility = VISIBLE

                        holder.tv_no_mycar.text = context.getString(R.string.pending)

                        holder.layout_car.setOnClickListener {
                            viewModel.patchDrivingInfo(true, null, tracking_id)
                            bottomSheetDialog.dismiss()
                        }
                        holder.iv_corp.visibility = GONE
                    }
                }else{
                    // 내 차가 아니에요
                    holder.layout_name.visibility = GONE
                    holder.tv_car_no.visibility = GONE
                    holder.tv_no_mycar.visibility = VISIBLE

                    holder.tv_no_mycar.text = context.getString(R.string.not_my_car)
                    holder.layout_car.setOnClickListener {
                        viewModel.patchDrivingInfo(false, null, tracking_id)
                        bottomSheetDialog.dismiss()
                    }

                    if(!isActive){
                        holder.layout_car.isSelected = true
                        TextViewCompat.setTextAppearance(holder.tv_no_mycar, R.style.car_selected)
                    }
                    holder.iv_corp.visibility = GONE

                }
            }
        }

        override fun getItemCount(): Int {
            return mycarEntities.size
        }
    }

    class MyCarEntitiesHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_car_name:TextView = view.findViewById(R.id.tv_car_name)
        val tv_car_no:TextView = view.findViewById(R.id.tv_car_no)
        val layout_name:LinearLayout = view.findViewById(R.id.layout_name)
        val tv_no_mycar:TextView = view.findViewById(R.id.tv_no_mycar)
        val layout_car:LinearLayout = view.findViewById(R.id.layout_car)
        val iv_corp:ImageView = view.findViewById(R.id.iv_corp)
    }

    class ChooseCorpAdapter(
        private val context: Context,
        private val corpList: MutableList<String>,
        private val bottomSheetDialog: BottomSheetDialog,
        private val viewModel: DetailDriveHistoryViewModel,
        private val tracking_id: String,
        private val userCarId: String? = null,
        private val type:String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_choose_corp, parent, false)
            return ChooseCorpHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ChooseCorpHolder) {
                val corp = corpList.get(position)
                holder.tv_corp.text = corp

                if(holder.tv_corp.text.equals(type)){
                    holder.layout_car.isSelected = true
                    TextViewCompat.setTextAppearance(holder.tv_corp, R.style.type_selected)
                }

                holder.layout_car.setOnClickListener {
                    if(userCarId != null){
                        CustomDialog(context, "이동수단 변경", "이동 수단을 법인차로 저장하면 더이상\n변경할 수 없습니다. 변경하시겠습니까?", "변경","취소",  object : CustomDialog.DialogCallback{
                            override fun onConfirm() {
                                viewModel.patchCorpType(userCarId, true, getNameFromDescription(corp), tracking_id = tracking_id)
                                bottomSheetDialog.dismiss()
                            }

                            override fun onCancel() {
                                bottomSheetDialog.dismiss()
                            }

                        }).show()
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return corpList.size
        }


        fun getNameFromDescription(description: String): String {
            return CorpType.values().find { it.description == description }?.name!!
        }

    }

    class ChooseCorpHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout_car:LinearLayout = view.findViewById(R.id.layout_car)
        val tv_corp:TextView = view.findViewById(R.id.tv_corp)
    }


    fun getMatchingTitle(vWorldResponse: VWorldResponse, vWorldDetailResponse: VWorldDetailResponse): String {
        for (item in vWorldDetailResponse.response.result.items) {
            val title = item.title
            if (keywords.any { keyword -> title.contains(keyword) }) {
                return title // 매칭되는 title을 찾으면 바로 반환
            }
        }

        return vWorldResponse.response.result.first().text // 매칭되는 title이 없으면 null 반환
    }


}