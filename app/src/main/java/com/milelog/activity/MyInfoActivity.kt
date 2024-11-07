package com.milelog.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.milelog.CustomDialogForEditText
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.request.PatchProfilesRequest
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MyInfoActivity: BaseRefreshActivity() {
    lateinit var btn_back:ImageView
    lateinit var tv_nickname:TextView
    lateinit var tv_withdrawal:TextView
    lateinit var nickName:String
    lateinit var tv_login_oauth:TextView
    lateinit var tv_email:TextView
    lateinit var btn_edit_nickname:ConstraintLayout
    lateinit var iv_circle: CircleImageView
    private lateinit var imageMultipart: MultipartBody.Part // 선택한 이미지

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myinfo)

        init()
        setListener()
    }

    fun init(){
        btn_back = findViewById(R.id.btn_back)
        tv_nickname = findViewById(R.id.tv_nickname)
        tv_withdrawal = findViewById(R.id.tv_withdrawal)
        tv_login_oauth = findViewById(R.id.tv_login_oauth)
        tv_email = findViewById(R.id.tv_email)
        iv_circle = findViewById(R.id.iv_circle)
        btn_edit_nickname = findViewById(R.id.btn_edit_nickname)
        nickName = intent.getStringExtra("nickname")!!
        tv_nickname.text = nickName
        tv_login_oauth.text = intent.getStringExtra("provider")!!
        tv_email.text = intent.getStringExtra("email")

        Glide.with(this@MyInfoActivity)
            .asBitmap()
            .load(intent.getStringExtra("url"))
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

    }

    fun setListener(){
        btn_back.setOnClickListener {
            finish()
        }

        tv_withdrawal.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyInfoActivity, WithdrawalActivity::class.java))
            }

        })

        btn_edit_nickname.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                CustomDialogForEditText(this@MyInfoActivity, "내 정보", "별명", nickName,"저장","취소",  object : CustomDialogForEditText.DialogCallback{
                    override fun onConfirm(contents:String) {
                        val nickNameRequestBody = RequestBody.create(MultipartBody.FORM, contents)
                        val imageUpdateTypeRequestBody = RequestBody.create(MultipartBody.FORM, "NONE")

                        apiService().patchAccountProfiles("Bearer " + PreferenceUtil.getPref(this@MyInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,nickNameRequestBody, imageUpdateTypeRequestBody, null).enqueue(object :Callback<ResponseBody>{
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if(response.code() == 200 || response.code() == 201){
                                    showCustomToast(this@MyInfoActivity, "저장 되었습니다.")

                                    tv_nickname.text = contents

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

                }).show()            }

        })

        iv_circle.setOnClickListener {
            startCrop()
        }
    }

    fun setResources(){

    }

    private fun startCrop() {
        cropImage.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                setMaxZoom(99999)
                setAspectRatio(1,1)
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
            val imageUpdateTypeRequestBody = RequestBody.create(MultipartBody.FORM, "UPDATE")


            apiService().patchAccountProfiles("Bearer " + PreferenceUtil.getPref(this@MyInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,null, imageUpdateTypeRequestBody, imageMultipart) .enqueue(object :Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 200 || response.code() == 201){
                        showCustomToast(this@MyInfoActivity, "저장 되었습니다.")

                        iv_circle.setImageBitmap(bitmap)

                    }else if(response.code() == 401){
                        logout()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

            })
        } else {

        }
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

        val reqFile = leftImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

        return if (isBanner)
            MultipartBody.Part.createFormData("image", leftImageFile.name, reqFile)
        else
            MultipartBody.Part.createFormData("image", leftImageFile.name, reqFile)
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