package com.huyuhui.utils_kotlin.demo.bar

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import com.google.android.material.navigation.NavigationBarView
import com.huyuhui.hyhutilskotlin.bar.BarUtils
import com.huyuhui.utils_kotlin.demo.BaseActivity
import com.huyuhui.utils_kotlin.demo.R
import com.huyuhui.utils_kotlin.demo.bar.fragments.AFragment
import com.huyuhui.utils_kotlin.demo.bar.fragments.BFragment
import com.huyuhui.utils_kotlin.demo.bar.fragments.CFragment
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
                }

                R.id.navigation_dashboard -> {
                    transaction.hide(aFragment).hide(cFragment).show(bFragment)
                }

                R.id.navigation_notifications -> {
                    transaction.hide(aFragment).hide(bFragment).show(cFragment)
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
            onItemSelectedListener.onNavigationItemSelected(binding.navView.menu[0])
        }.commit()
        binding.navView.setOnItemSelectedListener(onItemSelectedListener)
        enableEdgeToEdge()
        BarUtils.with(this).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
            }
            isStatusBarLightMode = false
        }
    }
}