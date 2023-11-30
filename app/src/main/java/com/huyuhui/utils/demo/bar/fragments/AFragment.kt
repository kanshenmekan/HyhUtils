package com.huyuhui.utils.demo.bar.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import com.huyuhui.utils.bar.BarHelper
import com.huyuhui.utils.demo.BaseFragment
import com.huyuhui.utils.demo.databinding.FragmentABinding

class AFragment : BaseFragment<FragmentABinding>() {

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
            barHelper.setStatusBarColor(Color.TRANSPARENT)
                .setNavBarColor(Color.parseColor("#22050505"))
                .immerse(true, WindowInsetsCompat.Type.statusBars()).apply()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            val barHelper = BarHelper(requireActivity().window)
            barHelper.setStatusBarColor(Color.TRANSPARENT)
                .setNavBarColor(Color.parseColor("#22050505"))
                .immerse(true, WindowInsetsCompat.Type.statusBars()).apply()
        }
    }
}