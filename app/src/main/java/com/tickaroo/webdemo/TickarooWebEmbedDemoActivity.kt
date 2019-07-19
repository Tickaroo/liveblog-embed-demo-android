package com.tickaroo.webdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.customViewContainer
import kotlinx.android.synthetic.main.activity_main.loadingView
import kotlinx.android.synthetic.main.activity_main.refresh_container
import kotlinx.android.synthetic.main.activity_main.webView

class TickarooWebEmbedDemoActivity : Activity() {
  private var customViewCallback: WebChromeClient.CustomViewCallback? = null
  private val webChromeClient = MainWebChromeClient()

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    CookieManager.getInstance().setAcceptCookie(true)
    CookieManager.setAcceptFileSchemeCookies(true)
    CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
    webView.settings.run {
      javaScriptEnabled = true
      useWideViewPort = true
      loadWithOverviewMode = true
      setSupportMultipleWindows(true)
    }
    webView.webChromeClient = webChromeClient
    webView.webViewClient = MainWebViewClient()

    refresh_container.setOnClickListener {
      loadingView.visibility = View.VISIBLE
      webView.visibility = View.INVISIBLE
      webView.reload()
      refresh_container.visibility = View.GONE
    }

  }

  override fun onResume() {
    super.onResume()
    if (webView.url.isNullOrEmpty()) {
      webView.loadUrl("http://builds.tickaroo.net/feedtest/embedjs-tik3-nordbayern-video-inline.html")
    } else {
      webView.loadUrl("javascript:window.location.reload( true )")
    }
  }

  override fun onBackPressed() {
    if (customViewContainer.visibility == View.VISIBLE) {
      webChromeClient.onHideCustomView()
    }
    if (webView.canGoBack()) {
      webView.goBack()
    } else {
      super.onBackPressed()
    }
  }

  inner class MainWebChromeClient : WebChromeClient() {
    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
      customViewCallback = callback
      customViewContainer.addView(view)
      customViewContainer.visibility = View.VISIBLE
      window?.decorView?.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onShowCustomView(view: View?, requestedOrientation: Int, callback: CustomViewCallback?) {
      onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
      window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

      if (customViewContainer.childCount == 0) {
        return
      }
      customViewContainer.removeAllViews()
      customViewContainer.visibility = View.GONE
      customViewCallback?.onCustomViewHidden()
    }

    override fun onCreateWindow(view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: Message): Boolean {
      val newWebView = WebView(view.context)
      val transport = resultMsg.obj as WebView.WebViewTransport
      transport.webView = newWebView
      resultMsg.sendToTarget()
      return true
    }
  }

  inner class MainWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = when {
      url.toLowerCase().startsWith("intent") -> {
        true
      }
      url.toLowerCase().startsWith("mailto://") -> {
        startActivity(Intent(Intent.ACTION_SENDTO).apply {
          data = Uri.parse(url)
        })
        true
      }
      else -> super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
      if (request.url.scheme?.toLowerCase() == "intent") {
        return true
      } else if (request.url.scheme?.toLowerCase() == "mailto") {
        startActivity(Intent(Intent.ACTION_SENDTO).apply {
          data = request.url
        })
        return true
      }

      return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String) {
      super.onPageFinished(view, url)
      loadingView?.visibility = View.GONE
      webView?.visibility = View.VISIBLE
    }

    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
      Toast.makeText(this@TickarooWebEmbedDemoActivity, "Etwas ist schief gelaufen.", Toast.LENGTH_LONG).show()
      finish()
      return true
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
      refresh_container.visibility = View.VISIBLE
      Log.d("TickarooWebEmbed", "error: $errorCode, $description")
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
      refresh_container.visibility = View.VISIBLE
      if (VERSION.SDK_INT >= VERSION_CODES.M) {
        Log.d("TickarooWebEmbed", "error: ${error?.errorCode}, ${error?.description}")
      }
    }
  }
}