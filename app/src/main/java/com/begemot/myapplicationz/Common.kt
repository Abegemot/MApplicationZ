package com.begemot.myapplicationz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.net.URLEncoder

const val kTock="<TN>."//""<TM>." //" <TM> "//"~"//"(tm)"//"(©)"

suspend fun translateListKArticles(lKAOriginal:List<KArticle>,tlang:String,olang:String):List<OriginalTransLink>{
    val sb = StringBuilder()
    lKAOriginal.forEach { sb.append(it.title) }
    val lTranslated = translate33(sb.toString(), tlang,olang)
    Timber.d("ltranslated-->${lTranslated[1]}")
    val l_KA=lKAOriginal.zip(lTranslated,{a,c->OriginalTransLink(KArticle(a.title,""),c)})
    Timber.d("translateListKArticles lTranslated size = ${lTranslated.size}  lKAOriginal size = ${lKAOriginal.size}")
    return l_KA
}

suspend fun translateListKArticles33(lKAOriginal:List<KArticle>,tlang:String,olang:String):List<OriginalTransLink>{
   // val sb = StringBuilder()
    val l= mutableListOf<OriginalTransLink>()
    lKAOriginal.forEach {
       // sb.append(it.title)
        val lTranslated = translate33(it.title, tlang, olang)
        l.add(OriginalTransLink(it,lTranslated.toString()))
    }
    return l

   // Timber.d("ltranslated-->${lTranslated[1]}")
   // val l_KA=lKAOriginal.zip(lTranslated,{a,c->OriginalTransLink(KArticle(a.title,""),c)})
   // Timber.d("translateListKArticles lTranslated size = ${lTranslated.size}  lKAOriginal size = ${lKAOriginal.size}")
    //return l_KA
}


suspend fun getWebPage(sUrl:String): Document =
    withContext(Dispatchers.IO) {
        Jsoup.connect(sUrl).get()
    }

suspend fun getWebPagePOST(sUrl:String,sjason:String): String =
    withContext(Dispatchers.IO) {
       val cr= Jsoup.connect(sUrl)
            .method(Connection.Method.POST)
           .ignoreContentType(true)
            .execute()
        cr.body()
    }

suspend fun getWebPagePOSTJS(sUrl:String,sjason:String): String =
    withContext(Dispatchers.IO) {
        val cr= Jsoup.connect(sUrl)
            .header("Content-Type","application/json")
            .header("Accept","application/json")
            .header("Authorization","Bearer auth application-default print-access-token ")
            .followRedirects(true)
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .data("key","AIzaSyBP1dsYp-jPF6PfVetJWcguNLiFouZ3mjo")
            .method(Connection.Method.POST)
            .requestBody(sjason)
            .execute()
        cr.body()
    }

suspend fun translate33(text:String,tlang:String,olang:String):List<String>{
    Timber.d("->translate 33")
    val lot=mutableListOf<String>()
    //gettranslatedText(text,lang)
    val slT=splitLongText33(text)
    var n=0
    slT.forEach {

       // if(n==0){
           // Timber.d("T33 --> $it")
           val RT2=gettranslatedText33(it,tlang,olang)
           lot.addAll(RT2)
            n++
     //   }

    }
    Timber.d("<-translate ok")
    return lot
}


suspend fun translate2(text:String,tlang:String,olang:String):MutableList<OriginalTrans>{
    Timber.d("->translate 2")
    val lot=mutableListOf<OriginalTrans>()
    //gettranslatedText(text,lang)
    val slT=splitLongText(text)
    slT.forEach {
        Timber.d("T2 --> $it")
         val RT2=gettranslatedText(it,tlang,olang)
         //val rl=RT2 as ResultTranslation.ResultList
         lot.addAll(RT2)
    }
    Timber.d("<-translate ok")
    return lot
}

fun splitLongText33(text:String):List<String>{
    val maxlen=3000
    val resultList= mutableListOf<String>()
    var LS = mutableListOf<String>()
    if(text.length<maxlen) { LS.add(text); return LS}
    LS= separateParagrafs33(text).toMutableList()
    val bs=StringBuilder()
    while(LS.size>0){
        val txt=LS.removeAt(0)
        if((txt.length+bs.length)<maxlen){
            bs.append(txt)
            bs.append(kTock)
        }else{
            resultList.add(bs.toString())
            bs.clear()
            bs.append(txt)
            //if(text[txt.length].equals("."))
                bs.append(kTock)
        }
    }
    if(bs.length>0) resultList.add(bs.toString()+ kTock)
    return resultList
}


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

suspend fun gettranslatedText33(text: String, tlang: String,olang:String):List<String> = withContext(
    Dispatchers.IO) {
    Timber.d("->  gettranslatedText33")
    Timber.d(text)
    val url =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + "$olang" + "&tl=" + "$tlang" + "&dt=t&q=" + URLEncoder.encode(
            text,
            "utf-8"
        )
    val d = Jsoup.connect(url).ignoreContentType(true).get().text()
    Timber.d(" size google response = ${d.length}")
    val t = JSONArray(d)
    //val lOriginalTrans= mutableListOf<OriginalTrans>()
    val qsm = t.getJSONArray(0)

    val sbOriginal=StringBuilder(20000)
    val sbTranslated=StringBuilder(20000)
    for (i in 0 until qsm.length()) {
        val l = qsm.getJSONArray(i)
        val so=l.getString(1)
        val st=l.getString(0)
        sbOriginal.append(so)
        sbTranslated.append(st)
       // lOriginalTrans.add(originalTrans)
    }
    val LO=sbOriginal.split(kTock.toRegex())
    val LT=sbTranslated.split(kTock.toRegex())
    Timber.d("j")
    Timber.d("sbO=$sbOriginal")
    Timber.d("sbT=$sbTranslated")
   /* Timber.d("LO ${LO.size}->$LO")

    LO.forEach{
        Timber.d("->$it")
    }
    Timber.d("LT ${LT.size}->$LT")
    LT.forEach{
        Timber.d("->$it")
    }*/
    if(LO.size==LT.size) Timber.d(" ---------------------SSSSSSSSSSSSSIIIIIIIIIII-----lo-${LO.size}--lt-${LT.size}----")
    else Timber.d("----------------------CACA-------------------lo--${LO.size}--lt-${LT.size}-----------------------------")
    Timber.d("<-  gettranslatedText")
    LT
   // lOriginalTrans
}


suspend fun gettranslatedText(text: String, tlang: String,olang:String):MutableList<OriginalTrans> = withContext(
    Dispatchers.IO) {
    Timber.d("->  gettranslatedText")
    Timber.d(text)
    val url =
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
fun separateParagrafs33(text: String): List<String> {
    val sP = text.split(kTock)
    return sP
}