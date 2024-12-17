package com.milelog.activity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.milelog.CommonUtil
import com.milelog.DividerItemDecoration
import com.milelog.R
import com.milelog.retrofit.response.Meta
import com.milelog.room.entity.AlarmEntity
import com.milelog.viewmodel.AlarmViewModel
import com.milelog.viewmodel.BaseViewModel


class AlarmActivity: BaseRefreshActivity() {
    lateinit var rv_alarm: RecyclerView
    lateinit var btn_back: ImageView
    lateinit var layout_no_data: ConstraintLayout

    private val alarmViewModel: AlarmViewModel by viewModels()
    var notifications: MutableList<AlarmEntity> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        init()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 0 // 알림을 생성할 때 사용한 ID
        notificationManager.cancel(notificationId)

        alarmViewModel.getAlarms(0)
        alarmViewModel.setAllAlarm.observe(this@AlarmActivity, BaseViewModel.EventObserver {
            it?.let{

                if(notifications.size > 0){
                    layout_no_data.visibility = GONE
                    rv_alarm.visibility = VISIBLE
                }else{
                    layout_no_data.visibility = VISIBLE
                    rv_alarm.visibility = GONE
                }

                if(notifications.size != 0)
                    notifications.removeLast()

                var hasNext = false

                if(alarmViewModel.alarmCnt > it.size + notifications.size){
                    hasNext = true
                }

                notifications.addAll(it.toMutableList())
                notifications.add(AlarmEntity("","","","","","","",false))

                rv_alarm.adapter = NotificationAdapter(context = this, alarmViewModel = alarmViewModel, notifications = notifications, meta = Meta(0,0,hasNext,"",""), callback = object:NotificationAdapter.DriveCallback{
                    override fun clickedMore(meta: Meta, histories: MutableList<AlarmEntity>) {
                        alarmViewModel.getAlarms(notifications.size-1)
                    }
                })

                for(noti in notifications){
                    alarmViewModel.updateIsRequired(noti.idx)
                }
            }
        })

        alarmViewModel.updateIsRequired.observe(this@AlarmActivity, BaseViewModel.EventObserver{
            for(noti in notifications){
                if(noti.idx == it){
                    noti.isRequired = false
                }
            }
            (rv_alarm.adapter as NotificationAdapter).notifyDataSetChanged()
        })
    }

    private fun init(){
        alarmViewModel.init(this)

        rv_alarm = findViewById(R.id.rv_alarm)
        btn_back = findViewById(R.id.btn_back)
        layout_no_data = findViewById(R.id.layout_no_data)

        rv_alarm.layoutManager = LinearLayoutManager(this@AlarmActivity)
        val dividerItemDecoration = DividerItemDecoration(this@AlarmActivity, R.color.gray_50, dpToPx(12f)) // 색상 리소스와 구분선 높이 설정
        rv_alarm.addItemDecoration(dividerItemDecoration)

        btn_back.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                finish()
            }

        })
    }

    class NotificationAdapter(
        private val context: Context,
        private val alarmViewModel: AlarmViewModel,
        private val notifications: MutableList<AlarmEntity>,
        private val meta: Meta,
        private val callback: DriveCallback
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
            private const val VIEW_TYPE_LAST_ITEM = 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == notifications.size - 1) VIEW_TYPE_LAST_ITEM else VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_ITEM) {
                val view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false)
                NotificationViewHolder(view)
            } else {
                val view = LayoutInflater.from(context).inflate(R.layout.item_drive_history_last, parent, false)
                LastItemViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is NotificationViewHolder) {
                val notiItem = notifications[position]

                holder.tv_alarm.text = notiItem.title
                holder.tv_alarm_contents.text = notiItem.body

                if(notiItem.type == "MARKETING"){
                    holder.iv_alarm.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_marketing_alarm))
                } else if(notiItem.type == "NOTICE"){
                    holder.iv_alarm.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_noti_alarm))

                } else if(notiItem.type == "DRIVING"){
                    holder.iv_alarm.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_drive_history_alarm))
                } else{
                    holder.iv_alarm.visibility = INVISIBLE
                }

                if(notiItem.isRequired){
                    holder.ic_required.visibility = VISIBLE
                }else{
                    holder.ic_required.visibility = GONE
                }

                holder.layout_noti.setOnClickListener(object:OnSingleClickListener(){
                    override fun onSingleClick(v: View?) {
                        /**
                         * 상세화면 클릭 시 isRequired false 처리
                         */
//                        alarmViewModel.updateIsRequired(notiItem.idx)

                        if(notiItem.deepLink.isNotEmpty())
                            context.startActivity(Intent(context, AlarmDetailActivity::class.java).putExtra("url",notiItem.deepLink))
                    }
                })

                holder.tv_alarm_date.text = CommonUtil.getAlarmDate(notiItem.timestamp)





            } else if (holder is LastItemViewHolder) {
                if (!meta.hasNextPage) {
                    holder.tvLast.text = "마지막 알림이에요"
                    holder.tvMore.visibility = View.GONE
                    holder.tvLast.visibility = View.VISIBLE
                    holder.btn_more.setOnClickListener { object :OnSingleClickListener(){
                        override fun onSingleClick(v: View?) {

                        }
                    }
                    }
                } else {
                    holder.tvMore.visibility = View.VISIBLE
                    holder.tvLast.visibility = View.GONE

                    holder.btn_more.setOnClickListener { object :OnSingleClickListener(){
                        override fun onSingleClick(v: View?) {
                            callback.clickedMore(meta, notifications)
                        }
                    }
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return notifications.size
        }

        class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMore: TextView = view.findViewById(R.id.tv_more)
            val btn_more:ConstraintLayout = view.findViewById(R.id.btn_more)
            val tvLast: TextView = view.findViewById(R.id.tv_last)
        }

        interface DriveCallback {
            fun clickedMore(meta: Meta, histories: MutableList<AlarmEntity>)
        }
    }

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout_noti:LinearLayout = view.findViewById(R.id.layout_noti)
        val iv_alarm: ImageView = view.findViewById(R.id.iv_alarm)
        val tv_alarm: TextView = view.findViewById(R.id.tv_alarm)
        val ic_required: ImageView = view.findViewById(R.id.ic_required)
        val tv_alarm_contents: TextView = view.findViewById(R.id.tv_alarm_contents)
        val tv_alarm_date: TextView = view.findViewById(R.id.tv_alarm_date)
    }

}