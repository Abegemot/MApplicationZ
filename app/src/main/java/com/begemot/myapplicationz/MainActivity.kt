   package com.begemot.myapplicationz

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
//import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.ui.core.Alignment
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Favorite
import androidx.ui.material.icons.filled.KeyboardArrowDown
import androidx.ui.material.icons.filled.KeyboardArrowUp
import androidx.ui.material.icons.filled.Settings
import androidx.ui.res.vectorResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.begemot.kclib.*
import io.grpc.myproto.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LoginUi() }
    }
    override fun onBackPressed() {
        //super.onBackPressed()
        StatusApp.currentScreen = Screens.ListHeadlines()
    }
}

sealed class Screens {
    class ListHeadlines : Screens()
    class FullArticle(val originalTransLink: OriginalTransLink) : Screens()
}

sealed class AppStatus {
    class Idle : AppStatus()
    class Loading : AppStatus()
    class Error(val sError: String) : AppStatus()
}

@Model
object StatusApp {
    var currentScreen: Screens = Screens.ListHeadlines()
    var currentStatus: AppStatus = AppStatus.Loading()
    var fontSize:Int= prefs.fontSize
   /* get()= prefs.fontSize
    set(value) {
        prefs.fontSize=value
    }*/
    var lang:String=prefs.kLang

}

@Composable
fun LoginUi() {
    val s = Word.newBuilder()
        .setId(10)
        .setRomanized("ZZTOPwwwwwy")
        .build()
    val kt = state { kTheme.DARK }
    val selectLang = state { false }
    MaterialTheme(colors = kt.value.theme) {
        Column() {
            TopAppBar(
                //color = MaterialTheme.colors.primary,
                modifier = Modifier.fillMaxWidth(),
                title = {title(StatusApp)} ,
                actions = {
                    IconButton(onClick = { kt.value = kTheme.next(kt.value) }
                    ) {
                        Icon(Icons.Filled.Favorite)
                    }
                    IconButton(onClick = { selectLang.value = true }) {
                        Icon(Icons.Filled.Settings)
                    }
                }
            )
            if (selectLang.value) selectLanguage(selectLang, StatusApp)
            Surface() {
                val s = StatusApp.currentScreen
                when (s) {
                    is Screens.ListHeadlines -> headlinesScreen(StatusApp)
                    is Screens.FullArticle -> articleScreen(s.originalTransLink, StatusApp)
                }
            }
       }
    }
}

 @Composable
 fun title(statusApp: StatusApp){
     Column() {
         Text(text = "RT novesti")
         val currScreen=statusApp.currentScreen
         val sAux=when(currScreen){
             is Screens.FullArticle->" Article"
             is Screens.ListHeadlines->" Headlines"
         }
         Text(" $sAux")
     }
 }


@Composable
fun headlinesScreen(statusApp: StatusApp) {
    val lHeadlines = state{ mutableListOf<OriginalTransLink>()}
    onCommit(statusApp.lang){
        getLTransArticles(lHeadlines,statusApp)
    }
    val status=statusApp.currentStatus
    when(status){
        is AppStatus.Loading -> waiting()
        is AppStatus.Error -> displayError(status.sError)
        is AppStatus.Idle -> drawHeadlines(loriginalTransLink =lHeadlines.value , statusApp =statusApp )
    }
}

@Composable
 fun waiting(){
   Box(modifier = Modifier.fillMaxSize(),gravity = Alignment.Center,backgroundColor = Color.Transparent  ) {
       CircularProgressIndicator()
   }
}

@Composable
   fun displayError(sError:String){
       VerticalScroller() {
           Box(border = Border(2.dp, Color.Blue)) {
               Text("Error : $sError}")
           }
       }
   }



@Composable
fun drawHeadlines(
    loriginalTransLink: MutableList<OriginalTransLink>,
    statusApp: StatusApp
) {
    val original=state{true}
    AdapterList(data = loriginalTransLink) {
        Card(shape = RoundedCornerShape(8.dp),elevation = 7.dp, modifier = Modifier.fillMaxHeight() + Modifier.padding(2.dp) )  {
            Column() {
                val bplaytext=state{false}
                        Clickable(onClick = {original.value=true; bplaytext.value=true  }) {
                            KText2(txt = it.kArticle.title, size = statusApp.fontSize)
                        }
                        Clickable(onClick = {original.value=false; bplaytext.value=true  }) {
                            KText2(txt = it.translated, size = statusApp.fontSize)
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Clickable(onClick = { statusApp.currentScreen = Screens.FullArticle(it) }) {
                                Icon(vectorResource(id = R.drawable.ic_link_24px),modifier = Modifier.padding(0.dp,0.dp,15.dp,3.dp))
                            }
                        }
                        if(bplaytext.value){
                            if(original.value) playText(bplaytext,it.kArticle.title,statusApp)
                            else playText(bplaytext,it.translated,statusApp,original.value)
                        }
                }
            }
        }
}


@Composable
fun articleScreen(originalTransLink: OriginalTransLink, statusApp: StatusApp) {
    val trans3 = state { mutableListOf<OriginalTrans>() }
    onCommit(statusApp.lang) {
        getTranslationLink(originalTransLink, trans3, statusApp)
    }
    val status=statusApp.currentStatus
    when(status){
        is AppStatus.Loading -> waiting()
        is AppStatus.Error-> displayError(sError = status.sError)
        is AppStatus.Idle->drawListOriginalTranslated(originalTransLink,loriginalTranslate = trans3.value,statusApp = statusApp)
    }
 }

@Composable
fun drawListOriginalTranslated(originalTransLink: OriginalTransLink,loriginalTranslate:MutableList<OriginalTrans>,statusApp: StatusApp){
   // KWindow() {
    val original=state{true}
        AdapterList(data = loriginalTranslate) {
            Card(shape= RoundedCornerShape(8.dp),elevation = 7.dp, modifier = Modifier.fillMaxHeight()+ Modifier.padding(2.dp)+ Modifier.fillMaxWidth()) {
                Column() {
                    val bplaytext=state{false}
                    Clickable(onClick ={original.value=true; bplaytext.value=true} ) {
                        KText2(it.original,size = statusApp.fontSize)
                    }
                    Clickable(onClick ={original.value=false; bplaytext.value=true} ) {
                        KText2(it.translated, size = statusApp.fontSize)
                    }
                    if(bplaytext.value){
                        if(original.value) playText(bplaytext,it.original,statusApp)
                        else playText(bplaytext,it.translated,statusApp,original.value)
                    }
                }
            }
        }
 //   }
}

 @Composable
 fun playText(bplayText:MutableState<Boolean>,txt:String,statusApp: StatusApp,original:Boolean=true){
     val context = ContextAmbient.current
     var msg=""
    lateinit var t1:TextToSpeech
     var lan=""
     var result=0
     if(original) lan="ru" else lan=statusApp.lang
     t1 = TextToSpeech(
         context,
         TextToSpeech.OnInitListener { status ->
             if (status != TextToSpeech.ERROR) {
                result= t1.setLanguage(Locale.forLanguageTag(lan))
                if(result==TextToSpeech.LANG_MISSING_DATA) msg="Missing data"
                if(result==TextToSpeech.LANG_NOT_SUPPORTED) msg="Lang not supported"

             }
         })
     Dialog(onCloseRequest = {t1.shutdown(); bplayText.value=false}){
         //Box(modifier = Modifier.fillMaxWidth(),backgroundColor = Color.Green){
         KWindow() {
             if(result==0) KText2(txt,size = statusApp.fontSize)
             else    KText2(msg,size = statusApp.fontSize)
             IconButton(onClick = {t1.speak(txt,TextToSpeech.QUEUE_FLUSH,null,null)}) {
                 Icon(vectorResource(id = R.drawable.ic_volume_up_24px))
             }
         }
     }

 }

@Composable
fun selectLanguage(selectLang: MutableState<Boolean>, statusApp: StatusApp) {
    Dialog(onCloseRequest = { selectLang.value = false }) {
        val localFontSize=state{statusApp.fontSize}
        KWindow() {
            KHeader(txt = "Settings", onClick = {})
            val radioOptions = listOf("en", "es", "it","ur")
            val indx=radioOptions.indexOf(statusApp.lang)
            val (selectedOption, onOptionSelected) = state { radioOptions[indx] }


           // RadioGroup(options = radioOptions,selectedOption = selectedOption,onSelectedChange = onOptionSelected)
            Row(verticalGravity =Alignment.CenterVertically ) {

                Box(modifier = Modifier.preferredWidth(110.dp)) {
                    RadioGroup(options = radioOptions,selectedOption = selectedOption,onSelectedChange = onOptionSelected)
                }

                Row(modifier = Modifier.padding(4.dp),verticalGravity = Alignment.CenterVertically) {
                    IconButton(onClick = {localFontSize.value=(localFontSize.value)-1}) {
                        Icon(Icons.Filled.KeyboardArrowDown)
                    }
                    KText2("Text Size",size=localFontSize.value)
                    IconButton(onClick = {localFontSize.value=(localFontSize.value)+1}) {
                        Icon(Icons.Filled.KeyboardArrowUp)
                    }

                }

            }
            KButtonBar {
                Button(onClick = {
                   // selectedLang.value = selectedOption
                    selectLang.value = false
                    statusApp.lang = selectedOption
                    statusApp.fontSize = localFontSize.value
                    prefs.fontSize = localFontSize.value
                    prefs.kLang = selectedOption
                }) {
                    Text("oK")
                }
            }
        }
    }
    //  }

}

data class KArticle(val title: String = "", val link: String = "", val desc: String = "")
data class OriginalTransLink(val kArticle: KArticle,val translated: String)
data class OriginalTrans(val original:String="",val translated:String="")


suspend fun getArticles():List<KArticle> = withContext(Dispatchers.IO) {
    fun transFigure(el: Element): KArticle {
        val title = el.select("h3").text()+". "
        val link = el.select("a[href]").first().attr("abs:href")
        return KArticle(title, link, "")
    }
    fun transSection(el: Element): KArticle {
        val title = el.select("h3").text()+". "
       // val desc = el.select("p").text()
        val link = el.select("h3").select("a[href]").attr("abs:href")
        return KArticle(title, link, "")
    }

    val s = "https://russian.rt.com/inotv"
    val doc = Jsoup.connect(s).get()
    var art = doc.select("figure")
    val l1 = art.map { it -> transFigure(it) }

    art = doc.select("section.block-white.materials-preview").select("article")
    val l2 = art.map { it -> transSection(it) }
    l1 + l2
}

suspend fun getArticle(originalTransLink: OriginalTransLink) = withContext(Dispatchers.IO) {
    fun transArticleintro(el: Element): String {
        //println("el $el")
        return el.text()
    }

    val doc = Jsoup.connect(originalTransLink.kArticle.link).get()
    println(originalTransLink.kArticle.link)
    var art = doc.select("div.article-intro")
    val l1 = art.map { it -> "${originalTransLink.kArticle.title}"+transArticleintro(it) }

    println("l1 ${l1.size}  $l1")

    val arts = doc.select("div.article-body")
    val l2 = arts.map { it -> transArticleintro(it) }
    println("l2 ${l2.size} $l2")
    val lt= (l1 + l2).joinToString()
    lt
}

fun getLTransArticles(tt:MutableState<MutableList<OriginalTransLink>>,statusApp: StatusApp){
    statusApp.currentStatus = AppStatus.Loading()
    GlobalScope.launch(Dispatchers.Main) {
        val LA = getArticles()

        val sb=StringBuilder()
        LA.forEach{ sb.append(it.title) }
        val rt=translate(sb.toString(),statusApp.lang)
        val q=(rt as ResultTranslation.ResultList).Lorigtrans

        val JJ=LA.zip(q,{a,c->OriginalTransLink(a,c.translated)})
        tt.value=JJ.toMutableList()
        statusApp.currentStatus = AppStatus.Idle()
    }
}

 sealed class ResultTranslation{
        class ResultList(val Lorigtrans:MutableList<OriginalTrans>):ResultTranslation()
        class Error(val sError:String):ResultTranslation()
 }

suspend fun translate(text:String,lang:String):ResultTranslation{
    val lot=mutableListOf<OriginalTrans>()
    return try {
        //gettranslatedText(text,lang)
        val slT=splitLongText(text)
        slT.forEach {
            val RT2=gettranslatedText(it,lang)
            val rl=RT2 as ResultTranslation.ResultList
            lot.addAll(rl.Lorigtrans)
        }
        return ResultTranslation.ResultList(lot)

    } catch (e: Exception) {
        ResultTranslation.Error("text size ${text.length} ${e.toString()}")
    }
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



suspend fun gettranslatedText(text: String, lang: String):ResultTranslation = withContext(Dispatchers.IO) {
   println("get translated text")
    var url =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + "ru" + "&tl=" + "$lang" + "&dt=t&q=" + URLEncoder.encode(
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
    ResultTranslation.ResultList(lOriginalTrans)
}
fun separateParagrafs(text: String): List<String> {
    val sP = text.split(". ")
    return sP
}

fun getTranslationLink(originalTransLink: OriginalTransLink, trans: MutableState<MutableList<OriginalTrans>>, statusApp: StatusApp) {
    println("--------------------gettranslationlink")
    statusApp.currentStatus = AppStatus.Loading()
    GlobalScope.launch(Dispatchers.Main) {
        val original = getArticle(originalTransLink)
        println("original : $original")
        //val sall = gettranslatedText(original, statusApp.lang)
        val sall = translate(original, statusApp.lang)
        when(sall){
            is ResultTranslation.ResultList->{trans.value=sall.Lorigtrans; statusApp.currentStatus = AppStatus.Idle() }
            is ResultTranslation.Error->{statusApp.currentStatus=AppStatus.Error(sall.sError)}
        }
        //trans.value = sall
        //statusApp.currentStatus = AppStatus.Idle()
    }
}


@Preview
@Composable
fun HolaPanoli(){
    val radioOptions = listOf("en", "es", "it")
   // Dialog(onCloseRequest = {}){
 //       Box() {
Row(verticalGravity =Alignment.CenterVertically ) {

    Box(modifier = Modifier.preferredWidth(110.dp)) {
        RadioGroup(options = radioOptions, selectedOption = null, onSelectedChange = {}  )
    }

        Row(modifier = Modifier.padding(4.dp),verticalGravity = Alignment.CenterVertically) {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.KeyboardArrowDown)
            }
            Text("Text Size")
            IconButton(onClick = {}) {
                Icon(Icons.Filled.KeyboardArrowUp)
            }

        }

}


}