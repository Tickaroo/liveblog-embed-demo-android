package com.tickaroo.webdemo.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tickaroo.webdemo.R
import com.tickaroo.webdemo.setupLiveblogWebView
import kotlinx.android.synthetic.main.fragment_liveblog.*

class LiveblogEmbedCodeFragment : Fragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
    inflater.inflate(R.layout.fragment_liveblog, container, false)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    refreshContainer.setOnClickListener {
      loadingView.visibility = View.VISIBLE
      webView.visibility = View.INVISIBLE
      webView.reload()
      refreshContainer.visibility = View.GONE
    }

    webView.setupLiveblogWebView(
      loadingView = loadingView,
      refreshContainer = refreshContainer,
    )

    webView.loadDataWithBaseURL(
      getString(R.string.liveblogUrl),
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