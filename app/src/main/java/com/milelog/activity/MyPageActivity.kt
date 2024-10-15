package com.milelog.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.response.GetAccountProfilesResponse
import com.milelog.retrofit.response.TermsSummaryResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type

class MyPageActivity: BaseRefreshActivity() {
    lateinit var layout_nickname:ConstraintLayout
    lateinit var btn_drive_history:ConstraintLayout
    lateinit var btn_alarm_setting:ConstraintLayout
    lateinit var btn_setting:ConstraintLayout
    lateinit var btn_terms:ConstraintLayout
    lateinit var btn_personal_info:ConstraintLayout
    lateinit var btn_logout: TextView
    lateinit var btn_back: ImageView
    lateinit var getAccountProfilesResponse: GetAccountProfilesResponse
    lateinit var tv_email:TextView
    lateinit var tv_nickname:TextView
    lateinit var termsSummaryResponse: List<TermsSummaryResponse>
    lateinit var iv_circle:CircleImageView
    private lateinit var imageMultipart: MultipartBody.Part // 선택한 이미지


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        init()
        setListener()
//        startCrop()
    }

    fun init(){
        layout_nickname = findViewById(R.id.layout_nickname)
        btn_drive_history = findViewById(R.id.btn_drive_history)
        btn_alarm_setting = findViewById(R.id.btn_alarm_setting)
        btn_setting = findViewById(R.id.btn_setting)
        btn_terms = findViewById(R.id.btn_terms)
        btn_personal_info = findViewById(R.id.btn_personal_info)
        btn_logout = findViewById(R.id.btn_logout)
        btn_back = findViewById(R.id.btn_back)
        tv_email = findViewById(R.id.tv_email)
        tv_nickname = findViewById(R.id.tv_nickname)
        iv_circle = findViewById(R.id.iv_circle)

//        Glide.with(this)
//            .asBitmap()
//            .load("url")
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onLoadCleared(placeholder: Drawable?) {}
//
//                override fun onLoadFailed(errorDrawable: Drawable?) {
//                    super.onLoadFailed(errorDrawable)
//
//                    //showAlertDialog("프로필 이미지를 불러오는데 실패했습니다.")
//                }
//
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    iv_circle.setImageBitmap(resource)
//                }
//            })

        apiService().getTerms("MILELOG_USAGE").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()

                    val gson = Gson()
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

    private fun startCrop() {
        cropImage.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                setMaxZoom(99999)
                setAutoZoomEnabled(true)
                setCropMenuCropButtonTitle("확인")
                setImageSource(includeGallery = true, includeCamera = false)
            }
        )
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent

            val bitmap: Bitmap
            val selectedImageUri: Uri = uriContent!!

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

            imageMultipart = buildImageBodyPart(this, "profileImg", bitmap)
        } else {

        }
    }

    fun setListener(){
        layout_nickname.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, MyInfoActivity::class.java).putExtra("nickname",getAccountProfilesResponse.nickName).putExtra("email", getAccountProfilesResponse.user.email).putExtra("provider",getAccountProfilesResponse.user.provider.text.en))
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


        btn_terms.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(term.title.contains("이용약관")){
                        startActivity(Intent(this@MyPageActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }
            }

        })

        btn_personal_info.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(term.title.contains("개인정보 수집 및 이용 동의")){
                        if(term.isRequired)
                            startActivity(Intent(this@MyPageActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }
            }

        })


        btn_back.setOnClickListener { finish() }

        btn_logout.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                logout()
            }

        })

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


        tv_email.setOnClickListener{
            val clip = ClipData.newPlainText("email", tv_email.text.toString())
            // ClipData를 ClipboardManager에 설정하여 클립보드에 복사합니다.
            clipboardManager.setPrimaryClip(clip)
            // 복사 완료 메시지를 표시할 수 있습니다. (선택 사항)
            Toast.makeText(this, "복사되었습니다.", Toast.LENGTH_SHORT).show()
        }
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
                    getAccountProfilesResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetAccountProfilesResponse::class.java
                    )

                    tv_email.text = getAccountProfilesResponse.user.email
                    tv_nickname.text = getAccountProfilesResponse.nickName + "님"

                }else if(response.code() == 401){
                    logout()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun buildImageBodyPart(
        context: Context,
        fileName: String,
        bitmap: Bitmap,
        randomName: String = "",
        quality: Int = 40,
        isBanner: Boolean = false,
        isEdit: Boolean = false,
    ): MultipartBody.Part {
        val leftImageFile: File = when {
            isBanner -> {
                convertBitmapToFile(context, randomName, quality, bitmap)
            }
            isEdit -> {
                convertBitmapToFile(context, fileName, quality, bitmap)
            }
            else -> {
                convertBitmapToFile(context, fileName, quality, bitmap)
            }
        }

        val reqFile = leftImageFile.asRequestBody("image/*".toMediaTypeOrNull())

        return if (isBanner)
            MultipartBody.Part.createFormData(fileName, leftImageFile.name, reqFile)
        else
            MultipartBody.Part.createFormData(fileName, leftImageFile.name, reqFile)
    }

    fun convertBitmapToFile(context: Context, fileName: String, quality: Int, bitmap: Bitmap): File {
        //create a file to write bitmap data
        val file = File(context.cacheDir, "${fileName}.jpeg")
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality /*ignored for PNG*/, bos)
        val bitMapData = bos.toByteArray()

        //write the bytes in file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            fos?.write(bitMapData)
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}