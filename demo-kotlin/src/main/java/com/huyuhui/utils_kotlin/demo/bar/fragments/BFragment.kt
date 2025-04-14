package com.huyuhui.utils_kotlin.demo.bar.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.huyuhui.utils_kotlin.demo.BaseFragment
import com.huyuhui.utils_kotlin.demo.databinding.FragmentBBinding

class BFragment : BaseFragment<FragmentBBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.statusBar) { v: View, insets: WindowInsetsCompat ->
            insets.getInsets(WindowInsetsCompat.Type.statusBars()).run {
                Log.e("123", "111 $this ${binding.root.paddingTop}")
                v.setBackgroundColor(Color.GREEN)
                v.updateLayoutParams {
                    height = top
                }
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {

            //BottomNavigationBar会自己处理导航栏的insets
            barUtils.apply {
                setNavBarVisibility(true)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            barUtils.apply {
                setNavBarVisibility(true)
            }
        }
    }
}