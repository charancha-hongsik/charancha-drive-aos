package com.milelog.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.milelog.DividerItemDecoration
import com.milelog.R
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.DetectUserEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetectedStatusActivity: BaseRefreshActivity() {
    lateinit var rv_status:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detected_status)

        rv_status = findViewById(R.id.rv_detected_statsus)

        rv_status.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, R.color.gray_50, dpToPx(12f)) // 색상 리소스와 구분선 높이 설정
        rv_status.addItemDecoration(dividerItemDecoration)


        val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(this)
        driveDatabase.detectUserDao().detectUserEntity?.let {
            if(it.size > 0){
                rv_status.adapter = DetectedStatusAdapter(context = this, detectedUserEntity = it.toMutableList())
            }
        }

    }

    class DetectedStatusAdapter(
        private val context: Context,
        private val detectedUserEntity: MutableList<DetectUserEntity>,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_detected_status, parent, false)
            return DetectedStatusViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is DetectedStatusViewHolder) {
                val userEntity = detectedUserEntity[position]

                holder.tv_detected1.text = convertTimestampToMMddHHmmssSSS(userEntity.timestamp.toLong())
                holder.tv_detected2.text = userEntity.verification
                holder.tv_detected3.text = userEntity.start_stop
                holder.tv_detected4.text = userEntity.sensor_state.toString()

                holder.tv_detected1.setOnClickListener {
                    Toast.makeText(context,holder.tv_detected1.text , Toast.LENGTH_SHORT).show()
                }

                holder.tv_detected2.setOnClickListener {
                    Toast.makeText(context,holder.tv_detected2.text , Toast.LENGTH_SHORT).show()

                }

                holder.tv_detected3.setOnClickListener {
                    Toast.makeText(context,holder.tv_detected3.text , Toast.LENGTH_SHORT).show()

                }

                holder.tv_detected4.setOnClickListener {
                    Toast.makeText(context,holder.tv_detected4.text , Toast.LENGTH_SHORT).show()

                }
            }
        }

        override fun getItemCount(): Int {
            return detectedUserEntity.size
        }

        fun convertTimestampToMMddHHmmssSSS(timestamp: Long): String {
            val date = Date(timestamp)  // Long 타입의 timestamp를 Date 객체로 변환
            val format = SimpleDateFormat("MM월dd일 HH시mm분ss초.SSS", Locale.getDefault())  // 밀리초까지 포함한 포맷 설정
            return format.format(date)  // 포맷에 맞게 변환하여 반환
        }
    }

    class DetectedStatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_detected1:TextView  = view.findViewById(R.id.tv_detected_text1)
        val tv_detected2:TextView  = view.findViewById(R.id.tv_detected_text2)
        val tv_detected3:TextView  = view.findViewById(R.id.tv_detected_text3)
        val tv_detected4:TextView  = view.findViewById(R.id.tv_detected_text4)
    }
}