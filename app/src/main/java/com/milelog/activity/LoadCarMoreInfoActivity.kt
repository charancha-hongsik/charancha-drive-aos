package com.milelog.activity

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.CarDetail
import com.milelog.CustomDialog
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.activity.CarDetailActivity.Companion.FUEL
import com.milelog.retrofit.request.EditMyCarRequest
import com.milelog.retrofit.request.PostMyCarRequest
import com.milelog.retrofit.response.CarDetailResponse
import com.milelog.retrofit.response.GetMyCarInfoItem
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.retrofit.response.PostMyCarResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class LoadCarMoreInfoActivity: BaseRefreshActivity() {
    lateinit var listView: ListView
    lateinit var layout_select: CoordinatorLayout
    private lateinit var tv_car_no: TextView
    private lateinit var tv_owner: TextView
    private lateinit var tv_releaseDt: TextView
    private lateinit var tv_car_id: TextView
    private lateinit var postMyCarResponse: PostMyCarResponse
    private lateinit var ib_arrow_register_car: ImageButton
    private lateinit var btn_car_more_info: ConstraintLayout
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var tv_car_name:TextView
    private lateinit var iv_arrow: ImageView
    private lateinit var btn_fuel:ConstraintLayout
    private lateinit var tv_fuel:TextView
    private lateinit var btn_next: ConstraintLayout
    private lateinit var btn_delete:TextView
    private lateinit var btn_save:TextView
    private lateinit var btn_edit: LinearLayout
    private lateinit var btn_edit_car:ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loadcar_moreinfo)

        init()
        setInfo()
        setListener()
    }

    override fun onBackPressed() {
        if(layout_select.visibility == VISIBLE){
            layout_select.visibility = GONE
        }else{
            finish()
        }
    }

    private fun init(){
        tv_car_no = findViewById(R.id.tv_car_no)
        tv_owner = findViewById(R.id.tv_owner)
        tv_releaseDt = findViewById(R.id.tv_releaseDt)
        tv_car_id = findViewById(R.id.tv_car_id)
        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)
        btn_car_more_info = findViewById(R.id.btn_car_more_info)
        tv_car_name = findViewById(R.id.tv_car_name)
        iv_arrow = findViewById(R.id.iv_arrow)
        layout_select = findViewById(R.id.layout_select)
        listView = findViewById(R.id.listView)
        btn_fuel = findViewById(R.id.btn_fuel)
        tv_fuel = findViewById(R.id.tv_fuel)
        btn_next = findViewById(R.id.btn_next)
        btn_delete = findViewById(R.id.btn_delete)
        btn_save = findViewById(R.id.btn_save)
        btn_edit = findViewById(R.id.btn_edit)
        btn_edit_car = findViewById(R.id.btn_edit_car)

        val jsonString = intent.getStringExtra("carInfo")
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
        if(!postMyCarResponse.fuelNm.isNullOrEmpty())
            tv_fuel.text = postMyCarResponse.fuelNm

        if(postMyCarResponse.makerCd == null && postMyCarResponse.modelCd == null) {
            tv_car_name.text = "선택해 주세요"
            iv_arrow.visibility = GONE
            btn_car_more_info.visibility = VISIBLE
            btn_next.isSelected = false
            btn_next.isClickable = false
        } else{
            val carName = listOfNotNull(
                postMyCarResponse.makerNm,
                if (postMyCarResponse.modelDetailNm.isNullOrEmpty()) postMyCarResponse.modelNm else null, // modelDetailNm이 없을 때만 modelNm 추가
                postMyCarResponse.modelDetailNm,
                postMyCarResponse.gradeNm,
                postMyCarResponse.gradeDetailNm
            ).filterNot { it.isNullOrEmpty() } // null이나 빈 문자열을 필터링
                .joinToString(" ")

            iv_arrow.visibility = VISIBLE
            btn_car_more_info.visibility = GONE
            tv_car_name.text = carName

            if(!postMyCarResponse.fuelCd.isNullOrEmpty()){
                btn_next.isSelected = true
                btn_next.isClickable = true
            }
        }

        if(intent.getBooleanExtra("edit",false)){
            btn_edit.visibility = VISIBLE
            btn_next.visibility = GONE
        }else{
            btn_edit.visibility = GONE
            btn_next.visibility = VISIBLE
        }
    }

    private fun setListener(){
        ib_arrow_register_car.setOnClickListener{
            finish()
        }

        btn_car_more_info.setOnClickListener {
            resultLauncher.launch(
                Intent(this@LoadCarMoreInfoActivity, CarDetailActivity::class.java)
                    .putExtra("carInfo", Gson().toJson(postMyCarResponse))
            )
        }

        iv_arrow.setOnClickListener {
            resultLauncher.launch(
                Intent(this@LoadCarMoreInfoActivity, CarDetailActivity::class.java)
                    .putExtra("carInfo", Gson().toJson(postMyCarResponse))
            )
        }

        btn_edit_car.setOnClickListener {
            resultLauncher.launch(
                Intent(this@LoadCarMoreInfoActivity, CarDetailActivity::class.java)
                    .putExtra("carInfo", Gson().toJson(postMyCarResponse))
            )
        }

        layout_select.setOnClickListener{
            layout_select.visibility = GONE
        }

        btn_fuel.setOnClickListener {
            apiService().getCharanchaCode("Bearer " + PreferenceUtil.getPref(this@LoadCarMoreInfoActivity, PreferenceUtil.ACCESS_TOKEN, "")!!, FUEL, null).enqueue(object:
                Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                    val jsonString = response.body()?.string()

                    val gson = Gson()
                    val type: Type = object : TypeToken<List<CarDetailResponse>>() {}.type
                    val carDetails:List<CarDetailResponse> = gson.fromJson(jsonString, type)

                    val itemList: MutableList<CarDetail> = ArrayList()

                    for(carDetail in carDetails)
                        itemList.add(CarDetail(carDetail.code, carDetail.codeNm))

                    // adapter 생성
                    val adapter = CarDetailAdapter(this@LoadCarMoreInfoActivity, R.layout.edit_fuel_textview, itemList)

                    // listView에 adapter 연결
                    listView.adapter = adapter

                    layout_select.visibility = VISIBLE
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

            })
        }


        btn_next.setOnClickListener {
                val gson = Gson()
                val jsonParam =
                    gson.toJson(
                        PostMyCarRequest(
                            licensePlateNumber = postMyCarResponse.licensePlateNumber,
                            ownerName = postMyCarResponse.ownerName,
                            vehicleIdentificationNumber = postMyCarResponse.vehicleIdentificationNumber,
                            carName = postMyCarResponse.carName,
                            makerCd = postMyCarResponse.makerCd!!,
                            modelCd = postMyCarResponse.modelCd!!,
                            modelDetailCd = postMyCarResponse.modelDetailCd,
                            gradeCd = postMyCarResponse.gradeCd,
                            gradeDetailCd = postMyCarResponse.gradeDetailCd,
                            fuelCd = postMyCarResponse.fuelCd!!
                        )
                    )

                apiService(60).postMyCar(
                    "Bearer " + PreferenceUtil.getPref(this,  PreferenceUtil.ACCESS_TOKEN, ""), jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
                    Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(response.code() == 201 || response.code() == 200){
                            if(intent.getBooleanExtra("add",false)){
                                startActivity(
                                    Intent(this@LoadCarMoreInfoActivity, MyGarageActivity::class.java).addFlags(
                                        FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
                                    ))
                                finish()
                            }else{
                                val getMyCarInfoItem = Gson().fromJson(
                                    response.body()?.string(),
                                    GetMyCarInfoItem::class.java
                                )
                                PreferenceUtil.putPref(this@LoadCarMoreInfoActivity, PreferenceUtil.USER_CARID, getMyCarInfoItem.id)
                                PreferenceUtil.putPref(this@LoadCarMoreInfoActivity,  PreferenceUtil.KM_MILE, "km")
                                startActivity(
                                    Intent(this@LoadCarMoreInfoActivity, MainActivity::class.java).addFlags(
                                        FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                                    ))
                                finish()
                            }
                        }else if(response.code() == 401){
                            logout()
                        } else{
                            showCustomToast(this@LoadCarMoreInfoActivity,"차량 등록에 실패했습니다.")

                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        showCustomToast(this@LoadCarMoreInfoActivity,"차량 등록에 실패했습니다.")

                    }

                })
            }

        btn_delete.setOnClickListener {
            CustomDialog(this, "자동차 정보 삭제", "자동차 정보를 삭제하면 기존 데이터는 삭제됩니다. 삭제 하시겠습니까?", "삭제","취소",  object : CustomDialog.DialogCallback{
                override fun onConfirm() {
                    apiService().deleteMyCarByCarId("Bearer " + PreferenceUtil.getPref(this@LoadCarMoreInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, intent.getStringExtra("carId")!!).enqueue(object :
                        Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if(response.code() == 204){
                                finish()
                            }else if(response.code() == 401){
                                logout()
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        }

                    })
                }

                override fun onCancel() {


                }

            }).show()
        }

        btn_save.setOnClickListener {

            val gson = GsonBuilder().serializeNulls().create()
            val jsonParam =
                gson.toJson(
                    EditMyCarRequest(
                        licensePlateNumber = postMyCarResponse.licensePlateNumber,
                        ownerName = postMyCarResponse.ownerName,
                        carName = postMyCarResponse.carName,
                        makerCd = postMyCarResponse.makerCd,
                        modelCd = postMyCarResponse.modelCd,
                        modelDetailCd = postMyCarResponse.modelDetailCd,
                        gradeCd = postMyCarResponse.gradeCd,
                        gradeDetailCd = postMyCarResponse.gradeDetailCd,
                        fuelCd = postMyCarResponse.fuelCd
                    )
                )

            apiService(60).patchCarInfoByCarId(
                "Bearer " + PreferenceUtil.getPref(this,  PreferenceUtil.ACCESS_TOKEN, ""),intent.getStringExtra("carId")!! ,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
                Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 201 || response.code() == 200){

                        if(intent.getBooleanExtra("edit",false)){
                            finish()
                        }else{
                            val getMyCarInfoItem = Gson().fromJson(
                                response.body()?.string(),
                                GetMyCarInfoItem::class.java
                            )
                            PreferenceUtil.putPref(this@LoadCarMoreInfoActivity, PreferenceUtil.USER_CARID, getMyCarInfoItem.id)
                            PreferenceUtil.putPref(this@LoadCarMoreInfoActivity,  PreferenceUtil.KM_MILE, "km")
                            startActivity(
                                Intent(this@LoadCarMoreInfoActivity, MainActivity::class.java).addFlags(
                                    FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                                ))
                            finish()
                        }
                    }else if(response.code() == 401){
                        logout()
                    } else{
                        showCustomToast(this@LoadCarMoreInfoActivity,"차량 등록에 실패했습니다.")

                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showCustomToast(this@LoadCarMoreInfoActivity,"차량 등록에 실패했습니다.")

                }

            })
        }
    }

    inner class CarDetailAdapter(context: Context, resource: Int, items: List<CarDetail>) : ArrayAdapter<CarDetail>(context, resource, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.edit_fuel_textview, parent, false)

            val carDetail = getItem(position)

            carDetail?.let{
                val tv_fuel_each = view.findViewById<TextView>(R.id.tv_fuel)
                tv_fuel_each.text = carDetail.name

                tv_fuel_each.setOnClickListener {
                    tv_fuel.text = carDetail.name
                    postMyCarResponse.fuelCd = carDetail.code
                    postMyCarResponse.fuelNm = carDetail.name

                    layout_select.visibility = GONE
                }
            }

            return view
        }
    }
}