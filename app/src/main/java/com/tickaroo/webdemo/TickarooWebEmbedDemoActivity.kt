package com.tickaroo.webdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.tickaroo.webdemo.fragments.LiveblogEmbedCodeFragment
import com.tickaroo.webdemo.fragments.LiveblogEmbedRecyclerViewFragment
import com.tickaroo.webdemo.fragments.LiveblogEmbedUrlFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class TickarooWebEmbedDemoActivity : AppCompatActivity() {
  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    viewPager.offscreenPageLimit = 2
    val adapter = EmbedDemoAdapter(supportFragmentManager)
    viewPager.adapter = adapter
    viewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
      override fun onPageSelected(position: Int) {
        when (position) {
          0 -> bottomNavigation.menu.findItem(R.id.menu_liveblog_embed_url).isChecked = true
          1 -> bottomNavigation.menu.findItem(R.id.menu_liveblog_embed_code).isChecked = true
          2 -> bottomNavigation.menu.findItem(R.id.menu_liveblog_rv).isChecked = true
        }
      }

    })

    bottomNavigation.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.menu_liveblog_embed_url -> viewPager.currentItem = 0
        R.id.menu_liveblog_embed_code -> viewPager.currentItem = 1
        R.id.menu_liveblog_rv -> viewPager.currentItem = 2
      }
      true
    }
  }

  inner class EmbedDemoAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    private val urlFragment = LiveblogEmbedUrlFragment()
    private val codeFragment = LiveblogEmbedCodeFragment()
    private val rvFragment = LiveblogEmbedRecyclerViewFragment()

    override fun getCount(): Int = 3

    override fun getItem(position: Int) = when (position) {
      0 -> urlFragment
      1 -> codeFragment
      else -> rvFragment
    }

  }
}