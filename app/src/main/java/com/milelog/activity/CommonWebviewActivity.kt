package com.milelog.activity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View.VISIBLE
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import com.milelog.BuildConfig.BASE_TERMS_URL
import com.milelog.R
import com.milelog.activity.LoginActivity.MilelogPublicApi

class CommonWebviewActivity: BaseActivity() {
    lateinit var wv_common:WebView
    var url:String = BASE_TERMS_URL
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

        //chrome inspect 디버깅 모드
        WebView.setWebContentsDebuggingEnabled(true)

        // javascriptInterface 설정
        wv_common.addJavascriptInterface(MilelogPublicApi(this@CommonWebviewActivity), "MilelogPublicApi")

        wv_common.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
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
        fun closeWebview(){
            activity.finish()
        }
    }
}