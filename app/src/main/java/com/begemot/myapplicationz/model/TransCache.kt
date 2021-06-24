package com.begemot.myapplicationz.model

import com.begemot.knewscommon.*
import timber.log.Timber


open class   TText{
    open fun getText():String{
        return ""
    }
 //   fun getPinYin(): Pinyin {
 //       return Pinyin()
 //   }
}

sealed class TransClass:TText(){

    class WithPinYin(val lPy: List<Pinyin> = emptyList()):TransClass(){
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

    class NoPinYin(val lStr:List<String> = emptyList()):TransClass(){
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
    private val mT4 = mutableMapOf<String, TransClass>()

    suspend fun getTrans3(otext: String, olang: String, tlang: String): TransClass {
        Timber.d("orig '$otext' olang $olang tlang $tlang")
        val key = otext + tlang
        val x = mT4[key]
        if (x != null) return x
        val trans = XgetTranslatedString(otext, olang, tlang)

        if (trans.translated.isEmpty()) return TransClass.NoTrans()
        if (trans.romanizedt.lPy.isNotEmpty()) {
            val l=TransClass.WithPinYin(trans.romanizedt.lPy)
            mT4[key]=l
            return l
        }else{
            val l=TransClass.NoPinYin(listOf(trans.translated))
            mT4[key]=l
            return l
        }
    }
}

