package com.huyuhui.hyhutilskotlin.language

import java.util.Locale

@Suppress("unused")
object LocaleContract {

    /** 中文  */
    @JvmStatic
    val chineseLocale: Locale by lazy {
        Locale.CHINESE
    }

    /** 简体中文  */
    @JvmStatic
    val simplifiedChineseLocale: Locale by lazy {
        Locale.SIMPLIFIED_CHINESE
    }

    /** 繁体中文  */
    @JvmStatic
    val traditionalChineseLocale: Locale by lazy {
        Locale.TRADITIONAL_CHINESE
    }

    @JvmStatic
    val taiWanLocale: Locale
        get() = traditionalChineseLocale

    /** 新加坡  */
    @JvmStatic
    val singaporeLocale: Locale by lazy {
        Locale("zh", "SG")
    }


    /** 英语  */
    @JvmStatic
    val englishLocale: Locale by lazy {
        Locale.ENGLISH
    }

    /** 法语  */
    @JvmStatic
    val frenchLocale: Locale by lazy {
        Locale.FRENCH
    }

    /** 德语  */
    @JvmStatic
    val germanLocale: Locale by lazy {
        Locale.GERMAN
    }

    /** 意大利语  */
    @JvmStatic
    val italianLocale: Locale by lazy {
        Locale.ITALIAN
    }

    /** 日语  */
    @JvmStatic
    val japaneseLocale: Locale by lazy {
        Locale.JAPANESE
    }

    /** 韩语  */
    @JvmStatic
    val koreanLocale: Locale by lazy {
        Locale.KOREAN
    }

    /** 越南语  */
    @JvmStatic
    val vietnameseLocale: Locale by lazy {
        Locale("vi")
    }

    /** 荷兰语  */
    @JvmStatic
    val dutchLocale: Locale by lazy {
        Locale("af")
    }

    /** 阿尔巴尼亚语  */
    @JvmStatic
    val albanianLocale: Locale by lazy {
        Locale("sq")
    }

    /** 阿拉伯语  */
    @JvmStatic
    val arabicLocale: Locale by lazy {
        Locale("ar")
    }

    /** 亚美尼亚语  */
    @JvmStatic
    val armenianLocale: Locale by lazy {
        Locale("hy")
    }

    /** 阿塞拜疆语  */
    @JvmStatic
    val azerbaijaniLocale: Locale by lazy {
        Locale("az")
    }

    /** 巴斯克语  */
    @JvmStatic
    val basqueLocale: Locale by lazy {
        Locale("eu")
    }

    /** 白俄罗斯  */
    @JvmStatic
    val belarusianLocale: Locale by lazy {
        Locale("be")
    }


    /** 保加利亚  */
    @JvmStatic
    val bulgariaLocale: Locale by lazy {
        Locale("bg")
    }


    /** 加泰罗尼亚  */
    @JvmStatic
    val catalonianLocale: Locale by lazy {
        Locale("ca")
    }

    /** 克罗埃西亚  */
    @JvmStatic
    val croatiaLocale: Locale by lazy {
        Locale("hr")
    }

    /** 捷克  */
    @JvmStatic
    val czechRepublicLocale: Locale by lazy {
        Locale("cs")
    }

    /** 丹麦文  */
    @JvmStatic
    val danishLocale: Locale by lazy {
        Locale("da")
    }

    /** 迪维希语  */
    @JvmStatic
    val dhivehiLocale: Locale by lazy {
        Locale("div")
    }

    /** 荷兰  */
    @JvmStatic
    val netherlandsLocale: Locale by lazy {
        Locale("nl")
    }


    /** 法罗语  */
    @JvmStatic
    val faroeseLocale: Locale by lazy {
        Locale("fo")
    }


    /** 爱沙尼亚  */
    @JvmStatic
    val estoniaLocale: Locale by lazy {
        Locale("et")
    }

    /** 波斯语  */
    @JvmStatic
    val farsiLocale: Locale by lazy {
        Locale("fa")
    }

    /** 芬兰语  */
    @JvmStatic
    val finnishLocale: Locale by lazy {
        Locale("fi")
    }


    /** 加利西亚  */
    @JvmStatic
    val galiciaLocale: Locale by lazy {
        Locale("gl")
    }

    /** 格鲁吉亚州  */
    @JvmStatic
    val georgiaLocale: Locale by lazy {
        Locale("ka")
    }


    /** 希腊  */
    @JvmStatic
    val greeceLocale: Locale by lazy {
        Locale("el")
    }

    /** 古吉拉特语  */
    @JvmStatic
    val gujaratiLocale: Locale by lazy {
        Locale("gu")
    }


    /** 希伯来  */
    @JvmStatic
    val hebrewLocale: Locale by lazy {
        Locale("he")
    }


    /** 北印度语  */
    @JvmStatic
    val hindiLocale: Locale by lazy {
        Locale("hi")
    }


    /** 匈牙利  */
    @JvmStatic
    val hungaryLocale: Locale by lazy {
        Locale("hu")
    }


    /** 冰岛语  */
    @JvmStatic
    val icelandicLocale: Locale by lazy {
        Locale("is")
    }


    /** 印尼  */
    @JvmStatic
    val indonesiaLocale: Locale by lazy {
        Locale("id")
    }


    /** 卡纳达语  */
    @JvmStatic
    val kannadaLocale: Locale by lazy {
        Locale("kn")
    }


    /** 哈萨克语  */
    @JvmStatic
    val kazakhLocale: Locale by lazy {
        Locale("kk")
    }


    /** 贡根文  */
    @JvmStatic
    val konkaniLocale: Locale by lazy {
        Locale("kok")
    }


    /** 吉尔吉斯斯坦  */
    @JvmStatic
    val kyrgyzLocale: Locale by lazy {
        Locale("ky")
    }


    /** 拉脱维亚  */
    @JvmStatic
    val latviaLocale: Locale by lazy {
        Locale("lv")
    }


    /** 立陶宛  */
    @JvmStatic
    val lithuaniaLocale: Locale by lazy {
        Locale("lt")
    }


    /** 马其顿  */
    @JvmStatic
    val macedoniaLocale: Locale by lazy {
        Locale("mk")
    }

    /** 马来  */
    @JvmStatic
    val malayLocale: Locale? by lazy {
        Locale("ms")
    }


    /** 马拉地语  */
    @JvmStatic
    val marathiLocale: Locale by lazy {
        Locale("mr")
    }


    /** 蒙古  */
    @JvmStatic
    val mongoliaLocale: Locale by lazy {
        Locale("mn")
    }

    /** 挪威  */
    @JvmStatic
    val norwayLocale: Locale by lazy {
        Locale("no")
    }

    /** 波兰  */
    @JvmStatic
    val polandLocale: Locale by lazy {
        Locale("pl")
    }


    /** 葡萄牙语  */
    @JvmStatic
    val portugalLocale: Locale by lazy {
        Locale("pt")
    }


    /** 旁遮普语（印度和巴基斯坦语）  */
    @JvmStatic
    val punjabLocale: Locale by lazy {
        Locale("pa")
    }

    /** 罗马尼亚语  */
    @JvmStatic
    val romanianLocale: Locale by lazy {
        Locale("ro")
    }


    /** 俄国  */
    @JvmStatic
    val russiaLocale: Locale by lazy {
        Locale("ru")
    }


    /** 梵文  */
    @JvmStatic
    val sanskritLocale: Locale by lazy {
        Locale("sa")
    }


    /** 斯洛伐克  */
    @JvmStatic
    val slovakiaLocale: Locale by lazy {
        Locale("sk")
    }

    /** 斯洛文尼亚  */
    @JvmStatic
    val sloveniaLocale: Locale by lazy {
        Locale("sl")
    }


    /** 西班牙语  */
    @JvmStatic
    val spainLocale: Locale by lazy {
        Locale("es")
    }

    /** 斯瓦希里语  */
    @JvmStatic
    val swahiliLocale: Locale by lazy {
        Locale("sw")
    }


    /** 瑞典  */
    @JvmStatic
    val swedenLocale: Locale by lazy {
        Locale("sv")
    }

    /** 叙利亚语  */
    @JvmStatic
    val syriacLocale: Locale by lazy {
        Locale("syr")
    }

    /** 坦米尔  */
    @JvmStatic
    val tamilLocale: Locale by lazy {
        Locale("ta")
    }

    /** 泰国  */
    @JvmStatic
    val thailandLocale: Locale by lazy {
        Locale("th")
    }

    /** 鞑靼语  */
    @JvmStatic
    val tatarLocale: Locale by lazy {
        Locale("tt")
    }


    /** 泰卢固语  */
    @JvmStatic
    val teluguLocale: Locale by lazy {
        Locale("te")
    }

    /** 土耳其语  */
    @JvmStatic
    val turkishLocale: Locale by lazy {
        Locale("tr")
    }

    /** 乌克兰  */
    @JvmStatic
    val ukraineLocale: Locale by lazy {
        Locale("uk")
    }

    /** 乌尔都语  */
    @JvmStatic
    val urduLocale: Locale by lazy {
        Locale("ur")
    }


    /** 乌兹别克语  */
    @JvmStatic
    val uzbekLocale: Locale by lazy {
        Locale("uz")
    }
}