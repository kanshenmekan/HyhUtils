package com.huyuhui.utils.demo.bar.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.huyuhui.utils.bar.BarConfig
import com.huyuhui.utils.demo.BaseFragment
import com.huyuhui.utils.demo.databinding.FragmentBBinding

class BFragment : BaseFragment<FragmentBBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden){
            val barConfig = BarConfig.Builder().statusBarColor(Color.GREEN)
                .isStatusBarLightMode(false)
                .navBarVisible(true).build()
            barConfig.apply(barUtils)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            val barConfig = BarConfig.Builder().statusBarColor(Color.GREEN)
                .isStatusBarLightMode(false)
                .navBarVisible(true).build()
            barConfig.apply(barUtils)
        }
    }
}