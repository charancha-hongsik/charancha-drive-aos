package com.milelog.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
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
import com.milelog.activity.LoadCarMoreInfoActivity.Companion.CORPORATE
import com.milelog.retrofit.response.DriveItem
import com.milelog.retrofit.response.GetDriveHistoryResponse
import com.milelog.room.entity.MyCarsEntity
import com.nex3z.flowlayout.FlowLayout
import okhttp3.ResponseBody
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ExcelActivity:BaseRefreshActivity() {
    lateinit var layout_flow: FlowLayout
    lateinit var btn_save_excel:TextView
    lateinit var btn_choose_date:LinearLayout
    lateinit var tv_chosen_date:TextView
    lateinit var btn_back: ImageView
    var selectedDate:String = "2024년 10월"
    lateinit var workbook: Workbook

    val filterList: MutableList<CarListFilter> = mutableListOf()
    var carIdForFilter: String? = null
    var isActiveForFilter: Boolean? = null

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
        btn_back = findViewById(R.id.btn_back)

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

                        val matchingFilter = filterList.find { it.name == tv_car_name.text }
                        carIdForFilter = matchingFilter?.id
                        if(tv_car_name.text.toString().equals("전체")){
                            isActiveForFilter = null
                            carIdForFilter = null
                        }
                        else if(tv_car_name.text.toString().equals(getString(R.string.pending))){
                            isActiveForFilter = true
                            carIdForFilter = "null"
                        }else if(tv_car_name.text.toString().equals(getString(R.string.not_my_car))){
                            isActiveForFilter = false
                            carIdForFilter = "null"
                        }else{
                            isActiveForFilter = true
                        }


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

    fun getDrivingData(getDriveHistroyResponse: GetDriveHistoryResponse): List<Map<String,String>>{
        val drivingData:MutableList<Map<String,String>> = mutableListOf()

        for(history in getDriveHistroyResponse.items){
            drivingData.add(mapOf(
                "주행 시작 일시" to formatToLocalTimeForExcel(history.startTime),
                "주행 종료 일시" to formatToLocalTimeForExcel(history.endTime),
                "주행 시간" to history.totalTime.toString(),
                "주행 거리 (km)" to history.totalDistance.toString(),
                "데이터 인증" to history.verification,
                "이동 수단" to (history.userCar?.carName ?: ""),
                "주행 목적" to  (history.userCar?.type ?: ""),
                "사용자 구분" to (history.userCar?.type ?: ""),
                "법인 사용자 이름" to (history.userCar?.data?.name ?: ""),
                "법인 부서명" to (history.userCar?.data?.department ?: ""),
                "운전자 메모" to (history.memo?:""),
                "출발지" to (history.startAddress?.parcel?.name ?: ""),
                "도착지" to (history.endAddress?.parcel?.name ?: ""),
                "방문지" to (history.endAddress?.places?.takeIf { it.isNotEmpty() }?.get(0)?.name ?: ""),
                "고속 주행 거리 (km)" to history.highSpeedDrivingDistance.toString(),
                "저속 주행 거리 (km)" to history.lowSpeedDrivingDistance.toString(),
                "고속 주행 거리 비율 (%)" to history.highSpeedDrivingDistancePercentage.toString(),
                "저속 주행 거리 비율 (%)" to history.lowSpeedDrivingDistancePercentage.toString(),
                "최고 속력 (km/h)" to history.maxSpeed.toString(),
                "평균 속력 (km/h)" to history.averageSpeed.toString(),
                "고속 주행 최고 속력 (km/h)" to history.highSpeedDrivingMaxSpeed.toString(),
                "저속 주행 최고 속력 (km/h)" to history.lowSpeedDrivingMaxSpeed.toString(),
                "급가속 횟수" to history.rapidAccelerationCount.toString(),
                "급감속 횟수" to history.rapidDecelerationCount.toString(),
                "급출발 횟수" to history.rapidStartCount.toString(),
                "급정지 횟수" to history.rapidStopCount.toString(),
                "최적 주행 거리 (km)" to history.optimalDrivingDistance.toString(),
                "가혹 주행 거리 (km)" to history.harshDrivingDistance.toString(),
                "최적 주행 비율 (%)" to history.optimalDrivingPercentage.toString(),
                "가혹 주행 비율 (%)" to history.harshDrivingPercentage.toString()
            ))
        }

        return drivingData
    }

    fun setClickListener(){

        btn_save_excel.setOnClickListener {
            apiService().getDrivingDetailHistories(
                token = "Bearer " + PreferenceUtil.getPref(this,  PreferenceUtil.ACCESS_TOKEN, "")!!,
                size = 100,
                order = "DESC",
                afterCursor =  null,
                beforeCursor = null,
                key = "startTime",
                startTime = getDateRange(selectedDate).second,
                endTime = getDateRange(selectedDate).first,
                isActive = isActiveForFilter,
                userCarId = carIdForFilter).enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.code() == 200 || response.code() == 201){
                        val jsonString = response.body()?.string()
                        val getDriveHistroyResponse = GsonBuilder().serializeNulls().create().fromJson(
                            jsonString,
                            GetDriveHistoryResponse::class.java
                        )

//                        createDrivingDataWithHeaders(getDrivingData(getDriveHistroyResponse))
                        createDrivingDataWithHeaders2(getDrivingData(getDriveHistroyResponse), getDriveHistroyResponse.items)

                    } else{

                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

            })
        }

        btn_choose_date.setOnClickListener {
            showBottomSheetForChooseDate()
        }

        btn_back.setOnClickListener {
            finish()
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

    fun createDrivingDataWithHeaders2(drivingData: List<Map<String, String>>, driveItems:List<DriveItem>){
        workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("마일로그 상세 기록")

        // Define main headers and their sub-headers
        val mainHeaders = listOf(
            "주행", "", "", "", "", "", "", "사용자 구분", "", "","",
            "방문지", "", "", "고속/저속 주행", "", "", "",
            "속력", "", "", "", "급가감속", "", "", "",
            "최적/가혹 주행", "", "", ""
        )

        val headers = listOf(
            "주행 시작 일시", "주행 종료 일시", "주행 시간", "주행 거리 (km)", "데이터 인증", "이동 수단", "주행 목적",
            "개인/법인", "법인 사용자 이름", "법인 부서명","운전자 메모",
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

        val sheet1 = workbook.createSheet("국세청 양식")

// 열 너비 설정
        sheet1.setColumnWidth(0, (17 * 256) / 6)
        sheet1.setColumnWidth(1, (86 * 256) / 6)
        sheet1.setColumnWidth(2, (32 * 256) / 6)
        sheet1.setColumnWidth(3, (29 * 256) / 6)
        sheet1.setColumnWidth(4, (36 * 256) / 6)
        sheet1.setColumnWidth(5, (26 * 256) / 6)
        sheet1.setColumnWidth(6, (34 * 256) / 6)
        sheet1.setColumnWidth(7, (37 * 256) / 6)
        sheet1.setColumnWidth(8, (31 * 256) / 6)
        sheet1.setColumnWidth(9, (37 * 256) / 6)
        sheet1.setColumnWidth(10, (62 * 256) / 6)
        sheet1.setColumnWidth(11, (44 * 256) / 6)
        sheet1.setColumnWidth(12, (44 * 256) / 6)
        sheet1.setColumnWidth(13, (16 * 256) / 6)
        sheet1.setColumnWidth(14, (72 * 256) / 6)
        sheet1.setColumnWidth(15, (18 * 256) / 6)
        sheet1.setColumnWidth(16, (75 * 256) / 6)

// 행 높이 설정
        sheet1.createRow(0).heightInPoints = 24f // Row 1
        sheet1.createRow(1).heightInPoints = 18f // Row 2
        sheet1.createRow(2).heightInPoints = 8f  // Row 3
        sheet1.createRow(3).heightInPoints = 8f  // Row 4
        sheet1.createRow(5).heightInPoints = 8f  // Row 6
        sheet1.createRow(6).heightInPoints = 25f // Row 7
        sheet1.createRow(7).heightInPoints = 24f // Row 8
        sheet1.createRow(8).heightInPoints = 23f // Row 9
        sheet1.createRow(9).heightInPoints = 23f // Row 10
        sheet1.createRow(10).heightInPoints = 26f // Row 11
        sheet1.createRow(11).heightInPoints = 25f // Row 12
        sheet1.createRow(12).heightInPoints = 26f // Row 13
        sheet1.createRow(13).heightInPoints = 23f // Row 14
        sheet1.createRow(14).heightInPoints = 20f
        mergeCells(sheet1, 14, 2, 14, 3)  // C to D
        mergeCells(sheet1, 14, 4, 14, 5)  // E to F
        mergeCells(sheet1, 14, 6, 14, 7)  // G to H
        mergeCells(sheet1, 14, 8, 14, 9)  // I to J
        mergeCells(sheet1, 14, 11, 14, 13) // L to N
        mergeCells(sheet1, 14, 14, 14, 15) // O to P

        val lastNo1 = driveItems.size + 16
        val lastNo2 = driveItems.size + 17
        val lastNo3 = driveItems.size + 18
        val lastNo4 = driveItems.size + 19
        val lastNo5 = driveItems.size + 20


        sheet1.createRow(lastNo1).heightInPoints = 30f // Row 78
        sheet1.createRow(lastNo2).heightInPoints = 29f // Row 79
        sheet1.createRow(lastNo3).heightInPoints = 30f // Row 78
        sheet1.createRow(lastNo4).heightInPoints = 29f // Row 79        sheet1.createRow(lastNo1).heightInPoints = 30f // Row 78
        sheet1.createRow(lastNo5).heightInPoints = 29f // Row 79


        mergeCells(sheet1, 0, 1, 0, 16) // Merge B1 to Q1
        val cellB1 = sheet1.getRow(0).createCell(1)
        cellB1.setCellValue("【업무용승용차 운행기록부에 관한 별지 서식】<2016.4.1. 제정>")
        cellB1.cellStyle = createCellStyle(workbook, 10, bold = false, verticalAlignment = VerticalAlignment.CENTER)
        val border = workbook.createCellStyle()
        border.setBorderBottom(BorderStyle.THIN)
        border.setBottomBorderColor(IndexedColors.BLACK.index)
        cellB1.cellStyle = border

        // Row 2 to Row 5 - 과세기간 영역
        mergeCells(sheet1, 1, 1, 4, 2) // B2:C5
        val cellB2 = sheet1.getRow(1).createCell(1)
        cellB2.setCellValue("과   세   기   간")
        cellB2.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 1, 3, 1, 6) // D2:G2
        val cellD2 = sheet1.getRow(1).createCell(3)
        cellD2.setCellValue(".      .      .")
        cellD2.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 2, 3, 3, 6) // D3:G4
        val cellD3 = sheet1.getRow(2).createCell(3)
        cellD3.setCellValue("~")
        cellD3.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 4, 3, 4, 6) // D5:G5
        val row4 = sheet1.getRow(4) ?: sheet1.createRow(4)  // Row 4가 없으면 새로 생성
        val cellD5 = row4.createCell(3)
        cellD5.setCellValue(".      .      .")
        cellD5.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        // Title: 업무용승용차 운행기록부
        mergeCells(sheet1, 1, 7, 4, 12) // H2:M5
        val cellH2 = sheet1.getRow(1).createCell(7)
        cellH2.setCellValue("업무용승용차 운행기록부")
        cellH2.cellStyle = createCellStyle(workbook, 18, bold = true, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        // 상호명 & 사업자등록번호
        mergeCells(sheet1, 1, 13, 2, 14) // N2:O3
        val cellN2 = sheet1.getRow(1).createCell(13)
        cellN2.setCellValue("상      호      명")
        cellN2.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 3, 13, 4, 14) // N4:O5
        val cellN4 = sheet1.getRow(3).createCell(13)
        cellN4.setCellValue("사업자등록번호")
        cellN4.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


        // 1. 기본정보 관련 병합 및 스타일링
        mergeCells(sheet1, 7, 1, 7, 8) // B7:I7
        val cellB7 = sheet1.getRow(7).createCell(1)
        cellB7.setCellValue("1. 기본정보")
        cellB7.cellStyle = createCellStyle(workbook, 12, bold = true, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 8, 1, 8, 3) // B8:D8
        val cellB8 = sheet1.getRow(8).createCell(1)
        cellB8.setCellValue("①차 종")
        cellB8.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


        var carName = ""
        if(driveItems.size > 0){
            carName = driveItems.get(0).userCar?.carName?:""
        }

        mergeCells(sheet1, 9, 1, 9, 3) // B8:D8
        val cellB9 = sheet1.getRow(9).createCell(1)
        cellB9.setCellValue(carName)
        cellB9.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        var carNumber = ""
        if(driveItems.size > 0){
            carNumber = driveItems.get(0).userCar?.licensePlateNumber?:""
        }

        mergeCells(sheet1, 8, 4, 8, 7) // E8:H8
        val cellE8 = sheet1.getRow(8).createCell(4)
        cellE8.setCellValue("②자동차등록번호")
        cellE8.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 9, 4, 9, 7) // E8:H8
        val cellE9 = sheet1.getRow(9).createCell(4)
        cellE9.setCellValue(carNumber)
        cellE9.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

// 2. 업무용 사용비율 계산 병합 및 스타일링
        mergeCells(sheet1, 11, 1, 11, 8) // B11:I11
        val cellB11 = sheet1.getRow(11).createCell(1)
        cellB11.setCellValue("2. 업무용 사용비율 계산")
        cellB11.cellStyle = createCellStyle(workbook, 12, bold = true, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 12, 1, 14, 1) // B12:B14
        val cellB12 = sheet1.getRow(12).createCell(1)
        cellB12.setCellValue("③사용일자(요일)")
        cellB12.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 12, 2, 12, 5) // C12:F12
        val cellC12 = sheet1.getRow(12).createCell(2)
        cellC12.setCellValue("④사용자")
        cellC12.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        // B78:B79 병합 및 스타일 설정
        mergeCells(sheet1, lastNo1, 1, lastNo2, 1) // B78:B79
        val cellB78 = sheet1.getRow(lastNo1).createCell(1)
        cellB78.cellStyle = createCellStyle(workbook, 10)

// C78:F79 병합 및 스타일 설정
        mergeCells(sheet1, lastNo1, 2, lastNo2, 5) // C78:F79
        val cellC78 = sheet1.getRow(lastNo1).createCell(2)
        cellC78.cellStyle = createCellStyle(workbook, 10)

// G78:K78 병합 및 스타일 설정
        mergeCells(sheet1, lastNo1, 6, lastNo1, 10) // G78:K78
        val cellG78 = sheet1.getRow(lastNo1).createCell(6)
        cellG78.setCellValue("⑪과세기간 총주행 거리(㎞)")
        cellG78.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, lastNo2, 6, lastNo2, 10) // G78:K78
        val cellG79 = sheet1.getRow(lastNo2).createCell(6)
        cellG79.setCellValue(((driveItems.sumOf { it.totalDistance }/1000).toInt()).toString() + "km")
        cellG79.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

// L78:P78 병합 및 스타일 설정
        mergeCells(sheet1, lastNo1, 11, lastNo1, 15) // L78:P78
        val cellL78 = sheet1.getRow(lastNo1).createCell(11)
        cellL78.setCellValue("⑫과세기간 업무용 사용거리(㎞)")
        cellL78.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


        mergeCells(sheet1, lastNo2, 11, lastNo2, 15) // L78:P78
        val cellL79 = sheet1.getRow(lastNo2).createCell(11)
        cellL79.setCellValue((driveItems.sumOf { it.totalDistance }/1000).toInt().toString() + "km")
        cellL79.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

// Q78 스타일 설정
        val cellQ78 = sheet1.getRow(lastNo1).createCell(16)
        cellQ78.setCellValue("⑬업무사용비율(⑫/⑪)")
        cellQ78.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        // type이 null이 아닌 아이템 개수
        val nonNullTypeCount = driveItems.count { it.type != null }

        // 비율 계산
        val ratio:Double = if (driveItems.size > 0) nonNullTypeCount.toDouble() / driveItems.size.toDouble() else 0.0


// G79:K79 병합
        val cellQ79 = sheet1.getRow(lastNo2).createCell(16)
        cellQ79.setCellValue((ratio*100).toInt().toString() + "%")
        cellQ79.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)



// 1. 부서 & 성명 - 셀 병합 수정 (2칸, 1행)
        mergeCells(sheet1, 14, 2, 14, 3) // C13:D13 (부서)
        val cellC13 = sheet1.getRow(14).createCell(2)
        cellC13.setCellValue("부서")
        cellC13.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 14, 4, 14, 5) // E13:F13 (성명)
        val cellE13 = sheet1.getRow(14).createCell(4)
        cellE13.setCellValue("성명")
        cellE13.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

// 2. ⑤주행 전 계기판의 거리(㎞) - 셀 병합 수정 (2칸, 1행)
        mergeCells(sheet1, 14, 6, 14, 7) // G13:H13 (⑤주행 전 계기판의 거리(㎞))
        val cellG13 = sheet1.getRow(14).createCell(6)
        cellG13.setCellValue("⑤주행 전 계기판의 거리(㎞)")
        cellG13.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

// 3. ⑥주행 후 계기판의 거리(㎞) - 셀 병합 수정 (2칸, 1행)
        mergeCells(sheet1, 14, 8, 14, 9) // I13:J13 (⑥주행 후 계기판의 거리(㎞))
        val cellI13 = sheet1.getRow(14).createCell(8)
        cellI13.setCellValue("⑥주행 후 계기판의 거리(㎞)")
        cellI13.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

// 3. 운행 내역 관련 병합 및 스타일링
        mergeCells(sheet1, 12, 6, 12, 16) // G12:Q12
        val cellG12 = sheet1.getRow(12).createCell(6)
        cellG12.setCellValue("운 행     내 역")
        cellG12.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


        mergeCells(sheet1, 13, 10, 14, 10) // K13:K14
        val cellK13 = sheet1.getRow(13).createCell(10)
        cellK13.setCellValue("⑦주행 거리(㎞)")
        cellK13.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

// 4. 업무용 사용거리 세부항목
        mergeCells(sheet1, 13, 11, 13, 15) // L13:P13
        val cellL13 = sheet1.getRow(13).createCell(11)
        cellL13.setCellValue("업무용 사용거리(㎞)")
        cellL13.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 14, 11, 14, 13) // L14:N14
        val cellL14 = sheet1.getRow(14).createCell(11)
        cellL14.setCellValue("⑧출․퇴근용(㎞)")
        cellL14.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 14, 14, 14, 15) // O14:P14
        val cellO14 = sheet1.getRow(14).createCell(14)
        cellO14.setCellValue("⑨일반 업무용(㎞)")
        cellO14.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        mergeCells(sheet1, 13, 16, 14, 16) // Q13:Q14
        val cellQ13 = sheet1.getRow(13).createCell(16)
        cellQ13.setCellValue("⑩비 고")
        cellQ13.cellStyle = createCellStyle(workbook, 10, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


        for((index, item) in driveItems.withIndex()){
            var i = index+15
            sheet1.createRow(i).heightInPoints = 20f
            mergeCells(sheet1, i, 2, i, 3)  // C to D
            mergeCells(sheet1, i, 4, i, 5)  // E to F
            mergeCells(sheet1, i, 6, i, 7)  // G to H
            mergeCells(sheet1, i, 8, i, 9)  // I to J
            mergeCells(sheet1, i, 11, i, 13) // L to N
            mergeCells(sheet1, i, 14, i, 15) // O to P

            /**
             * 사용일자
             */
            val cellB15 = sheet1.getRow(i).createCell(1)
            cellB15.setCellValue(convertDateFormatForExcel(item.startTime))
            cellB15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

            /**
             * 부서
             */
            val cellC15 = sheet1.getRow(i).createCell(2)
            cellC15.setCellValue(item.userCar?.data?.department?:"")
            cellC15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

            /**
             * 성명
             */
            val cellE15 = sheet1.getRow(i).createCell(4)
            cellE15.setCellValue(item.userCar?.data?.name?:"")
            cellE15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

            /**
             * ⑤주행 전
             * 계기판의 거리(㎞)
             */
            val cellG15 = sheet1.getRow(i).createCell(6)
            cellG15.setCellValue("")
            cellG15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


            /**
             * ⑥주행 후
             * 계기판의 거리(㎞)
             */
            val cellI15 = sheet1.getRow(i).createCell(8)
            cellI15.setCellValue("")
            cellI15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


            /**
             * ⑦주행거리(㎞)
             */
            val cellK15 = sheet1.getRow(i).createCell(10)
            cellK15.setCellValue(((item.totalDistance/1000).toInt()).toString() + "km")
            cellK15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


            /**
             * ⑧출․퇴근용(㎞)
             */
            var distanceForCommute = 0.0
            var distanceForWork = 0.0

            item.type?.let{
                if(it.equals(DetailDriveHistoryActivity.CorpType.COMMUTE.name)){
                    distanceForCommute = item.totalDistance
                }

                if(it.equals(DetailDriveHistoryActivity.CorpType.WORK.name)){
                    distanceForWork = item.totalDistance
                }


            }

            val cellL15 = sheet1.getRow(i).createCell(11)
            cellL15.setCellValue(distanceForCommute.toInt().toString() + "km")
            cellL15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


            /**
             * ⑨일반 업무용(㎞)
             */
            val cellO15 = sheet1.getRow(i).createCell(14)
            cellO15.setCellValue(distanceForWork.toInt().toString() + "km")
            cellO15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)


            /**
             * ⑩비 고
             */
            val cellQ15 = sheet1.getRow(i).createCell(16)
            cellQ15.setCellValue(item.type)
            cellQ15.cellStyle = createCellStyle(workbook, 10, bold = false, horizontalAlignment = HorizontalAlignment.CENTER, verticalAlignment = VerticalAlignment.CENTER)

        }


        // 현재 날짜와 시간 구하기
        val currentDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        try {
            // 현재 날짜를 기반으로 파일 이름 생성
            val fileName = "DrivingData_$currentDate.xlsx"

            // 공유할 파일 URI를 생성할 File 객체를 설정합니다.
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            // 파일에 워크북 데이터를 저장
            FileOutputStream(file).use { output ->
                workbook.write(output)
            }

            // 공유를 위한 FileProvider URI 생성
            val fileUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider", // FileProvider의 authority는 AndroidManifest에서 설정
                file
            )

            // 공유 Intent 생성 (ACTION_SEND)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // XLSX MIME 타입
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // URI 읽기 권한 부여
            }

            // 저장 Intent 생성 (ACTION_CREATE_DOCUMENT)
            val saveIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // XLSX MIME 타입
                putExtra(Intent.EXTRA_TITLE, fileName) // 사용자에게 보여줄 파일 이름
            }
            saveIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // 두 개의 Intent를 포함하는 Chooser
            val chooserIntent = Intent.createChooser(shareIntent, "주행 데이터 공유하기").apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(saveIntent)) // 저장 옵션 추가
            }

            // OS 공유/저장 Bottom Sheet 호출
            startActivityForResult(chooserIntent, 1)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "파일 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun isMerged(sheet: Sheet, row: Int, col: Int): Boolean {
        val mergedRegions = sheet.mergedRegions
        for (region in mergedRegions) {
            if (region.isInRange(row, col)) {
                return true
            }
        }
        return false
    }

    // 병합 처리
    fun mergeCells(sheet: Sheet, startRow: Int, startCol: Int, endRow: Int, endCol: Int) {
        if (!isMerged(sheet, startRow, startCol) && !isMerged(sheet, endRow, endCol)) {
            sheet.addMergedRegion(CellRangeAddress(startRow, endRow, startCol, endCol))
        }
    }

    fun createCellStyle(
        workbook: Workbook,
        fontSize: Int,
        bold: Boolean = false,
        verticalAlignment: VerticalAlignment = VerticalAlignment.BOTTOM,
        horizontalAlignment: HorizontalAlignment = HorizontalAlignment.LEFT
    ): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.fontHeightInPoints = fontSize.toShort()
        font.bold = bold
        style.setFont(font)
        style.verticalAlignment = verticalAlignment
        style.alignment = horizontalAlignment
        return style
    }


    fun createDrivingDataWithHeaders(drivingData: List<Map<String, String>>) {
        workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("마일로그 상세 기록")

        // Define main headers and their sub-headers
        val mainHeaders = listOf(
            "주행", "", "", "", "", "", "", "사용자 구분", "", "","",
            "방문지", "", "", "고속/저속 주행", "", "", "",
            "속력", "", "", "", "급가감속", "", "", "",
            "최적/가혹 주행", "", "", ""
        )

        val headers = listOf(
            "주행 시작 일시", "주행 종료 일시", "주행 시간", "주행 거리 (km)", "데이터 인증", "이동 수단", "주행 목적",
            "개인/법인", "법인 사용자 이름", "법인 부서명","운전자 메모",
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

        try {
            // 현재 날짜를 기반으로 파일 이름 생성
            val fileName = "DrivingData_$currentDate.xlsx"

            // 공유할 파일 URI를 생성할 File 객체를 설정합니다.
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            // 파일에 워크북 데이터를 저장
            FileOutputStream(file).use { output ->
                workbook.write(output)
            }

            // 공유를 위한 FileProvider URI 생성
            val fileUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider", // FileProvider의 authority는 AndroidManifest에서 설정
                file
            )

            // 공유 Intent 생성 (ACTION_SEND)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // XLSX MIME 타입
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // URI 읽기 권한 부여
            }

            // 저장 Intent 생성 (ACTION_CREATE_DOCUMENT)
            val saveIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // XLSX MIME 타입
                putExtra(Intent.EXTRA_TITLE, fileName) // 사용자에게 보여줄 파일 이름
            }
            saveIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // 두 개의 Intent를 포함하는 Chooser
            val chooserIntent = Intent.createChooser(shareIntent, "주행 데이터 공유하기").apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(saveIntent)) // 저장 옵션 추가
            }

            // OS 공유/저장 Bottom Sheet 호출
            startActivityForResult(chooserIntent, 1)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "파일 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    // 선택된 URI에 대한 OutputStream을 열고
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        // 워크북을 선택된 파일에 저장
                        workbook.write(outputStream)
                    }
                    Toast.makeText(this, "파일이 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "파일 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                } finally {
                    // 파일 저장이 끝났다면 워크북을 닫음
                    workbook.close()
                }
            }
        }
    }

    fun createCarUsageRecord() {
        val workbook = XSSFWorkbook()  // Apache POI를 사용하여 엑셀 파일 생성
        val sheet = workbook.createSheet("업무용승용차 운행기록부")

        // 첫 번째 행: 업무용승용차 운행기록부에 관한 별지 서식
        var row = sheet.createRow(0)
        row.createCell(1).setCellValue("【업무용승용차 운행기록부에 관한 별지 서식】<2016.4.1. 제정>")
        sheet.addMergedRegion(CellRangeAddress(0, 0, 1, 16))  // A1부터 Q1까지 병합

        // 두 번째부터 다섯 번째 행까지 병합하고 "과세기간" 추가
        row = sheet.createRow(1)
        row.createCell(1).setCellValue("과세기간")
        sheet.addMergedRegion(CellRangeAddress(1, 4, 1, 2))  // A2부터 B5까지 병합

        // 병합된 셀을 가운데 정렬로 설정
        val cellStyle = workbook.createCellStyle()
        cellStyle.alignment = HorizontalAlignment.CENTER
        row.getCell(0).cellStyle = cellStyle

        // 파일 이름 설정
        val currentDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "CarUsageRecord_$currentDate.xlsx"

        // 파일 경로 설정
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            FileOutputStream(file).use { output ->
                workbook.write(output)
            }
            println("엑셀 파일이 성공적으로 저장되었습니다.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun formatToLocalTimeForExcel(isoDate: String): String {
        try {
            // ISO 8601 형식의 날짜 문자열을 Date 객체로 변환
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC") // 입력 날짜가 UTC 기준임을 명시

            val date = isoFormat.parse(isoDate) ?: return "" // 날짜 파싱

            // 로컬 시간대 기준으로 포맷 변경
            val localFormat = SimpleDateFormat("yyyy.MM.dd(E) HH:mm:ss", Locale.KOREAN)
            localFormat.timeZone = TimeZone.getDefault() // 현재 로컬 시간대

            return localFormat.format(date) // 변환된 날짜 반환
        } catch (e: Exception) {
            e.printStackTrace()
            return "" // 예외 발생 시 빈 문자열 반환
        }
    }


}