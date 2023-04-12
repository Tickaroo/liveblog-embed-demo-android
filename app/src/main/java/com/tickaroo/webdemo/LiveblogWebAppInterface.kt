package com.tickaroo.webdemo

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface

class LiveblogWebAppInterface(private val context: Context) {
  @JavascriptInterface
  fun copyToClipboard(text: String?) {
    val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Liveblog Sharing", text)
    clipboard.setPrimaryClip(clip)
  }
}