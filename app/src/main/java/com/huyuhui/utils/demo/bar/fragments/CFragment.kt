package com.huyuhui.utils.demo.bar.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import androidx.core.view.WindowInsetsCompat
import com.huyuhui.utils.bar.BarHelper
import com.huyuhui.utils.demo.BaseFragment
import com.huyuhui.utils.demo.databinding.FragmentCBinding


class CFragment : BaseFragment<FragmentCBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden){
            val barHelper = BarHelper(requireActivity().window)
            barHelper.setStatusBarColor(Color.TRANSPARENT).setStatusBarLightMode(false)
                .titleView(binding.tv)
                .setNavBarVisible(false)
                .immerse(true,WindowInsetsCompat.Type.statusBars())
                .apply()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            val barHelper = BarHelper(requireActivity().window)
            barHelper.setStatusBarColor(Color.TRANSPARENT).setStatusBarLightMode(false)
                .titleView(binding.tv)
                .setNavBarVisible(false)
                .immerse(true,WindowInsetsCompat.Type.statusBars())
                .apply()
        }
    }
}