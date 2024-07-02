package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.GetMyCarInfoResponse
import com.charancha.drive.retrofit.response.SignInResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class EditCarInfoActivity:BaseActivity() {
    lateinit var tv_car_no: TextView
    lateinit var tv_car_owner:TextView
    lateinit var tv_car_id:TextView
    lateinit var tv_car_model_name:TextView
    lateinit var tv_car_year:TextView
    lateinit var tv_car_fuel:TextView
    lateinit var layout_delete:ConstraintLayout
    lateinit var layout_edit:ConstraintLayout
    lateinit var getMyCarInfoResponse:GetMyCarInfoResponse


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
        tv_car_model_name = findViewById(R.id.tv_car_model_name)
        tv_car_year = findViewById(R.id.tv_car_year)
        tv_car_fuel = findViewById(R.id.tv_car_fuel)
        layout_delete = findViewById(R.id.layout_delete)
        layout_edit = findViewById(R.id.layout_edit)

        layout_edit.setOnClickListener {

        }

        layout_delete.setOnClickListener {
            apiService().deleteMyCarByCarId(PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, getMyCarInfoResponse.id).enqueue(object :Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    TODO("Not yet implemented")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })


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
                        apiService().getCarInfoinquiryByCarId(PreferenceUtil.getPref(this@EditCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, getMyCarInfoResponses.get(0).id).enqueue(object :Callback<ResponseBody>{
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if(response.code() == 200){
                                    val getMyCarInfoResponse = Gson().fromJson(
                                        response.body()?.string(),
                                        GetMyCarInfoResponse::class.java
                                    )

                                    tv_car_no.text = getMyCarInfoResponse.licensePlateNumber
                                    tv_car_owner.text = getMyCarInfoResponse.ownerName
                                    tv_car_id.text = getMyCarInfoResponse.id
                                    tv_car_model_name.text = getMyCarInfoResponse.carName
                                    tv_car_year.text = getMyCarInfoResponse.carYear.toString()
                                    tv_car_fuel.text = getMyCarInfoResponse.fuel

                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                            }

                        })
                    }else{

                    }
                }else{
                    startActivity(Intent(this@EditCarInfoActivity, OnBoardingActivity::class.java))
                    finish()
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