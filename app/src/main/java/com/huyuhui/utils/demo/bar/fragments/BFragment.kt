package com.huyuhui.utils.demo.bar.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.huyuhui.utils.bar.BarHelper
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
            val barHelper = BarHelper(requireActivity().window)
            barHelper.setStatusBarColor(Color.TRANSPARENT).setStatusBarLightMode(true)
                .titleView(binding.tv)
                .setNavBarVisible(false)
                .immerse(true).apply()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            val barHelper = BarHelper(requireActivity().window)
            barHelper.setStatusBarColor(Color.TRANSPARENT).setStatusBarLightMode(true)
                .titleView(binding.tv)
                .setNavBarVisible(false)
                .immerse(true).apply()
        }
    }
}