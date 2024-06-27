package com.charancha.drive.activity


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoadCarInfoActivity:BaseActivity() {
    lateinit var iv_animation:ImageView
    lateinit var animation_parent: ConstraintLayout
    lateinit var ib_arrow_register_car:ImageButton
    lateinit var iv_animation_pot1:View
    lateinit var iv_animation_pot2:View
    lateinit var iv_animation_pot3:View
    lateinit var animation_load_parent:ConstraintLayout

    var carNo:String? = null
    var carOwner:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_car)

        init()
    }

    private fun init(){
        carNo = intent.getStringExtra("carNo")
        carOwner = intent.getStringExtra("carOwner")


        setResources()
        getInfoInquiry()
    }

    private fun setResources(){
        iv_animation = findViewById(R.id.iv_animation)
        animation_parent = findViewById(R.id.animation_parent)
        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)
        iv_animation_pot1 = findViewById(R.id.iv_animation_pot1)
        iv_animation_pot2 = findViewById(R.id.iv_animation_pot2)
        iv_animation_pot3 = findViewById(R.id.iv_animation_pot3)
        animation_load_parent = findViewById(R.id.animation_load_parent)

        ib_arrow_register_car.setOnClickListener {
            finish()
        }

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

    fun getInfoInquiry(){
        if(carNo != null && carOwner != null){
            apiService().getCarInfoInquiry("Bearer " + PreferenceUtil.getPref(this@LoadCarInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, carNo!!, carOwner!!).enqueue(object :
                Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 200){
                        response.body()?.let{
                            val intent = Intent(this@LoadCarInfoActivity, RegisterCarActivity::class.java)
                            intent.putExtra("response", it.string())
                            setResult(RESULT_OK, intent)
                            finish()
                        }?:{
                            val intent = Intent(this@LoadCarInfoActivity, RegisterCarActivity::class.java)
                            setResult(RESULT_CANCELED, intent)
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

            })

        }
    }
}