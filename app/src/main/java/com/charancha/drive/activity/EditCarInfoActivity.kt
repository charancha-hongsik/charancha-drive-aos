package com.charancha.drive.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.charancha.drive.CustomDialog
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.request.EditMyCarRequest
import com.charancha.drive.retrofit.request.PostMyCarRequest
import com.charancha.drive.retrofit.request.SignUpRequest
import com.charancha.drive.retrofit.response.GetMyCarInfoResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class EditCarInfoActivity:BaseActivity() {
    lateinit var tv_car_no: TextView
    lateinit var tv_car_owner:TextView
    lateinit var tv_car_id:TextView
    lateinit var et_car_model_name: EditText
    lateinit var et_car_year:EditText
    lateinit var tv_car_fuel:TextView
    lateinit var layout_delete:ConstraintLayout
    lateinit var layout_edit:ConstraintLayout
    lateinit var getMyCarInfoResponse:GetMyCarInfoResponse
    lateinit var ib_close: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editcarinfo)

        init()
        setInfo()
    }

    fun init(){
        tv_car_no = findViewById(R.id.tv_car_no)
        tv_car_owner = findViewById(R.id.tv_car_owner)
        tv_car_id = findViewById(R.id.tv_car_id)
        et_car_model_name = findViewById(R.id.et_car_model_name)
        et_car_year = findViewById(R.id.et_car_year)
        tv_car_fuel = findViewById(R.id.tv_car_fuel)
        layout_delete = findViewById(R.id.layout_delete)
        layout_edit = findViewById(R.id.layout_edit)
        ib_close = findViewById(R.id.ib_close)

        ib_close.setOnClickListener {
            finish()
        }

        et_car_model_name.setOnFocusChangeListener { view, b ->
            if(b){
                et_car_model_name.hint = ""
            }else{

            }
        }

        et_car_year.setOnFocusChangeListener { view, b ->
            if(b){
                et_car_year.hint = ""
            }else{

            }
        }


        layout_edit.setOnClickListener {
            var carYear = getMyCarInfoResponse.carYear
            var carModelName = getMyCarInfoResponse.carName

            if(et_car_year.text.toString().isNotEmpty()){
                carYear = et_car_year.text.toString().toInt()
            }
            if(et_car_model_name.text.toString().isNotEmpty()){
                carModelName = et_car_model_name.text.toString()
            }

            val gson = Gson()
            val jsonParam =
                gson.toJson(EditMyCarRequest(getMyCarInfoResponse.licensePlateNumber, getMyCarInfoResponse.ownerName, carYear, carModelName, getMyCarInfoResponse.fuel))

            apiService().patchCarInfoByCarId("Bearer " + PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, getMyCarInfoResponse.id, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 200){
                        Toast.makeText(this@EditCarInfoActivity, "내 차 정보가 수정되었어요.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

            })
        }

        layout_delete.setOnClickListener {
            CustomDialog(this, "자동차 정보 삭제", "자동차 정보를 삭제하면 기존 데이터는 삭제됩니다. 삭제 하시겠습니까?", "삭제","취소",  object : CustomDialog.DialogCallback{
                override fun onConfirm() {
                    apiService().deleteMyCarByCarId("Bearer " + PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, getMyCarInfoResponse.id).enqueue(object :Callback<ResponseBody>{
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            startActivity(Intent(this@EditCarInfoActivity, OnBoardingActivity::class.java))
                            finish()
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
                }

                override fun onCancel() {


                }

            }).show()
        }
    }

    fun setInfo(){
        apiService().getMyCarInfo("Bearer " + PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                if(response.code() == 200){
                    val jsonString = response.body()?.string()

                    val type: Type = object : TypeToken<List<GetMyCarInfoResponse?>?>() {}.type
                    val getMyCarInfoResponses:List<GetMyCarInfoResponse> = Gson().fromJson(jsonString, type)

                    if(getMyCarInfoResponses.size > 0){
                        apiService().getCarInfoinquiryByCarId("Bearer " + PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, getMyCarInfoResponses.get(0).id).enqueue(object :Callback<ResponseBody>{
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if(response.code() == 200){
                                    getMyCarInfoResponse = Gson().fromJson(
                                        response.body()?.string(),
                                        GetMyCarInfoResponse::class.java
                                    )

                                    tv_car_no.text = getMyCarInfoResponse.licensePlateNumber
                                    tv_car_owner.text = getMyCarInfoResponse.ownerName
                                    tv_car_id.text = getMyCarInfoResponse.id
                                    et_car_model_name.hint = getMyCarInfoResponse.carName
                                    et_car_year.hint = getMyCarInfoResponse.carYear.toString()
                                    tv_car_fuel.text = getMyCarInfoResponse.fuel

                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                            }

                        })
                    }else{

                    }
                }else{

                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {

            }
        })
    }





}