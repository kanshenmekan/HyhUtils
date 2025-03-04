package com.huyuhui.utils_kotlin.demo

import android.content.Intent
import android.os.Bundle
import com.huyuhui.utils_kotlin.demo.bar.BarActivity
import com.huyuhui.utils_kotlin.demo.databinding.ActivityMainBinding
import com.huyuhui.utils_kotlin.demo.languages.LanguagesActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLanguages.setOnClickListener {
            startActivity(Intent(this@MainActivity, LanguagesActivity::class.java))
        }

        binding.btnBar.setOnClickListener {
            startActivity(Intent(this@MainActivity, BarActivity::class.java))
        }
    }
}