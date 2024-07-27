package com.charancha.drive.activity

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.request.PostMyCarRequest
import com.charancha.drive.retrofit.response.GetMyCarInfoResponse
import com.charancha.drive.retrofit.response.PostMyCarResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterCarActivity: BaseActivity() {
    lateinit var view_register_percent1: View
    lateinit var view_register_percent2:View
    lateinit var view_register_percent3:View
    lateinit var ib_arrow_register_car:ImageButton
    lateinit var tv_register_car_hint: TextView
    lateinit var et_register_car: EditText
    lateinit var btn_next:ConstraintLayout
    lateinit var tv_register_car:TextView
    lateinit var tv_register_car_caution:TextView
    lateinit var layout_after_inquiry:ConstraintLayout
    lateinit var layout_before_inquiry:ConstraintLayout
    lateinit var tv_car_no:TextView
    lateinit var tv_car_owner:TextView
    lateinit var tv_car_id:TextView
    lateinit var et_car_model_name:EditText
    lateinit var et_car_year:EditText
    lateinit var tv_car_fuel:TextView
    lateinit var tv_confirm:TextView
    lateinit var postMyCarResponse:PostMyCarResponse

    lateinit var constraint_fuel_select:ConstraintLayout
    lateinit var layout_fuel_select: CoordinatorLayout
    lateinit var persistent_bottom_sheet: LinearLayout
    lateinit var behavior: BottomSheetBehavior<LinearLayout>
    lateinit var listview:ListView



    lateinit var getPostMyCarResult:ActivityResultLauncher<Intent>


    var no = 0

    var carNo:String? = null
    var carOwner:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_car)

        init()

    }

    fun init(){
        setResources()
        persistentBottomSheetEvent()

        getPostMyCarResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                val jsonString = it.data?.getStringExtra("response")

                val gson = Gson()
                postMyCarResponse = gson.fromJson(jsonString, PostMyCarResponse::class.java)

                setAfterInquiry()
            } else{
                no--
            }
        }
    }

    fun setResources(){
        view_register_percent1 = findViewById(R.id.view_register_percent1)
        view_register_percent2 = findViewById(R.id.view_register_percent2)
        view_register_percent3 = findViewById(R.id.view_register_percent3)

        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)
        tv_register_car_hint = findViewById(R.id.tv_register_car_hint)
        et_register_car = findViewById(R.id.et_register_car)
        listview = findViewById(R.id.listView)


        tv_register_car = findViewById(R.id.tv_register_car)
        tv_register_car_caution = findViewById(R.id.tv_register_car_caution)

        layout_after_inquiry = findViewById(R.id.layout_after_inquiry)
        layout_before_inquiry = findViewById(R.id.layout_before_inquiry)
        tv_car_no = findViewById(R.id.tv_car_no)
        tv_car_owner = findViewById(R.id.tv_car_owner)
        tv_car_id = findViewById(R.id.tv_car_id)
        et_car_model_name = findViewById(R.id.et_car_model_name)
        et_car_year = findViewById(R.id.et_car_year)
        tv_car_fuel = findViewById(R.id.tv_car_fuel)
        tv_confirm = findViewById(R.id.tv_confirm)

        et_car_model_name.setOnFocusChangeListener { view, b ->
            if(b){
                et_car_model_name.hint = ""
            }else{
                et_car_model_name.hint = postMyCarResponse.carName
            }
        }

        et_car_year.setOnFocusChangeListener { view, b ->
            if(b){
                et_car_year.hint = ""
            }else{
                et_car_year.hint = postMyCarResponse.carYear
            }
        }

        btn_next = findViewById(R.id.btn_next)
        btn_next.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                when(no){
                    0 -> {
                        carNo = et_register_car.text.toString()
                        setCarOwnerPage()
                    }

                    1 -> {
                        carOwner = et_register_car.text.toString()
                        getPostMyCarResult.launch(Intent(this@RegisterCarActivity, LoadCarInfoActivity::class.java).putExtra("carNo",carNo).putExtra("carOwner",carOwner))

                        btn_next.isSelected = false
                        btn_next.isClickable = false
                    }

                    2 -> {
                        var carYear = postMyCarResponse.carYear
                        var carName = postMyCarResponse.carName

                        if(et_car_year.text.toString().isNotEmpty()){
                            carYear = et_car_year.text.toString()
                        }

                        if(et_car_model_name.text.toString().isNotEmpty()){
                            carName = et_car_model_name.text.toString()
                        }


                        val gson = Gson()
                        val jsonParam =
                            gson.toJson(PostMyCarRequest(
                                licensePlateNumber=tv_car_id.text.toString(),
                                ownerName=tv_car_owner.text.toString(),
                                vehicleIdentificationNumber=tv_car_no.text.toString(),
                                carYear= carYear.toInt(),
                                carName = carName,
                                fuel = tv_car_fuel.text.toString()
                            ))

                        apiService().postMyCar(
                            "Bearer " + PreferenceUtil.getPref(this@RegisterCarActivity,  PreferenceUtil.ACCESS_TOKEN, ""), jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
                            Callback<ResponseBody>{
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if(response.code() == 201 || response.code() == 200){
                                    val getMyCarInfoResponse = Gson().fromJson(
                                        response.body()?.string(),
                                        GetMyCarInfoResponse::class.java
                                    )
                                    PreferenceUtil.putPref(this@RegisterCarActivity, PreferenceUtil.USER_CARID, getMyCarInfoResponse.id)
                                    PreferenceUtil.putPref(this@RegisterCarActivity,  PreferenceUtil.KM_MILE, "km")
                                    startActivity(Intent(this@RegisterCarActivity, MainActivity::class.java).addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK))
                                    finish()
                                }else{
                                    showCustomToast(this@RegisterCarActivity,"차량 등록에 실패했습니다.")

                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                showCustomToast(this@RegisterCarActivity,"차량 등록에 실패했습니다.")

                            }

                        })
                    }

                }

                no++
            }
        })


        view_register_percent1.isSelected = true
        et_register_car.setOnFocusChangeListener { view, b ->
            if(b){
                tv_register_car_hint.visibility = GONE
            }else{
                tv_register_car_hint.visibility = VISIBLE

            }
        }

        ib_arrow_register_car.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                no--
                when(no){
                    -1 -> {
                        finish()
                    }

                    0 -> {
                        setCarNoPage()
                    }

                    1 -> {
                        setCarOwnerPage()
                    }

                    2 -> {


                    }

                    3 -> {

                    }
                }            }

        })

        et_register_car.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let{
                    if(p0.length>=7){
                        btn_next.isSelected = true
                        btn_next.isClickable = true
                    }else{
                        btn_next.isSelected = false
                        btn_next.isClickable = false
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        et_car_year.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let{
                    if(p0.length>=4 || p0.length==0){
                        if(tv_car_fuel.text.equals("선택하세요.")){
                            btn_next.isSelected = false
                            btn_next.isClickable = false
                        }else{
                            btn_next.isSelected = true
                            btn_next.isClickable = true
                        }
                    }else{
                        btn_next.isSelected = false
                        btn_next.isClickable = false
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })


        constraint_fuel_select = findViewById(R.id.constraint_fuel_select)
        layout_fuel_select = findViewById(R.id.layout_fuel_select)
        persistent_bottom_sheet = findViewById(R.id.persistent_bottom_sheet)

        constraint_fuel_select.setOnClickListener {
            layout_fuel_select.visibility = VISIBLE
        }

        layout_fuel_select.setOnClickListener {
            layout_fuel_select.visibility = GONE
        }

        val itemList: MutableList<String?> = ArrayList()

        tv_car_fuel.text = "선택하세요."

        // 데이터 추가
        itemList.add("가솔린")
        itemList.add("디젤")
        itemList.add("LPG")
        itemList.add("전기")
        itemList.add("수소")
        itemList.add("CNG")
        itemList.add("가솔린+LPG")
        itemList.add("가솔린+CNG")
        itemList.add("가솔린+전기")
        itemList.add("디젤+전기")
        itemList.add("LPG+전기")
        itemList.add("기타")


        // adapter 생성
        val adapter = ArrayAdapter(this, R.layout.edit_fuel_textview, R.id.tv_fuel, itemList)


        // listView에 adapter 연결
        listview.adapter = adapter
        listview.setOnItemClickListener { parent, view, position, l ->
            val fuel = parent.getItemAtPosition(position) as String
            tv_car_fuel.text = fuel
            btn_next.isClickable = true
            btn_next.isSelected = true

            layout_fuel_select.visibility = GONE

        }

    }

    /**
     * 1. 최대 9자리까지 입력 가능
     * 2. 화면 진입 시 input에 포커스
     * 3.
     */
    fun setCarNoPage(){
        tv_register_car.text = resources.getString(R.string.register_car_no_title)
        tv_register_car_caution.text = resources.getString(R.string.register_car_no_errormessage)
        et_register_car.text = null
        et_register_car.hint = "123가1234"
        carNo = null

        view_register_percent1.isSelected = true
        view_register_percent2.isSelected = false
        view_register_percent3.isSelected = false

        showKeyboard(et_register_car)

        et_register_car.filters = arrayOf(InputFilter.LengthFilter(9))


        et_register_car.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let{
                    if(p0.length>=7){
                        btn_next.isSelected = true
                        btn_next.isClickable = true
                    }else{
                        btn_next.isSelected = false
                        btn_next.isClickable = false
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


    }

    /**
     * 1. 최대 17자리까지 입력 가능
     * 2. 화면 진입 시 input에 focus
     */
    fun setCarOwnerPage(){
        layout_after_inquiry.visibility = GONE
        layout_before_inquiry.visibility = VISIBLE

        tv_register_car.text = resources.getString(R.string.register_car_owner_title)
        tv_register_car_caution.text = resources.getString(R.string.register_car_owner_errormessage)
        et_register_car.text = null
        et_register_car.hint = "김마일"
        carOwner = null

        view_register_percent1.isSelected = true
        view_register_percent2.isSelected = true
        view_register_percent3.isSelected = false

        showKeyboard(et_register_car)

        et_register_car.filters = arrayOf(InputFilter.LengthFilter(17))

        et_register_car.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let{
                    if(p0.length>=2){
                        btn_next.isSelected = true
                        btn_next.isClickable = true
                    }else{
                        btn_next.isSelected = false
                        btn_next.isClickable = false
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    fun setAfterInquiry(){
        layout_before_inquiry.visibility = GONE
        layout_after_inquiry.visibility = VISIBLE

        tv_car_fuel.text = "선택하세요."
        tv_car_owner.text = postMyCarResponse.ownerName
        tv_car_id.text = postMyCarResponse.vehicleIdentificationNumber
        tv_car_no.text = postMyCarResponse.licensePlateNumber
        et_car_year.hint = postMyCarResponse.carYear
        et_car_model_name.hint = postMyCarResponse.carName

        view_register_percent1.isSelected = true
        view_register_percent2.isSelected = true
        view_register_percent3.isSelected = true

        tv_confirm.text = "확인했어요"
        tv_confirm.isSelected = false
    }



    override fun onBackPressed() {
        no--
        when(no){
            -1 -> {
                finish()
            }

            0 -> {
                setCarNoPage()
            }

            1 -> {
                setCarOwnerPage()
            }

            2 -> {


            }

            3 -> {

            }
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
                        layout_fuel_select.visibility = GONE
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
}