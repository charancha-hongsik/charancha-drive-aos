//package com.milelog.activity
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.view.View.GONE
//import android.view.View.VISIBLE
//import android.widget.*
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.coordinatorlayout.widget.CoordinatorLayout
//import com.milelog.CustomDialog
//import com.milelog.PreferenceUtil
//import com.milelog.R
//import com.milelog.retrofit.request.EditMyCarRequest
//import com.milelog.retrofit.response.GetMyCarInfoResponse
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.RequestBody.Companion.toRequestBody
//import okhttp3.ResponseBody
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.lang.reflect.Type
//
//
//class EditCarInfoActivity: BaseRefreshActivity() {
//    lateinit var tv_car_no: TextView
//    lateinit var tv_car_owner:TextView
//    lateinit var tv_car_id:TextView
//    lateinit var et_car_model_name: EditText
//    lateinit var layout_car_name:ConstraintLayout
//    lateinit var et_car_year:EditText
//    lateinit var layout_car_year:ConstraintLayout
//    lateinit var tv_car_fuel:TextView
//    lateinit var layout_delete:ConstraintLayout
//    lateinit var layout_edit:ConstraintLayout
//    lateinit var getMyCarInfoResponse: GetMyCarInfoResponse
//    lateinit var ib_close: ImageView
//    lateinit var constraint_fuel_select:ConstraintLayout
//    lateinit var layout_fuel_select:CoordinatorLayout
//    lateinit var persistent_bottom_sheet: LinearLayout
//    lateinit var behavior: BottomSheetBehavior<LinearLayout>
//    lateinit var listview:ListView
//    lateinit var car_id:String
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_editcarinfo)
//
//        init()
//        setInfo()
//        persistentBottomSheetEvent()
//    }
//
//    fun init(){
//        car_id = intent.getStringExtra("car_id")!!
//        tv_car_no = findViewById(R.id.tv_car_no)
//        tv_car_owner = findViewById(R.id.tv_car_owner)
//        tv_car_id = findViewById(R.id.tv_car_id)
//        et_car_model_name = findViewById(R.id.et_car_model_name)
//        layout_car_name = findViewById(R.id.layout_car_name)
//        et_car_year = findViewById(R.id.et_car_year)
//        layout_car_year = findViewById(R.id.layout_car_year)
//        tv_car_fuel = findViewById(R.id.tv_car_fuel)
//        layout_delete = findViewById(R.id.layout_delete)
//        layout_edit = findViewById(R.id.layout_edit)
//        ib_close = findViewById(R.id.ib_close)
//        constraint_fuel_select = findViewById(R.id.constraint_fuel_select)
//        layout_fuel_select = findViewById(R.id.layout_fuel_select)
//        persistent_bottom_sheet = findViewById(R.id.persistent_bottom_sheet)
//        listview = findViewById(R.id.listView)
//
//        constraint_fuel_select.setOnClickListener {
//            layout_fuel_select.visibility = VISIBLE
//        }
//
//        layout_fuel_select.setOnClickListener {
//            layout_fuel_select.visibility = GONE
//        }
//
//        ib_close.setOnClickListener {
//            finish()
//        }
//
//        et_car_model_name.setOnFocusChangeListener { view, b ->
//            if(b){
//                et_car_model_name.hint = ""
//            }else{
//                et_car_model_name.hint = getMyCarInfoResponse.carName
//
//            }
//        }
//
//        layout_car_name.setOnClickListener(object:OnSingleClickListener(){
//            override fun onSingleClick(v: View?) {
//                et_car_model_name.performClick()
//            }
//
//        })
//
//        et_car_year.setOnFocusChangeListener { view, b ->
//            if(b){
//                et_car_year.hint = ""
//            }else{
//                et_car_year.hint = getMyCarInfoResponse.modelYear
//
//            }
//        }
//
//        layout_car_year.setOnClickListener(object: OnSingleClickListener(){
//            override fun onSingleClick(v: View?) {
//                et_car_year.performClick()
//            }
//
//        })
//
//        layout_edit.setOnClickListener(object: OnSingleClickListener(){
//            override fun onSingleClick(v: View?) {
//                var modelYear = getMyCarInfoResponse.modelYear
//                var carModelName = getMyCarInfoResponse.carName
//
//                if(et_car_year.text.toString().isNotEmpty()){
//                    modelYear = et_car_year.text.toString()
//                }
//                if(et_car_model_name.text.toString().isNotEmpty()){
//                    carModelName = et_car_model_name.text.toString()
//                }
//
//                val gson = Gson()
//                val jsonParam =
//                    gson.toJson(EditMyCarRequest(getMyCarInfoResponse.licensePlateNumber, getMyCarInfoResponse.ownerName, modelYear, carModelName, tv_car_fuel.text.toString()))
//
//                apiService().patchCarInfoByCarId("Bearer " + PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, getMyCarInfoResponse.id, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :Callback<ResponseBody>{
//                    override fun onResponse(
//                        call: Call<ResponseBody>,
//                        response: Response<ResponseBody>
//                    ) {
//                        if(response.code() == 200 || response.code() == 201){
//                            showCustomToast(this@EditCarInfoActivity, "내 차 정보가 수정되었어요.")
//
//                            finish()
//                        }else if(response.code() == 401){
//                            logout()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//
//                    }
//
//                })
//            }
//
//        })
//
//        layout_delete.setOnClickListener(object: OnSingleClickListener(){
//            override fun onSingleClick(v: View?) {
//                CustomDialog(this@EditCarInfoActivity, "자동차 정보 삭제", "자동차 정보를 삭제하면 기존 데이터는 삭제됩니다. 삭제 하시겠습니까?", "삭제","취소",  object : CustomDialog.DialogCallback{
//                    override fun onConfirm() {
//                        apiService().deleteMyCarByCarId("Bearer " + PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, getMyCarInfoResponse.id).enqueue(object :Callback<ResponseBody>{
//                            override fun onResponse(
//                                call: Call<ResponseBody>,
//                                response: Response<ResponseBody>
//                            ) {
//                                if(response.code() == 204){
//                                    startActivity(Intent(this@EditCarInfoActivity, OnBoardingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
//                                    finish()
//                                }else if(response.code() == 401){
//                                    logout()
//                                }
//                            }
//
//                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                            }
//
//                        })
//                    }
//
//                    override fun onCancel() {
//
//
//                    }
//
//                }).show()
//            }
//        })
//
//
//        val itemList: MutableList<String?> = ArrayList()
//
//        // 데이터 추가
//        itemList.add("가솔린")
//        itemList.add("디젤")
//        itemList.add("LPG")
//        itemList.add("전기")
//        itemList.add("수소")
//        itemList.add("CNG")
//        itemList.add("가솔린+LPG")
//        itemList.add("가솔린+CNG")
//        itemList.add("가솔린+전기")
//        itemList.add("디젤+전기")
//        itemList.add("LPG+전기")
//        itemList.add("기타")
//
//
//        // adapter 생성
//        val adapter = ArrayAdapter(this, R.layout.edit_fuel_textview, R.id.tv_fuel, itemList)
//
//
//        // listView에 adapter 연결
//        listview.adapter = adapter
//        listview.setOnItemClickListener { parent, view, position, l ->
//            val fuel = parent.getItemAtPosition(position) as String
//            tv_car_fuel.text = fuel
//            layout_fuel_select.visibility = GONE
//        }
//
//    }
//
//    fun setInfo(){
//        apiService().getCarInfoinquiryByCarId("Bearer " + PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, car_id).enqueue(object :Callback<ResponseBody>{
//            override fun onResponse(
//                call: Call<ResponseBody>,
//                response: Response<ResponseBody>
//            ) {
//                if(response.code() == 200 || response.code() == 201){
//                    getMyCarInfoResponse = Gson().fromJson(
//                        response.body()?.string(),
//                        GetMyCarInfoResponse::class.java
//                    )
//
//                    tv_car_no.text = getMyCarInfoResponse.licensePlateNumber
//                    tv_car_owner.text = getMyCarInfoResponse.ownerName
//                    tv_car_id.text = getMyCarInfoResponse.vehicleIdentificationNumber
//                    et_car_model_name.hint = getMyCarInfoResponse.carName
//                    et_car_year.hint = getMyCarInfoResponse.modelYear
//
//                }else if(response.code() == 401){
//                    logout()
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//
//            }
//
//        })
//    }
//
//
//
//    private fun persistentBottomSheetEvent() {
//        behavior = BottomSheetBehavior.from(persistent_bottom_sheet)
//        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                // 슬라이드 되는 도중 계속 호출
//                // called continuously while dragging
//                Log.d("testset", "onStateChanged: 드래그 중")
//            }
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when(newState) {
//                    BottomSheetBehavior.STATE_COLLAPSED-> {
//                        Log.d("testset", "onStateChanged: 접음")
//                        layout_fuel_select.visibility = GONE
//                    }
//                    BottomSheetBehavior.STATE_DRAGGING-> {
//                        Log.d("testset", "onStateChanged: 드래그")
//                    }
//                    BottomSheetBehavior.STATE_EXPANDED-> {
//                        Log.d("testset", "onStateChanged: 펼침")
//                    }
//                    BottomSheetBehavior.STATE_HIDDEN-> {
//                        Log.d("testset", "onStateChanged: 숨기기")
//                    }
//                    BottomSheetBehavior.STATE_SETTLING-> {
//                        Log.d("testset", "onStateChanged: 고정됨")
//                    }
//                }
//            }
//        })
//    }
//
//}