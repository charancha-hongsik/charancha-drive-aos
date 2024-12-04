package com.milelog.activity


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.response.PostMyCarResponse
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.LoadCarInfoViewModel
import com.milelog.viewmodel.state.GetCarInfoInquiryState
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoadCarInfoActivity: BaseRefreshActivity() {
    private val loadCarInfoViewModel: LoadCarInfoViewModel by viewModels()

    lateinit var iv_animation:ImageView
    lateinit var animation_parent: ConstraintLayout
    lateinit var ib_arrow_register_car:ImageButton
    lateinit var iv_animation_pot1:View
    lateinit var iv_animation_pot2:View
    lateinit var iv_animation_pot3:View
    lateinit var animation_load_parent:ConstraintLayout

    var carNo:String? = null
    var carOwner:String? = null
    var add:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_car)

        init()
    }

    private fun init(){
        loadCarInfoViewModel.init(applicationContext)

        carNo = intent.getStringExtra("carNo")
        carOwner = intent.getStringExtra("carOwner")
        add = intent.getBooleanExtra("add",false)

        setResources()
        setObserver()

        if(carNo != null && carOwner != null)
            loadCarInfoViewModel.getCarInfoInquiry(carNo!!, carOwner!!)
    }

    private fun setObserver() {
        loadCarInfoViewModel.setCarInfoInquiry.observe(
            this@LoadCarInfoActivity,
            BaseViewModel.EventObserver { state ->
                when (state) {
                    is GetCarInfoInquiryState.Loading -> {

                    }

                    is GetCarInfoInquiryState.Success -> {
                        val vehicleIdentificationNumber = Gson().fromJson(state.data, PostMyCarResponse::class.java).vehicleIdentificationNumber
                        apiService().getMyCarCount("Bearer " + PreferenceUtil.getPref(this@LoadCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, vehicleIdentificationNumber = vehicleIdentificationNumber).enqueue(object :
                            Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {

                                if(response.code() == 200 || response.code() == 201){
                                    if(response.body()!!.string().toInt() > 0){
                                        Toast.makeText(this@LoadCarInfoActivity, "동일한 차량이 이미 등록되어 있어요.",
                                            Toast.LENGTH_SHORT).show()
                                        finish()
                                    }else{
                                        val intent = Intent(this@LoadCarInfoActivity, LoadCarMoreInfoActivity::class.java)
                                        intent.putExtra("carInfo", state.data)
                                        intent.putExtra("add",add)
                                        startActivity(intent)
                                        finish()
                                    }
                                }else if(response.code() == 401){
                                    logout()
                                }
                            }

                            override fun onFailure(
                                call: Call<ResponseBody>,
                                t: Throwable
                            ) {

                            }
                        })
                    }

                    is GetCarInfoInquiryState.Error -> {
                        if (state.code == 401) {
                            logout()
                        }else{
                            showCustomToast(this@LoadCarInfoActivity,"차량 번호 또는 소유자명이 일치하지 않습니다.")
                            finish()
                        }
                    }

                    is GetCarInfoInquiryState.Empty -> {
                        showCustomToast(this@LoadCarInfoActivity, "empty")
                        finish()
                    }
                }
            })
    }



    private fun setResources(){
        iv_animation = findViewById(R.id.iv_animation)
        animation_parent = findViewById(R.id.animation_parent)
        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)
        iv_animation_pot1 = findViewById(R.id.iv_animation_pot1)
        iv_animation_pot2 = findViewById(R.id.iv_animation_pot2)
        iv_animation_pot3 = findViewById(R.id.iv_animation_pot3)
        animation_load_parent = findViewById(R.id.animation_load_parent)

        ib_arrow_register_car.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                finish()
            }

        })

        // 상하 이동 애니메이션 설정
        animation_parent.post {
            // 상하 이동 애니메이션 설정
            val animation = TranslateAnimation(0f, 0f, 0f, animation_parent.height.toFloat()-1)
            animation.duration = 1500 // 애니메이션 시간 (밀리초)

            animation.repeatCount = TranslateAnimation.INFINITE // 무한 반복
            animation.repeatMode = TranslateAnimation.REVERSE // 역방향으로 반복

            // 애니메이션 시작
            iv_animation.startAnimation(animation)
        }

        animation_load_parent.post{
            // 상하 이동 애니메이션 설정
            val animation1 = TranslateAnimation(0f, 0f, 0f, -(animation_load_parent.height.toFloat()/3))
            animation1.duration = 750 // 애니메이션 시간 (밀리초)

            animation1.repeatCount = TranslateAnimation.INFINITE // 무한 반복
            animation1.repeatMode = TranslateAnimation.REVERSE // 역방향으로 반복

            val animation2 = TranslateAnimation(0f, 0f, 0f, -(animation_load_parent.height.toFloat()/3))
            animation2.duration = 750 // 애니메이션 시간 (밀리초)

            animation2.repeatCount = TranslateAnimation.INFINITE // 무한 반복
            animation2.repeatMode = TranslateAnimation.REVERSE // 역방향으로 반복

            val animation3 = TranslateAnimation(0f, 0f, 0f, -(animation_load_parent.height.toFloat()/3))
            animation3.duration = 750 // 애니메이션 시간 (밀리초)

            animation3.repeatCount = TranslateAnimation.INFINITE // 무한 반복
            animation3.repeatMode = TranslateAnimation.REVERSE // 역방향으로 반복


            // 애니메이션 시작
            iv_animation_pot1.startAnimation(animation1)

            Handler(Looper.getMainLooper()).postDelayed({
                iv_animation_pot2.startAnimation(animation2)

            }, 250)


            Handler(Looper.getMainLooper()).postDelayed({
                iv_animation_pot3.startAnimation(animation3)

            }, 500)

        }
    }
}