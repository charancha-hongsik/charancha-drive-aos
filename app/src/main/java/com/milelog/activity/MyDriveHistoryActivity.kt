package com.milelog.activity

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.milelog.ChosenDate
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.viewmodel.MyDriveHistoryViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.CarListFilter
import com.milelog.DividerItemDecoration
import com.milelog.activity.MyDriveHistoryActivity.DriveHistoryAdapter.LastItemViewHolder
import com.milelog.retrofit.response.DriveItem
import com.milelog.retrofit.response.Meta
import com.milelog.retrofit.response.NewDriveHistoryResponse
import com.milelog.room.entity.MyCarsEntity
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.state.GetDriveHistoryMoreState
import com.milelog.viewmodel.state.GetDriveHistoryState
import com.nex3z.flowlayout.FlowLayout
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*


class MyDriveHistoryActivity: BaseRefreshActivity() {
    lateinit var lv_date: RecyclerView
    lateinit var btn_back: ImageView

    lateinit var button_choose_date_overlay: Button
    lateinit var layout_choose_date: CoordinatorLayout
    lateinit var persistent_bottom_sheet: LinearLayout
    lateinit var btn_close_select_date: ImageView
    lateinit var btn_a_month: TextView
    lateinit var btn_six_month: TextView
    lateinit var btn_each_month: TextView
    lateinit var btn_inquire_date: TextView
    lateinit var tv_selected_date: TextView
    lateinit var tv_inquire_scope: TextView
    lateinit var listView_choose_date_own: ListView
    lateinit var layout_select_main: LinearLayout
    lateinit var btn_select_date_from_list: ConstraintLayout
    lateinit var layout_date_own: ConstraintLayout
    lateinit var layout_no_data: ConstraintLayout
    lateinit var layout_flow: FlowLayout

    lateinit var behavior: BottomSheetBehavior<LinearLayout>
    lateinit var selectedDate: String

    lateinit var resultLauncher: ActivityResultLauncher<Intent>
    val filterList: MutableList<CarListFilter> = mutableListOf()
    var histories: MutableList<DriveItem> = mutableListOf()
    var newHistories: MutableList<NewDriveHistoryResponse> = mutableListOf()


    /**
     * 전체 -> carId null / isActive null
     * 미확정 -> carId null / isActive true
     * 내 차가 아니에요 -> carId null / isActive false
     * 내 차 -> carId not null / isActive true
     */
    var carIdForFilter: String? = null
    var isActiveForFilter: Boolean? = null
    var startTimeForFilter: String = getCurrentAndPastTimeForISO(29).second
    var endTimeForFilter: String = getCurrentAndPastTimeForISO(29).first

    private val historyViewModel: MyDriveHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive_history)

        historyViewModel.init(applicationContext)
        init()
        setObserver()

        lv_date.layoutManager = LinearLayoutManager(this@MyDriveHistoryActivity)
        val dividerItemDecoration = DividerItemDecoration(
            this@MyDriveHistoryActivity,
            R.color.gray_50,
            32
        ) // 색상 리소스와 구분선 높이 설정
        lv_date.addItemDecoration(dividerItemDecoration)

    }

    private fun setObserver() {

        historyViewModel.driveHistoryMoreResult.observe(
            this@MyDriveHistoryActivity,
            BaseViewModel.EventObserver { state ->
                when (state) {
                    is GetDriveHistoryMoreState.Loading -> {

                    }

                    is GetDriveHistoryMoreState.Success -> {
                        updateHistories(state.data.items, newHistories)

                        newHistories.add(NewDriveHistoryResponse("", mutableListOf()))
                        (lv_date.adapter as DateHistoriesAdapter).notifyDataSetChanged()
                    }

                    is GetDriveHistoryMoreState.Error -> {
                        if (state.code == 401) {
                            logout()
                        } else {
                            lv_date.visibility = GONE
                            layout_no_data.visibility = VISIBLE
                        }
                    }

                    is GetDriveHistoryMoreState.Empty -> {

                    }
                }

            })

        historyViewModel.driveHistoryResult.observe(
            this@MyDriveHistoryActivity,
            BaseViewModel.EventObserver { state ->
                when (state) {
                    is GetDriveHistoryState.Loading -> {

                    }

                    is GetDriveHistoryState.Success -> {
                        val getDriveHistroyResponse = state.data

                        if (getDriveHistroyResponse.items.size > 0) {
                            histories.addAll(getDriveHistroyResponse.items)
                            newHistories = groupDriveItemsByLocalDate(getDriveHistroyResponse.items)

                            newHistories.add(NewDriveHistoryResponse("", mutableListOf()))
                            val dateAdapter = DateHistoriesAdapter(
                                this@MyDriveHistoryActivity,
                                state.data.meta,
                                newHistories,
                                object :
                                    DriveHistoryAdapter.DriveCallback {
                                    override fun clickedMore(
                                        meta: Meta,
                                        histories: MutableList<NewDriveHistoryResponse>
                                    ) {
                                        newHistories.removeLast()
                                        historyViewModel.getHistoriesMore(
                                            state.startTime,
                                            state.endTime,
                                            meta,
                                            userCarId = carIdForFilter,
                                            isActive = isActiveForFilter
                                        )
                                    }
                                })

                            lv_date.adapter = dateAdapter

                            lv_date.visibility = VISIBLE
                            layout_no_data.visibility = GONE
                        } else {
                            lv_date.visibility = GONE
                            layout_no_data.visibility = VISIBLE
                        }
                    }

                    is GetDriveHistoryState.Error -> {
                        if (state.code == 401) {
                            logout()
                        } else {
                            lv_date.visibility = GONE
                            layout_no_data.visibility = VISIBLE
                        }
                    }

                    is GetDriveHistoryState.Empty -> {

                    }
                }
            })
    }

    fun init() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val trackingId = it.data?.getStringExtra("trackingId")
                    val isActive = it.data?.getBooleanExtra("isActive", true)
                    val userCarId = it.data?.getStringExtra("userCarId")

                    for (history in histories) {
                        if (history.id.equals(trackingId)) {
                            history.isActive = isActive!!
                            history.userCarId = userCarId
                        }
                    }


                    (lv_date.adapter as DateHistoriesAdapter).notifyDataSetChanged()
                }
            }

        lv_date = findViewById(R.id.lv_history)
        btn_back = findViewById(R.id.btn_back)

        button_choose_date_overlay = findViewById(R.id.button_choose_date_overlay)
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
        layout_flow = findViewById(R.id.layout_flow)

        PreferenceUtil.getPref(this, PreferenceUtil.MY_CAR_ENTITIES, "")?.let {
            if (it != "") {
                val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                val myCarsListOnDevice: MutableList<MyCarsEntity> = mutableListOf()
                myCarsListOnDevice.addAll(
                    GsonBuilder().serializeNulls().create().fromJson(it, type)
                )

                filterList.add(CarListFilter(null, "전체", null))
                filterList.add(CarListFilter(null, "미확정", true))
                filterList.add(CarListFilter(null, "내 차가 아니에요", false))

                for (car in myCarsListOnDevice) {
                    filterList.add(CarListFilter(car.id, car.name, car.isActive))
                    Log.d("testestesest", "testsetsetsetsese :: " + car.id)
                }


                val carNameTextViews = mutableListOf<TextView>()

                for (filter in filterList) {
                    // Inflate the ConstraintLayout view
                    val constraintLayoutView =
                        layoutInflater.inflate(R.layout.item_drive_history_car, layout_flow, false)

                    // Find the TextView within the newly inflated ConstraintLayout
                    val tv_car_name = constraintLayoutView.findViewById<TextView>(R.id.tv_car_name)
                    tv_car_name.text = filter.name


                    if (filter.name.equals("전체")) {
                        (tv_car_name.parent as ConstraintLayout).isSelected = true
                        TextViewCompat.setTextAppearance(tv_car_name, R.style.car_filter_selected)
                    }

                    // Add the TextView reference to the list
                    carNameTextViews.add(tv_car_name)

                    // Set click listener for the first TextView (or any condition)
                    tv_car_name.setOnClickListener {
                        // Iterate over the list and update the background of all TextViews
                        for (textView in carNameTextViews) {
                            if (textView == tv_car_name) {
                                // Change background of the clicked TextView
                                (textView.parent as ConstraintLayout).isSelected = true
                                TextViewCompat.setTextAppearance(
                                    textView,
                                    R.style.car_filter_selected
                                )
                                val matchingFilter = filterList.find { it.name == tv_car_name.text }
                                carIdForFilter = matchingFilter?.id

                            } else {
                                // Reset the background of other TextViews
                                (textView.parent as ConstraintLayout).isSelected = false
                                TextViewCompat.setTextAppearance(
                                    textView,
                                    R.style.car_filter_unselected
                                )
                            }
                        }

                        if (tv_car_name.text.equals("전체")) {
                            isActiveForFilter = null
                        } else if (tv_car_name.text.equals("미확정")) {
                            isActiveForFilter = true
                            carIdForFilter = "null"
                        } else if (tv_car_name.text.equals("내 차가 아니에요")) {
                            isActiveForFilter = false
                            carIdForFilter = "null"
                        } else {
                            isActiveForFilter = true
                        }

                        historyViewModel.getHistories(
                            startTimeForFilter,
                            endTimeForFilter,
                            userCarId = carIdForFilter,
                            isActive = isActiveForFilter
                        )
                    }


                    // Add the inflated view to the parent layout
                    layout_flow.addView(constraintLayoutView)
                }
            }
        }




        btn_back.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                finish()
            }

        })

        button_choose_date_overlay.setOnClickListener {
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

        btn_inquire_date.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (layout_select_main.visibility == GONE) {

                    listView_choose_date_own.visibility = GONE
                    layout_select_main.visibility = VISIBLE

                    tv_selected_date.text = selectedDate
                } else {
                    if (btn_a_month.isSelected) {
                        startTimeForFilter = getCurrentAndPastTimeForISO(29).second
                        endTimeForFilter = getCurrentAndPastTimeForISO(29).first
                        setInquireScope(
                            formatDateRangeForAMonth(
                                startTimeForFilter,
                                endTimeForFilter
                            )
                        )
                        historyViewModel.getHistories(
                            startTimeForFilter,
                            endTimeForFilter,
                            userCarId = carIdForFilter,
                            isActive = isActiveForFilter
                        )
                    } else if (btn_six_month.isSelected) {
                        startTimeForFilter = getCurrentAndPastTimeForISO(SIX_MONTH).second
                        endTimeForFilter = getCurrentAndPastTimeForISO(SIX_MONTH).first
                        setInquireScope(formatDateRange(startTimeForFilter, endTimeForFilter))
                        historyViewModel.getHistories(
                            startTimeForFilter,
                            endTimeForFilter,
                            userCarId = carIdForFilter,
                            isActive = isActiveForFilter
                        )

                    } else if (btn_each_month.isSelected) {
                        startTimeForFilter = getDateRange(selectedDate).second
                        endTimeForFilter = getDateRange(selectedDate).first
                        setInquireScope(getDateRangeString(selectedDate))
                        historyViewModel.getHistories(
                            startTimeForFilter,
                            endTimeForFilter,
                            userCarId = carIdForFilter,
                            isActive = isActiveForFilter
                        )

                        lv_date.visibility = VISIBLE
                        layout_no_data.visibility = GONE
                    }
                    layout_choose_date.visibility = GONE

                }
            }
        })

        historyViewModel.getHistories(
            startTimeForFilter,
            endTimeForFilter,
            userCarId = carIdForFilter,
            isActive = isActiveForFilter
        )

        persistentBottomSheetEvent()
        setResources()
    }

    fun setResources() {
        btn_a_month.isSelected = true

        val itemList = getDateList()

        selectedDate = itemList.get(0).date
        tv_selected_date.text = selectedDate

        setInquireScope(
            formatDateRangeForAMonth(
                getCurrentAndPastTimeForISO(29).second,
                getCurrentAndPastTimeForISO(29).first
            )
        )


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

    class DriveHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutNotActive: LinearLayout = view.findViewById(R.id.layout_not_active)
        val layoutActive: LinearLayout = view.findViewById(R.id.layout_active)
        val btnDriveHistory: ConstraintLayout = view.findViewById(R.id.btn_drive_history)
        val tvDistance: TextView = view.findViewById(R.id.tv_distance)
        val tvStartTime: TextView = view.findViewById(R.id.tv_start_time)
        val tvEndTime: TextView = view.findViewById(R.id.tv_end_time)
        val tvDistance2: TextView = view.findViewById(R.id.tv_distance2)
        val tvStartTime2: TextView = view.findViewById(R.id.tv_start_time2)
        val tvEndTime2: TextView = view.findViewById(R.id.tv_end_time2)
    }

    class DriveHistoryAdapter(
        private val context: Context,
        private val histories: MutableList<DriveItem>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_drive_history, parent, false)
            return DriveHistoryViewHolder(view)

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is DriveHistoryViewHolder) {
                val driveItem = histories[position]

                holder.tvDistance.text = transferDistanceWithUnit(
                    driveItem.totalDistance,
                    PreferenceUtil.getPref(context, PreferenceUtil.KM_MILE, "km")!!
                )
                holder.tvStartTime.text = transformTimeToHHMM(driveItem.startTime)
                holder.tvEndTime.text = transformTimeToHHMM(driveItem.endTime)

                holder.tvDistance2.text = transferDistanceWithUnit(
                    driveItem.totalDistance,
                    PreferenceUtil.getPref(context, PreferenceUtil.KM_MILE, "km")!!
                )
                holder.tvStartTime2.text = transformTimeToHHMM(driveItem.startTime)
                holder.tvEndTime2.text = transformTimeToHHMM(driveItem.endTime)

                if (driveItem.isActive && !driveItem.userCarId.isNullOrEmpty()) {
                    holder.layoutNotActive.visibility = View.GONE
                    holder.layoutActive.visibility = View.VISIBLE
                    holder.btnDriveHistory.isClickable = false
                } else {
                    holder.layoutNotActive.visibility = View.VISIBLE
                    holder.layoutActive.visibility = View.GONE
                }

                holder.btnDriveHistory.setOnClickListener {
                    (context as MyDriveHistoryActivity).resultLauncher.launch(
                        Intent(context, DetailDriveHistoryActivity::class.java)
                            .putExtra("trackingId", driveItem.id)
                            .putExtra("isActive", driveItem.isActive)
                    )
                }
            }
        }

        override fun getItemCount(): Int {
            return histories.size
        }

        class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMore: TextView = view.findViewById(R.id.tv_more)
            val tvLast: TextView = view.findViewById(R.id.tv_last)
        }

        interface DriveCallback {
            fun clickedMore(meta: Meta, histories: MutableList<NewDriveHistoryResponse>)
        }

        private fun transferDistanceWithUnit(meters: Double, distance_unit: String): String {
            return if (distance_unit == "km") {
                String.format(Locale.KOREAN, "%.0fkm", meters / 1000)
            } else {
                val milesPerMeter = 0.000621371
                String.format(Locale.KOREAN, "%.0fmile", meters * milesPerMeter)
            }
        }

        private fun transformTimeToHHMM(isoDate: String): String {
            val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)
            val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))
            val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            return kstTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        private fun transformTimeToDate(isoDate: String): String {
            val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)
            val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))
            val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            return kstTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
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
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.d("testset", "onStateChanged: 접음")
//                        layout_choose_date.visibility = GONE
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Log.d("testset", "onStateChanged: 드래그")
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("testset", "onStateChanged: 펼침")
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Log.d("testset", "onStateChanged: 숨기기")

                    }

                    BottomSheetBehavior.STATE_SETTLING -> {
                        Log.d("testset", "onStateChanged: 고정됨")
                    }
                }
            }
        })
    }

    fun getDateList(): MutableList<ChosenDate> {
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
            if (i == 0) {
                choseDateList.add(ChosenDate(dateList.get(i), true))
            } else {
                choseDateList.add(ChosenDate(dateList.get(i), false))
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

    private fun setInquireScope(scope: String) {
        tv_inquire_scope.text = scope
    }

    class DateHistoriesAdapter(
        private val context: Context,
        private val meta: Meta,
        private val dateList: List<NewDriveHistoryResponse>,
        private val callback: DriveHistoryAdapter.DriveCallback
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dateTextView: TextView = view.findViewById(R.id.tv_date)
            val driveItemsRecyclerView: RecyclerView =
                view.findViewById(R.id.driveItemsRecyclerView)
        }

        companion object {
            private const val VIEW_TYPE_ITEM = 0
            private const val VIEW_TYPE_LAST_ITEM = 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == dateList.size - 1) VIEW_TYPE_LAST_ITEM else VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_ITEM) {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
                DateViewHolder(view)
            } else {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_drive_history_last, parent, false)
                LastItemViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is DateViewHolder) {
                val dateItem = dateList[position]
                holder.dateTextView.text = dateItem.date

                // 하위 RecyclerView 어댑터 설정
                val driveItemAdapter = DriveHistoryAdapter(
                    context,
                    dateItem.items)

                if (holder.driveItemsRecyclerView.itemDecorationCount == 0) {
                    val dividerItemDecoration = DividerItemDecoration(context, R.color.gray_50, 12)
                    holder.driveItemsRecyclerView.addItemDecoration(dividerItemDecoration)
                }
                holder.driveItemsRecyclerView.adapter = driveItemAdapter
                holder.driveItemsRecyclerView.layoutManager =
                    LinearLayoutManager(holder.itemView.context)
            } else if (holder is LastItemViewHolder) {
                if (meta.afterCursor.isNullOrBlank()) {
                    holder.tvMore.visibility = View.GONE
                    holder.tvLast.visibility = View.VISIBLE
                } else {
                    holder.tvMore.visibility = View.VISIBLE
                    holder.tvLast.visibility = View.GONE
                }

                holder.tvMore.setOnClickListener {
                    callback.clickedMore(meta, dateList.toMutableList())
                }
            }
        }

        override fun getItemCount(): Int = dateList.size
    }

    fun groupDriveItemsByLocalDate(driveItems: MutableList<DriveItem>): MutableList<NewDriveHistoryResponse> {
        val utcFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        utcFormatter.timeZone = TimeZone.getTimeZone("UTC")

        // 한국어 또는 원하는 로케일에 맞춘 날짜 형식
        val localDateFormatter = SimpleDateFormat("M월 d일 E요일", Locale.getDefault())
        localDateFormatter.timeZone = TimeZone.getDefault()

        val groupedDriveItems = driveItems.groupBy { item ->
            val date = utcFormatter.parse(item.createdAt)
            localDateFormatter.format(date)
        }

        return groupedDriveItems.map { (date, items) ->
            NewDriveHistoryResponse(date = date, items = items.toMutableList())
        }.toMutableList()
    }

    fun updateHistories(histories: MutableList<DriveItem>, newHistories: MutableList<NewDriveHistoryResponse>) {
        histories.forEach { driveItem ->
            // 현지 시간대로 변환하여 날짜 형식을 "6월 13일 화요일" 형식으로 변환
            val localDate = convertToLocalDate(driveItem.createdAt)

            // 같은 날짜가 있는지 확인
            val existingHistoryGroup = newHistories.find { it.date == localDate }

            if (existingHistoryGroup != null) {
                // 기존 날짜 그룹이 있으면 해당 그룹에 아이템 추가
                existingHistoryGroup.items.add(driveItem)
            } else {
                // 기존 날짜 그룹이 없으면 새로 추가
                newHistories.add(NewDriveHistoryResponse(date = localDate, items = mutableListOf(driveItem)))
            }
        }
    }

    fun convertToLocalDate(createdAt: String): String {
        // ISO 형식의 UTC 날짜 문자열을 ZonedDateTime으로 변환
        val zonedDateTime = ZonedDateTime.parse(createdAt)

        // 로컬 시간대로 변환 후 원하는 형식으로 변환
        val formatter = DateTimeFormatter.ofPattern("M월 d일 E요일", Locale.KOREAN)
        return zonedDateTime.format(formatter)
    }
}