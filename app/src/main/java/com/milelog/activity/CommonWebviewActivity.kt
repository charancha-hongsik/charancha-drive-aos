package com.milelog.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View.VISIBLE
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.milelog.BuildConfig.BASE_TERMS_URL
import com.milelog.PreferenceUtil
import com.milelog.R
import gun0912.tedimagepicker.builder.TedImagePicker
import java.io.File
import java.io.FileOutputStream

class CommonWebviewActivity: BaseActivity() {
    lateinit var wv_common:WebView
    var url:String = BASE_TERMS_URL

    /**
     * firebase
     */
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    // 이미지를 압축하여 40% 퀄리티로 저장하는 함수
    fun compressImage(uri: Uri, context: Context): File? {
        try {
            // 선택된 Uri에서 이미지 경로를 얻고, Bitmap으로 변환
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

            // 압축된 파일 경로
            val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(compressedFile)

            // Bitmap을 압축하여 파일로 저장 (70% 품질)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.flush()
            outputStream.close()

            return compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_webview)

        intent.getStringExtra("url")?.let{
            url = it
        }

        init()
        setWebview()
    }

    private fun init(){
        wv_common = findViewById(R.id.wv_common)
    }

    fun setWebview(){
        wv_common.visibility = VISIBLE
        wv_common.settings.loadWithOverviewMode = true // 화면에 맞게 WebView 사이즈를 정의
        wv_common.settings.useWideViewPort = true //html 컨텐츠가 웹뷰에 맞게 나타나도록 합니다.
        wv_common.settings.defaultTextEncodingName = "UTF-8" // TextEncoding 이름 정의
        wv_common.settings.javaScriptEnabled = true
        wv_common.settings.userAgentString = "Mozilla/5.0 AppleWebKit/535.19 Chrome/56.0.0 Mobile Safari/535.19"
        wv_common.settings.domStorageEnabled = true
        wv_common.settings.cacheMode = WebSettings.LOAD_DEFAULT
        wv_common.settings.textZoom = 100 // System 텍스트 사이즈 변경되지 않게
        wv_common.settings.allowContentAccess = true
        wv_common.settings.allowFileAccess = true

        //chrome inspect 디버깅 모드
        WebView.setWebContentsDebuggingEnabled(true)

        // javascriptInterface 설정
        wv_common.addJavascriptInterface(MilelogPublicApi(this@CommonWebviewActivity), "MilelogPublicApi")

        wv_common.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback = filePathCallback

                return true

            }
        }

        wv_common.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                syncCookie()
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        wv_common.loadUrl(url)

        // 쿠키 설정
        syncCookie()
    }


    private fun syncCookie(){
        wv_common.settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(wv_common, true)
        cookieManager.flush()
    }

    class MilelogPublicApi(val activity: CommonWebviewActivity) {
        @JavascriptInterface
        fun openMyPage(){
            activity.startActivity(Intent(activity, MyPageActivity::class.java))
            Log.d("testsetestestset","testestestestset ::openMyPage ")
        }

        @JavascriptInterface
        fun openNotification(){
            activity.startActivity(Intent(activity, AlarmActivity::class.java))
            Log.d("testsetestestset","testestestestset ::openNotification ")

        }

        @JavascriptInterface
        fun openMyGarage(){
            activity.startActivity(Intent(activity, MyGarageActivity::class.java))
            Log.d("testsetestestset","testestestestset ::openMyGarage ")

        }

        @JavascriptInterface
        fun openDrivingDetail(trackingId:String){
            activity.startActivity(Intent(activity, DetailDriveHistoryActivity::class.java).putExtra("trackingId", trackingId))
            Log.d("testsetestestset","testestestestset ::openDrivingDetail ")

        }

        @JavascriptInterface
        fun openDrivings(){
            activity.startActivity(Intent(activity, MyDriveHistoryActivity::class.java))
            Log.d("testsetestestset","testestestestset ::openDrivings ")

        }

        @JavascriptInterface
        fun openDrivingDistanceStats(userCarId: String){
            activity.startActivity(Intent(activity, DrivenDistanceActivity::class.java).putExtra("userCarId", userCarId))
            Log.d("testsetestestset","testestestestset ::openDrivingDistanceStats ")

        }

        @JavascriptInterface
        fun openAverageDrivingDistanceStats(userCarId: String){
            activity.startActivity(Intent(activity, AverageDrivenDistanceActivity::class.java).putExtra("userCarId", userCarId))
            Log.d("testsetestestset","testestestestset ::openAverageDrivingDistanceStats ")

        }

        @JavascriptInterface
        fun openDrivingTimeStats(userCarId: String){
            activity.startActivity(Intent(activity, DrivenTimeActivity::class.java).putExtra("userCarId", userCarId))
            Log.d("testsetestestset","testestestestset ::openDrivingTimeStats ")

        }


        @JavascriptInterface
        fun openDrivingScoreStats(userCarId: String){
            activity.startActivity(Intent(activity, MyScoreActivity::class.java).putExtra("userCarId", userCarId))
            Log.d("testsetestestset","testestestestset ::openDrivingScoreStats :: " + userCarId)
        }

        @JavascriptInterface
        fun getAccessToken():String{
            return PreferenceUtil.getPref(activity, PreferenceUtil.ACCESS_TOKEN, "")!!
        }

        @JavascriptInterface
        fun getRefreshToken():String{
            return PreferenceUtil.getPref(activity, PreferenceUtil.REFRESH_TOKEN, "")!!
        }

        @JavascriptInterface
        fun closeWebview(){
            activity.finish()
        }

        @JavascriptInterface
        fun openBrowser(url:String){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.chrome")
            try {
                activity.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                // Chrome browser presumably not installed so allow user to choose instead
                intent.setPackage(null)
                activity.startActivity(intent)
            }
        }

        @JavascriptInterface
        fun openImagePicker(currentCount:Int, maxCount:Int){
            try {
                // Use TedImagePicker for selecting multiple images
                TedImagePicker.with(activity)
                    .max(maxCount-currentCount, "최대 " + maxCount + "개까지 등록할 수 있습니다." )
                    .cancelListener {
                        activity.fileChooserCallback?.onReceiveValue(null)
                        activity.fileChooserCallback = null
                    }
                    .startMultiImage { uriList ->
                        if (uriList.isEmpty()) {
                            activity.fileChooserCallback?.onReceiveValue(null)
                        } else {
                            // Compress selected images and collect compressed URIs
                            val compressedUris = mutableListOf<Uri>()
                            for (selectedUri in uriList) {
                                val compressedFile = activity.compressImage(selectedUri, activity)
                                if (compressedFile != null) {
                                    val compressedUri = Uri.fromFile(compressedFile)
                                    compressedUris.add(compressedUri)
                                } else {
                                    Log.d("test", "Compressed file is null for URI: $selectedUri")
                                }
                            }

                            // Pass compressed URIs to the file chooser callback
                            if (compressedUris.isNotEmpty()) {
                                activity.fileChooserCallback?.onReceiveValue(compressedUris.toTypedArray())
                            } else {
                                activity.fileChooserCallback?.onReceiveValue(null)
                            }
                        }
                    }
            } catch (e: Exception) {
                activity.fileChooserCallback?.onReceiveValue(null)
                activity.fileChooserCallback = null
            }
        }
    }

    override fun onBackPressed() {
        if (wv_common.canGoBack()) {
            wv_common.goBack()
        } else {
            // Finish the activity if WebView cannot go back
            super.onBackPressed()
        }
    }
}