package com.charancha.drive.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.car.app.hardware.info.Speed
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.request.PatchDrivingInfo
import com.charancha.drive.retrofit.request.SignUpRequest
import com.charancha.drive.retrofit.response.GetDrivingInfoResponse
import com.charancha.drive.viewmodel.DetailDriveHistoryViewModel
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
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class DetailDriveHistoryActivity: BaseActivity() {
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
    var distance_unit = "km"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_drive_history)

        detailDriveHistoryViewModel.init(applicationContext)

        init()
        setResources()
        getDriveDetail()

        distance_unit = PreferenceUtil.getPref(this@DetailDriveHistoryActivity,  PreferenceUtil.KM_MILE, "km")!!
    }

    private fun init(){

        detailDriveHistoryViewModel.setDriveForApp.observe(this@DetailDriveHistoryActivity, DetailDriveHistoryViewModel.EventObserver {
            for(raw in it.gpses){
                polylines.add(LatLng(raw.latitude,raw.longtitude))
            }

            if(polylines.size != 0){
                setMapData()
            }
        })
        tracking_id = intent.getStringExtra("tracking_id").toString()
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

    }

    private fun setResources(){
        btn_back.setOnClickListener {
            finish()
        }

        btn_choose_mycar.setOnClickListener {
            layout_my_drive.visibility = VISIBLE
        }

        layout_my_drive.setOnClickListener {
            layout_my_drive.visibility = GONE
        }

        btn_set_mycar.setOnClickListener {
            if(btn_mycar.isSelected){
                val gson = Gson()
                val jsonParam =
                    gson.toJson(PatchDrivingInfo(true))
                apiService().patchDrivingInfo("Bearer " + PreferenceUtil.getPref(this@DetailDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object:Callback<ResponseBody>{
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {

                        if(response.code() == 200){
                            tv_mycar.visibility = VISIBLE
                            tv_not_mycar.visibility = GONE
                        }
                        layout_my_drive.visibility = GONE

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
                        if(response.code() == 200){
                            tv_mycar.visibility = GONE
                            tv_not_mycar.visibility = VISIBLE
                        }
                        layout_my_drive.visibility = GONE

                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        layout_my_drive.visibility = GONE
                    }
                })

            }
        }

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

    private fun getDateFromTimeStamp(timeStamp:Long) : String{
        val format = SimpleDateFormat("yyyy-MM-dd / HH:mm:ss")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val time = Date()

        return format.format(timeStamp).toString()
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
                if(response.code() == 200){
                    val getDrivingInfoResponse = Gson().fromJson(response.body()?.string(), GetDrivingInfoResponse::class.java)

                    tv_date.text = transformTimeToYYYYMMDD(getDrivingInfoResponse.createdAt)
                    tv_distance.text = transferDistanceUnit(getDrivingInfoResponse.totalDistance)
                    tv_start_time.text = transformTimeToHHMM(getDrivingInfoResponse.startTime)
                    tv_end_time.text = transformTimeToHHMM(getDrivingInfoResponse.endTime)
                    tv_start_time_info.text = transformTimeToYYYYMMDDHHMMSS(getDrivingInfoResponse.startTime)
                    tv_end_time_info.text = transformTimeToYYYYMMDDHHMMSS(getDrivingInfoResponse.endTime)
                    tv_drive_time_info.text = transformSecondsToHHMMSS(getDrivingInfoResponse.totalTime)
                    tv_drive_distance_info.text = transferDistanceUnit(getDrivingInfoResponse.totalDistance)
                    tv_drive_verification_info.text = getDrivingInfoResponse.verification
                    tv_high_speed_driving_percent_info.text = getDrivingInfoResponse.highSpeedDrivingDistancePercentage.toString() + "%"
                    tv_low_speed_driving_percent_info.text = getDrivingInfoResponse.lowSpeedDrivingDistancePercentage.toString() + "%"
                    tv_max_speed_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.maxSpeed)
                    tv_high_speed_average_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.highSpeedDrivingAverageSpeed)
                    tv_low_speed_average_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.lowSpeedDrivingAverageSpeed)
                    tv_rapid_start_count.text = getDrivingInfoResponse.rapidStartCount.toInt().toString() + "회"
                    tv_rapid_acc_count_info.text = getDrivingInfoResponse.rapidAccelerationCount.toInt().toString() + "회"
                    tv_rapid_stop_count_info.text = getDrivingInfoResponse.rapidStopCount.toInt().toString() + "회"
                    tv_rapid_desc_count_info.text = getDrivingInfoResponse.rapidDecelerationCount.toInt().toString() + "회"

                    if(isMyCarScope(getDrivingInfoResponse.endTime)){
                        tv_scope_date_mycar.text = transformDateTo30Dayslater(getDrivingInfoResponse.endTime)
                        tv_mycar_scope_info.visibility = GONE
                    } else{
                        tv_scope_date_mycar.text = "변경 가능 기간이 지났어요."
                        tv_mycar_scope_info.visibility = VISIBLE
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
                TODO("Not yet implemented")
            }

        })
    }

    private fun formatIsoToKorean(isoDate: String): String {
        // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환
        val zonedDateTime = ZonedDateTime.parse(isoDate)

        // 원하는 형식의 DateTimeFormatter 생성
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초", Locale.KOREAN)

        // 포맷된 문자열 반환
        return zonedDateTime.format(formatter)
    }

    private fun transformTimeToYYYYMMDD(isoDate: String):String{
        // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환
        val zonedDateTime = ZonedDateTime.parse(isoDate)

        // 원하는 형식의 DateTimeFormatter 생성
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)

        // 포맷된 문자열 반환
        return zonedDateTime.format(formatter)
    }

    private fun transformTimeToHHMM(isoDate: String):String{
        // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환
        val zonedDateTime = ZonedDateTime.parse(isoDate)

        // 원하는 형식의 DateTimeFormatter 생성
        val formatter = DateTimeFormatter.ofPattern("HH시 mm분", Locale.KOREAN)

        // 포맷된 문자열 반환
        return zonedDateTime.format(formatter)
    }

    private fun getSpeedWithDistanceUnit(speed: Double):String{
        if(distance_unit == "km"){
            return String.format(Locale.KOREAN, "%.5fkm/h", speed)
        }else{
            val milesPerKilometer = 0.621371
            return String.format(Locale.KOREAN, "%.5fmile/h", speed * milesPerKilometer)
        }
    }

    private fun transformTimeToYYYYMMDDHHMMSS(isoDate: String):String{
        // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환
        val zonedDateTime = ZonedDateTime.parse(isoDate)

        // 원하는 형식의 DateTimeFormatter 생성
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일\nHH시 mm분 ss초", Locale.KOREAN)

        // 포맷된 문자열 반환
        return zonedDateTime.format(formatter)
    }

    private fun transformSecondsToHHMMSS(seconds:Double):String{
        val hours = seconds.toInt() / 3600
        val minutes = (seconds.toInt() % 3600) / 60
        val secs = seconds.toInt() % 60

        return "${hours}시간 ${minutes}분 ${secs}초"
    }

    fun transferDistanceUnit(meters:Double):String{
        if(distance_unit == "km"){
            return String.format(Locale.KOREAN, "%.5fkm", meters / 1000)
        }else{
            val milesPerMeter = 0.000621371
            return String.format(Locale.KOREAN, "%.5fmile",meters * milesPerMeter)
        }
    }

    private fun transformTimestampToHHMM(timestamp:Long):String{
        val instant = Instant.ofEpochMilli(timestamp)

        // Instant를 LocalDateTime으로 변환 (기본 시간대 사용)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        // 원하는 형식의 DateTimeFormatter 생성
        val formatter = DateTimeFormatter.ofPattern("HH시 mm분", Locale.KOREAN)

        // 포맷된 문자열 반환
        return dateTime.format(formatter)
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

}