package com.huyuhui.utils_kotlin.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    protected VB binding;
//    @Override
//    protected void attachBaseContext(Context newBase) {
//        // 绑定语种
//        super.attachBaseContext(MultiLanguages.attach(newBase));
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CommonKtKt.inflateWithGeneric(this);
        setContentView(binding.getRoot());
    }

}