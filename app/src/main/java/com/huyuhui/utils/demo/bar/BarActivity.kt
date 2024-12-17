package com.huyuhui.utils.demo.bar

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.viewpager2.widget.ViewPager2
import com.huyuhui.utils.bar.BarUtils
import com.huyuhui.utils.demo.BaseActivity
import com.huyuhui.utils.demo.R
import com.huyuhui.utils.demo.databinding.ActivityBarBinding
import com.huyuhui.utils.demo.evaluate
import com.huyuhui.utils.demo.placeholder.PlaceholderFragment
import kotlin.math.abs

class BarActivity : BaseActivity<ActivityBarBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragments =
            mutableListOf(PlaceholderFragment.newInstance(1), PlaceholderFragment.newInstance(2))
        binding.viewpager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter =
                ViewPager2Adapter(fragmentManager = supportFragmentManager, lifecycle, fragments)
        }
        val barUtils = BarUtils(window)
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
                Color.parseColor("#22050505"),
                Color.TRANSPARENT
            )
        }
        setSupportActionBar(binding.toolbar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            barUtils.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        barUtils.setStatusBarColor(Color.parseColor("#22050505")).setStatusBarLightMode(false)
            .setNavBarColor(Color.parseColor("#22050505")).setStatusBarLightMode(true)
            .setStatusBarVisibility(false)
            .titleView(binding.toolbar).immerse()
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