package com.charancha.drive.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.TextViewCompat
import com.charancha.drive.ChosenDate
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.*
import com.charancha.drive.viewmodel.MyDriveHistoryViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*


class MyDriveHistoryActivity: BaseRefreshActivity() {
    lateinit var lv_history:ListView
    lateinit var btn_back:ImageView

    lateinit var btn_choose_date: ImageView
    lateinit var layout_choose_date: CoordinatorLayout
    lateinit var persistent_bottom_sheet: LinearLayout
    lateinit var btn_close_select_date:ImageView
    lateinit var btn_a_month:TextView
    lateinit var btn_six_month:TextView
    lateinit var btn_each_month:TextView
    lateinit var btn_inquire_date:TextView
    lateinit var tv_selected_date:TextView
    lateinit var tv_inquire_scope:TextView
    lateinit var listView_choose_date_own:ListView
    lateinit var layout_select_main:LinearLayout
    lateinit var btn_select_date_from_list:ConstraintLayout
    lateinit var layout_date_own:ConstraintLayout
    lateinit var layout_no_data:ConstraintLayout

    lateinit var behavior: BottomSheetBehavior<LinearLayout>
    lateinit var selectedDate:String

    lateinit var resultLauncher: ActivityResultLauncher<Intent>


    private val historyViewModel: MyDriveHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive_history)

        init()

        historyViewModel.init(applicationContext)
        historyViewModel.getAllDrive()
    }

    fun init(){
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                val trackingId = it.data?.getStringExtra("trackingId")
                val isActive = it.data?.getBooleanExtra("isActive",true)
            }
        }

        lv_history = findViewById(R.id.lv_history)
        btn_back = findViewById(R.id.btn_back)

        btn_choose_date = findViewById(R.id.btn_choose_date)
        layout_choose_date = findViewById(R.id.layout_choose_date)
        persistent_bottom_sheet = findViewById(R.id.persistent_bottom_sheet)
        btn_close_select_date = findViewById(R.id.btn_close_select_date)
        btn_a_month = findViewById(R.id.btn_a_month)
        btn_six_month = findViewById(R.id.btn_six_month)
        btn_each_month = findViewById(R.id.btn_each_month)
        btn_inquire_date = findViewById(R.id.btn_inquire_date)
        tv_selected_date = findViewById(R.id.tv_selected_date)
        tv_inquire_scope = findViewById(R.id.tv_inquire_scope)
        layout_select_main = findViewById(R.id.layout_select_main)
        layout_no_data = findViewById(R.id.layout_no_data)

        listView_choose_date_own = findViewById(R.id.listView_choose_date_own)
        btn_select_date_from_list = findViewById(R.id.btn_select_date_from_list)
        layout_date_own = findViewById(R.id.layout_date_own)


        btn_back.setOnClickListener {
            finish()
        }

        btn_choose_date.setOnClickListener {
            layout_choose_date.visibility = VISIBLE
        }

        layout_choose_date.setOnClickListener {
            layout_choose_date.visibility = GONE
        }

        btn_close_select_date.setOnClickListener {
            layout_choose_date.visibility = GONE
        }

        btn_inquire_date.setOnClickListener {
            layout_choose_date.visibility = GONE
        }

        btn_a_month.setOnClickListener {
            btn_a_month.isSelected = true
            btn_six_month.isSelected = false
            btn_each_month.isSelected = false

            TextViewCompat.setTextAppearance(btn_a_month, R.style.B1SBweight600)
            TextViewCompat.setTextAppearance(btn_six_month, R.style.B1Mweight500)
            TextViewCompat.setTextAppearance(btn_each_month, R.style.B1Mweight500)

            btn_select_date_from_list.visibility = GONE

        }

        btn_six_month.setOnClickListener {
            btn_a_month.isSelected = false
            btn_six_month.isSelected = true
            btn_each_month.isSelected = false

            TextViewCompat.setTextAppearance(btn_a_month, R.style.B1Mweight500)
            TextViewCompat.setTextAppearance(btn_six_month, R.style.B1SBweight600)
            TextViewCompat.setTextAppearance(btn_each_month, R.style.B1Mweight500)

            btn_select_date_from_list.visibility = GONE
            layout_date_own.visibility = GONE

        }

        btn_select_date_from_list.setOnClickListener {
            listView_choose_date_own.visibility = VISIBLE
            layout_select_main.visibility = GONE
            btn_inquire_date.visibility = GONE
        }

        btn_each_month.setOnClickListener {
            btn_a_month.isSelected = false
            btn_six_month.isSelected = false
            btn_each_month.isSelected = true

            TextViewCompat.setTextAppearance(btn_a_month, R.style.B1Mweight500)
            TextViewCompat.setTextAppearance(btn_six_month, R.style.B1Mweight500)
            TextViewCompat.setTextAppearance(btn_each_month, R.style.B1SBweight600)

            btn_select_date_from_list.visibility = VISIBLE
        }

        btn_inquire_date.setOnClickListener {
            if(layout_select_main.visibility == GONE){

                listView_choose_date_own.visibility = GONE
                layout_select_main.visibility = VISIBLE

                tv_selected_date.text = selectedDate
            }else{
                if(btn_a_month.isSelected){
                    setInquireScope(getLastMonthRangeString())
                    getHistories(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
                }else if(btn_six_month.isSelected){
                    setInquireScope(getLastSixMonthsRangeString())
                    getHistories(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)


                }else if(btn_each_month.isSelected){


                    setInquireScope(getDateRangeString(selectedDate))
                    getHistories(getDateRange(selectedDate).second,getDateRange(selectedDate).first)

                    lv_history.visibility = VISIBLE
                    layout_no_data.visibility = GONE
                }
                layout_choose_date.visibility = GONE

            }
        }

        getHistories(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
        persistentBottomSheetEvent()
        setResources()
    }

    fun setResources(){
        btn_a_month.isSelected = true

        val itemList = getDateList()

        selectedDate = itemList.get(0).date
        tv_selected_date.text = selectedDate

        setInquireScope(getLastMonthRangeString())


        // adapter 생성
        val dateAdapter = DetailManageScoreActivity.DateAdapter(
            this,
            itemList,
            object : DetailManageScoreActivity.DateAdapter.DateCallback {
                override fun chosenDate(date: String) {
                    selectedDate = date

                    for (list in itemList) {
                        list.selected = false
                        if (list.date == date) {
                            list.selected = true
                        }
                    }
                    (listView_choose_date_own.adapter as DetailManageScoreActivity.DateAdapter).notifyDataSetChanged()

                    listView_choose_date_own.visibility = GONE
                    layout_select_main.visibility = VISIBLE
                    btn_inquire_date.visibility = VISIBLE

                    tv_selected_date.text = selectedDate

                }
            })

        // listView에 adapter 연결
        listView_choose_date_own.adapter = dateAdapter

    }

    fun getHistoriesMore(startTime:String, endTime:String, meta:Meta, histories: MutableList<DriveItem>){
        apiService().getDrivingHistories(
            "Bearer " + PreferenceUtil.getPref(this@MyDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            30,
            "DESC",
            meta.afterCursor,
            null,
            "startTime",
            startTime,
            endTime).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val getDriveHistroyResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDriveHistoryResponse::class.java
                    )

                    histories.addAll(getDriveHistroyResponse.items)

                    histories.add(DriveItem("","","","","",false,"","",0.0,0.0))
                    meta.afterCursor = getDriveHistroyResponse.meta.afterCursor
                    (lv_history.adapter as DriveHistoryAdapter).notifyDataSetChanged()
                }else{
                    lv_history.visibility = GONE
                    layout_no_data.visibility = VISIBLE
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }
    fun getHistories(startTime:String, endTime:String){
        apiService().getDrivingHistories(
            "Bearer " + PreferenceUtil.getPref(this@MyDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            30,
            "DESC",
            null,
            null,
            "startTime",
            startTime,
            endTime).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val getDriveHistroyResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDriveHistoryResponse::class.java
                    )


                    if(getDriveHistroyResponse.items.size > 0){
                        val driveAdapter = DriveHistoryAdapter(
                            this@MyDriveHistoryActivity,
                            getDriveHistroyResponse.items, getDriveHistroyResponse.meta, object :DriveHistoryAdapter.DriveCallback{
                                override fun clickedMore(meta: Meta, histories: MutableList<DriveItem>) {
                                    histories.removeLast()
                                    getHistoriesMore(startTime, endTime, meta, histories)

                                }
                            })

                        getDriveHistroyResponse.items.add(DriveItem("","","","","",false,"","",0.0,0.0))

                        lv_history.adapter = driveAdapter

                        lv_history.visibility = VISIBLE
                        layout_no_data.visibility = GONE
                    }else{
                        lv_history.visibility = GONE
                        layout_no_data.visibility = VISIBLE
                    }

                } else{
                    lv_history.visibility = GONE
                    layout_no_data.visibility = VISIBLE
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    class DriveHistoryAdapter(context: Context, val histories: MutableList<DriveItem>, var meta: Meta, val callback:DriveCallback) : ArrayAdapter<DriveItem>(context, 0, histories) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            if(position != histories.size -1){
                var listItemView = LayoutInflater.from(context).inflate(R.layout.item_drive_history, parent, false)


                val driveItem = getItem(position)

                val layout_not_active = listItemView!!.findViewById<LinearLayout>(R.id.layout_not_active)
                val layout_active = listItemView!!.findViewById<LinearLayout>(R.id.layout_active)

                val btn_drive_history = listItemView!!.findViewById<ConstraintLayout>(R.id.btn_drive_history)

                val tvDate = listItemView!!.findViewById<TextView>(R.id.tv_date)
                val tv_distance = listItemView!!.findViewById<TextView>(R.id.tv_distance)
                val tv_start_time = listItemView!!.findViewById<TextView>(R.id.tv_start_time)
                val tv_end_time = listItemView!!.findViewById<TextView>(R.id.tv_end_time)

                val tvDate2 = listItemView!!.findViewById<TextView>(R.id.tv_date2)
                val tv_distance2 = listItemView!!.findViewById<TextView>(R.id.tv_distance2)
                val tv_start_time2 = listItemView!!.findViewById<TextView>(R.id.tv_start_time2)
                val tv_end_time2 = listItemView!!.findViewById<TextView>(R.id.tv_end_time2)



                tvDate.text = transformTimeToDate(driveItem?.startTime!!)
                tv_distance.text = transferDistanceWithUnit(driveItem?.totalDistance!!, PreferenceUtil.getPref(context,  PreferenceUtil.KM_MILE, "km")!!)
                tv_start_time.text = transformTimeToHHMM(driveItem?.startTime!!)
                tv_end_time.text = transformTimeToHHMM(driveItem?.endTime!!)

                tvDate2.text = transformTimeToDate(driveItem?.startTime!!)
                tv_distance2.text = transferDistanceWithUnit(driveItem?.totalDistance!!, PreferenceUtil.getPref(context,  PreferenceUtil.KM_MILE, "km")!!)
                tv_start_time2.text = transformTimeToHHMM(driveItem?.startTime!!)
                tv_end_time2.text = transformTimeToHHMM(driveItem?.endTime!!)

                if(driveItem!!.isActive){
                    layout_not_active.visibility = GONE
                    layout_active.visibility = VISIBLE

                    btn_drive_history.isClickable = false
                }else{
                    layout_not_active.visibility = VISIBLE
                    layout_active.visibility = GONE
                }

                btn_drive_history.setOnClickListener {
                    Log.d("testtestet","testestsed :: " + driveItem?.isActive)
                    (context as MyDriveHistoryActivity).resultLauncher.launch(Intent(context, DetailDriveHistoryActivity::class.java).putExtra("trackingId", driveItem?.id).putExtra("isActive", driveItem?.isActive))
//                    var intent = Intent(context, DetailDriveHistoryActivity::class.java)
//                    intent.putExtra("tracking_id", driveItem?.id)
//                    context.startActivity(intent)
                }

                return listItemView
            } else{
                var listItemView = LayoutInflater.from(context).inflate(R.layout.item_drive_history_last, parent, false)

                val tv_more = listItemView!!.findViewById<TextView>(R.id.tv_more)
                val tv_last = listItemView!!.findViewById<TextView>(R.id.tv_last)

                tv_more.setOnClickListener {
                    if(!meta.afterCursor.isNullOrBlank()){
                        callback.clickedMore(meta, histories)
                    }
                }

                if(meta.afterCursor.isNullOrBlank()){
                    tv_more.visibility = GONE
                    tv_last.visibility = VISIBLE
                }else{
                    tv_more.visibility = VISIBLE
                    tv_last.visibility = GONE
                }

                return listItemView
            }
        }

        interface DriveCallback {
            fun clickedMore(meta:Meta, histories: MutableList<DriveItem>)

        }

        fun transferDistanceWithUnit(meters:Double, distance_unit:String):String{
            if(distance_unit == "km"){
                return String.format(Locale.KOREAN, "%.3fkm", meters / 1000)
            }else{
                val milesPerMeter = 0.000621371
                return String.format(Locale.KOREAN, "%.3fmile",meters * milesPerMeter)
            }
        }

        private fun transformTimeToHHMM(isoDate: String):String{
            // UTC 시간 파싱
            val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)

            // ZonedDateTime으로 변환
            val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))

            // 한국 시간대로 변환 (UTC+9)
            val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

            // HH:mm 형식으로 변환
            val kstTimeStr = kstTime.format(DateTimeFormatter.ofPattern("HH:mm"))

            // 포맷된 문자열 반환
            return kstTimeStr
        }

        private fun transformTimeToDate(isoDate: String):String{
            // UTC 시간 파싱
            val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)

            // ZonedDateTime으로 변환
            val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))

            // 한국 시간대로 변환 (UTC+9)
            val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

            // HH:mm 형식으로 변환
            val kstTimeStr = kstTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))

            // 포맷된 문자열 반환
            return kstTimeStr
        }

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

    fun getDateList():MutableList<ChosenDate>{
        val currentDate = LocalDate.now()

        // 날짜 형식을 지정합니다. 예: "2024년 6월"
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월")

        // 결과를 저장할 리스트를 생성합니다.
        val dateList = mutableListOf<String>()

        // 36개월 동안의 날짜를 역순으로 추가합니다.
        for (i in 0 until 36) {
            val date = currentDate.minusMonths(i.toLong())
            val formattedDate = date.format(formatter)
            dateList.add(formattedDate)
        }

        val choseDateList = mutableListOf<ChosenDate>()

        for (i in 0 until 36) {
            if( i == 0){
                choseDateList.add(ChosenDate(dateList.get(i),true))
            }else{
                choseDateList.add(ChosenDate(dateList.get(i),false))
            }
        }


        return choseDateList
    }


    fun getLastMonthRangeString(): String {
        val currentDate = LocalDate.now()
        val startDate = currentDate.minusMonths(1).plusDays(1) // 한 달 전의 첫째 날
        val endDate = currentDate

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
        val startDateString = if (startDate.month == endDate.month) {
            startDate.format(dateFormatter).replaceFirst(" ", "")
        } else {
            startDate.format(dateFormatter)
        }
        val endDateString = endDate.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))

        return "$startDateString ~ $endDateString"
    }


    fun getLastSixMonthsRangeString(): String {
        val currentDate = LocalDate.now()
        val startDate = currentDate.minusMonths(6).plusDays(1) // 한 달 전의 첫째 날
        val endDate = currentDate

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
        val startDateString = if (startDate.month == endDate.month) {
            startDate.format(dateFormatter).replaceFirst(" ", "")
        } else {
            startDate.format(dateFormatter)
        }
        val endDateString = endDate.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))

        return "$startDateString ~ $endDateString"
    }

    fun getDateRangeString(yearMonth: String): String {

        // 주어진 문자열을 LocalDate로 파싱 (해당 월의 첫날)
        val startDate = LocalDate.parse("$yearMonth 1일", DateTimeFormatter.ofPattern("yyyy년 M월 d일"))

        // 해당 월의 마지막 날 계산
        val endDate = startDate.with(TemporalAdjusters.lastDayOfMonth())

        // 결과 문자열 생성
        return "${startDate.year}년 ${startDate.monthValue}월 1일 ~ ${endDate.dayOfMonth}일"
    }

    private fun setInquireScope(scope:String){
        tv_inquire_scope.text = scope
    }
}