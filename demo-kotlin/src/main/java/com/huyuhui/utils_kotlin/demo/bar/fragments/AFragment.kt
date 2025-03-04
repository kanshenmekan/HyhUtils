package com.huyuhui.utils_kotlin.demo.bar.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import com.huyuhui.hyhutilskotlin.bar.BarConfig
import com.huyuhui.hyhutilskotlin.bar.apply
import com.huyuhui.utils_kotlin.demo.BaseFragment
import com.huyuhui.utils_kotlin.demo.databinding.FragmentABinding

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
            val barConfig = BarConfig.Builder().statusBarColor(Color.TRANSPARENT)
                .navBarColor(Color.parseColor("#22050505"))
                .immerse(WindowInsetsCompat.Type.statusBars()).build()
            barUtils.apply(barConfig)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            val barConfig = BarConfig.Builder().statusBarColor(Color.TRANSPARENT)
                .navBarColor(Color.parseColor("#22050505"))
                .immerse(WindowInsetsCompat.Type.statusBars()).build()
            barUtils.apply(barConfig)
        }
    }
}