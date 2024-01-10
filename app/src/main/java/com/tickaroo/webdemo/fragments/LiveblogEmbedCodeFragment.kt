package com.tickaroo.webdemo.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tickaroo.webdemo.R
import com.tickaroo.webdemo.databinding.FragmentLiveblogBinding
import com.tickaroo.webdemo.setupLiveblogWebView

class LiveblogEmbedCodeFragment : Fragment() {
  private var binding: FragmentLiveblogBinding? = null
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    binding = FragmentLiveblogBinding.inflate(inflater, container, false)
    return binding!!.root
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding?.apply {
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
}