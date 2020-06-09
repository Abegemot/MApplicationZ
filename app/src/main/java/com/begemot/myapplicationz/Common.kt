package com.begemot.myapplicationz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.jsoup.Jsoup
import timber.log.Timber
import java.net.URLEncoder
suspend fun translate2(text:String,tlang:String,olang:String):MutableList<OriginalTrans>{
    Timber.d("->translate 2")
    val lot=mutableListOf<OriginalTrans>()
    //gettranslatedText(text,lang)
    val slT=splitLongText(text)
    slT.forEach {
         val RT2=gettranslatedText(it,tlang,olang)
         //val rl=RT2 as ResultTranslation.ResultList
         lot.addAll(RT2)
    }
    Timber.d("<-translate ok")
    return lot
}



/*suspend fun translate(text:String,lang:String):ResultTranslation{
    Timber.d("->translate")
    val lot=mutableListOf<OriginalTrans>()
    return try {
        //gettranslatedText(text,lang)
        val slT=splitLongText(text)
        slT.forEach {
            val RT2=gettranslatedText(it,lang)
            val rl=RT2 as ResultTranslation.ResultList
            lot.addAll(rl.Lorigtrans)
        }
        Timber.d("<-translate ok")
        return ResultTranslation.ResultList(lot)

    } catch (e: Exception) {
        Timber.d("<-translate error")
        ResultTranslation.Error("text size ${text.length} ${e.toString()}")
    }
}*/

fun splitLongText(text:String):List<String>{
    val maxlen=3000
    val resultList= mutableListOf<String>()
    var LS = mutableListOf<String>()
    if(text.length<maxlen) { LS.add(text); return LS}
    LS= separateParagrafs(text).toMutableList()
    val bs=StringBuilder()
    while(LS.size>0){
        val txt=LS.removeAt(0)
        if((txt.length+bs.length)<maxlen){
            bs.append(txt)
            bs.append(". ")
        }else{
            resultList.add(bs.toString())
            bs.clear()
            bs.append(txt)
            if(text[txt.length].equals("."))
                bs.append("x. ")
        }
    }
    if(bs.length>0) resultList.add(bs.toString())
    return resultList
}

suspend fun gettranslatedText(text: String, tlang: String,olang:String):MutableList<OriginalTrans> = withContext(
    Dispatchers.IO) {
    Timber.d("->  gettranslatedText")
    Timber.d(text)
    var url =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + "$olang" + "&tl=" + "$tlang" + "&dt=t&q=" + URLEncoder.encode(
            text,
            "utf-8"
        )
    val d = Jsoup.connect(url).ignoreContentType(true).get().text()
    val t = JSONArray(d)
    val lOriginalTrans= mutableListOf<OriginalTrans>()
    val qsm = t.getJSONArray(0)
    for (i in 0 until qsm.length()) {
        val l = qsm.getJSONArray(i)
        val originalTrans=OriginalTrans(l.getString(1),l.getString(0))
        lOriginalTrans.add(originalTrans)
    }
    Timber.d("<-  gettranslatedText")
    lOriginalTrans
}
fun separateParagrafs(text: String): List<String> {
    val sP = text.split(". ")
    return sP
}
