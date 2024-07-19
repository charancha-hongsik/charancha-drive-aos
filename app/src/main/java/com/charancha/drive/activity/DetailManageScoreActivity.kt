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
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

    lateinit var listView_choose_date_own:ListView
    lateinit var layout_select_main:LinearLayout
    lateinit var btn_inquire_date:TextView
    lateinit var btn_select_date_from_list:ConstraintLayout
    lateinit var tv_selected_date:TextView
    lateinit var layout_date_own:ConstraintLayout
    lateinit var tv_inquire_scope:TextView
    lateinit var layout_there_is_data:LinearLayout
    lateinit var layout_no_data:ConstraintLayout


    lateinit var behavior: BottomSheetBehavior<LinearLayout>
    lateinit var selectedDate:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_managescore)

        init()
        setResources()
        setListener()
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

        persistentBottomSheetEvent()

    }

    fun setResources(){
        tv_detail_managescroe_title.text = intent.getStringExtra("title")
        btn_a_month.isSelected = true

        val itemList = getDateList()

        selectedDate = itemList.get(0).date
        tv_selected_date.text = selectedDate

        setInquireScope(getLastMonthRangeString())


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
                    layout_there_is_data.visibility = INVISIBLE
                    layout_no_data.visibility = VISIBLE
                }else if(btn_six_month.isSelected){
                    setInquireScope(getLastSixMonthsRangeString())
                    layout_there_is_data.visibility = VISIBLE
                    layout_no_data.visibility = GONE
                }else if(btn_each_month.isSelected){
                    setInquireScope(getDateRangeString(selectedDate))
                    layout_there_is_data.visibility = VISIBLE
                    layout_no_data.visibility = GONE
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