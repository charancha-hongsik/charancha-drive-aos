package com.milelog.activity

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.CarListFilter
import com.milelog.CarViews
import com.milelog.DividerItemDecoration
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.room.entity.MyCarsEntity
import com.nex3z.flowlayout.FlowLayout
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class ExcelActivity:BaseRefreshActivity() {
    lateinit var layout_flow: FlowLayout
    lateinit var btn_save_excel:TextView
    lateinit var btn_choose_date:LinearLayout
    lateinit var tv_chosen_date:TextView
    var selectedDate:String = "2024년 10월"

    val filterList: MutableList<CarListFilter> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excel)

        init()
        setClickListener()
    }

    fun init(){
        layout_flow = findViewById(R.id.layout_flow)
        btn_save_excel = findViewById(R.id.btn_save_excel)
        btn_choose_date = findViewById(R.id.btn_choose_date)
        tv_chosen_date = findViewById(R.id.tv_chosen_date)

        selectedDate = getNowMonth()
        tv_chosen_date.text = selectedDate

        PreferenceUtil.getPref(this, PreferenceUtil.MY_CAR_ENTITIES, "")?.let {
            if (it != "") {
                val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                val myCarsListOnDevice: MutableList<MyCarsEntity> = mutableListOf()
                myCarsListOnDevice.addAll(
                    GsonBuilder().serializeNulls().create().fromJson(it, type)
                )

                filterList.add(CarListFilter(null,null, "전체", null))
                filterList.add(CarListFilter(null, null, "미확정", true))
                filterList.add(CarListFilter(null, null, "내 차가 아니에요", false))

                for (car in myCarsListOnDevice) {
                    filterList.add(CarListFilter(car.id, car.number?.takeLast(4), car.name, car.isActive))
                }


                val carViews = mutableListOf<CarViews>()


                for (filter in filterList) {
                    // Inflate the ConstraintLayout view
                    val constraintLayoutView =
                        layoutInflater.inflate(R.layout.item_drive_history_car, layout_flow, false)


                    // Find the TextView within the newly inflated ConstraintLayout
                    val tv_car_name = constraintLayoutView.findViewById<TextView>(R.id.tv_car_name)
                    tv_car_name.text = filter.name
                    val tv_car_number = constraintLayoutView.findViewById<TextView>(R.id.tv_car_number)
                    val divider = constraintLayoutView.findViewById<View>(R.id.divider)
                    val view_parent = constraintLayoutView.findViewById<ConstraintLayout>(R.id.view_parent)

                    // Add the TextView reference to the list
                    carViews.add(CarViews(view_parent, tv_car_name, tv_car_number, divider))

                    if(filter.carNum.isNullOrEmpty()){
                        tv_car_number.visibility = GONE
                        divider.visibility = GONE
                    }else{
                        tv_car_number.visibility = VISIBLE
                        tv_car_number.text = filter.carNum

                        divider.visibility = VISIBLE
                    }

                    if (filter.name.equals("전체")) {
                        ((tv_car_name.parent as LinearLayout).parent as ConstraintLayout).isSelected = true
                        TextViewCompat.setTextAppearance(tv_car_name, R.style.car_filter_selected)
                    }

                    (tv_car_name.parent as LinearLayout).setOnClickListener {
                        // Iterate over the list and update the background of all TextViews
                        for (view in carViews) {
                            if (view.tv_car_name == tv_car_name) {
                                // Change background of the clicked TextView
                                view.view_parent.isSelected = true

                                TextViewCompat.setTextAppearance(
                                    view.tv_car_name,
                                    R.style.car_filter_selected
                                )

                                TextViewCompat.setTextAppearance(
                                    view.tv_car_num,
                                    R.style.car_filter_selected
                                )

                                val matchingFilter = filterList.find { it.name == tv_car_name.text }

                            } else {
                                // Reset the background of other TextViews
                                view.view_parent.isSelected = false
                                TextViewCompat.setTextAppearance(
                                    view.tv_car_name,
                                    R.style.car_filter_unselected
                                )

                                TextViewCompat.setTextAppearance(
                                    view.tv_car_num,
                                    R.style.car_filter_unselected
                                )
                            }
                        }
                    }


                    // Add the inflated view to the parent layout
                    layout_flow.addView(constraintLayoutView)
                }

            }
        }
    }

    fun setClickListener(){
        btn_save_excel.setOnClickListener {
            val drivingData = listOf(
                mapOf(
                    "주행 시작 일시" to "2024.12.31(화) 03:20:09",
                    "주행 종료 일시" to "2024.12.31(화) 04:20:09",
                    "주행 시간" to "1:06:00",
                    "주행 거리 (km)" to "202",
                    "데이터 인증" to "L1",
                    "이동 수단" to "5989 BMW 7시리즈",
                    "주행 목적" to "출/퇴근",
                    "사용자 구분" to "법인",
                    "법인 사용자 이름" to "홍길동",
                    "법인 부서명" to "영업 1팀",
                    "출발지" to "서울특별시 동대문구 답십리동 530-18",
                    "도착지" to "강원특별자치도 속초시 조양동 1542-12",
                    "방문지" to "배꽃나라 주유소",
                    "고속 주행 거리 (km)" to "20",
                    "저속 주행 거리 (km)" to "5",
                    "고속 주행 거리 비율 (%)" to "80",
                    "저속 주행 거리 비율 (%)" to "20",
                    "최고 속력 (km/h)" to "152",
                    "평균 속력 (km/h)" to "72",
                    "고속 주행 최고 속력 (km/h)" to "132",
                    "저속 주행 최고 속력 (km/h)" to "40",
                    "급가속 횟수" to "2",
                    "급감속 횟수" to "1",
                    "급출발 횟수" to "2",
                    "급정지 횟수" to "1",
                    "최적 주행 거리 (km)" to "21",
                    "가혹 주행 거리 (km)" to "2",
                    "최적 주행 비율 (%)" to "90",
                    "가혹 주행 비율 (%)" to "10"
                ),
                // 추가 데이터는 계속해서 추가
                mapOf(
                    "주행 시작 일시" to "2024.12.31(화) 03:20:09",
                    "주행 종료 일시" to "2024.12.31(화) 04:20:09",
                    "주행 시간" to "1:06:00",
                    "주행 거리 (km)" to "202",
                    "데이터 인증" to "L1",
                    "이동 수단" to "5989 BMW 7시리즈",
                    "주행 목적" to "출/퇴근",
                    "사용자 구분" to "법인",
                    "법인 사용자 이름" to "홍길동",
                    "법인 부서명" to "영업 1팀",
                    "출발지" to "서울특별시 동대문구 답십리동 530-18",
                    "도착지" to "강원특별자치도 속초시 조양동 1542-12",
                    "방문지" to "배꽃나라 주유소",
                    "고속 주행 거리 (km)" to "20",
                    "저속 주행 거리 (km)" to "5",
                    "고속 주행 거리 비율 (%)" to "80",
                    "저속 주행 거리 비율 (%)" to "20",
                    "최고 속력 (km/h)" to "152",
                    "평균 속력 (km/h)" to "72",
                    "고속 주행 최고 속력 (km/h)" to "132",
                    "저속 주행 최고 속력 (km/h)" to "40",
                    "급가속 횟수" to "2",
                    "급감속 횟수" to "1",
                    "급출발 횟수" to "2",
                    "급정지 횟수" to "1",
                    "최적 주행 거리 (km)" to "21",
                    "가혹 주행 거리 (km)" to "2",
                    "최적 주행 비율 (%)" to "90",
                    "가혹 주행 비율 (%)" to "10"
                ),
            )

            // 함수 호출
            createDrivingDataWithHeaders(drivingData)
        }

        btn_choose_date.setOnClickListener {
            showBottomSheetForChooseDate()
        }
    }

    fun showBottomSheetForChooseDate() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)

        // Inflate the layout
        val bottomSheetView = this.layoutInflater.inflate(R.layout.dialog_choose_date, null)

        val rv_choose_corp = bottomSheetView.findViewById<RecyclerView>(R.id.rv_choose_corp)

        rv_choose_corp.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, R.color.white_op_100, this.dpToPx(8f)) // 색상 리소스와 구분선 높이 설정
        rv_choose_corp.addItemDecoration(dividerItemDecoration)

        rv_choose_corp.adapter = ChooseDateAdapter(context = this, dateList = getLast24Months(), selectedDate = selectedDate, bottomSheetDialog, callback = object :ChooseDateAdapter.DateCallback{
            override fun onClicked(date: String) {
                selectedDate = date
                tv_chosen_date.text = date
            }

        })

        // Set the content view of the dialog
        bottomSheetDialog.setContentView(bottomSheetView)

        // Show the dialog
        bottomSheetDialog.show()
    }

    fun getLast24Months(): List<String> {
        val months = mutableListOf<String>()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
        var currentDate = LocalDate.now()

        repeat(24) {
            months.add(currentDate.format(formatter))
            currentDate = currentDate.minusMonths(1)
        }

        return months
    }

    fun getNowMonth(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
        var currentDate = LocalDate.now()

        return currentDate.format(formatter)
    }

    class ChooseDateAdapter(
        private val context: Context,
        private val dateList: List<String>,
        private val selectedDate:String,
        private val bottomSheetDialog: BottomSheetDialog,
        private val callback:DateCallback
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_choose_date, parent, false)
            return ChooseCorpHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ChooseCorpHolder) {
                val corp = dateList.get(position)
                holder.tv_date.text = corp

                if(corp.equals(selectedDate)){
                    holder.layout_date.isSelected = true
                    TextViewCompat.setTextAppearance(holder.tv_date, R.style.date_selected)
                }else{
                    holder.layout_date.isSelected = false
                    TextViewCompat.setTextAppearance(holder.tv_date, R.style.date_unselected)
                }

                holder.layout_date.setOnClickListener {
                    callback.onClicked(corp)
                    bottomSheetDialog.dismiss()
                }
            }
        }

        override fun getItemCount(): Int {
            return dateList.size
        }

        interface DateCallback {
            fun onClicked(date: String)
        }

    }

    class ChooseCorpHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout_date:LinearLayout = view.findViewById(R.id.layout_date)
        val tv_date:TextView = view.findViewById(R.id.tv_date)
    }

    fun createDrivingDataWithHeaders(drivingData: List<Map<String, String>>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Driving Data")

        // Define main headers and their sub-headers
        val mainHeaders = listOf(
            "주행", "", "", "", "", "", "", "사용자 구분", "", "",
            "방문지", "", "", "고속/저속 주행", "", "", "",
            "속력", "", "", "", "급가감속", "", "", "",
            "최적/가혹 주행", "", "", ""
        )

        val headers = listOf(
            "주행 시작 일시", "주행 종료 일시", "주행 시간", "주행 거리 (km)", "데이터 인증", "이동 수단", "주행 목적",
            "개인/법인", "법인 사용자 이름", "법인 부서명",
            "출발지", "도착지", "방문지",
            "고속 주행 거리 (km)", "저속 주행 거리 (km)", "고속 주행 거리 비율 (%)", "저속 주행 거리 비율 (%)",
            "최고 속력 (km/h)", "평균 속력 (km/h)", "고속 주행 최고 속력 (km/h)", "저속 주행 최고 속력 (km/h)",
            "급가속 횟수", "급감속 횟수", "급출발 횟수", "급정지 횟수",
            "최적 주행 거리 (km)", "가혹 주행 거리 (km)", "최적 주행 비율 (%)", "가혹 주행 비율 (%)"
        )

        // Create header rows
        val headerRow1 = sheet.createRow(0)
        val headerRow2 = sheet.createRow(1)

        // Create bold style for main headers
        val boldFont = workbook.createFont().apply {
            bold = true
        }

        val boldStyle = workbook.createCellStyle().apply {
            setFont(boldFont)
            val skyBlueColor = XSSFColor(byteArrayOf(0xB2.toByte(), 0xCC.toByte(), 0xFF.toByte())) // #B2CCFF 색상
            setFillForegroundColor(skyBlueColor)
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
        }

        val lightGrayStyle = workbook.createCellStyle().apply {
            setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index) // 연한 그레이색
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
        }

        // Set main headers (merge cells for each group)
        mainHeaders.forEachIndexed { index, mainHeader ->
            val cell = headerRow1.createCell(index)
            cell.setCellValue(mainHeader)
            cell.cellStyle = boldStyle // Apply bold style
        }

        // Set sub-headers
        headers.forEachIndexed { index, header ->
            val cell = headerRow2.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = lightGrayStyle // Apply light gray background
        }

        // Fill data rows
        drivingData.forEachIndexed { rowIndex, data ->
            val row = sheet.createRow(rowIndex + 2) // Data starts from the third row
            headers.forEachIndexed { colIndex, header ->
                val cell = row.createCell(colIndex)
                cell.setCellValue(data[header] ?: "")
            }
        }

        // 수동으로 열 너비 설정
        headers.indices.forEach { colIndex ->
            var maxLength = 0
            for (rowIndex in 0 until sheet.physicalNumberOfRows) {
                val cell = sheet.getRow(rowIndex)?.getCell(colIndex)
                val cellValue = cell?.toString() ?: ""
                maxLength = maxOf(maxLength, cellValue.length)
            }
            sheet.setColumnWidth(colIndex, (maxLength + 2) * 256) // 너비를 설정 (2 추가)
        }

        // 현재 날짜와 시간 구하기
        val currentDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        // 파일 이름에 날짜/시간 추가
        val fileName = "DrivingData_$currentDate.xlsx"

        // 파일 경로 설정 (앱의 외부 저장소에 저장)
        val file = File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            // Fill data rows (생략)

            // 수동으로 열 너비 설정 (생략)

            // Write to file
            FileOutputStream(file).use { output ->
                workbook.write(output)
            }

            Toast.makeText(this, "다운로드 폴더에 저장됐습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()  // 에러를 출력
        } finally {
            workbook.close()
        }
    }
}