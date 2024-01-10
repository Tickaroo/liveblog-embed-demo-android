package com.tickaroo.webdemo.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.WebView
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tickaroo.webdemo.R
import com.tickaroo.webdemo.databinding.FragmentLiveblogRecyclerviewBinding
import com.tickaroo.webdemo.setupLiveblogWebView

class LiveblogEmbedRecyclerViewFragment : Fragment() {
  private var binding: FragmentLiveblogRecyclerviewBinding? = null
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    binding = FragmentLiveblogRecyclerviewBinding.inflate(inflater, container, false)
    return binding!!.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val adapter = LiveblogEmbedAdapter()
    binding?.apply {
      liveblogRecyclerView.layoutManager = LinearLayoutManager(view.context)
      liveblogRecyclerView.adapter = adapter
      adapter.data = listOfNotNull(
        UiBox(Color.GREEN),
        UiBox(Color.YELLOW),
        UiLiveBlog(liveblogUrl = getString(R.string.liveblogUrl)),
        UiBox(Color.RED),
        UiBox(Color.MAGENTA),
        UiBox(Color.CYAN),
      )
    }
  }

  sealed interface IUiScreenItem
  data class UiBox(@ColorInt val color: Int) : IUiScreenItem
  data class UiLiveBlog(val liveblogUrl: String) : IUiScreenItem

  class LiveblogEmbedAdapter : RecyclerView.Adapter<ViewHolder>() {
    companion object {
      private const val VIEW_TYPE_BOX = 0
      private const val VIEW_TYPE_LIVEBLOG = 1
    }

    var data: List<IUiScreenItem> = emptyList()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
      when (viewType) {
        VIEW_TYPE_BOX -> BoxViewHolder(view = View(parent.context))
        VIEW_TYPE_LIVEBLOG -> LiveblogViewHolder(webView = WebView(parent.context))
        else -> throw IllegalArgumentException("wrong viewType $viewType")
      }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = data.getOrNull(position) ?: throw IllegalArgumentException("missing data at position $position")
      when {
        item is UiBox && holder is BoxViewHolder -> holder.itemView.setBackgroundColor(item.color)
        item is UiLiveBlog && holder is LiveblogViewHolder -> holder.loadUrl(url = item.liveblogUrl)
      }
    }

    override fun getItemViewType(position: Int) =
      when (data.getOrNull(position)) {
        is UiBox -> VIEW_TYPE_BOX
        is UiLiveBlog -> VIEW_TYPE_LIVEBLOG
        else -> -1
      }
  }

  class BoxViewHolder(view: View) : ViewHolder(view) {
    init {
      view.layoutParams = MarginLayoutParams(MATCH_PARENT, 400).apply {
        bottomMargin = 20
      }
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  class LiveblogViewHolder(private val webView: WebView) : ViewHolder(webView) {

    init {
      webView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      webView.setupLiveblogWebView()

      // don’t scroll up on first tap
      webView.isFocusable = false
      webView.isFocusableInTouchMode = false
    }

    fun loadUrl(url: String) {
      webView.loadUrl(url)
    }
  }
}