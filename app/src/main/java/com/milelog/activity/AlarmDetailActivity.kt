package com.milelog.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View.VISIBLE
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.milelog.R

class AlarmDetailActivity :BaseRefreshActivity() {
    lateinit var wv_alarm_detail:WebView
    lateinit var url:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_detail)


        url = intent.getStringExtra("url")!!
        setWebview()
    }

    fun setWebview(){
        wv_alarm_detail = findViewById(R.id.wv_alarm_detail)
        wv_alarm_detail.visibility = VISIBLE
        wv_alarm_detail.settings.loadWithOverviewMode = true // 화면에 맞게 WebView 사이즈를 정의
        wv_alarm_detail.settings.useWideViewPort = true //html 컨텐츠가 웹뷰에 맞게 나타나도록 합니다.
        wv_alarm_detail.settings.defaultTextEncodingName = "UTF-8" // TextEncoding 이름 정의
        wv_alarm_detail.settings.javaScriptEnabled = true
        wv_alarm_detail.settings.userAgentString = "Mozilla/5.0 AppleWebKit/535.19 Chrome/56.0.0 Mobile Safari/535.19"
        wv_alarm_detail.settings.domStorageEnabled = true
        wv_alarm_detail.settings.cacheMode = WebSettings.LOAD_DEFAULT
        wv_alarm_detail.settings.textZoom = 100 // System 텍스트 사이즈 변경되지 않게

        wv_alarm_detail.clearCache(true)
        wv_alarm_detail.clearHistory()
        CookieManager.getInstance().removeAllCookie()
        CookieManager.getInstance().removeSessionCookie()


        //chrome inspect 디버깅 모드
        WebView.setWebContentsDebuggingEnabled(true)

        wv_alarm_detail.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
        }

        wv_alarm_detail.webViewClient = object: WebViewClient(){
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

        wv_alarm_detail.loadUrl(url)

        // 쿠키 설정
        syncCookie()
    }


    private fun syncCookie(){
        wv_alarm_detail.settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(wv_alarm_detail, true)
        cookieManager.flush()
    }
}