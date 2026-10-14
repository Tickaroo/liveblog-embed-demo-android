package com.tickaroo.webdemo

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface

class LiveblogWebAppInterface(
  private val context: Context,
  private val scrollCallback: (top: Double, correction: Boolean, jumpId: Int) -> Unit,
) {
  @JavascriptInterface
  fun copyToClipboard(text: String?) {
    val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Liveblog Sharing", text)
    clipboard.setPrimaryClip(clip)
  }

  // top is a Double because the JS bridge truncates a number passed to an int parameter,
  // and the lost fraction would then be multiplied by the display density.
  @JavascriptInterface
  fun tickarooLiveblogScrollTo(top: Double, correction: Boolean, jumpId: Int) {
    Log.d("TickarooWebEmbed", "tickarooLiveblogScrollTo top=$top correction=$correction jumpId=$jumpId")
    scrollCallback(top, correction, jumpId)
  }
}
