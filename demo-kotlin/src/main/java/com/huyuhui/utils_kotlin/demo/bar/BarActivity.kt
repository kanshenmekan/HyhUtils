package com.huyuhui.utils_kotlin.demo.bar

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.huyuhui.hyhutilskotlin.bar.BarUtils
import com.huyuhui.utils_kotlin.demo.BaseActivity
import com.huyuhui.utils_kotlin.demo.R
import com.huyuhui.utils_kotlin.demo.databinding.ActivityBarBinding
import com.huyuhui.utils_kotlin.demo.evaluate
import com.huyuhui.utils_kotlin.demo.placeholder.PlaceholderFragment
import kotlin.math.abs

class BarActivity : BaseActivity<ActivityBarBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.tv.bringToFront()
        binding = ActivityBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val barUtils = BarUtils(window)
        barUtils.apply {
            statusBarColor = "#22050505".toColorInt()
            isStatusBarLightMode = false
            navBarColor = "#22050505".toColorInt()
            isNavBarLightMode = true
            setStatusBarVisibility(false)
            titleView(binding.toolbar)
            immerse(WindowInsetsCompat.Type.statusBars())
        }
        val fragments =
            mutableListOf(PlaceholderFragment.newInstance(1), PlaceholderFragment.newInstance(2))
        binding.viewpager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter =
                ViewPager2Adapter(fragmentManager = supportFragmentManager, lifecycle, fragments)
        }
        binding.appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val alpha = abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            if (alpha > 0.3) {
                binding.toolbar.visibility = View.VISIBLE
                binding.toolbar.alpha = alpha
                barUtils.setStatusBarVisibility(true)
            } else {
                binding.toolbar.visibility = View.GONE
                barUtils.setStatusBarVisibility(false)
            }
//            binding.toolbar.alpha = abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            barUtils.statusBarColor = evaluate(
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange + 0.5f,
                "#22050505".toColorInt(),
                Color.TRANSPARENT
            )
        }
        setSupportActionBar(binding.toolbar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            barUtils.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.appbar) { v: View, insets: WindowInsetsCompat ->
            Log.e("123","${insets.getInsets(WindowInsetsCompat.Type.systemBars())}")
            return@setOnApplyWindowInsetsListener insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bar_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_more -> {
                startActivity(Intent(this@BarActivity, BarFragmentsActivity::class.java))
            }
        }
        return true
    }

}