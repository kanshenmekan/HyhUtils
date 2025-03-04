package com.huyuhui.utils_kotlin.demo.languages;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import com.huyuhui.hyhutilskotlin.language.LocaleContract;
import com.huyuhui.hyhutilskotlin.language.MultiLanguages;
import com.huyuhui.utils_kotlin.demo.BaseActivity;
import com.huyuhui.utils_kotlin.demo.R;
import com.huyuhui.utils_kotlin.demo.databinding.ActivityLanguagesBinding;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class LanguagesActivity extends BaseActivity<ActivityLanguagesBinding>
        implements RadioGroup.OnCheckedChangeListener {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebView = binding.wvMainWeb;
        mWebView.setWebViewClient(new LanguagesViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl("https://developer.android.google.cn/kotlin", generateLanguageRequestHeader());

        binding.tvMainLanguageApplication.setText(
                getApplication().getResources().getString(R.string.current_language));
        binding.tvMainLanguageSystem.setText(
                MultiLanguages.getLanguageString(this, MultiLanguages.getSystemLanguage(this), R.string.current_language));
        Locale locale = MultiLanguages.getAppLanguage();
        if (MultiLanguages.isSystemLanguage()) {
            binding.rgMainLanguages.check(R.id.rb_main_language_auto);
        } else {
            if ((MultiLanguages.equalsLanguage(LocaleContract.getSimplifiedChineseLocale(), locale) && (locale.getVariant().equals("Hans") || locale.getScript().equals("Hans")))
                    || MultiLanguages.equalsCountry(LocaleContract.getSimplifiedChineseLocale(), locale)) {
                binding.rgMainLanguages.check(R.id.rb_main_language_cn);
            } else if ((MultiLanguages.equalsLanguage(LocaleContract.getTraditionalChineseLocale(), locale) && (locale.getVariant().equals("Hant") || locale.getScript().equals("Hant")))
                    || MultiLanguages.equalsCountry(LocaleContract.getTraditionalChineseLocale(), locale)) {
                binding.rgMainLanguages.check(R.id.rb_main_language_tw);
            } else if (MultiLanguages.equalsLanguage(LocaleContract.getEnglishLocale(), locale)) {
                binding.rgMainLanguages.check(R.id.rb_main_language_en);
            } else {
                binding.rgMainLanguages.check(R.id.rb_main_language_auto);
            }
        }

        binding.rgMainLanguages.setOnCheckedChangeListener(this);
        binding.tvMainLanguageSystem.setOnClickListener(view -> {
            startActivity(new Intent(LanguagesActivity.this, LanguagesActivity2.class));

        });
        startService(new Intent(this, LanguagesService.class));
    }


    /**
     * {@link RadioGroup.OnCheckedChangeListener}
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (!group.findViewById(checkedId).isPressed()) return;
        // 是否改变了语言
        boolean hasChanged = false;
        if (checkedId == R.id.rb_main_language_auto) {
            // 跟随系统
            hasChanged = MultiLanguages.applySystemLanguage();
        } else if (checkedId == R.id.rb_main_language_cn) {
            // 简体中文
            hasChanged = MultiLanguages.applyCustomLanguage(LocaleContract.getSimplifiedChineseLocale());
        } else if (checkedId == R.id.rb_main_language_tw) {
            // 繁体中文
            hasChanged = MultiLanguages.applyCustomLanguage(LocaleContract.getTraditionalChineseLocale());
        } else if (checkedId == R.id.rb_main_language_en) {
            // 英语
            hasChanged = MultiLanguages.applyCustomLanguage(LocaleContract.getEnglishLocale());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        RadioGroup radioGroup = binding.rgMainLanguages;
        Locale locale = MultiLanguages.getAppLanguage();
        if (MultiLanguages.isSystemLanguage()) {
            radioGroup.check(R.id.rb_main_language_auto);
        } else {
            if ((MultiLanguages.equalsLanguage(LocaleContract.getSimplifiedChineseLocale(), locale) && (locale.getVariant().equals("Hans") || locale.getScript().equals("Hans")))
                    || MultiLanguages.equalsCountry(LocaleContract.getSimplifiedChineseLocale(), locale)) {
                radioGroup.check(R.id.rb_main_language_cn);
            } else if ((MultiLanguages.equalsLanguage(LocaleContract.getTraditionalChineseLocale(), locale) && (locale.getVariant().equals("Hant") || locale.getScript().equals("Hant")))
                    || MultiLanguages.equalsCountry(LocaleContract.getTraditionalChineseLocale(), locale)) {
                radioGroup.check(R.id.rb_main_language_tw);
            } else if (MultiLanguages.equalsLanguage(LocaleContract.getEnglishLocale(), locale)) {
                radioGroup.check(R.id.rb_main_language_en);
            } else {
                radioGroup.check(R.id.rb_main_language_auto);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
        mWebView.resumeTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清除历史记录
        mWebView.clearHistory();
        //停止加载
        mWebView.stopLoading();
        //加载一个空白页
        mWebView.loadUrl("about:blank");
        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(new WebViewClient());
        //移除WebView所有的View对象
        mWebView.removeAllViews();
        //销毁此的WebView的内部状态
        mWebView.destroy();
        stopService(new Intent(this,LanguagesService.class));
    }

    public static class LanguagesViewClient extends WebViewClient {

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String scheme = Uri.parse(url).getScheme();
            if (scheme == null) {
                return false;
            }
            switch (scheme) {
                // 如果这是跳链接操作
                case "http":
                case "https":
                    view.loadUrl(url, generateLanguageRequestHeader());
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    /**
     * 给 WebView 请求头添加语种环境
     */
    @NonNull
    public static Map<String, String> generateLanguageRequestHeader() {
        Map<String, String> map = new HashMap<>(1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 上面语种失效的问题解决方案
            // https://developer.android.google.cn/about/versions/13/features/app-languages?hl=zh-cn#consider-header
            map.put("Accept-Language", String.valueOf(MultiLanguages.getAppLanguage()));
        }
        return map;
    }
}