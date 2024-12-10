package com.milelog.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.response.GetAccountProfilesResponse
import com.milelog.retrofit.response.TermsSummaryResponse
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.BuildConfig.BASE_API_URL
import com.milelog.CustomDialog
import com.milelog.Endpoints.FAQ
import com.milelog.Endpoints.HOME
import com.milelog.Endpoints.INQUIRY
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class MyPageActivity: BaseRefreshActivity() {
    lateinit var layout_nickname:ConstraintLayout
    lateinit var btn_drive_history: LinearLayout
    lateinit var btn_alarm_setting:LinearLayout
    lateinit var btn_setting:LinearLayout
    lateinit var btn_logout: TextView
    lateinit var btn_back: ImageView
    lateinit var getAccountProfilesResponse: GetAccountProfilesResponse
    lateinit var tv_nickname:TextView
    lateinit var termsSummaryResponse: List<TermsSummaryResponse>
    lateinit var iv_circle:CircleImageView
    lateinit var btn_faq:LinearLayout
    lateinit var btn_inquiry:LinearLayout
    lateinit var btn_my_garage:LinearLayout
    lateinit var iv_edit:ImageView
    lateinit var btn_setting_bluetooth:LinearLayout
    lateinit var btn_drive_history_webview:LinearLayout
    lateinit var btn_reward_win:LinearLayout
    private lateinit var imageMultipart: MultipartBody.Part // 선택한 이미지


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        init()
        setListener()
    }

    fun init(){
        layout_nickname = findViewById(R.id.layout_nickname)
        btn_drive_history = findViewById(R.id.btn_drive_history)
        btn_alarm_setting = findViewById(R.id.btn_alarm_setting)
        btn_setting = findViewById(R.id.btn_setting)
        btn_logout = findViewById(R.id.btn_logout)
        btn_back = findViewById(R.id.btn_back)
        tv_nickname = findViewById(R.id.tv_nickname)
        iv_circle = findViewById(R.id.iv_circle)
        btn_faq = findViewById(R.id.btn_faq)
        btn_inquiry = findViewById(R.id.btn_inquiry)
        btn_my_garage = findViewById(R.id.btn_my_garage)
        iv_edit = findViewById(R.id.iv_edit)
        btn_setting_bluetooth = findViewById(R.id.btn_setting_bluetooth)
        btn_drive_history_webview = findViewById(R.id.btn_drive_history_webview)
        btn_reward_win = findViewById(R.id.btn_reward_win)


        apiService().getTerms("MILELOG_USAGE").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()

                    val gson = GsonBuilder().serializeNulls().create()
                    val type: Type = object : TypeToken<List<TermsSummaryResponse?>?>() {}.type
                    termsSummaryResponse = gson.fromJson(jsonString, type)

                }else if(response.code() == 401){
                    logout()
                } else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun setListener(){
        layout_nickname.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, MyInfoActivity::class.java).putExtra("nickname",getAccountProfilesResponse.nickName).putExtra("email", getAccountProfilesResponse.user.email).putExtra("provider",getAccountProfilesResponse.user.provider.text.en).putExtra("url",getAccountProfilesResponse.imageUrl))
            }

        })

        iv_circle.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, MyInfoActivity::class.java).putExtra("nickname",getAccountProfilesResponse.nickName).putExtra("email", getAccountProfilesResponse.user.email).putExtra("provider",getAccountProfilesResponse.user.provider.text.en).putExtra("url",getAccountProfilesResponse.imageUrl))

            }

        })

        btn_drive_history.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, MyDriveHistoryActivity::class.java))
            }

        })

        btn_alarm_setting.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, NotificationActivity::class.java))
            }

        })

        btn_setting.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, SettingActivity::class.java))
            }
        })

        btn_drive_history_webview.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, CommonWebviewActivity::class.java).putExtra("url", BASE_API_URL + HOME))
            }
        })


        btn_back.setOnClickListener { finish() }

        btn_logout.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                CustomDialog(
                    this@MyPageActivity,
                    null,
                    "로그아웃 하시겠습니까?",
                    "로그아웃",
                    "취소",
                    object : CustomDialog.DialogCallback {
                        override fun onConfirm() {
                            logout()
                        }

                        override fun onCancel() {

                        }

                    }).show()
            }

        })


        btn_faq.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, CommonWebviewActivity::class.java).putExtra("url", BASE_API_URL + FAQ))
            }

        })


        btn_inquiry.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, CommonWebviewActivity::class.java).putExtra("url", BASE_API_URL + INQUIRY))
            }

        })

        btn_my_garage.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, MyGarageActivity::class.java))
            }

        })

        iv_edit.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, MyInfoActivity::class.java).putExtra("nickname",getAccountProfilesResponse.nickName).putExtra("email", getAccountProfilesResponse.user.email).putExtra("provider",getAccountProfilesResponse.user.provider.text.en).putExtra("url",getAccountProfilesResponse.imageUrl))
            }

        })

        btn_setting_bluetooth.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, FindBluetoothActivity::class.java))

            }

        })

        btn_reward_win.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, WinRewardHistoryActivity::class.java))

            }
        })
    }

    override fun onResume() {
        super.onResume()
        setResources()
    }

    fun setResources(){
        apiService().getAccountProfiles("Bearer " + PreferenceUtil.getPref(this@MyPageActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if(response.code() == 200 || response.code() == 201){
                    try{

                        getAccountProfilesResponse = GsonBuilder().serializeNulls().create().fromJson(
                        response.body()?.string(),
                        GetAccountProfilesResponse::class.java
                    )


                    tv_nickname.text = getAccountProfilesResponse.nickName + "님"

                        Glide.with(this@MyPageActivity)
                            .asBitmap()
                            .load(getAccountProfilesResponse.imageUrl)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {}

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)

                                    //showAlertDialog("프로필 이미지를 불러오는데 실패했습니다.")
                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    iv_circle.setImageBitmap(resource)
                                }
                            })
                    }catch (e:Exception){

                    }


                }else if(response.code() == 401){
                    logout()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }
}