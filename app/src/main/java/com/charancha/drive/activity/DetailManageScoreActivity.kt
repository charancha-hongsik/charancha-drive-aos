package com.charancha.drive.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.charancha.drive.ChosenDate
import com.charancha.drive.R
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DetailManageScoreActivity:BaseActivity(){
    lateinit var tv_detail_managescroe_title: TextView
    lateinit var btn_back: ImageView
    lateinit var btn_choose_date: ImageView
    lateinit var layout_choose_date: CoordinatorLayout
    lateinit var persistent_bottom_sheet: LinearLayout
    lateinit var btn_close_select_date:ImageView
    lateinit var btn_a_month:TextView
    lateinit var btn_six_month:TextView
    lateinit var btn_each_month:TextView
    lateinit var btn_date_own:TextView

    lateinit var listView_choose_date_own:ListView
    lateinit var layout_select_main:LinearLayout
    lateinit var btn_inquire_date:TextView
    lateinit var btn_select_date_from_list:ConstraintLayout
    lateinit var tv_selected_date:TextView

    lateinit var behavior: BottomSheetBehavior<LinearLayout>

    var selectedDate:String? = null



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
        btn_date_own = findViewById(R.id.btn_date_own)

        persistentBottomSheetEvent()

    }

    fun setResources(){
        tv_detail_managescroe_title.text = intent.getStringExtra("title")
        btn_a_month.isSelected = true

        val itemList: MutableList<ChosenDate> = ArrayList()

        // 데이터 추가
        itemList.add(ChosenDate("2024년 7월", true))
        itemList.add(ChosenDate("2024년 8월", false))
        itemList.add(ChosenDate("2024년 9월", false))
        itemList.add(ChosenDate("2024년 10월", false))
        itemList.add(ChosenDate("2024년 11월", false))
        itemList.add(ChosenDate("2024년 12월", false))



        // adapter 생성
        val adapter = DateAdapter(this, itemList,object : DateAdapter.DateCallback{
            override fun chosenDate(date: String) {
                selectedDate = date

                for(list in itemList){
                    list.selected = false
                    if(list.date == date){
                        list.selected = true
                    }
                }
                (listView_choose_date_own.adapter as DateAdapter).notifyDataSetChanged()
            }

        })


        // listView에 adapter 연결
        listView_choose_date_own.adapter = adapter

    }

    fun setListener(){
        btn_back.setOnClickListener { finish() }

        btn_choose_date.setOnClickListener {
            layout_choose_date.visibility = VISIBLE

            listView_choose_date_own.visibility = GONE
            layout_select_main.visibility = VISIBLE
        }

        layout_choose_date.setOnClickListener {
            layout_choose_date.visibility = GONE
        }

        btn_close_select_date.setOnClickListener {
            layout_choose_date.visibility = GONE
        }

        btn_inquire_date.setOnClickListener {
            if(layout_select_main.visibility == GONE){
                listView_choose_date_own.visibility = GONE
                layout_select_main.visibility = VISIBLE

                selectedDate?.let {
                    tv_selected_date.text = it
                }
            }
        }

        btn_select_date_from_list.setOnClickListener {
            listView_choose_date_own.visibility = VISIBLE
            layout_select_main.visibility = GONE
        }

        btn_a_month.setOnClickListener {
            btn_a_month.isSelected = true
            btn_six_month.isSelected = false
            btn_each_month.isSelected = false
            btn_date_own.isSelected = false

            btn_select_date_from_list.visibility = GONE
        }

        btn_six_month.setOnClickListener {
            btn_a_month.isSelected = false
            btn_six_month.isSelected = true
            btn_each_month.isSelected = false
            btn_date_own.isSelected = false

            btn_select_date_from_list.visibility = GONE
        }

        btn_each_month.setOnClickListener {
            btn_a_month.isSelected = false
            btn_six_month.isSelected = false
            btn_each_month.isSelected = true
            btn_date_own.isSelected = false

            btn_select_date_from_list.visibility = VISIBLE
        }

        btn_date_own.setOnClickListener {
            btn_a_month.isSelected = false
            btn_six_month.isSelected = false
            btn_each_month.isSelected = false
            btn_date_own.isSelected = true

            btn_select_date_from_list.visibility = GONE

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
                        layout_choose_date.visibility = GONE
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
}