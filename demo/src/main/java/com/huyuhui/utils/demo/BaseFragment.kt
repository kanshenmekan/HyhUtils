package com.huyuhui.utils.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.huyuhui.utils.bar.BarUtils

open class BaseFragment<VB : ViewBinding> : Fragment() {
    protected lateinit var mActivity: AppCompatActivity
    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding!!
    protected lateinit var barUtils: BarUtils

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
        barUtils = BarUtils.with(mActivity)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBindingWithGeneric(container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
