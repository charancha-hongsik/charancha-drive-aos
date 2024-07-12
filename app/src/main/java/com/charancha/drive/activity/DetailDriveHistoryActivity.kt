package com.charancha.drive.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.request.AgreeTermsRequest
import com.charancha.drive.retrofit.request.GetDrivingInfoRequest
import com.charancha.drive.retrofit.response.GetDrivingInfoResponse
import com.charancha.drive.retrofit.response.SignInResponse
import com.charancha.drive.viewmodel.DetailDriveHistoryViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.security.auth.callback.Callback


class DetailDriveHistoryActivity: BaseActivity() {
    lateinit var tvTrackingId:TextView
    lateinit var tvTimestamp:TextView
    lateinit var tvRank:TextView
    lateinit var tvDistance:TextView
    lateinit var tvTime:TextView
    lateinit var tvRapid1:TextView
    lateinit var tvRapid2:TextView

    lateinit var tracking_id:String

    val polylines:MutableList<LatLng> = mutableListOf()

    private val mMap: GoogleMap? = null
    private var currentMarker: Marker? = null

    private val detailDriveHistoryViewModel: DetailDriveHistoryViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_drive_history)

        detailDriveHistoryViewModel.init(applicationContext)

        init()
        getDriveDetail()
    }

    private fun init(){
        tvTrackingId = findViewById(R.id.tv_tracking_id)
        tvTimestamp = findViewById(R.id.tv_timestamp)
        tvRank = findViewById(R.id.tv_rank)
        tvDistance = findViewById(R.id.tv_distance)
        tvTime = findViewById(R.id.tv_time)
        tvRapid1 = findViewById(R.id.tv_rapid1)
        tvRapid2 = findViewById(R.id.tv_rapid2)

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
        apiService().getDrivingInfo("Bearer " + PreferenceUtil.getPref(this@DetailDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id).enqueue(object :
            retrofit2.Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val getDrivingInfoResponse = Gson().fromJson(response.body()?.string(), GetDrivingInfoResponse::class.java)
                    Log.d("testestestestset","teststeststset :: totalTime " + getDrivingInfoResponse.totalTime)
                    Log.d("testestestestset","teststeststset :: totalDistance " + getDrivingInfoResponse.totalDistance)
                    Log.d("testestestestset","teststeststset :: optimalDrivingDistance" + getDrivingInfoResponse.optimalDrivingDistance)
                    Log.d("testestestestset","teststeststset :: lowSpeedDrivingDistancePercentage " + getDrivingInfoResponse.lowSpeedDrivingDistancePercentage)
                    Log.d("testestestestset","teststeststset :: lowSpeedDrivingMaxSpeed " + getDrivingInfoResponse.lowSpeedDrivingMaxSpeed)
                    Log.d("testestestestset","teststeststset :: averageSpeed " + getDrivingInfoResponse.averageSpeed)
                    Log.d("testestestestset","teststeststset :: lowSpeedDrivingDistancePercentage " + getDrivingInfoResponse.lowSpeedDrivingDistancePercentage)
                    Log.d("testestestestset","teststeststset :: startTime " + getDrivingInfoResponse.startTime)
                    Log.d("testestestestset","teststeststset :: optimalDrivingPercentage " + getDrivingInfoResponse.optimalDrivingPercentage)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }
}