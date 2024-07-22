package com.charancha.drive.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.TextViewCompat
import com.charancha.drive.*
import com.charancha.drive.retrofit.response.GetDrivingGraphDataResponse
import com.charancha.drive.retrofit.response.GetManageScoreResponse
import com.charancha.drive.retrofit.response.GetRecentDrivingStatisticsResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

class DetailManageScoreActivity:BaseRefreshActivity(){
    lateinit var tv_detail_managescroe_title: TextView
    lateinit var btn_back: ImageView
    lateinit var btn_choose_date: ImageView
    lateinit var layout_choose_date: CoordinatorLayout
    lateinit var persistent_bottom_sheet: LinearLayout
    lateinit var btn_close_select_date:ImageView
    lateinit var btn_a_month:TextView
    lateinit var btn_six_month:TextView
    lateinit var btn_each_month:TextView
    lateinit var layout_no_score:ConstraintLayout

    lateinit var listView_choose_date_own:ListView
    lateinit var layout_select_main:LinearLayout
    lateinit var btn_inquire_date:TextView
    lateinit var btn_select_date_from_list:ConstraintLayout
    lateinit var tv_selected_date:TextView
    lateinit var layout_date_own:ConstraintLayout
    lateinit var tv_inquire_scope:TextView
    lateinit var layout_there_is_data:LinearLayout
    lateinit var layout_no_data:ConstraintLayout

    lateinit var tv_no_score:TextView
    lateinit var tv_no_score1:TextView
    lateinit var iv_no_score:ImageView
    lateinit var tv_engine_score:TextView
    lateinit var tv_engine_info_average_distance:TextView
    lateinit var tv_engine_info_rapid_acc_de_count:TextView
    lateinit var tv_engine_info_high_speed_driving:TextView
    lateinit var tv_engine_info_best_driving:TextView
    lateinit var tv_engine_info_normal_driving:TextView
    lateinit var tv_increased_score:TextView

    lateinit var view_engine_chart_background:View
    lateinit var view_engine_chart_score:View

    lateinit var behavior: BottomSheetBehavior<LinearLayout>
    lateinit var selectedDate:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_managescore)

        init()
        setResources()
        setListener()
        setInitData()
    }

    fun init(){
        tv_detail_managescroe_title = findViewById(R.id.tv_detail_managescroe_title)
        btn_back = findViewById(R.id.btn_back)
        btn_choose_date = findViewById(R.id.btn_choose_date)
        layout_choose_date = findViewById(R.id.layout_choose_date)
        persistent_bottom_sheet = findViewById(R.id.persistent_bottom_sheet)
        btn_close_select_date = findViewById(R.id.btn_close_select_date)
        btn_a_month = findViewById(R.id.btn_a_month)
        listView_choose_date_own = findViewById(R.id.listView_choose_date_own)
        btn_inquire_date = findViewById(R.id.btn_inquire_date)
        layout_select_main = findViewById(R.id.layout_select_main)
        btn_select_date_from_list = findViewById(R.id.btn_select_date_from_list)
        tv_selected_date = findViewById(R.id.tv_selected_date)
        btn_six_month = findViewById(R.id.btn_six_month)
        btn_each_month = findViewById(R.id.btn_each_month)
        layout_date_own = findViewById(R.id.layout_date_own)
        tv_inquire_scope = findViewById(R.id.tv_inquire_scope)
        layout_there_is_data = findViewById(R.id.layout_there_is_data)
        layout_no_data = findViewById(R.id.layout_no_data)
        layout_no_score = findViewById(R.id.layout_no_score)

        view_engine_chart_background = findViewById(R.id.view_engine_chart_background)
        view_engine_chart_score = findViewById(R.id.view_engine_chart_score)

        tv_no_score = findViewById(R.id.tv_no_score)
        tv_no_score1 = findViewById(R.id.tv_no_score1)
        iv_no_score = findViewById(R.id.iv_no_score)
        tv_engine_score = findViewById(R.id.tv_engine_score)
        tv_engine_info_average_distance = findViewById(R.id.tv_engine_info_average_distance)
        tv_engine_info_rapid_acc_de_count = findViewById(R.id.tv_engine_info_rapid_acc_de_count)
        tv_engine_info_high_speed_driving = findViewById(R.id.tv_engine_info_high_speed_driving)
        tv_engine_info_best_driving = findViewById(R.id.tv_engine_info_best_driving)
        tv_engine_info_normal_driving = findViewById(R.id.tv_engine_info_normal_driving)
        tv_increased_score = findViewById(R.id.tv_increased_score)

        persistentBottomSheetEvent()

    }

    fun setResources(){
        tv_detail_managescroe_title.text = intent.getStringExtra("title")
        btn_a_month.isSelected = true

        val itemList = getDateList()

        selectedDate = itemList.get(0).date
        tv_selected_date.text = selectedDate


        // adapter 생성
        val dateAdapter = DateAdapter(this, itemList,object : DateAdapter.DateCallback{
            override fun chosenDate(date: String) {
                selectedDate = date

                for(list in itemList){
                    list.selected = false
                    if(list.date == date){
                        list.selected = true
                    }
                }
                (listView_choose_date_own.adapter as DateAdapter).notifyDataSetChanged()

                listView_choose_date_own.visibility = GONE
                layout_select_main.visibility = VISIBLE
                btn_inquire_date.visibility = VISIBLE

                tv_selected_date.text = selectedDate

            }

        })

        // listView에 adapter 연결
        listView_choose_date_own.adapter = dateAdapter

    }

    fun setInitData(){
        if(tv_detail_managescroe_title.text.contains("최근 관리 점수")){
            apiService().getRecentManageScoreStatistics(
                "Bearer " + PreferenceUtil.getPref(this@DetailManageScoreActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
                PreferenceUtil.getPref(this@DetailManageScoreActivity, PreferenceUtil.USER_CARID, "")!!
            ).enqueue(object: Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 200){
                        val getManageScoreResponse = Gson().fromJson(response.body()?.string(), GetManageScoreResponse::class.java)
                        if(getManageScoreResponse.total.totalEngineScore != 0.0){
                            setThereIsDatas(getManageScoreResponse)
                        }else{
                            setNoData()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

            })

            apiService().getRecentDrivingStatistics(
                "Bearer " + PreferenceUtil.getPref(this@DetailManageScoreActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
                PreferenceUtil.getPref(this@DetailManageScoreActivity, PreferenceUtil.USER_CARID, "")!!
            ).enqueue(object: Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.code() == 200){
                        val recentDrivingDistance = Gson().fromJson(
                            response.body()?.string(),
                            GetRecentDrivingStatisticsResponse::class.java
                        )

                        if(recentDrivingDistance.isRecent){
                            tv_engine_info_average_distance.text = transferDistanceWithUnit(recentDrivingDistance.perOne.totalDistance)

                            tv_engine_info_rapid_acc_de_count.text = transferNumWithRounds(recentDrivingDistance.total.totalRapidCount).toString() + "회"
                            tv_engine_info_high_speed_driving.text = transferNumWithRounds(recentDrivingDistance.average.highSpeedDrivingDistancePercentage).toString() + "%"
                            tv_engine_info_best_driving.text = transferNumWithRounds(recentDrivingDistance.average.optimalDrivingPercentage).toString() + "%"
                            tv_engine_info_normal_driving.text = transferNumWithRounds(recentDrivingDistance.average.constantSpeedDrivingDistancePercentage).toString() + "%"
                        }else{
                            tv_engine_info_average_distance.text = transferDistanceWithUnit(0.0)
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

            })

        }else{
            setInquireScope(getLastMonthRangeString())

            apiService().getManageScoreStatistics(
                "Bearer " + PreferenceUtil.getPref(this@DetailManageScoreActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
                PreferenceUtil.getPref(this@DetailManageScoreActivity, PreferenceUtil.USER_CARID, "")!!,
                getCurrentAndPastTimeForISO(29).second,
                getCurrentAndPastTimeForISO(29).first
            ).enqueue(object :Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 200){
                        val getManageScoreResponse = Gson().fromJson(response.body()?.string(), GetManageScoreResponse::class.java)
                        if(getManageScoreResponse.total.totalEngineScore != 0.0){
                            setThereIsDatas(getManageScoreResponse)
                        }else{
                            setNoData()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

            })
        }
    }

    fun setData(startTime:String, endTime:String){
        apiService().getManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DetailManageScoreActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DetailManageScoreActivity, PreferenceUtil.USER_CARID, "")!!,
            startTime,
            endTime
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200){
                    val getManageScoreResponse = Gson().fromJson(response.body()?.string(), GetManageScoreResponse::class.java)
                    if(getManageScoreResponse.total.totalEngineScore != 0.0){
                        setThereIsDatas(getManageScoreResponse)
                    }else{
                        setNoData()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DetailManageScoreActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DetailManageScoreActivity, PreferenceUtil.USER_CARID, "")!!,
            startTime,
            endTime,
            "startTime",
            "day"
        ).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val recentDrivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetRecentDrivingStatisticsResponse::class.java
                    )

                    if(recentDrivingDistance.total.totalDistance != 0.0){
                        tv_engine_info_average_distance.text = transferDistanceWithUnit(recentDrivingDistance.perOne.totalDistance)

                        tv_engine_info_rapid_acc_de_count.text = transferNumWithRounds(recentDrivingDistance.total.totalRapidCount).toString() + "회"
                        tv_engine_info_high_speed_driving.text = transferNumWithRounds(recentDrivingDistance.average.highSpeedDrivingDistancePercentage).toString() + "%"
                        tv_engine_info_best_driving.text = transferNumWithRounds(recentDrivingDistance.average.optimalDrivingPercentage).toString() + "%"
                        tv_engine_info_normal_driving.text = transferNumWithRounds(recentDrivingDistance.average.constantSpeedDrivingDistancePercentage).toString() + "%"

                    }else{
                        tv_engine_info_average_distance.text = transferDistanceWithUnit(0.0)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun setThereIsDatas(getManageScoreResponse:GetManageScoreResponse){
        layout_no_data.visibility = GONE
        layout_there_is_data.visibility = VISIBLE
        tv_increased_score.visibility = VISIBLE

        tv_no_score.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

        if(getManageScoreResponse.diffAverage.totalEngineScore < 0.0){
            layout_no_score.background = resources.getDrawable(R.drawable.radius8_sec)
            tv_no_score1.text = "아쉬워요. 지난 주행보다 " + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 하락했어요"
            iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_crying))

            tv_increased_score.text = "-" + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore).toString() + "점 감소"
            tv_increased_score.setTextColor(resources.getColor(R.color.sec_500))

            tv_increased_score.setTextColor(resources.getColor(R.color.gray_900))

            view_engine_chart_score.background = resources.getDrawable(R.drawable.radius999_sec500)
        }else if(getManageScoreResponse.diffAverage.totalEngineScore == 0.0){
            layout_no_score.background = resources.getDrawable(R.drawable.radius8_gray950)
            tv_no_score1.text = "점수 변동이 없어요"
            iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_good))

            tv_increased_score.text = "변동 없음"
            tv_increased_score.setTextColor(resources.getColor(R.color.gray_900))

            view_engine_chart_score.background = resources.getDrawable(R.drawable.radius999_gray950)

        }else if(getManageScoreResponse.diffAverage.totalEngineScore > 0.0){
            layout_no_score.background = resources.getDrawable(R.drawable.radius8_pri500)
            tv_no_score1.text = "굉장해요! 지난 주행보다 " + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 얻었어요"
            iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_love))

            tv_increased_score.text =  "+" +transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore).toString() + "점 증가"
            tv_increased_score.setTextColor(resources.getColor(R.color.pri_500))

            view_engine_chart_score.background = resources.getDrawable(R.drawable.radius999_pri500)

        }

        setEngineScoreChart((getManageScoreResponse.average.totalEngineScore/600).toFloat())
        tv_engine_score.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

    }

    fun setNoData(){
        layout_no_data.visibility = VISIBLE
        layout_there_is_data.visibility = GONE
        tv_increased_score.visibility = GONE
        setEngineScoreChart(0f)

        layout_no_score.background = resources.getDrawable(R.drawable.radius8_pri500)
        tv_no_score1.text = "아직 데이터가 없어요. 함께 달려볼까요?"
        iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))

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

    /**
     * 0.0 ~ 1
     */
    fun setEngineScoreChart(percent:Float){
        view_engine_chart_background.post {
            val backgroundWidth = view_engine_chart_background.width

            // Calculate 70% of the background view's width
            val chartWidth = (backgroundWidth * percent).toInt()

            // Apply the calculated width to view_normal_speed_driving_chart
            val layoutParams = view_engine_chart_score.layoutParams
            layoutParams.width = chartWidth
            view_engine_chart_score.layoutParams = layoutParams
        }
    }


    fun getDateRangeString(yearMonth: String): String {

        // 주어진 문자열을 LocalDate로 파싱 (해당 월의 첫날)
        val startDate = LocalDate.parse("$yearMonth 1일", DateTimeFormatter.ofPattern("yyyy년 M월 d일"))

        // 해당 월의 마지막 날 계산
        val endDate = startDate.with(TemporalAdjusters.lastDayOfMonth())

        // 결과 문자열 생성
        return "${startDate.year}년 ${startDate.monthValue}월 1일 ~ ${endDate.dayOfMonth}일"
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

    fun setListener(){
        btn_back.setOnClickListener { finish() }

        btn_choose_date.setOnClickListener {
            layout_choose_date.visibility = VISIBLE

            listView_choose_date_own.visibility = GONE
            layout_select_main.visibility = VISIBLE
        }

        layout_choose_date.setOnClickListener {
            if(layout_select_main.visibility == VISIBLE)
                layout_choose_date.visibility = GONE
            else {
                btn_inquire_date.performClick()
            }
        }

        btn_close_select_date.setOnClickListener {
            layout_choose_date.visibility = GONE
        }

        btn_inquire_date.setOnClickListener {
            if(layout_select_main.visibility == GONE){

                listView_choose_date_own.visibility = GONE
                layout_select_main.visibility = VISIBLE

                tv_selected_date.text = selectedDate
            }else{
                if(btn_a_month.isSelected){
                    setInquireScope(getLastMonthRangeString())
                    setData(getCurrentAndPastTimeForISO(29).second, getCurrentAndPastTimeForISO(29).first)
                }else if(btn_six_month.isSelected){
                    setInquireScope(getLastSixMonthsRangeString())
                    setData(getCurrentAndPastTimeForISO(150).second, getCurrentAndPastTimeForISO(150).first)

                }else if(btn_each_month.isSelected){
                    setInquireScope(getDateRangeString(selectedDate))
                    setData(getDateRange(selectedDate).second,getDateRange(selectedDate).first)

                }
                layout_choose_date.visibility = GONE

            }
        }

        btn_select_date_from_list.setOnClickListener {
            listView_choose_date_own.visibility = VISIBLE
            layout_select_main.visibility = GONE
            btn_inquire_date.visibility = GONE
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

        btn_each_month.setOnClickListener {
            btn_a_month.isSelected = false
            btn_six_month.isSelected = false
            btn_each_month.isSelected = true

            TextViewCompat.setTextAppearance(btn_a_month, R.style.B1Mweight500)
            TextViewCompat.setTextAppearance(btn_six_month, R.style.B1Mweight500)
            TextViewCompat.setTextAppearance(btn_each_month, R.style.B1SBweight600)

            btn_select_date_from_list.visibility = VISIBLE
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

    private fun setInquireScope(scope:String){
        tv_inquire_scope.text = scope
    }

    class DateAdapter(context: Context, date: List<ChosenDate>,val callback:DateCallback ) : ArrayAdapter<ChosenDate>(context, 0, date) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItemView = convertView
            if (listItemView == null) {
                listItemView = LayoutInflater.from(context).inflate(R.layout.choose_date_item, parent, false)
            }

            val chosenDate = getItem(position)

            val tvName = listItemView!!.findViewById<TextView>(R.id.tv_date)
            tvName.text = chosenDate?.date
            chosenDate?.selected?.let {
                tvName.isSelected = it

                if(it){
                    TextViewCompat.setTextAppearance(tvName, R.style.B1SBweight600)
                }else{
                    TextViewCompat.setTextAppearance(tvName, R.style.B1RWeight400)

                }
            }

            tvName.setOnClickListener {
                callback.chosenDate(tvName.text.toString())
            }



            return listItemView
        }

        interface DateCallback {
            fun chosenDate(date:String)

        }
    }

    private fun getCurrentYear():Int{
        return Calendar.getInstance().get(Calendar.YEAR)
    }
    private fun getCurrentMonth():Int{
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }
    private fun getCurrentDay():Int{
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }
}