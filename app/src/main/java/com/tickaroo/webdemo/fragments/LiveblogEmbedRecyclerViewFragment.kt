package com.tickaroo.webdemo.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
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
import kotlin.math.roundToInt

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

    private val dragCounter = DragCounter()

    var data: List<IUiScreenItem> = emptyList()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
      recyclerView.addOnScrollListener(dragCounter)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
      recyclerView.removeOnScrollListener(dragCounter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
      when (viewType) {
        VIEW_TYPE_BOX -> BoxViewHolder(view = View(parent.context))
        VIEW_TYPE_LIVEBLOG -> LiveblogViewHolder(webView = WebView(parent.context), dragCounter = dragCounter)
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

  // smoothScrollBy reports SETTLING, so DRAGGING only ever comes from the reader's own touch.
  class DragCounter : RecyclerView.OnScrollListener() {
    var count = 0
      private set

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      if (newState == RecyclerView.SCROLL_STATE_DRAGGING) count++
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
  class LiveblogViewHolder(
    private val webView: WebView,
    private val dragCounter: DragCounter,
  ) : ViewHolder(webView) {
    private var jumpId: Int? = null
    private var dragCountAtJump = 0
    private var pendingTop: Double? = null
    private var pendingUntil = 0L
    private var aimedWebViewTop: Int? = null

    init {
      webView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      webView.setupLiveblogWebView(
        pageStartedCallback = this::registerScrollCallback,
        pageFinishedCallback = this::registerScrollCallback,
        scrollCallback = { top, correction, jumpId ->
          webView.post { scrollToPost(top, correction, jumpId) }
        }
      )

      // The page reports a new top before this row has grown to the new content height, so near the end of the list
      // a scroll can run out of room. It is finished once the row has its new height.
      webView.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
        if (bottom - top != oldBottom - oldTop) webView.post { settle() }
      }

      // don’t scroll up on first tap
      webView.isFocusable = false
      webView.isFocusableInTouchMode = false
    }

    fun loadUrl(url: String) {
      webView.loadUrl(url)
    }

    private fun scrollToPost(top: Double, correction: Boolean, jumpId: Int) {
      // The embed sends the jump call before any correction, so a correction of an unknown jumpId means this view missed
      // that call; it is taken as the jump.
      if (!correction || jumpId != this.jumpId) {
        this.jumpId = jumpId
        dragCountAtJump = dragCounter.count
      } else if (dragCounter.count != dragCountAtJump) {
        Log.d("TickarooWebEmbed", "dropped correction of jumpId=$jumpId after a drag")
        return
      }
      pendingTop = top
      pendingUntil = SystemClock.uptimeMillis() + HOLD_MS
      scrollTo(top, correction)
    }

    private fun settle() {
      val top = pendingTop ?: return
      if (dragCounter.count != dragCountAtJump || SystemClock.uptimeMillis() > pendingUntil) {
        pendingTop = null
        return
      }
      if (scrollTo(top, correction = true) == 0) pendingTop = null
    }

    private fun scrollTo(top: Double, correction: Boolean): Int {
      val recyclerView = webView.parent as? RecyclerView ?: return 0

      val offsetViewBounds = Rect()
      webView.getDrawingRect(offsetViewBounds)
      recyclerView.offsetDescendantRectToMyCoords(webView, offsetViewBounds)
      val topInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, top.toFloat(), webView.context.resources.displayMetrics)
      val requested = offsetViewBounds.top + topInPx.roundToInt()

      // LinearLayoutManager estimates these from the visible rows; they are exact once the first or last row is visible.
      val offset = recyclerView.computeVerticalScrollOffset()
      val maxOffset = recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent()
      val scrollBy = (offset + requested).coerceIn(0, maxOf(maxOffset, 0)) - offset
      if (scrollBy != requested) Log.d("TickarooWebEmbed", "clamped scroll of jumpId=$jumpId from $requested to $scrollBy")

      // Re-aiming restarts the smooth scroll's easing, so a scroll already heading for this position is left alone.
      val settling = recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING
      val aim = offsetViewBounds.top - scrollBy
      if (settling && aim == aimedWebViewTop) return scrollBy
      aimedWebViewTop = aim

      // Once the jump has landed, an instant scroll keeps the post still while content above it grows.
      if (correction && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
        recyclerView.scrollBy(0, scrollBy)
      } else {
        recyclerView.smoothScrollBy(0, scrollBy)
      }
      return scrollBy
    }

    // Fixed arity: the JS bridge matches methods by argument count, and the embed passes info only to a function declaring two parameters.
    private fun registerScrollCallback() {
      webView.evaluateJavascript(
        "window.tickarooLiveblogScrollTo = function(top, info) { tik4Android.tickarooLiveblogScrollTo(top, !!(info && info.correction), info ? info.jumpId : 0) }",
        null,
      )
    }

    companion object {
      // The embed sends corrections for at most 10 s after the jump.
      private const val HOLD_MS = 10_000L
    }
  }
}