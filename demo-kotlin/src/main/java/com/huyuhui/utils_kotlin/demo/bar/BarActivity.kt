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
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                "#22050505".toColorInt(),
                "#22050505".toColorInt()
            ),
            navigationBarStyle = SystemBarStyle.light(
                "#22050505".toColorInt(),
                "#22050505".toColorInt()
            )
        )
        val barUtils = BarUtils(window)
        barUtils.apply {
            isStatusBarLightMode = false
            isNavBarLightMode = true
            setStatusBarVisibility(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                @Suppress("DEPRECATION")
                navBarColor = Color.WHITE
            }
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
            //android15 以上设置这个没效果，直接设置appbar的颜色就会影响statusBarColor的颜色了
            @Suppress("DEPRECATION")
            barUtils.statusBarColor = evaluate(
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange + 0.5f,
                "#22050505".toColorInt(),
                "#22050505".toColorInt()
            )
        }
        setSupportActionBar(binding.toolbar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            barUtils.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        //让collapsingToolbarLayout不要拦截toolbar的insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.collapsingToolbarLayout) { v: View, insets: WindowInsetsCompat ->
            return@setOnApplyWindowInsetsListener insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v: View, insets: WindowInsetsCompat ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = statusBarInsets.top)
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