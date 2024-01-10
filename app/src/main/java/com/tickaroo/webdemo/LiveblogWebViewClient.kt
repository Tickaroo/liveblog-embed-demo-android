package com.tickaroo.webdemo

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.Locale

class LiveblogWebViewClient(
  private val startIntentCallback: (Intent) -> Unit,
  private val pageStartedCallback: () -> Unit,
  private val pageFinishedCallback: () -> Unit,
  private val errorCallback: () -> Unit,
) : WebViewClient() {
  @Deprecated("Deprecated in Java")
  override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = when {
    url.lowercase(Locale.getDefault()).startsWith("mailto:") -> {
      startIntentCallback(Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse(url)
      })
      true
    }
    else -> {
      startIntentCallback(Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
      })
      true
    }
  }

  override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
    if (request.url.scheme?.lowercase(Locale.getDefault()) == "mailto") {
      startIntentCallback(Intent(Intent.ACTION_SENDTO).apply {
        data = request.url
      })
      true
    } else {
      startIntentCallback(Intent(Intent.ACTION_VIEW).apply {
        data = request.url
      })
      true
    }

  override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
    super.onPageStarted(view, url, favicon)
    pageStartedCallback()
  }

  override fun onPageFinished(view: WebView?, url: String) {
    super.onPageFinished(view, url)
    pageFinishedCallback()
  }

  @Deprecated("Deprecated in Java")
  override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
    Log.d("TickarooWebEmbed", "error: $errorCode, $description")
    errorCallback()
  }

  override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Log.d("TickarooWebEmbed", "error: ${error?.errorCode}, ${error?.description}")
    }
    if (request?.isForMainFrame == true) {
      errorCallback
    }
  }
}