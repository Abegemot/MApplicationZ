package com.begemot.myapplicationz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
//import com.google.gson.Gson


@Composable
fun resfreshWraper2(loading: Boolean, children: @Composable() () -> Unit) {
    //Timber.d("Z  Loading $loading")
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            Modifier.fillMaxWidth().background(Color.Transparent)
        ) {
            Column() {
                children()
            }
        }
        if (loading)
            Box(
                Modifier.fillMaxWidth().background(Color.Transparent)
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.BottomEnd).padding(2.dp))
            }
    }
}

@Composable
fun resfreshWraper(loading: Boolean,skip:Boolean=false, children: @Composable() () -> Unit) {
    //Timber.d("Z  Loading $loading")
    Box() {
        Box(
            Modifier.fillMaxSize().background(Color.Transparent)
        ) {
            Column() {
                children()
            }
        }
        if (loading && !skip )
            Box(
                Modifier.fillMaxSize().background(Color.Transparent)
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
    }
}

//@Preview
@Composable
fun bb() {
    resfreshWraper(true) {
        Text("Hola     aa")
        Text("Hola     bb")
        Text("Hola     cc")
        Text("Hola     dd")
        Text("Hola     ee")
        Text("Hola     ff")
    }
}





/*
suspend fun   getTranslatedHeadlines(lKA:List<KArticle>, olang:String, tlang:String):List<OriginalTransLink>{

    val jqwery=articlesToJson(lKA, olang,tlang)
    val la= translateJson(jqwery)
    val lT= JsonToListStrings(la)
    val result=lKA.zip(lT,{a,c-> OriginalTransLink(a,c.translatedText) })
    return result
}

data class jsonReq(
    val q:List<String>,
    val source:String,
    val target:String,
    val format:String = "text"
)

private fun articlesToJson(la:List<KArticle>,olang:String,tlang:String):String{
    if(la.size==0) return "NOP"
    //var gson=Gson()
    //gson.htmlSafe()
    //var rq=Gson().toJson(jsonReq(la.map{it.title},olang,tlang) )
    //Timber.d("  gSON-->$rq")
    return "rq"
}

suspend fun getWebPage(sUrl:String): Document =
    withContext(Dispatchers.IO) {
        Jsoup.connect(sUrl).get()
    }

suspend fun translateJson(sjason:String): String =
    withContext(Dispatchers.IO) {
        val apikey="AIzaSyBP1dsYp-jPF6PfVetJWcguNLiFouZ3mjo"
        val sUrl="https://www.googleapis.com/language/translate/v2?key=$apikey"
        Timber.d("URL: $sUrl")
        Timber.d("json: $sjason")
        val cr= Jsoup.connect(sUrl)
            .header("Content-Type","application/json")
            .header("Accept","application/json")
            //.followRedirects(true)
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .method(Connection.Method.POST)
            .requestBody(sjason)
            .execute()
        cr.body()
    }

data class Data ( val translations : List<Translations> )
data class Translations ( val translatedText : String )
data class Json4Kotlin_Base ( val data : Data )

private fun JsonToListStrings(json:String):List<Translations>{
    val ls= mutableListOf<String>()
   // val topic = Gson().fromJson(json, Json4Kotlin_Base::class.java)
   // return topic.data.translations
    return emptyList()
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
*/
