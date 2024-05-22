package com.charancha.drive.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.R
import com.charancha.drive.calculateData
import com.charancha.drive.room.DriveDto
import com.charancha.drive.room.entity.Drive
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class DetailDriveHistoryActivity: AppCompatActivity() {
    lateinit var tvTrackingId:TextView
    lateinit var tvTimestamp:TextView
    lateinit var tvRank:TextView
    lateinit var tvDistance:TextView
    lateinit var tvTime:TextView
    lateinit var tvRapid1:TextView
    lateinit var tvRapid2:TextView

    lateinit var driveDto:DriveDto
    val polylines:MutableList<LatLng> = mutableListOf()

    private val mMap: GoogleMap? = null
    private var currentMarker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_drive_history)

        init()
    }

    private fun init(){
        tvTrackingId = findViewById(R.id.tv_tracking_id)
        tvTimestamp = findViewById(R.id.tv_timestamp)
        tvRank = findViewById(R.id.tv_rank)
        tvDistance = findViewById(R.id.tv_distance)
        tvTime = findViewById(R.id.tv_time)
        tvRapid1 = findViewById(R.id.tv_rapid1)
        tvRapid2 = findViewById(R.id.tv_rapid2)

        driveDto = intent.getSerializableExtra("driveDto") as DriveDto

        for(raw in driveDto.jsonData){
            polylines.add(LatLng(raw.latitude,raw.longtitude))
        }

        /**
         * timeStamp
         * Verification
         * distance
         * time
         * sudden_stop (count)
         * sudden_acceleration (count)
         * sudden_deceleration (count)
         * sudden_start (count)
         * high_speed_driving (m)
         * low_speed_driving (m)
         * constant_speed_driving (m)
         * harsh_driving (m)
         */
        tvTrackingId.text = "id : " + driveDto.tracking_id
        tvTimestamp.text = "주행시작 : " + getDateFromTimeStamp(driveDto.timeStamp)
        tvRank.text = "랭크 : " + driveDto.verification
        tvDistance.text = "주행거리(m) : " + driveDto.distance_array.sum()
        tvTime.text = "주행 시간 : " + (TimeUnit.MILLISECONDS.toSeconds(driveDto.time)/60) + "분 " + (TimeUnit.MILLISECONDS.toSeconds(driveDto.time)%60) + "초"

        var contents = ""
        contents = contents + "주행 종료 : " + getDateFromTimeStamp((driveDto.timeStamp + driveDto.time)) + "\n"
        contents = contents +  "항속 주행 거리 : " + calculateData.getConstantSpeedDriving(driveDto.jsonData.toMutableList()).sum() + "\n"
        contents = contents + "Harsh Driving 거리: " + calculateData.getHarshDriving(driveDto.jsonData.toMutableList()).sum() + "\n"

        tvRapid1.text = contents

        if(polylines.size != 0){
            setMapData()
        }
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
}