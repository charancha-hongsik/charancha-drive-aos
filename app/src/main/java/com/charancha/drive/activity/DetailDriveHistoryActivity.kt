package com.charancha.drive.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.R
import com.charancha.drive.room.DriveDto
import com.charancha.drive.room.entity.Drive
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
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

        for(raw in driveDto.rawData){
            polylines.add(LatLng(raw.latitude,raw.longtitude))
        }


        tvTrackingId.text = "id : " + driveDto.tracking_id
        tvTimestamp.text = "주행시작 : " + getDateFromTimeStamp(driveDto.timeStamp)
        tvRank.text = "랭크 : " + driveDto.rank
        tvDistance.text = "주행거리(m) : " + driveDto.distance
        tvTime.text = "주행 시간 : " + (TimeUnit.MILLISECONDS.toSeconds(driveDto.time)/60) + "분 " + (TimeUnit.MILLISECONDS.toSeconds(driveDto.time)%60) + "초"
        tvRapid1.text = "주행 종료 : " + getDateFromTimeStamp((driveDto.timeStamp + driveDto.time))

        if(polylines.size != 0){
            setMapData()
        }
    }

    private fun getDateFromTimeStamp(timeStamp:Long) : String{
        val format = SimpleDateFormat("yyyy-MM-dd / hh:mm:ss")
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
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(polylines.get(polylines.size/2), 10f))

//            // Set listeners for click events.
//            it.setOnPolylineClickListener(this)
//            it.setOnPolygonClickListener(this)
        })
    }
}