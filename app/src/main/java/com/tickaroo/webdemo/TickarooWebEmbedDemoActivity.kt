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
import android.webkit.*
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class TickarooWebEmbedDemoActivity : Activity() {
  private var customViewCallback: WebChromeClient.CustomViewCallback? = null
  private val webChromeClient = MainWebChromeClient()

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    CookieManager.getInstance().setAcceptCookie(true)
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
      webView.loadDataWithBaseURL(
        "http://www.tickaroo.com/",
        getString(
          R.string.tickaroo_ticker_embed,
          getString(R.string.clientId),
          getString(R.string.liveblogId),
          getString(R.string.themeId),
        ), "text/html",
        "UTF-8", null
      )
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

      WindowCompat.setDecorFitsSystemWindows(window, false)
      WindowInsetsControllerCompat(window, customViewContainer).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    }

    override fun onShowCustomView(view: View?, requestedOrientation: Int, callback: CustomViewCallback?) {
      onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
      WindowCompat.setDecorFitsSystemWindows(window, true)
      WindowInsetsControllerCompat(window, customViewContainer).show(WindowInsetsCompat.Type.systemBars())

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
      url.lowercase(Locale.getDefault()).startsWith("mailto://") -> {
        startActivity(Intent(Intent.ACTION_SENDTO).apply {
          data = Uri.parse(url)
        })
        true
      }
      else -> super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
      if (request.url.scheme?.lowercase(Locale.getDefault()) == "mailto") {
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
      if (request?.isForMainFrame == true) {
        refresh_container.visibility = View.VISIBLE
      }
      if (VERSION.SDK_INT >= VERSION_CODES.M) {
        Log.d("TickarooWebEmbed", "error: ${error?.errorCode}, ${error?.description}")
      }
    }
  }
}