package com.milelog.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.milelog.R
import com.milelog.viewmodel.DetailDriveHistoryViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.StrokeStyle
import com.google.android.gms.maps.model.StyleSpan
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.CustomDialog
import com.milelog.DividerItemDecoration
import com.milelog.EditHistoryEntity
import com.milelog.PreferenceUtil
import com.milelog.activity.FindBluetoothActivity.MyCarEntitiesAdapter
import com.milelog.activity.LoadCarMoreInfoActivity.Companion.CORPORATE
import com.milelog.activity.LoadCarMoreInfoActivity.Companion.PERSONAL
import com.milelog.activity.MyInfoActivity.Companion.REQUEST_READ_EXTERNAL_STORAGE
import com.milelog.retrofit.request.DeleteImage
import com.milelog.retrofit.request.PatchCorpType
import com.milelog.retrofit.response.GetDrivingInfoResponse
import com.milelog.retrofit.response.UserCar
import com.milelog.retrofit.response.VWorldDetailResponse
import com.milelog.retrofit.response.VWorldResponse
import com.milelog.room.entity.MyCarsEntity
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.state.GetDrivingInfoState
import com.milelog.viewmodel.state.PatchCorpTypeState
import com.milelog.viewmodel.state.PatchDrivingInfoState
import com.milelog.viewmodel.state.PatchImageState
import com.milelog.viewmodel.state.PatchMemoState
import com.nex3z.flowlayout.FlowLayout
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


class DetailDriveHistoryActivity: BaseRefreshActivity() {
    val keywords = listOf("주유소", "정비소", "세차장", "폐차장", "중고차", "매매단지", "도이치오토월드", "캠핑","글램핑")


    val polylines:MutableList<LatLng> = mutableListOf()

    private var currentMarker: Marker? = null

    private val detailDriveHistoryViewModel: DetailDriveHistoryViewModel by viewModels()

    lateinit var tv_date:TextView
    lateinit var tv_distance:TextView
    lateinit var tv_start_time:TextView
    lateinit var tv_end_time:TextView
    lateinit var tv_start_time_info:TextView
    lateinit var tv_end_time_info:TextView
    lateinit var tv_drive_time_info:TextView
    lateinit var tv_drive_distance_info:TextView
    lateinit var tv_drive_verification_info:TextView
    lateinit var tv_high_speed_driving_percent_info:TextView
    lateinit var tv_low_speed_driving_percent_info:TextView
    lateinit var tv_max_speed_info:TextView
    lateinit var tv_high_speed_average_info:TextView
    lateinit var tv_low_speed_average_info:TextView
    lateinit var tv_rapid_start_count:TextView
    lateinit var tv_rapid_acc_count_info:TextView
    lateinit var tv_rapid_stop_count_info:TextView
    lateinit var tv_rapid_desc_count_info:TextView
    lateinit var btn_back: ImageView
    lateinit var tv_scope_date_mycar:TextView
    lateinit var tv_mycar:TextView
    lateinit var tv_type:TextView
    lateinit var btn_choose_mycar: LinearLayout
    lateinit var tv_mycar_scope_info:LinearLayout
    lateinit var btn_add_image:ConstraintLayout
    lateinit var tv_text_length:TextView
    private val PICK_IMAGE_REQUEST = 1
    lateinit var tv_edit:TextView

    lateinit var iv_tooltip_verification:ImageView
    lateinit var iv_tooltip_rapid_desc:ImageView
    lateinit var iv_tooltip_rapid_stop:ImageView
    lateinit var iv_tooltip_high_speed_average:ImageView
    lateinit var iv_tooltip_high_speed:ImageView
    lateinit var iv_tooltip_low_speed:ImageView
    lateinit var iv_tooltip_low_speed_average:ImageView
    lateinit var iv_tooltip_rapid_start:ImageView
    lateinit var iv_tooltip_rapid_acc:ImageView
    lateinit var tv_tv_add_image:TextView

    lateinit var tv_start_address:TextView
    lateinit var tv_end_address:TextView
    lateinit var tv_end_address_detail:TextView
    lateinit var view_edit_detail:ConstraintLayout

    lateinit var view_map:CardView
    lateinit var layout_drive_image:FlowLayout

    lateinit var view_edit1:View
    lateinit var view_edit2:View
    lateinit var view_edit3:View
    lateinit var view_edit4:View
    lateinit var view_edit5:View


    lateinit var iv_corp:ImageView

    lateinit var btn_choose_corp: ConstraintLayout
    private lateinit var imageMultipart: MultipartBody.Part // 선택한 이미지

    private var isCameraMoving = false
    private var currentAnimator: ValueAnimator? = null
    private var bluetoothNameExpected:String? = null

    lateinit var et_memo: EditText

    var isActive = true
    var userCarId:String? = null
    var userCar: UserCar? = null
    lateinit var tracking_id:String
    var pastMemo:String = ""
    var currentMemo:String = ""

    enum class CorpType(val description: String) {
        COMMUTE("출/퇴근"),
        NON_WORK("비업무"),
        WORK("일반업무");

        override fun toString(): String {
            return description
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_drive_history)

        detailDriveHistoryViewModel.init(applicationContext)

        init()
        setObserver()
        setResources()
    }

    private fun init(){
        tracking_id = intent.getStringExtra("trackingId").toString()
        isActive = intent.getBooleanExtra("isActive",true)
        intent.getStringExtra("userCar")?.let{
            userCar = Gson().fromJson(it, UserCar::class.java)
        }

        tv_text_length = findViewById(R.id.tv_text_length)
        btn_add_image = findViewById(R.id.btn_add_image)
        tv_date = findViewById(R.id.tv_date)
        tv_distance = findViewById(R.id.tv_distance)
        tv_start_time = findViewById(R.id.tv_start_time)
        tv_end_time = findViewById(R.id.tv_end_time)
        tv_start_time_info = findViewById(R.id.tv_start_time_info)
        tv_end_time_info = findViewById(R.id.tv_end_time_info)
        tv_drive_time_info = findViewById(R.id.tv_drive_time_info)
        tv_drive_distance_info = findViewById(R.id.tv_drive_distance_info)
        tv_drive_verification_info = findViewById(R.id.tv_drive_verification_info)
        tv_high_speed_driving_percent_info = findViewById(R.id.tv_high_speed_driving_percent_info)
        tv_low_speed_driving_percent_info = findViewById(R.id.tv_low_speed_driving_percent_info)
        tv_max_speed_info = findViewById(R.id.tv_max_speed_info)
        tv_low_speed_average_info = findViewById(R.id.tv_low_speed_average_info)
        tv_high_speed_average_info = findViewById(R.id.tv_high_speed_average_info)
        tv_rapid_start_count = findViewById(R.id.tv_rapid_start_count)
        tv_rapid_acc_count_info = findViewById(R.id.tv_rapid_acc_count_info)
        tv_rapid_stop_count_info = findViewById(R.id.tv_rapid_stop_count_info)
        tv_rapid_desc_count_info = findViewById(R.id.tv_rapid_desc_count_info)
        btn_back = findViewById(R.id.btn_back)
        btn_choose_mycar = findViewById(R.id.btn_choose_mycar)
        tv_scope_date_mycar = findViewById(R.id.tv_scope_date_mycar)
        tv_mycar_scope_info = findViewById(R.id.tv_mycar_scope_info)
        tv_type = findViewById(R.id.tv_type)
        tv_tv_add_image = findViewById(R.id.tv_tv_add_image)
        view_edit1 = findViewById(R.id.view_edit1)
        view_edit2 = findViewById(R.id.view_edit2)
        view_edit3 = findViewById(R.id.view_edit3)
        view_edit4 = findViewById(R.id.view_edit4)
        view_edit5 = findViewById(R.id.view_edit5)
        tv_edit = findViewById(R.id.tv_edit)



        iv_tooltip_verification = findViewById(R.id.iv_tooltip_verification)
        iv_tooltip_low_speed = findViewById(R.id.iv_tooltip_low_speed)
        iv_tooltip_high_speed = findViewById(R.id.iv_tooltip_high_speed)
        iv_tooltip_rapid_desc = findViewById(R.id.iv_tooltip_rapid_desc)
        iv_tooltip_rapid_stop = findViewById(R.id.iv_tooltip_rapid_stop)
        iv_tooltip_high_speed_average = findViewById(R.id.iv_tooltip_high_speed_average)
        iv_tooltip_low_speed_average = findViewById(R.id.iv_tooltip_low_speed_average)
        iv_tooltip_rapid_start = findViewById(R.id.iv_tooltip_rapid_start)
        iv_tooltip_rapid_acc = findViewById(R.id.iv_tooltip_rapid_acc)
        tv_mycar = findViewById(R.id.tv_mycar)
        layout_drive_image = findViewById(R.id.layout_drive_image)
        view_edit_detail = findViewById(R.id.view_edit_detail)

        view_edit_detail.setOnClickListener {

        }

        tv_start_address = findViewById(R.id.tv_start_address)
        tv_end_address = findViewById(R.id.tv_end_address)
        tv_end_address_detail = findViewById(R.id.tv_end_address_detail)

        btn_choose_corp = findViewById(R.id.btn_choose_corp)
        btn_choose_corp.setOnClickListener {
//            showBottomSheetForChooseCorp(userCar!!.id)
        }

        et_memo = findViewById(R.id.et_memo)

        et_memo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 텍스트 변경 전 처리 (현재는 필요하지 않음)
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let {
                    // 텍스트 길이가 10을 초과하면 마지막 글자 삭제
                    if (it.length > 10) {
                        et_memo.setText(it.substring(0, 10))  // 마지막 글자 자르기
                        et_memo.setSelection(10)  // 커서를 마지막으로 이동
                    } else {
                        // 텍스트 길이가 10 이하일 경우 tv_text_length 업데이트
                        tv_text_length.text = "${it.length}/10"
                    }

                    currentMemo = et_memo.text.toString()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                // 텍스트 변경 후 처리 (현재는 필요하지 않음)
            }
        })

        iv_corp = findViewById(R.id.iv_corp)

        view_map = findViewById(R.id.view_map)
    }

    fun showBottomSheetForEditHistory(editHistoryEntities:MutableList<EditHistoryEntity>) {
        // Create a BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)

        // Inflate the layout
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_edit_history, null)
        val rv_edit_history = bottomSheetView.findViewById<RecyclerView>(R.id.rv_edit_history)

        rv_edit_history.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, R.color.white_op_100, dpToPx(26f)) // 색상 리소스와 구분선 높이 설정
        rv_edit_history.addItemDecoration(dividerItemDecoration)

        rv_edit_history.adapter = EditHistoryAdapter(context = this, editHistoryEntities = editHistoryEntities)

        // Set the content view of the dialog
        bottomSheetDialog.setContentView(bottomSheetView)

        // Show the dialog
        bottomSheetDialog.show()
    }

    class EditHistoryAdapter(
        private val context: Context,
        private val editHistoryEntities: MutableList<EditHistoryEntity>,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_edit_history, parent, false)
            return EditHistoryHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is EditHistoryHolder) {
                val edit = editHistoryEntities.get(position)
                holder.tv_edit_title.text = edit.title
                holder.tv_current.text = edit.current
                holder.tv_past.text = edit.past
            }
        }

        override fun getItemCount(): Int {
            return editHistoryEntities.size
        }
    }

    class EditHistoryHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_edit_title:TextView = view.findViewById(R.id.tv_edit_title)
        val tv_current:TextView = view.findViewById(R.id.tv_current)
        val tv_past:TextView = view.findViewById(R.id.tv_past)
    }

    private fun setObserver(){
        detailDriveHistoryViewModel.patchMemo.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver { state ->
            when (state) {
                is PatchMemoState.Loading -> {

                }
                is PatchMemoState.Success -> {
                    val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                    intent.putExtra("isActive",isActive)
                    intent.putExtra("userCarId",userCarId)
                    intent.putExtra("trackingId",tracking_id)
                    intent.putExtra("userCar",Gson().toJson(userCar))
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is PatchMemoState.Error -> {
                    val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                    intent.putExtra("isActive",isActive)
                    intent.putExtra("userCarId",userCarId)
                    intent.putExtra("trackingId",tracking_id)
                    intent.putExtra("userCar",Gson().toJson(userCar))
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is PatchMemoState.Empty -> {
                    val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                    intent.putExtra("isActive",isActive)
                    intent.putExtra("userCarId",userCarId)
                    intent.putExtra("trackingId",tracking_id)
                    intent.putExtra("userCar",Gson().toJson(userCar))
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        })

        detailDriveHistoryViewModel.patchImage.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver { state ->
            when (state) {
                is PatchImageState.Loading -> {

                }
                is PatchImageState.Success -> {

                    while(layout_drive_image.childCount > 1) {
                        layout_drive_image.removeViewAt(layout_drive_image.childCount-1)
                    }


                    state.data.images?.let{
                        if(it.size > 0){
                            for(image in it){
                                addImageToLayout(url = image.url, image.id)
                            }
                        }else{
                            tv_tv_add_image.text = "0/5"
                        }
                    }
                }
                is PatchImageState.Error -> {

                }
                is PatchImageState.Empty -> {

                }
            }
        })

        detailDriveHistoryViewModel.patchCorpType.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver { state ->
            when (state) {
                is PatchCorpTypeState.Loading -> {

                }
                is PatchCorpTypeState.Success -> {
                    isActive = state.data.isActive
                    userCarId = state.data.userCarId
                    userCar = state.data.userCar

                    tv_type.text = CorpType.valueOf(state.data.type).description
                    btn_choose_corp.visibility = VISIBLE

                    tv_mycar.text = state.data.userCar.carName
                    iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star1))
                    iv_corp.visibility = VISIBLE

                    btn_choose_mycar.isClickable = false
                }
                is PatchCorpTypeState.Error -> {

                }
                is PatchCorpTypeState.Empty -> {

                }
            }
        })


        detailDriveHistoryViewModel.getMapData.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver {
            it?.let{
                for(raw in it.gpses){
                    polylines.add(LatLng(raw.latitude,raw.longtitude))
                }

                bluetoothNameExpected = it.bluetooth_name

                if(polylines.size != 0){
                    view_map.visibility = VISIBLE
                    tv_mycar_scope_info.visibility = GONE
                    setMapData()
                }
            }
        })

        detailDriveHistoryViewModel.patchDrivingInfo.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is PatchDrivingInfoState.Loading -> {

                }
                is PatchDrivingInfoState.Success -> {
                    isActive = state.data.isActive
                    userCarId = state.data.userCarId
                    userCar = state.data.userCar


                    if(state.data.isActive){
                        if(!state.data.userCarId.isNullOrEmpty()){
                            tv_mycar.text = userCar?.carName
                            iv_corp.visibility = VISIBLE

                            if(userCar?.type == PERSONAL){
                                iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star2))
                                btn_choose_corp.visibility = GONE
                            }else{
                                iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star1))
                                btn_choose_corp.visibility = VISIBLE
                            }

                        }else{
                            // 미확정
                            tv_mycar.text = getString(R.string.pending)
                            userCar = null
                            iv_corp.visibility = GONE
                            btn_choose_corp.visibility = GONE

                        }
                    }else{
                        // 내 차가 아니에요
                        userCar = null
                        tv_mycar.text = getString(R.string.not_my_car)
                        iv_corp.visibility = GONE
                        btn_choose_corp.visibility = GONE

                    }
                }
                is PatchDrivingInfoState.Error -> {


                    if(state.code == 401){
                        logout()

                    }
                }
                is PatchDrivingInfoState.Empty -> {

                }
            }
        })

        detailDriveHistoryViewModel.getDrivingInfo.observe(this@DetailDriveHistoryActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetDrivingInfoState.Loading -> {

                }
                is GetDrivingInfoState.Success -> {
                    val getDrivingInfoResponse = state.data

                    tv_date.text = transformTimeToDate(getDrivingInfoResponse.endTime)
                    tv_distance.text = transferDistanceWithUnit(getDrivingInfoResponse.totalDistance)
                    tv_start_time_info.text = transformTimeToDateWithTime(getDrivingInfoResponse.startTime)
                    tv_end_time_info.text = transformTimeToDateWithTime(getDrivingInfoResponse.endTime)
                    tv_drive_time_info.text = transformSecondsToHHMMSS(getDrivingInfoResponse.totalTime)
                    tv_drive_distance_info.text = transferDistanceWithUnit(getDrivingInfoResponse.totalDistance)
                    tv_drive_verification_info.text = getDrivingInfoResponse.verification
                    tv_high_speed_driving_percent_info.text = transferNumWithRounds(getDrivingInfoResponse.highSpeedDrivingDistancePercentage).toString() + "%"
                    tv_low_speed_driving_percent_info.text = transferNumWithRounds(getDrivingInfoResponse.lowSpeedDrivingDistancePercentage).toString() + "%"
                    tv_max_speed_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.maxSpeed)
                    tv_high_speed_average_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.highSpeedDrivingAverageSpeed)
                    tv_low_speed_average_info.text = getSpeedWithDistanceUnit(getDrivingInfoResponse.lowSpeedDrivingAverageSpeed)
                    tv_rapid_start_count.text = getDrivingInfoResponse.rapidStartCount.toInt().toString() + "회"
                    tv_rapid_acc_count_info.text = getDrivingInfoResponse.rapidAccelerationCount.toInt().toString() + "회"
                    tv_rapid_stop_count_info.text = getDrivingInfoResponse.rapidStopCount.toInt().toString() + "회"
                    tv_rapid_desc_count_info.text = getDrivingInfoResponse.rapidDecelerationCount.toInt().toString() + "회"
                    et_memo.setText(getDrivingInfoResponse.memo)
                    pastMemo = getDrivingInfoResponse.memo?:""
                    state.data.type?.let{
                        tv_type.text = CorpType.valueOf(it).description
                    }

                    getDrivingInfoResponse.images?.let{
                        if(it.size > 0){
                            for(image in it){
                                addImageToLayout(url = image.url, image.id)
                            }
                        }
                    }

                    getDrivingInfoResponse.userCar?.let{
                        if(it.type.equals(CORPORATE)){
                            btn_choose_mycar.isClickable = false
                        }else{
                            btn_choose_mycar.isClickable = true
                        }
                    }


                    if(getDrivingInfoResponse.startAddress != null){
                        tv_start_address.text = getDrivingInfoResponse.startAddress.road?.name?:getDrivingInfoResponse.startAddress.parcel?.name?:"데이터 검토 중이에요"
                    }else{
                        tv_start_address.text = "데이터 검토 중이에요"
                    }
                    if(getDrivingInfoResponse.endAddress != null){
                        tv_end_address.text = getDrivingInfoResponse.endAddress.road?.name?:getDrivingInfoResponse.endAddress.parcel?.name?:"데이터 검토 중이에요"

                        if(!getDrivingInfoResponse.endAddress.places.isNullOrEmpty()){
                            tv_end_address_detail.text = getDrivingInfoResponse.endAddress.places?.get(0)?.name?:"데이터 검토 중이에요"

                            tv_end_address_detail.setOnClickListener {
                                startActivity(Intent(this@DetailDriveHistoryActivity, AllAddressActivity::class.java).putExtra("places",Gson().toJson(getDrivingInfoResponse.endAddress.places)))
                            }
                        }else{
                            tv_end_address_detail.text = "데이터 검토 중이에요"
                        }
                    }else{
                        tv_end_address.text = "데이터 검토 중이에요"
                        tv_end_address_detail.text = "데이터 검토 중이에요"

                    }

                    isActive = getDrivingInfoResponse.isActive
                    userCarId = getDrivingInfoResponse.userCarId

                    if(isActive){
                        if(!getDrivingInfoResponse.userCarId.isNullOrEmpty()){
                            // CarID

                            PreferenceUtil.getPref(this, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
                                if(it != ""){


                                    val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                                    val myCarsList: MutableList<MyCarsEntity> = GsonBuilder().serializeNulls().create().fromJson(it, type)

                                    val myCar = myCarsList.find { getDrivingInfoResponse.userCarId == it.id }
                                    tv_mycar.text = myCar?.name

                                    if(myCar?.type == PERSONAL){
                                        iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star2))
                                        btn_choose_corp.visibility = GONE
                                    }else{
                                        iv_corp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star1))
                                        btn_choose_corp.visibility = VISIBLE


                                    }
                                }
                            }

                        }else{
                            // 미확정
                            tv_mycar.text = getString(R.string.pending)
                            iv_corp.visibility = GONE
                            btn_choose_corp.visibility = GONE

                        }
                    }else{
                        // 내 차가 아니에요
                        tv_mycar.text = getString(R.string.not_my_car)
                        iv_corp.visibility = GONE
                        btn_choose_corp.visibility = GONE


                    }

                    if(isMyCarScope(getDrivingInfoResponse.endTime)){
                        tv_scope_date_mycar.text = transformDateTo30Dayslater(getDrivingInfoResponse.endTime)
                        btn_choose_mycar.isClickable = true
                    } else{
                        tv_scope_date_mycar.text = "변경 가능 기간이 지났어요."
                        btn_choose_mycar.isClickable = false
                    }


                    getDrivingInfoResponse.edits?.let{
                        var list:MutableList<EditHistoryEntity> = mutableListOf()

                        it.type?.let{
                            view_edit1.visibility = VISIBLE
                            tv_type.text = it

                            list.add(EditHistoryEntity(title = "주행 목적", past = getDrivingInfoResponse.type?:"데이터 없음", current = it))


                        }

                        it.startAddress?.let{
                            view_edit2.visibility = VISIBLE
                            tv_start_address.text = it

                            if(getDrivingInfoResponse.startAddress != null){
                                list.add(EditHistoryEntity(title = "출발지", past = getDrivingInfoResponse.startAddress.road?.name?:getDrivingInfoResponse.startAddress.parcel?.name?:"데이터 없음", current = it))
                            }else{
                                list.add(EditHistoryEntity(title = "출발지", past = "데이터 없음", current = it))
                            }

                        }

                        it.endAddress?.let{
                            view_edit3.visibility = VISIBLE
                            tv_end_address.text = it

                            if(getDrivingInfoResponse.endAddress != null){
                                list.add(EditHistoryEntity(title = "도착지", past = getDrivingInfoResponse.endAddress.road?.name?:getDrivingInfoResponse.endAddress.parcel?.name?:"데이터 없음", current = it))
                            }else{
                                list.add(EditHistoryEntity(title = "도착지", past = "데이터 없음", current = it))
                            }

                        }

                        it.place?.let{
                            view_edit4.visibility = VISIBLE
                            tv_end_address_detail.text = it

                            val result = if (!getDrivingInfoResponse.endAddress?.places.isNullOrEmpty()) {
                                getDrivingInfoResponse.endAddress?.places?.get(0)?.name?:"데이터 없음"
                            } else {
                                "데이터 없음"
                            }

                            list.add(EditHistoryEntity(title = "방문지", past = result, current = it))
                        }

                        it.totalDistance?.let{
                            view_edit5.visibility = VISIBLE
                            tv_drive_distance_info.text = transferDistanceWithUnit(it.toDouble())

                            list.add(EditHistoryEntity(title = "주행 거리", past = transferDistanceWithUnit(getDrivingInfoResponse.totalDistance), current = transferDistanceWithUnit(it.toDouble())))
                        }

                        if(it.excludeRecord){
                            tv_edit.text = "관리자에 의해 기록이 제외되었습니다."
                        }

                        if (it.type != null || it.totalDistance != null || it.startAddress != null || it.endAddress != null || it.place != null) {
                            view_edit_detail.visibility = VISIBLE
                        }


                        view_edit_detail.setOnClickListener {
                            if(list.size > 0)
                                showBottomSheetForEditHistory(list)
                        }


                    }

                }
                is GetDrivingInfoState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is GetDrivingInfoState.Empty -> {

                }
            }
        })
    }

    private fun setResources(){
        iv_tooltip_verification.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "데이터 인증이란?","주행 이력을 쌓는 방법에 따라 부여돼요.\n" +
                        "L3 = 안드로이드 오토, 애플 카플레이\n" +
                        "L2 = 블루투스\n" +
                        "L1 = 사용자 활동 자동 감지")
            }
        })

        iv_tooltip_low_speed.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "저속 주행이란?","저속 주행이란 0km/h 초과 40km/h 미만 속력으로 주행한 거리에요. 높을수록 좋아요!")
            }
        })

        iv_tooltip_high_speed.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "고속 주행이란?","80km/h 이상 150km/h 미만 사이의 속력으로 주행한 거리에요. 높을수록 좋아요!")
            }
        })

        iv_tooltip_rapid_desc.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "급감속이란?","초당 14km/h이상 감속 주행하고 속도가 6.0km/h 이상인 경우에요. 낮을수록 좋아요!")
            }
        })

        iv_tooltip_high_speed_average.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "고속 주행 평균 속력이란?","고속 주행 평균 속력은 80km/h 이상 150km/h 이하 속력으로 주행한 거리의 평균 속력이에요")
            }
        })

        iv_tooltip_low_speed_average.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "저속 주행 평균 속력이란?","저속 주행 평균 속력은 0km/h 초과 40km/h 미만 속력으로 주행한 거리의 평균 속력이에요")
            }
        })

        iv_tooltip_rapid_acc.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "급가속이란?","10km/h 초과 속도에서 초당 10km/h 이상 가속 주행한 경우에요. 낮을수록 좋아요!")
            }
        })

        iv_tooltip_rapid_start.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "급출발이란?","5.0km/h 이하 속도에서 출발하여 초당 10km/h이상 가속 주행한 경우에요. 낮을수록 좋아요!")
            }
        })

        iv_tooltip_rapid_stop.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@DetailDriveHistoryActivity, "급정지란?","초당 14km/h이상 감속 주행하고 속도가 5.0km/h 이하인 경우에요. 낮을수록 좋아요!")
            }
        })

        btn_add_image.setOnClickListener {
            if(layout_drive_image.childCount < 6){
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) { // API 29 이하
                    // 권한 체크
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        openGallery() // 권한이 있으면 크롭 시작
                    } else {
                        // 권한이 없으면 요청
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE)
                    }
                } else {
                    // API 30 이상은 권한 체크 없이 바로 크롭 시작
                    openGallery()
                }
            }else{
                Toast.makeText(this, "최대 5개까지 등록할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }


        btn_back.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                if(pastMemo.equals(currentMemo)){
                    val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
                    intent.putExtra("isActive",isActive)
                    intent.putExtra("userCarId",userCarId)
                    intent.putExtra("trackingId",tracking_id)
                    intent.putExtra("userCar",Gson().toJson(userCar))
                    setResult(RESULT_OK, intent)
                    finish()
                }else{
                    detailDriveHistoryViewModel.patchMemo(et_memo.text.toString(), tracking_id)
                }
            }
        })

        btn_choose_mycar.setOnClickListener {
            showBottomSheetForEditCar()
        }

        detailDriveHistoryViewModel.getMapData(tracking_id)
        detailDriveHistoryViewModel.getDrivingInfo(tracking_id)
    }


    private fun setMapData() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->

            /**
             * 폴리라인 추가
             */
            val polylineOptions = PolylineOptions()
                .clickable(true)
                .addAll(polylines)
                .addSpan(
                    StyleSpan(
                        StrokeStyle.gradientBuilder(
                            resources.getColor(R.color.map_start),
                            resources.getColor(R.color.map_end)
                        ).build()
                    )
                )
            googleMap.addPolyline(polylineOptions)

            /**
             * 지도의 Zoom 정도 설정
             */
            val boundsBuilder = LatLngBounds.Builder()
            for (point in polylines) {
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()
            val padding = 100  // 지도의 가장자리에 여유 공간을 주기 위한 패딩 (px 단위)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))


            googleMap.setOnMapLoadedCallback {
                try {

                    /**
                     * 마커 추가
                     */
                    val markerPosition = LatLng(polylines[0].latitude, polylines[0].longitude)
                    currentMarker = googleMap.addMarker(
                        MarkerOptions().position(markerPosition).title("marker")
                    )

                    /**
                     * 마커 애니메이션 추가 및 애니메이션
                     */
                    moveMarkerAlongPolyline(googleMap, 0)


                    googleMap.setOnCameraMoveStartedListener { reason ->
                        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            isCameraMoving = true
                            currentAnimator?.pause() // 애니메이션 일시 중지
                        }
                    }

                    googleMap.setOnCameraIdleListener {
                        if (isCameraMoving) {
                            isCameraMoving = false
                            currentAnimator?.start() // 애니메이션 재개
                        }
                    }
                }catch (e:Exception){

                }
            }
        }
    }
    private fun moveMarkerAlongPolyline(googleMap: GoogleMap, index: Int) {
        val startPosition = polylines[index]
        val endPosition = polylines[(index + 1) % polylines.size]
        val zoomLevel = googleMap.cameraPosition.zoom

        /**
         * 줌 레벨에 따라 애니메이션 지속 시간 설정
         */
        var duration = 10L
        if (zoomLevel > 15) {
            duration = 120L
        } else if (zoomLevel < 15) {
            duration = 10L
        }

        /**
         * 이전 애니메이션 취소
         */
        currentAnimator?.cancel()


        /**
         * 마커 애니메이션 추가
         */
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = duration
        valueAnimator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            val newPosition = LatLng(
                startPosition.latitude + (endPosition.latitude - startPosition.latitude) * fraction,
                startPosition.longitude + (endPosition.longitude - startPosition.longitude) * fraction
            )
            // 마커의 위치만 업데이트
            currentMarker?.position = newPosition
        }
        valueAnimator.start()
        currentAnimator = valueAnimator


        /**
         * 마커 애니메이션 종료 리스너
         */
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // 애니메이터 초기화
                currentAnimator = null
            }
        })

        /**
         * 다음 지점으로 이동
         */
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val nextIndex = (index + 1) % polylines.size
            if (nextIndex < polylines.size - 1) {
                moveMarkerAlongPolyline(googleMap, nextIndex)
            }
        }, duration)
    }

    override fun onDestroy() {
        super.onDestroy()

        // 현재 진행 중인 애니메이터가 있으면 취소
        currentAnimator?.cancel()
        currentAnimator = null

        // 현재 마커가 있으면 제거
        currentMarker?.remove()
        currentMarker = null
    }

    // 두 지점 사이의 회전 각도를 계산하는 함수
    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizedEndDegrees = end - start
        val direction = if (normalizedEndDegrees > 180 || normalizedEndDegrees < -180) -1 else 1
        return start + direction * fraction * 360
    }

    // LatLng 객체 사이의 보간을 담당하는 인터페이스
    interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng

        class Linear : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                val lng = (b.longitude - a.longitude) * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private fun transformSecondsToHHMMSS(seconds:Double):String{
        val hours = seconds.toInt() / 3600
        val minutes = (seconds.toInt() % 3600) / 60
        val secs = seconds.toInt() % 60

        return "${hours}시간 ${minutes}분 ${secs}초"
    }


    fun transformDateTo30Dayslater(isoDate: String): String {
        // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환
        val zonedDateTime = ZonedDateTime.parse(isoDate).withZoneSameInstant(ZoneId.systemDefault())

        // 30일 후의 날짜 계산
        val newZonedDateTime = zonedDateTime.plusDays(30)

        // 원하는 형식의 DateTimeFormatter 생성
        val formatter = DateTimeFormatter.ofPattern("MM월 dd일", Locale.KOREAN)

        // 포맷된 문자열 반환
        val formattedDate = newZonedDateTime.format(formatter)
        return "$formattedDate" + "까지만 변경 가능해요"
    }

    fun isMyCarScope(isoDate: String): Boolean {
        // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환 (로컬 시간대로 변경)
        val zonedDateTime = ZonedDateTime.parse(isoDate).withZoneSameInstant(ZoneId.systemDefault())

        // 현재 로컬 날짜와 시간
        val now = ZonedDateTime.now(ZoneId.systemDefault())

        // 주어진 날짜로부터 31일 후의 날짜 계산
        val dateAfter30Days = zonedDateTime.plusDays(31).withHour(0).withMinute(0).withSecond(0).withNano(0)
        // 현재 날짜가 주어진 날짜와 31일 후의 날짜 사이에 있는지 확인
        return now.isAfter(zonedDateTime) && now.isBefore(dateAfter30Days)
    }


    override fun onBackPressed() {
        if(pastMemo.equals(currentMemo)){
            val intent = Intent(this@DetailDriveHistoryActivity, MyDriveHistoryActivity::class.java)
            intent.putExtra("isActive",isActive)
            intent.putExtra("userCarId",userCarId)
            intent.putExtra("trackingId",tracking_id)
            intent.putExtra("userCar",Gson().toJson(userCar))
            setResult(RESULT_OK, intent)
            finish()
        }else{
            detailDriveHistoryViewModel.patchMemo(et_memo.text.toString(), tracking_id)
        }
    }

    fun showBottomSheetForEditCar() {
        PreferenceUtil.getPref(this, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
            if(it != ""){
                // Create a BottomSheetDialog
                val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)

                // Inflate the layout
                val bottomSheetView = this.layoutInflater.inflate(R.layout.dialog_connected_bluetooth, null)
                val tv_connected_bluetooth:TextView = bottomSheetView.findViewById(R.id.tv_connected_bluetooth)

                if(bluetoothNameExpected.isNullOrEmpty()){
                    tv_connected_bluetooth.visibility = GONE
                }else{
                    tv_connected_bluetooth.visibility = VISIBLE
                    tv_connected_bluetooth.text = "연결했던 블루투스: " + bluetoothNameExpected

                    if(tv_mycar.text.equals(getString(R.string.pending))){
                        tv_connected_bluetooth.visibility = VISIBLE
                    }else{
                        tv_connected_bluetooth.visibility = GONE
                    }
                }

                val rv_registered_car = bottomSheetView.findViewById<RecyclerView>(R.id.rv_registered_car)

                rv_registered_car.layoutManager = LinearLayoutManager(this)
                val dividerItemDecoration = DividerItemDecoration(this, R.color.white_op_100, this.dpToPx(8f)) // 색상 리소스와 구분선 높이 설정
                rv_registered_car.addItemDecoration(dividerItemDecoration)

                val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                val myCarsList: MutableList<MyCarsEntity> = GsonBuilder().serializeNulls().create().fromJson(it, type)

                for(car in myCarsList){
                    car.isActive = true
                }

                myCarsList.add(MyCarsEntity(null,null,null,null,null,false))
                myCarsList.add(MyCarsEntity(null,null,null,null,null,true))


                rv_registered_car.adapter = MyCarEntitiesAdapter(context = this, mycarEntities = myCarsList, tracking_id = tracking_id, viewModel = detailDriveHistoryViewModel, bottomSheetDialog, isActive, userCarId )

                // Set the content view of the dialog
                bottomSheetDialog.setContentView(bottomSheetView)

                // Show the dialog
                bottomSheetDialog.show()
            }
        }
    }

    fun showBottomSheetForChooseCorp(userCarId: String) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)

        // Inflate the layout
        val bottomSheetView = this.layoutInflater.inflate(R.layout.dialog_choose_corp, null)

        val rv_choose_corp = bottomSheetView.findViewById<RecyclerView>(R.id.rv_choose_corp)

        rv_choose_corp.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, R.color.white_op_100, this.dpToPx(8f)) // 색상 리소스와 구분선 높이 설정
        rv_choose_corp.addItemDecoration(dividerItemDecoration)

        rv_choose_corp.adapter = ChooseCorpAdapter(context = this, corpList = mutableListOf(CorpType.COMMUTE.description, CorpType.WORK.description, CorpType.NON_WORK.description), bottomSheetDialog, detailDriveHistoryViewModel, tracking_id, userCarId,tv_type.text.toString())

        // Set the content view of the dialog
        bottomSheetDialog.setContentView(bottomSheetView)

        // Show the dialog
        bottomSheetDialog.show()
    }

    class MyCarEntitiesAdapter(
        private val context: Context,
        private val mycarEntities: MutableList<MyCarsEntity>,
        private val tracking_id:String,
        private val viewModel: DetailDriveHistoryViewModel,
        private val bottomSheetDialog:BottomSheetDialog,
        private val isActive:Boolean,
        private val userCarId:String?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_connected_bluetooth, parent, false)
            return MyCarEntitiesHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MyCarEntitiesHolder) {
                val myCarsEntity = mycarEntities[position]

                if(myCarsEntity.isActive!!){
                    if(!myCarsEntity.name.isNullOrEmpty()){
                        if(isActive){
                            if(!userCarId.isNullOrEmpty()) {
                                if(myCarsEntity.id.equals(userCarId)){
                                    holder.layout_car.isSelected = true
                                    TextViewCompat.setTextAppearance(holder.tv_car_name, R.style.car_selected)
                                    TextViewCompat.setTextAppearance(holder.tv_car_no, R.style.car_no_selected)
                                }
                            }
                        }

                        myCarsEntity.type?.let{
                            if(it.equals(CORPORATE)){
                                holder.iv_corp.visibility = VISIBLE
                            }else{
                                holder.iv_corp.visibility = GONE
                            }
                        }

                        holder.layout_name.visibility = VISIBLE
                        holder.tv_car_no.visibility = VISIBLE
                        holder.tv_no_mycar.visibility = GONE

                        holder.tv_car_name.text = myCarsEntity.name

                        holder.layout_car.setOnClickListener {
                            myCarsEntity.type?.let{
                                if(it.equals(CORPORATE)){
                                    (context as DetailDriveHistoryActivity).showBottomSheetForChooseCorp(myCarsEntity.id!!)
                                    bottomSheetDialog.dismiss()
                                }else{
                                    viewModel.patchDrivingInfo(true, myCarsEntity.id, tracking_id)
                                    bottomSheetDialog.dismiss()
                                }
                            }
                        }

                        holder.tv_car_no.text = myCarsEntity.number
                    }else{
                        if(isActive) {
                            if (userCarId.isNullOrEmpty()) {
                                holder.layout_car.isSelected = true
                                TextViewCompat.setTextAppearance(holder.tv_no_mycar, R.style.car_selected)

                            }
                        }
                        // 미확정
                        holder.layout_name.visibility = GONE
                        holder.tv_car_no.visibility = GONE
                        holder.tv_no_mycar.visibility = VISIBLE

                        holder.tv_no_mycar.text = context.getString(R.string.pending)

                        holder.layout_car.setOnClickListener {
                            viewModel.patchDrivingInfo(true, null, tracking_id)
                            bottomSheetDialog.dismiss()
                        }
                        holder.iv_corp.visibility = GONE
                    }
                }else{
                    // 내 차가 아니에요
                    holder.layout_name.visibility = GONE
                    holder.tv_car_no.visibility = GONE
                    holder.tv_no_mycar.visibility = VISIBLE

                    holder.tv_no_mycar.text = context.getString(R.string.not_my_car)
                    holder.layout_car.setOnClickListener {
                        viewModel.patchDrivingInfo(false, null, tracking_id)
                        bottomSheetDialog.dismiss()
                    }

                    if(!isActive){
                        holder.layout_car.isSelected = true
                        TextViewCompat.setTextAppearance(holder.tv_no_mycar, R.style.car_selected)
                    }
                    holder.iv_corp.visibility = GONE

                }
            }
        }

        override fun getItemCount(): Int {
            return mycarEntities.size
        }
    }

    class MyCarEntitiesHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_car_name:TextView = view.findViewById(R.id.tv_car_name)
        val tv_car_no:TextView = view.findViewById(R.id.tv_car_no)
        val layout_name:LinearLayout = view.findViewById(R.id.layout_name)
        val tv_no_mycar:TextView = view.findViewById(R.id.tv_no_mycar)
        val layout_car:LinearLayout = view.findViewById(R.id.layout_car)
        val iv_corp:ImageView = view.findViewById(R.id.iv_corp)
    }

    class ChooseCorpAdapter(
        private val context: Context,
        private val corpList: MutableList<String>,
        private val bottomSheetDialog: BottomSheetDialog,
        private val viewModel: DetailDriveHistoryViewModel,
        private val tracking_id: String,
        private val userCarId: String? = null,
        private val type:String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_choose_corp, parent, false)
            return ChooseCorpHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ChooseCorpHolder) {
                val corp = corpList.get(position)
                holder.tv_corp.text = corp

//                if(holder.tv_corp.text.equals(type)){
//                    holder.layout_car.isSelected = true
//                    TextViewCompat.setTextAppearance(holder.tv_corp, R.style.type_selected)
//                }

                holder.layout_car.setOnClickListener {
                    if(userCarId != null){
                        CustomDialog(context, "이동수단 변경", "이동 수단을 법인차로 저장하면 더이상\n변경할 수 없습니다. 변경하시겠습니까?", "변경","취소",  object : CustomDialog.DialogCallback{
                            override fun onConfirm() {
                                viewModel.patchCorpType(userCarId, true, getNameFromDescription(corp), tracking_id = tracking_id)
                                bottomSheetDialog.dismiss()
                            }

                            override fun onCancel() {
                                bottomSheetDialog.dismiss()
                            }

                        }).show()
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return corpList.size
        }


        fun getNameFromDescription(description: String): String {
            return CorpType.values().find { it.description == description }?.name!!
        }

    }

    class ChooseCorpHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout_car:LinearLayout = view.findViewById(R.id.layout_car)
        val tv_corp:TextView = view.findViewById(R.id.tv_corp)
    }


    fun getMatchingTitle(vWorldResponse: VWorldResponse, vWorldDetailResponse: VWorldDetailResponse): String {
        for (item in vWorldDetailResponse.response.result.items) {
            val title = item.title
            if (keywords.any { keyword -> title.contains(keyword) }) {
                return title // 매칭되는 title을 찾으면 바로 반환
            }
        }

        return vWorldResponse.response.result.first().text // 매칭되는 title이 없으면 null 반환
    }

    private val getMultipleImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null) {
            var imageParts:MutableList<MultipartBody.Part> = mutableListOf()

            Log.d("testestsetest","testestestesset size :: " + uris.size)
            Log.d("testestsetest","testestestesset childCount :: " + (5-(layout_drive_image.childCount-1)))

            /**
             * (5-(layout_drive_image.childCount-1)) 는 최대로 업로드 할 수 있는 갯수
             */

            if(uris.size <= (5-(layout_drive_image.childCount-1))){
                uris.forEach { uri ->
                    val bitmap: Bitmap
                    val selectedImageUri: Uri = uri

                    selectedImageUri.let {
                        if (Build.VERSION.SDK_INT < 28) {
                            bitmap = MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                selectedImageUri
                            )
                        } else {
                            val source = ImageDecoder.createSource(
                                this.contentResolver,
                                selectedImageUri
                            )
                            bitmap = ImageDecoder.decodeBitmap(source)
                        }
                    }

                    // 임시 파일 생성
                    val imageFile = File.createTempFile("temp_image", ".jpg", cacheDir).apply {
                        outputStream().use { output ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                        }
                    }

                    // MultipartBody.Part 생성
                    val imagePart = MultipartBody.Part.createFormData(
                        "images.create", imageFile.name, imageFile.asRequestBody("image/jpeg".toMediaType())
                    )

                    // 이미지 리스트로 추가
                    imageParts.add(imagePart)
                }

                apiService().patchDrivingInfo("Bearer " + PreferenceUtil.getPref(this,  PreferenceUtil.ACCESS_TOKEN, "")!!, drivingId = tracking_id, null, imageParts) .enqueue(object :
                    Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(response.code() == 200 || response.code() == 201){
                            showCustomToast(this@DetailDriveHistoryActivity, "저장 되었습니다.")

                            val jsonString = response.body()?.string()
                            val getDrivingInfoResponse = GsonBuilder().serializeNulls().create().fromJson(jsonString, GetDrivingInfoResponse::class.java)

                            while(layout_drive_image.childCount > 1) {
                                layout_drive_image.removeViewAt(layout_drive_image.childCount-1)
                            }


                            getDrivingInfoResponse.images?.let{
                                if(it.size > 0){
                                    for(image in it){
                                        addImageToLayout(url = image.url, image.id)
                                    }
                                }else{
                                    tv_tv_add_image.text = "0/5"
                                }
                            }

                        }else if(response.code() == 401){
                            logout()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                    }

                })
            }else{
                Toast.makeText(this, "최대 5개까지 등록할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 갤러리에서 이미지 선택하기
    private fun openGallery() {
        getMultipleImages.launch("image/*") // 복수 이미지 선택
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 권한이 허용된 경우 크롭 시작
                openGallery()
            } else {
                // 권한이 거부된 경우 사용자에게 알림
                showCustomToast(this, "권한이 필요합니다.")
            }
        }
    }

    private fun addImageToLayout(url:String, id:String){
        val constraintLayoutView = layoutInflater.inflate(R.layout.item_drive_image, layout_drive_image, false)

        // Find the ImageView within the newly inflated ConstraintLayout
        val iv_drive_image = constraintLayoutView.findViewById<ImageView>(R.id.iv_drive_image)
        val btn_delete_image = constraintLayoutView.findViewById<ImageView>(R.id.btn_delete_image)

        btn_delete_image.setOnClickListener {
            deleteImageToLayout(id)
        }

        // Load the image into the ImageView with Glide
        Glide.with(this@DetailDriveHistoryActivity)
            .asBitmap()
            .load(url)
            .transform(RoundedCornersTransformation(5, 0))  // 5dp의 반경
            .into(iv_drive_image)

        // Add the inflated ConstraintLayout to the parent LinearLayout
        layout_drive_image.addView(constraintLayoutView)

        tv_tv_add_image.text = (layout_drive_image.childCount - 1).toString() + "/5"
    }

    private fun deleteImageToLayout(id:String){
        detailDriveHistoryViewModel.patchImages(listOf(DeleteImage(id)), tracking_id)
    }


}