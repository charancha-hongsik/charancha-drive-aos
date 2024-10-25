package com.milelog.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.milelog.R
import com.milelog.retrofit.response.PostMyCarResponse

class LoadCarMoreInfoActivity: BaseRefreshActivity() {
    private lateinit var tv_car_no: TextView
    private lateinit var tv_owner: TextView
    private lateinit var tv_releaseDt: TextView
    private lateinit var tv_car_id: TextView
    private lateinit var postMyCarResponse: PostMyCarResponse
    private lateinit var ib_arrow_register_car: ImageButton
    private lateinit var btn_car_more_info: ConstraintLayout
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var tv_car_name:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loadcar_moreinfo)

        init()
        setInfo()
    }

    private fun init(){
        tv_car_no = findViewById(R.id.tv_car_no)
        tv_owner = findViewById(R.id.tv_owner)
        tv_releaseDt = findViewById(R.id.tv_releaseDt)
        tv_car_id = findViewById(R.id.tv_car_id)
        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)
        btn_car_more_info = findViewById(R.id.btn_car_more_info)
        tv_car_name = findViewById(R.id.tv_car_name)

        ib_arrow_register_car.setOnClickListener{
            finish()
        }

        btn_car_more_info.setOnClickListener {
            resultLauncher.launch(
                Intent(this@LoadCarMoreInfoActivity, CarDetailActivity::class.java)
                    .putExtra("carInfo", intent.getStringExtra("response"))
            )
        }

        val jsonString = intent.getStringExtra("response")
        val gson = Gson()
        postMyCarResponse = gson.fromJson(jsonString, PostMyCarResponse::class.java)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                postMyCarResponse = gson.fromJson(it.data?.getStringExtra("carInfo"), PostMyCarResponse::class.java)
                setInfo()
            }
        }
    }

    private fun setInfo(){
        tv_car_no.text = postMyCarResponse.licensePlateNumber
        tv_owner.text = postMyCarResponse.ownerName
        tv_releaseDt.text = postMyCarResponse.releaseDt
        tv_car_id.text = postMyCarResponse.vehicleIdentificationNumber

        if(postMyCarResponse.makerCd == null && postMyCarResponse.modelCd == null)
            tv_car_name.text = "선택해 주세요"
        else{
            val carName = listOfNotNull(
                postMyCarResponse.makerNm,
                if (postMyCarResponse.modelDetailNm.isNullOrEmpty()) postMyCarResponse.modelNm else null, // modelDetailNm이 없을 때만 modelNm 추가
                postMyCarResponse.modelDetailNm,
                postMyCarResponse.gradeNm,
                postMyCarResponse.gradeDetailNm
            ).filterNot { it.isNullOrEmpty() } // null이나 빈 문자열을 필터링
                .joinToString(" ")

            tv_car_name.text = carName
        }

//        val gson = Gson()
//        val jsonParam =
//            gson.toJson(
//                PostMyCarRequest(
//                    licensePlateNumber=tv_car_no.text.toString(),
//                    ownerName=tv_car_owner.text.toString(),
//                    vehicleIdentificationNumber=tv_car_id.text.toString(),
//                    modelYear= modelYear,
//                    carName = carName,
//                    fuel = tv_car_fuel.text.toString()
//                )
//            )
//
//        apiService(60).postMyCar(
//            "Bearer " + PreferenceUtil.getPref(this,  PreferenceUtil.ACCESS_TOKEN, ""), jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
//            Callback<ResponseBody> {
//            override fun onResponse(
//                call: Call<ResponseBody>,
//                response: Response<ResponseBody>
//            ) {
//                if(response.code() == 201 || response.code() == 200){
//                    val getMyCarInfoResponse = Gson().fromJson(
//                        response.body()?.string(),
//                        GetMyCarInfoResponse::class.java
//                    )
//                    PreferenceUtil.putPref(this@LoadCarMoreInfoActivity, PreferenceUtil.USER_CARID, getMyCarInfoResponse.id)
//                    PreferenceUtil.putPref(this@LoadCarMoreInfoActivity,  PreferenceUtil.KM_MILE, "km")
//                    startActivity(
//                        Intent(this@LoadCarMoreInfoActivity, MainActivity::class.java).addFlags(
//                            FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
//                        ))
//                    finish()
//                }else if(response.code() == 401){
//                    logout()
//                } else{
//                    showCustomToast(this@LoadCarMoreInfoActivity,"차량 등록에 실패했습니다.")
//
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                showCustomToast(this@LoadCarMoreInfoActivity,"차량 등록에 실패했습니다.")
//
//            }
//
//        })
    }
}