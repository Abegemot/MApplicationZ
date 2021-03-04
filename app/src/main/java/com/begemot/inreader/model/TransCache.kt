package com.begemot.inreader.model

import com.begemot.knewscommon.Pinyin
import com.begemot.knewscommon.getPinying
import com.begemot.knewscommon.gettranslatedText
import com.begemot.knewscommon.translatePayString
import timber.log.Timber


open class   TText{
    open fun getText():String{
        return "Ñ->O "
    }
    fun getPinYin(): Pinyin {
        return Pinyin()
    }
}

sealed class TransClass:TText(){

    class WithPinYin(val lPy: List<Pinyin>):TransClass(){
        override fun getText():String {

            val x=lPy.fold(""){sum, element -> "$sum ${element.w}"}
            Timber.d("getText PinYin->$x")
            return x
            //return "With PinYin"+(lPy.fold(""){sum, element -> "$sum ${((element.first.w}"})
            //return "With PinYin  ${lPy.lPy.toString()}"
        }

        override fun toString(): String {
            val x=lPy.fold("-><-"){sum, element -> "$sum ${element.w}"}
            Timber.d("toString PinYin->$x")
            return x

        }
    }

    class NoPinYin(val lStr:List<String>):TransClass(){
        override fun getText():String {
            val x = lStr.joinToString(" ")
//            Timber.d("getText NoPinYin->$x")
            return x
        }

        override fun toString(): String {

            val x = lStr.joinToString(" ")
            Timber.d("toString NoPinYin->$x")
            return x
        }
    }
    class NoTrans():TransClass()
}


class TransCache {
    private val mT4 = mutableMapOf<String,TransClass>()


    fun getTrans3(otext: String, olang: String, tlang: String): TransClass {
        Timber.d("orig '$otext' olang $olang tlang $tlang")
        val key = otext + tlang
        val x = mT4[key]
        if (x != null) return x
        val a = gettranslatedTextX(otext, olang, tlang)
        if (a.isEmpty()) return TransClass.NoTrans()
        else {
            if (tlang.equals("zh")) {
                mT4[key] = TransClass.WithPinYin(getPinying(a))
                return mT4[key]!!
            }else
//                Timber.d("ZZZZZZZZZZZZZZZZZZZZZ  a='$a'")
                mT4[key] = TransClass.NoPinYin(listOf(a))  //listOf(Pinyin(a, ""))
        }
        return TransClass.NoPinYin(listOf(a)) //listOf(Pinyin(a, ""))
    }
}

fun gettranslatedTextX(txt: String, olang: String, tlang: String): String {
    Timber.d("text to translate :'$txt' olang $olang tlang $tlang")
    try {
        if(txt.isEmpty()) return ""
        val r= gettranslatedText(txt, olang, tlang)[0].translated
        //Timber.d("Translation->$r")
        return r
        //return translatePayString(txt,olang,tlang)
        //return "$txt  & translated  "
    } catch (e: java.lang.Exception) {
        Timber.d("Exception $e")
        return translatePayString(txt,olang,tlang)

    }

}
