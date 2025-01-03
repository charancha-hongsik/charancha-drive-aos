package com.milelog.activity

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.milelog.CustomDialogForEditText
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.GaScreenName
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    companion object {
        const val REQUEST_READ_EXTERNAL_STORAGE = 100 // 권한 요청 코드
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myinfo)

        init()
        setListener()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(GaScreenName.SCREEN_MY_INFO, this::class.java.simpleName)
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
        val provider = intent.getStringExtra("provider")?.lowercase() ?: ""

        tv_login_oauth.text = when {
            provider.equals("google", ignoreCase = true) -> "구글"
            provider.equals("apple", ignoreCase = true) -> "애플"
            else -> "알 수 없음" // 다른 값일 경우 처리
        }
        tv_email.text = intent.getStringExtra("email")
        tv_email.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                val clip = ClipData.newPlainText("email", tv_email.text.toString())
                // ClipData를 ClipboardManager에 설정하여 클립보드에 복사합니다.
                clipboardManager.setPrimaryClip(clip)

                // Android 12 이하인 경우, "클립보드에 복사되었습니다" 메시지를 표시
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {  // Android 12 이하
                    Toast.makeText(this@MyInfoActivity, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

        })



        Glide.with(this@MyInfoActivity)
            .asBitmap()
            .load(intent.getStringExtra("url"))
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Log.d("testestes","testsetsees :: onLoadFailed")

                    //showAlertDialog("프로필 이미지를 불러오는데 실패했습니다.")
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.d("testestes","testsetsees :: onResourceReady")
                    iv_circle.setImageBitmap(resource)
                }
            })

    }

    fun setListener(){
        btn_back.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                finish()
            }

        })

        tv_withdrawal.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyInfoActivity, WithdrawalActivity::class.java))
            }

        })

        btn_edit_nickname.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                setEditNickNameLog()

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
                                    showCustomToast(this@MyInfoActivity, "저장되었습니다.")

                                    tv_nickname.text = contents
                                    nickName = contents

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

        iv_circle.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) { // API 29 이하
                    // 권한 체크
                    if (ContextCompat.checkSelfPermission(this@MyInfoActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        startCrop() // 권한이 있으면 크롭 시작
                    } else {
                        // 권한이 없으면 요청
                        ActivityCompat.requestPermissions(this@MyInfoActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE)
                    }
                } else {
                    // API 30 이상은 권한 체크 없이 바로 크롭 시작
                    startCrop()
                }
            }

        })
    }

    fun setResources(){

    }

    private fun startCrop() {
        logScreenView(GaScreenName.SCREEN_SELECT_PICTURE_SINGULAR, this::class.java.simpleName)

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

    private fun setCropLog(){
        logScreenView(GaScreenName.SCREEN_CROP_PROFILE_PICTURE, this::class.java.simpleName)
    }

    private fun setEditNickNameLog(){
        logScreenView(GaScreenName.SCREEN_EDIT_NICKNAME, this::class.java.simpleName)
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
                        showCustomToast(this@MyInfoActivity, "저장되었습니다.")

                        setCropLog()

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

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 권한이 허용된 경우 크롭 시작
                startCrop()
            } else {
                // 권한이 거부된 경우 사용자에게 알림
                showCustomToast(this, "권한이 필요합니다.")
            }
        }
    }


}