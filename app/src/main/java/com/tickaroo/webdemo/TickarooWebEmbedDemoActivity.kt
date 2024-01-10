package com.tickaroo.webdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.tickaroo.webdemo.databinding.ActivityMainBinding
import com.tickaroo.webdemo.fragments.LiveblogEmbedCodeFragment
import com.tickaroo.webdemo.fragments.LiveblogEmbedRecyclerViewFragment
import com.tickaroo.webdemo.fragments.LiveblogEmbedUrlFragment
import java.util.*


class TickarooWebEmbedDemoActivity : AppCompatActivity() {
  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Create all Fragments at once to prevent jumping in RecyclerView
    binding.viewPager.offscreenPageLimit = 2

    val adapter = EmbedDemoAdapter(activity = this)
    binding.viewPager.adapter = adapter
    binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        when (position) {
          0 -> binding.bottomNavigation.menu.findItem(R.id.menu_liveblog_embed_url).isChecked = true
          1 -> binding.bottomNavigation.menu.findItem(R.id.menu_liveblog_embed_code).isChecked = true
          2 -> binding.bottomNavigation.menu.findItem(R.id.menu_liveblog_rv).isChecked = true
        }
      }

    })

    binding.bottomNavigation.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.menu_liveblog_embed_url -> binding.viewPager.currentItem = 0
        R.id.menu_liveblog_embed_code -> binding.viewPager.currentItem = 1
        R.id.menu_liveblog_rv -> binding.viewPager.currentItem = 2
      }
      true
    }
  }

  class EmbedDemoAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int) = when (position) {
      0 -> LiveblogEmbedUrlFragment()
      1 -> LiveblogEmbedCodeFragment()
      else -> LiveblogEmbedRecyclerViewFragment()
    }

  }
}