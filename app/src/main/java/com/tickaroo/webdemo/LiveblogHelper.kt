package com.tickaroo.webdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupLiveblogWebView(
  loadingView: View? = null,
  refreshContainer: View? = null,
  pageStartedCallback: () -> Unit = {},
  pageFinishedCallback: () -> Unit = {},
  scrollCallback: (Int) -> Unit = {},
) {
  settings.run {
    javaScriptEnabled = true
    useWideViewPort = true
    loadWithOverviewMode = true
    setSupportMultipleWindows(true)
  }
  addJavascriptInterface(
    LiveblogWebAppInterface(
      context = context,
      scrollCallback = scrollCallback,
    ), "tik4Android"
  )

  webChromeClient = LiveblogWebChromeClient()
  webViewClient = LiveblogWebViewClient(
    startIntentCallback = { intent ->
      (context as? Activity)?.apply {
        if (intent.resolveActivity(packageManager) != null) {
          startActivity(intent)
        }
      }
    },
    pageStartedCallback = pageStartedCallback,
    pageFinishedCallback = {
      pageFinishedCallback()
      loadingView?.visibility = View.GONE
      visibility = View.VISIBLE
    },
    errorCallback = {
      refreshContainer?.visibility = View.VISIBLE
    },
  )
}