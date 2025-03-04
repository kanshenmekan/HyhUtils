package com.huyuhui.utils_kotlin.demo.bar

import android.os.Bundle
import com.google.android.material.navigation.NavigationBarView
import com.huyuhui.utils_kotlin.demo.bar.fragments.AFragment
import com.huyuhui.utils_kotlin.demo.bar.fragments.BFragment
import com.huyuhui.utils_kotlin.demo.bar.fragments.CFragment
import com.huyuhui.utils_kotlin.demo.BaseActivity
import com.huyuhui.utils_kotlin.demo.R
import com.huyuhui.utils_kotlin.demo.databinding.ActivityBarFragmentsBinding

class BarFragmentsActivity : BaseActivity<ActivityBarFragmentsBinding>() {
    private val aFragment = AFragment()
    private val bFragment = BFragment()
    private val cFragment = CFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onItemSelectedListener = NavigationBarView.OnItemSelectedListener { menuItem ->
            val transaction = supportFragmentManager.beginTransaction()
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    transaction
                        .hide(bFragment).hide(cFragment).show(aFragment)
//                    binding.toolbar.visibility = View.GONE
//                    val barHelper = BarHelper(window)
//                    barHelper.setStatusBarColor(Color.TRANSPARENT)
//                        .setNavBarColor(Color.parseColor("#22050505"))
//                        .immerse(true,WindowInsetsCompat.Type.statusBars()).apply()
                }

                R.id.navigation_dashboard -> {
                    transaction.hide(aFragment).hide(cFragment).show(bFragment)
//                    binding.toolbar.visibility = View.VISIBLE
//                    val barHelper = BarHelper(window)
//                    barHelper.setStatusBarColor(Color.TRANSPARENT).setStatusBarLightMode(true)
//                        .titleView(binding.toolbar)
//                        .setNavBarVisible(false)
//                        .immerse(true).apply()
                }

                R.id.navigation_notifications -> {
                    transaction.hide(aFragment).hide(bFragment).show(cFragment)
//                    binding.toolbar.visibility = View.VISIBLE
//                    val barHelper = BarHelper(window)
//                    barHelper.setStatusBarColor(Color.GREEN).setStatusBarLightMode(false)
//                        .immerse(false).apply()
                }
            }
            transaction.commit()
            true
        }
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, aFragment)
                .add(R.id.fragment_container, bFragment).add(R.id.fragment_container, cFragment)
                .hide(bFragment)
                .hide(cFragment)
                .show(aFragment)
            onItemSelectedListener.onNavigationItemSelected(binding.navView.menu.getItem(0))
        }.commit()
        binding.navView.setOnItemSelectedListener(onItemSelectedListener)
    }
}