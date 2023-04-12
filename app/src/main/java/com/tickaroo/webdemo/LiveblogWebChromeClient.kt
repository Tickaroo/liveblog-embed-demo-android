package com.tickaroo.webdemo

import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebView

class LiveblogWebChromeClient: WebChromeClient() {
  override fun onCreateWindow(view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: Message): Boolean {
    val newWebView = WebView(view.context)
    val transport = resultMsg.obj as WebView.WebViewTransport
    transport.webView = newWebView
    resultMsg.sendToTarget()
    return true
  }
}