package com.huyuhui.utils_kotlin.demo.bar.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.huyuhui.utils_kotlin.demo.BaseFragment
import com.huyuhui.utils_kotlin.demo.databinding.FragmentCBinding


class CFragment : BaseFragment<FragmentCBinding>() {
    private var isImmerse = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tv.setOnClickListener {
            isImmerse = !isImmerse
            binding.statusBar.requestApplyInsets()
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.statusBar) { v: View, insets: WindowInsetsCompat ->
            insets.getInsets(WindowInsetsCompat.Type.statusBars()).run {
                if (isImmerse) {
                    v.setBackgroundColor(Color.RED)
                } else {
                    v.setBackgroundColor(Color.BLUE)
                }
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
            barUtils.apply {
                setNavBarVisibility(false)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            barUtils.apply {
                setNavBarVisibility(false)
            }
        }
    }
}