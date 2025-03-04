package com.huyuhui.utils_kotlin.demo.bar.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import com.huyuhui.hyhutilskotlin.bar.BarConfig
import com.huyuhui.hyhutilskotlin.bar.apply
import com.huyuhui.utils_kotlin.demo.BaseFragment
import com.huyuhui.utils_kotlin.demo.databinding.FragmentCBinding


class CFragment : BaseFragment<FragmentCBinding>() {
    private var isImmerse = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tv.setOnClickListener {
//            if (isImmerse){
//                barUtils.exitImmerse().statusBarColor = Color.BLUE
//                isImmerse = false
//            }else{
//                barUtils.immerse(WindowInsetsCompat.Type.statusBars()).titleView(binding.tv).statusBarColor = Color.TRANSPARENT
//                isImmerse = true
//            }
            if (isImmerse){
                barUtils.exitImmerse().statusBarColor = Color.BLUE
                isImmerse = false
            }else{
                barUtils.immerse(WindowInsetsCompat.Type.statusBars()).titleView(binding.tv).statusBarColor = Color.TRANSPARENT
                isImmerse = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden){
            val barConfig = BarConfig.Builder().statusBarColor(Color.TRANSPARENT)
                .isStatusBarLightMode(false)
                .navBarVisible(false).immerse(WindowInsetsCompat.Type.statusBars()).titleView(binding.tv).build()
            barUtils.apply(barConfig)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            val barConfig = BarConfig.Builder().statusBarColor(Color.TRANSPARENT)
                .isStatusBarLightMode(false)
                .navBarVisible(false).immerse(WindowInsetsCompat.Type.statusBars()).titleView(binding.tv).build()
            barUtils.apply(barConfig)
        }
    }
}