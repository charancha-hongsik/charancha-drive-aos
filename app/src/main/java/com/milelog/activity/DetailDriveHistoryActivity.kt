package com.milelog.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.milelog.R
import com.milelog.PreferenceUtil
import com.milelog.retrofit.request.PatchDrivingInfo
import com.milelog.retrofit.response.GetDrivingInfoResponse
import com.milelog.viewmodel.DetailDriveHistoryViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


class DetailDriveHistoryActivity: BaseRefreshActivity() {
    lateinit var tracking_id:String

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
    lateinit var btn_mycar: LinearLayout
    lateinit var btn_not_mycar:LinearLayout
    lateinit var tv_scope_date_mycar:TextView

    lateinit var layout_my_drive:CoordinatorLayout
    lateinit var persistent_bottom_sheet:LinearLayout
    lateinit var behavior: BottomSheetBehavior<LinearLayout>

    lateinit var btn_choose_mycar: ConstraintLayout
    lateinit var btn_set_mycar:TextView
    lateinit var tv_mycar:LinearLayout
    lateinit var tv_not_mycar:TextView
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

    lateinit var view_map:CardView

    var isActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_drive_history)

        detailDriveHistoryViewModel.init(applicationContext)

        init()
        setResources()
        getDriveDetail()
    }

    private fun init(){
        tracking_id = intent.getStringExtra("trackingId").toString()
        isActive = intent.getBooleanExtra("isActive",true)

        detailDriveHistoryViewModel.getDrive(tracking_id)

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
        btn_mycar = findViewById(R.id.btn_mycar)
        btn_not_mycar = findViewById(R.id.btn_not_mycar)
        layout_my_drive = findViewById(R.id.layout_my_drive)
        persistent_bottom_sheet = findViewById(R.id.persistent_bottom_sheet)
        btn_choose_mycar = findViewById(R.id.btn_choose_mycar)
        btn_set_mycar = findViewById(R.id.btn_set_mycar)
        tv_mycar = findViewById(R.id.tv_mycar)
        tv_not_mycar = findViewById(R.id.tv_not_mycar)
        tv_scope_date_mycar = findViewById(R.id.tv_scope_date_mycar)
        tv_mycar_scope_info = findViewById(R.id.tv_mycar_scope_info)

        iv_tooltip_verification = findViewById(R.id.iv_tooltip_verification)
        iv_tooltip_low_speed = findViewById(R.id.iv_tooltip_low_speed)
        iv_tooltip_high_speed = findViewById(R.id.iv_tooltip_high_speed)
        iv_tooltip_rapid_desc = findViewById(R.id.iv_tooltip_rapid_desc)
        iv_tooltip_rapid_stop = findViewById(R.id.iv_tooltip_rapid_stop)
        iv_tooltip_high_speed_average = findViewById(R.id.iv_tooltip_high_speed_average)
        iv_tooltip_low_speed_average = findViewById(R.id.iv_tooltip_low_speed_average)
        iv_tooltip_rapid_start = findViewById(R.id.iv_tooltip_rapid_start)
        iv_tooltip_rapid_acc = findViewById(R.id.iv_tooltip_rapid_acc)

        view_map = findViewById(R.id.view_map)

        detailDriveHistoryViewModel.setDriveForApp.observe(this@DetailDriveHistoryActivity, DetailDriveHistoryViewModel.EventObserver {
            it?.let{
                for(raw in it.gpses){
                    polylines.add(LatLng(raw.latitude,raw.longtitude))
                }

                if(polylines.size != 0){
                    view_map.visibility = VISIBLE
                    tv_mycar_scope_info.visibility = GONE
                    setMapData()
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
                val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                intent.putExtra("isActive",isActive)
                intent.putExtra("trackingId",tracking_id)
                setResult(RESULT_OK, intent)
                finish()
            }
        })

        btn_choose_mycar.setOnClickListener {
            layout_my_drive.visibility = VISIBLE
        }

        layout_my_drive.setOnClickListener {
            layout_my_drive.visibility = GONE
        }

        btn_set_mycar.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                if(btn_mycar.isSelected){
                    val gson = Gson()
                    val jsonParam =
                        gson.toJson(PatchDrivingInfo(true))
                    apiService().patchDrivingInfo("Bearer " + PreferenceUtil.getPref(this@DetailDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object:Callback<ResponseBody>{
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            try {
                                if (response.code() == 200 || response.code() == 201) {
                                    tv_mycar.visibility = VISIBLE
                                    tv_not_mycar.visibility = GONE

                                    isActive = true

                                }
                                layout_my_drive.visibility = GONE
                            }catch (e:Exception){

                            }

                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            layout_my_drive.visibility = GONE
                        }

                    })
                }else{
                    val gson = Gson()
                    val jsonParam =
                        gson.toJson(PatchDrivingInfo(false))

                    apiService().patchDrivingInfo("Bearer " + PreferenceUtil.getPref(this@DetailDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object:Callback<ResponseBody>{
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if(response.code() == 200 || response.code() == 201){
                                tv_mycar.visibility = GONE
                                tv_not_mycar.visibility = VISIBLE

                                isActive = false
                            }
                            layout_my_drive.visibility = GONE

                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            layout_my_drive.visibility = GONE
                        }
                    })

                }
            }

        })

        btn_mycar.setOnClickListener {
            btn_mycar.isSelected = true
            btn_not_mycar.isSelected = false
        }

        btn_not_mycar.setOnClickListener {
            btn_mycar.isSelected = false
            btn_not_mycar.isSelected = true
        }

        btn_mycar.isSelected = true

        persistentBottomSheetEvent()
    }


    private fun setMapData(){
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(OnMapReadyCallback {
            // Add polylines to the map.
            // Polylines are useful to show a route or some other connection between points.


            it.addPolyline(
                PolylineOptions()
                    .clickable(true)
                    .addAll(polylines))

            // Position the map's camera near Alice Springs in the center of Australia,
            // and set the zoom factor so most of Australia shows on the screen.
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(polylines.get(polylines.size/2), 13f))


            // 마커를 추가합니다.
            val markerPosition = LatLng(polylines[0].latitude, polylines[0].longitude)
            currentMarker = it.addMarker(MarkerOptions().position(markerPosition).title("marker"))

            // 첫 번째 마커부터 시작하여 나머지 마커를 이동시키는 애니메이션을 시작합니다.
            moveMarkerAlongPolyline(it, 0)

        })
    }

    // Polyline을 따라 마커를 이동시키는 애니메이션을 생성합니다.
    private fun moveMarkerAlongPolyline(googleMap: GoogleMap,  index: Int) {
        val startPosition = polylines[index]
        val endPosition = polylines[(index + 1) % polylines.size]
        val duration = 10L // 애니메이션의 지속 시간
        val handler = Handler(Looper.getMainLooper())

        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = duration
        valueAnimator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            val newPosition = LatLng(
                startPosition.latitude + (endPosition.latitude - startPosition.latitude) * fraction,
                startPosition.longitude + (endPosition.longitude - startPosition.longitude) * fraction
            )
            currentMarker?.position = newPosition // 현재 마커의 위치 업데이트
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition))
        }
        valueAnimator.start()

        // 다음 지점으로 이동합니다.
        handler.postDelayed({
            val nextIndex = (index + 1) % polylines.size
            if(nextIndex >= polylines.size-1)

            else
                moveMarkerAlongPolyline(googleMap, nextIndex)

        }, duration)
    }

    private fun animateMarkerToGB(marker: Marker, finalPosition: LatLng, hideMarker: Boolean) {
        val startPosition = marker.position
        val endPosition = finalPosition
        val startRotation = marker.rotation
        val latLngInterpolator = LatLngInterpolator.Linear()
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 10000 // 애니메이션 지속 시간 (10초)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { animation ->
            val v = animation.animatedFraction
            val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
            marker.position = newPosition
            marker.rotation = computeRotation(v, startRotation, 0f)
        }
        valueAnimator.start()
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

    private fun getDriveDetail(){
        apiService().getDrivingInfo("Bearer " + PreferenceUtil.getPref(this@DetailDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val getDrivingInfoResponse = Gson().fromJson(response.body()?.string(), GetDrivingInfoResponse::class.java)

                    tv_date.text = transformTimeToDate(getDrivingInfoResponse.createdAt)
                    tv_distance.text = transferDistanceWithUnit(getDrivingInfoResponse.totalDistance)
                    tv_start_time.text = transformTimeToHHMM(getDrivingInfoResponse.startTime)
                    tv_end_time.text = transformTimeToHHMM(getDrivingInfoResponse.endTime)
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

                    isActive = getDrivingInfoResponse.isActive

                    if(isMyCarScope(getDrivingInfoResponse.endTime)){
                        tv_scope_date_mycar.text = transformDateTo30Dayslater(getDrivingInfoResponse.endTime)
                    } else{
                        tv_scope_date_mycar.text = "변경 가능 기간이 지났어요."
                    }

                    if(getDrivingInfoResponse.isActive){
                        tv_mycar.visibility = VISIBLE
                        tv_not_mycar.visibility = GONE
                        btn_mycar.isSelected = true
                        btn_not_mycar.isSelected = false
                    }else{
                        tv_mycar.visibility = GONE
                        tv_not_mycar.visibility = VISIBLE
                        btn_mycar.isSelected = false
                        btn_not_mycar.isSelected = true
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
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


    private fun persistentBottomSheetEvent() {
        behavior = BottomSheetBehavior.from(persistent_bottom_sheet)
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

    override fun onBackPressed() {
        val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
        intent.putExtra("isActive",isActive)
        intent.putExtra("trackingId",tracking_id)
        setResult(RESULT_OK, intent)
        finish()
    }

}