package com.begemot.inreader.model

import com.begemot.inreader.KLocale
import com.begemot.inreader.prefs
import timber.log.Timber


class KKLang {
    fun getSelectedLangs(Olng:String,Tlng:String):List<KLocale2>{
        val lExcudedLang = listOf(Olng,Tlng)
        val lSelected = prefs.selectedLang.split(",")  //"ca,en" ...
        val sele = lKLocales3.filterKeys { lSelected.contains(it) and !lExcudedLang.contains(it) }
        //val sele = lKLocales3.filterKeys { lSelected.contains(it) and !it.equals(Tlng) }
        Timber.d("Olng=$Olng  Tlng=$Tlng ....sele=$sele")
        return sele.values.toList()
        //return emptyList()

    }
}

data class KLocale2(
    val displayName: String = "",
    val acronim: String = "",
    var checked: Boolean = false
) {
    override fun toString(): String {
        return "$displayName ($acronim)"
    }
}

private val lKLocales3= mapOf<String,KLocale2>(
   "af" to KLocale2("Afrikaans", "af"),
   "sq" to KLocale2("Albanian", "sq"),
   "am" to KLocale2("Amharic", "am"),
   "ar" to KLocale2("Arabic", "ar"),
   "hy" to KLocale2("Armenian", "hy"),
   "az" to KLocale2("Azerbaijani", "az"),
   "eu" to KLocale2("Basque", "eu"),
   "be" to KLocale2("Belarusian", "be"),
   "bn" to KLocale2("Bengali", "bn"),
   "bs" to KLocale2("Bosnian", "bs"),
   "bg" to KLocale2("Bulgarian", "bg"),
   "ca" to KLocale2("Catalan", "ca"),
   "ceb" to KLocale2("Cebuano", "ceb"),
   "zh" to KLocale2("Chinese (Simplified)", "zh"),
   "zh-TW" to  KLocale2("Chinese (Traditional)", "zh-TW"),
   "co" to KLocale2("Corsican", "co"),
   "hr" to KLocale2("Croatian", "hr"),
   "cs" to KLocale2("Czech", "cs"),
   "da" to KLocale2("Danish", "da"),
   "nl" to KLocale2("Dutch", "nl"),
   "en" to KLocale2("English", "en"),
   "eo" to KLocale2("Esperanto", "eo"),
   "et" to KLocale2("Estonian", "et"),
   "fi" to KLocale2("Finnish", "fi"),
   "fr" to KLocale2("French", "fr"),
   "fy" to KLocale2("Frisian", "fy"),
   "gl" to KLocale2("Galician", "gl"),
   "ka" to KLocale2("Georgian", "ka"),
   "de" to KLocale2("German", "de"),
   "el" to KLocale2("Greek", "el"),
   "gu" to KLocale2("Gujarati", "gu"),
   "ha" to KLocale2("Hausa", "ha"),
   "ht" to KLocale2("Haitian Creole", "ht"),
   "haw" to KLocale2("Hawaiian", "haw"),
   "he" to KLocale2("Hebrew", "he"), //or iw
   "hi" to KLocale2("Hindi", "hi"),
   "hmn" to KLocale2("Hmong", "hmn"),
   "hu" to KLocale2("Hungarian", "hu"),
   "is" to KLocale2("Icelandic", "is"),
   "ig" to KLocale2("Igbo", "ig"),
   "id" to KLocale2("Indonesian", "id"),
   "ga" to KLocale2("Irish", "ga"),
   "it" to KLocale2("Italian", "it"),
   "ja" to KLocale2("Japanese", "ja"),
   "jv" to KLocale2("Javanese", "jv"),
   "kn" to KLocale2("Kannada", "kn"),
   "kk" to KLocale2("Kazakh", "kk"),
   "km" to KLocale2("Khmer", "km"),
   "rw" to KLocale2("Kinyarwanda", "rw"),
   "ko" to KLocale2("Korean", "ko"),
   "ku" to KLocale2("Kurdish", "ku"),
   "ky" to KLocale2("Kyrgyz", "ky"),
   "lo" to KLocale2("Lao", "lo"),
   "la" to KLocale2("Latin", "la"),
   "lv" to KLocale2("Latvian", "lv"),
   "lt" to KLocale2("Lithuanian", "lt"),
   "lb" to KLocale2("Luxembourgish", "lb"),
   "mk" to KLocale2("Macedonian", "mk"),
   "mg" to KLocale2("Malagasy", "mg"),
   "ms" to KLocale2("Malay", "ms"),
   "ml" to KLocale2("Malayalam", "ml"),
   "mt" to KLocale2("Maltese", "mt"),
   "mi" to KLocale2("Maori", "mi"),
   "mr" to KLocale2("Marathi", "mr"),
   "my" to KLocale2("Myanmar (Burmese)", "my"),
   "mn" to KLocale2("Mongolian", "mn"),
   "ne" to KLocale2("Nepali", "ne"),
   "ny" to KLocale2("Nyanja (Chichewa)", "ny"),
   "no" to KLocale2("Norwegian", "no"),
   "or" to KLocale2("Odia (Oriya)", "or"),
   "ps" to KLocale2("Pashto", "ps"),
   "fa" to KLocale2("Persian", "fa"),
   "pl" to KLocale2("Polish", "pl"),
   "pt" to KLocale2("Portuguese  Brazil", "pt"),
   "pa" to KLocale2("Punjabi", "pa"),
   "ro" to KLocale2("Romanian", "ro"),
   "ru" to KLocale2("Russian", "ru"),
   "sm" to KLocale2("Samoan", "sm"),
   "gd" to KLocale2("Scots Gaelic", "gd"),
   "sr" to KLocale2("Serbian", "sr"),
   "st" to KLocale2("Sesotho", "st"),
   "sn" to KLocale2("Shona", "sn"),
   "si" to KLocale2("Sinhala (Sinhalese)", "si"),
   "sd" to KLocale2("Sindhi", "sd"),
   "sk" to KLocale2("Slovak", "sk"),
   "sl" to KLocale2("Slovenian", "sl"),
   "so" to KLocale2("Somali", "so"),
   "es" to KLocale2("Spanish", "es"),
   "su" to KLocale2("Sundanese", "su"),
   "sw" to KLocale2("Swahili", "sw"),
   "sv" to KLocale2("Swedish", "sv"),
   "tg" to KLocale2("Tajik", "tg"),
   "ta" to KLocale2("Tamil", "ta"),
   "tt" to KLocale2("Tatar", "tt"),
   "te" to KLocale2("Telugu", "te"),
   "th" to KLocale2("Thai", "th"),
   "tr" to KLocale2("Turkish", "tr"),
   "tk" to KLocale2("Turkmen", "tk"),
   "uk" to KLocale2("Ukrainian", "uk"),
   "ur" to KLocale2("Urdu", "ur"),
   "ug" to KLocale2("Uyghur", "ug"),
   "uz" to KLocale2("Uzbek", "uz"),
   "vi" to KLocale2("Vietnamese", "vi"),
   "cy" to KLocale2("Welsh", "cy"),
   "xh" to KLocale2("Xhosa", "xh"),
   "yi" to KLocale2("Yiddish", "yi"),
   "yo" to KLocale2("Yoruba", "yo"),
   "zu" to KLocale2("Zulu", "zu")
)


private val lKLocale2s = listOf(
    KLocale2("Afrikaans", "af"),
    KLocale2("Albanian", "sq"),
    KLocale2("Amharic", "am"),
    KLocale2("Arabic", "ar"),
    KLocale2("Armenian", "hy"),
    KLocale2("Azerbaijani", "az"),
    KLocale2("Basque", "eu"),
    KLocale2("Belarusian", "be"),
    KLocale2("Bengali", "bn"),
    KLocale2("Bosnian", "bs"),
    KLocale2("Bulgarian", "bg"),
    KLocale2("Catalan", "ca"),
    KLocale2("Cebuano", "ceb"),
    KLocale2("Chinese (Simplified)", "zh"),
    KLocale2("Chinese (Traditional)", "zh-TW"),
    KLocale2("Corsican", "co"),
    KLocale2("Croatian", "hr"),
    KLocale2("Czech", "cs"),
    KLocale2("Danish", "da"),
    KLocale2("Dutch", "nl"),
    KLocale2("English", "en"),
    KLocale2("Esperanto", "eo"),
    KLocale2("Estonian", "et"),
    KLocale2("Finnish", "fi"),
    KLocale2("French", "fr"),
    KLocale2("Frisian", "fy"),
    KLocale2("Galician", "gl"),
    KLocale2("Georgian", "ka"),
    KLocale2("German", "de"),
    KLocale2("Greek", "el"),
    KLocale2("Gujarati", "gu"),
    KLocale2("Hausa", "ha"),
    KLocale2("Haitian Creole", "ht"),
    KLocale2("Hawaiian", "haw"),
    KLocale2("Hebrew", "he"), //or iw
    KLocale2("Hindi", "hi"),
    KLocale2("Hmong", "hmn"),
    KLocale2("Hungarian", "hu"),
    KLocale2("Icelandic", "is"),
    KLocale2("Igbo", "ig"),
    KLocale2("Indonesian", "id"),
    KLocale2("Irish", "ga"),
    KLocale2("Italian", "it"),
    KLocale2("Japanese", "ja"),
    KLocale2("Javanese", "jv"),
    KLocale2("Kannada", "kn"),
    KLocale2("Kazakh", "kk"),
    KLocale2("Khmer", "km"),
    KLocale2("Kinyarwanda", "rw"),
    KLocale2("Korean", "ko"),
    KLocale2("Kurdish", "ku"),
    KLocale2("Kyrgyz", "ky"),
    KLocale2("Lao", "lo"),
    KLocale2("Latin", "la"),
    KLocale2("Latvian", "lv"),
    KLocale2("Lithuanian", "lt"),
    KLocale2("Luxembourgish", "lb"),
    KLocale2("Macedonian", "mk"),
    KLocale2("Malagasy", "mg"),
    KLocale2("Malay", "ms"),
    KLocale2("Malayalam", "ml"),
    KLocale2("Maltese", "mt"),
    KLocale2("Maori", "mi"),
    KLocale2("Marathi", "mr"),
    KLocale2("Myanmar (Burmese)", "my"),
    KLocale2("Mongolian", "mn"),
    KLocale2("Nepali", "ne"),
    KLocale2("Nyanja (Chichewa)", "ny"),
    KLocale2("Norwegian", "no"),
    KLocale2("Odia (Oriya)", "or"),
    KLocale2("Pashto", "ps"),
    KLocale2("Persian", "fa"),
    KLocale2("Polish", "pl"),
    KLocale2("Portuguese  Brazil", "pt"),
    KLocale2("Punjabi", "pa"),
    KLocale2("Romanian", "ro"),
    KLocale2("Russian", "ru"),
    KLocale2("Samoan", "sm"),
    KLocale2("Scots Gaelic", "gd"),
    KLocale2("Serbian", "sr"),
    KLocale2("Sesotho", "st"),
    KLocale2("Shona", "sn"),
    KLocale2("Sinhala (Sinhalese)", "si"),
    KLocale2("Sindhi", "sd"),
    KLocale2("Slovak", "sk"),
    KLocale2("Slovenian", "sl"),
    KLocale2("Somali", "so"),
    KLocale2("Spanish", "es"),
    KLocale2("Sundanese", "su"),
    KLocale2("Swahili", "sw"),
    KLocale2("Swedish", "sv"),
    KLocale2("Tajik", "tg"),
    KLocale2("Tamil", "ta"),
    KLocale2("Tatar", "tt"),
    KLocale2("Telugu", "te"),
    KLocale2("Thai", "th"),
    KLocale2("Turkish", "tr"),
    KLocale2("Turkmen", "tk"),
    KLocale2("Ukrainian", "uk"),
    KLocale2("Urdu", "ur"),
    KLocale2("Uyghur", "ug"),
    KLocale2("Uzbek", "uz"),
    KLocale2("Vietnamese", "vi"),
    KLocale2("Welsh", "cy"),
    KLocale2("Xhosa", "xh"),
    KLocale2("Yiddish", "yi"),
    KLocale2("Yoruba", "yo"),
    KLocale2("Zulu", "zu")
)