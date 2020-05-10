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
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Favorite
import androidx.ui.material.icons.filled.KeyboardArrowDown
import androidx.ui.material.icons.filled.KeyboardArrowUp
import androidx.ui.material.icons.filled.Settings
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


 @Model
class KArticles(var articles: List<KArticle>)
val KA = KArticles(listOf(KArticle("uni", "deri", "cateriddd")))

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
//    class HeadLine(val article: KArticle) : Screens()
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
    onActive { search(StatusApp) }
    val s = Word.newBuilder()
        .setId(10)
        .setRomanized("ZZTOPwwwwwy")
        .build()
    val kt = state { kTheme.DARK }
    val selectLang = state { false }
    //val currentLang = state { "es" }
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
                    is Screens.ListHeadlines -> listArticles(StatusApp)
                    is Screens.FullArticle -> viewLink(s.originalTransLink, StatusApp)
                    //is Screens.HeadLine -> viewShort(s.article,StatusApp)
                }
            }
       }
    }
}

 @Composable
 fun title(statusApp: StatusApp){
     Column() {
         Text(text = " RT novesti ${prefs.fontSize}  ${prefs.kLang}")
         val currScreen=statusApp.currentScreen
         val sAux=when(currScreen){
             is Screens.FullArticle->"Full Article"
           //  is Screens.HeadLine->"Head Line"
             is Screens.ListHeadlines->"List of Headlines"
         }
         Text(" $sAux")
     }
 }





@Composable
fun listArticles(statusApp: StatusApp) {
    val lArticles=state{ listOf<OriginalTransLink>()}
    onCommit(statusApp.lang){
        getLTransArticles(lArticles,statusApp)
    }
    Box {
        if (statusApp.currentStatus is AppStatus.Loading) {
            Box(modifier = Modifier.fillMaxSize(),gravity = Alignment.Center,backgroundColor = Color.Transparent  ) {
                CircularProgressIndicator()
            }
        } else
        AdapterList(data = lArticles.value) {
            drawArticle(it, statusApp)
        }
    }
}

@Composable
fun drawArticle(
    originalTransLink: OriginalTransLink,
    statusApp: StatusApp
) {
    Card(shape= RoundedCornerShape(8.dp),elevation = 7.dp, modifier = Modifier.fillMaxHeight()+ Modifier.padding(2.dp))
    {
        Column() {
            Clickable(onClick = { }) {
           // Clickable(onClick = { currItem.value = it;viewShort.value = true }) {
                Column() {
                    KText2(txt=originalTransLink.kArticle.title,size = statusApp.fontSize)
                    KText2(txt=originalTransLink.translated,size = statusApp.fontSize)
                    if (originalTransLink.kArticle.desc.length > 0) KText2(txt = originalTransLink.kArticle.desc,size = statusApp.fontSize)
                }
            }
            //Box(modifier = Modifier.fillMaxWidth(),gravity = Alignment.TopEnd) {
             Row(horizontalArrangement = Arrangement.End,modifier = Modifier.fillMaxWidth() ){
                 Clickable(onClick = { statusApp.currentScreen=Screens.FullArticle(originalTransLink)  }) {
                 //Clickable(onClick = { viewLink.value = true; currItem.value = it }) {
                         KTextLink(txt = "-->LINK  ")
                }
            }
        }
    }
}


@Composable
fun viewLink(originalTransLink: OriginalTransLink, statusApp: StatusApp) {
    val trans3 = state { LOriginalTrans() }
    onCommit(statusApp.lang) {
        getTranslationLink(originalTransLink, trans3, statusApp)
    }

    Box() {
        when(statusApp.currentStatus){
            is AppStatus.Loading -> {Box(modifier = Modifier.fillMaxSize(),gravity = Alignment.Center,backgroundColor = Color.Transparent  ) {
                CircularProgressIndicator()
            } }
            is AppStatus.Idle->drawListOriginalTranslated(originalTransLink,loriginalTranslate = trans3.value,statusApp = statusApp)
            is AppStatus.Error->{
                VerticalScroller() {
                    Box(border = Border(2.dp, Color.Blue)) {
                        Text("Error view Link ${(statusApp.currentStatus as AppStatus.Error).sError}")
                    }
                }
            }
        }
    }
    /*KWindow() {
        drawListOriginalTranslated(loriginalTranslate = trans3.value)

    }*/
}

@Composable
fun drawListOriginalTranslated(originalTransLink: OriginalTransLink,loriginalTranslate:LOriginalTrans,statusApp: StatusApp){
    KWindow() {


      //  KText2(txt = originalTransLink.kArticle.title)
      //  KText2(txt = originalTransLink.translated)
        //KText2(txt = article.)
       // loriginalTranslate.lOriginalTrans.add(0, OriginalTrans(originalTransLink.kArticle.title,originalTransLink.translated))
        AdapterList(data = loriginalTranslate.lOriginalTrans) {
        Card(shape= RoundedCornerShape(8.dp),elevation = 7.dp, modifier = Modifier.fillMaxHeight()+ Modifier.padding(2.dp)+ Modifier.fillMaxWidth()) {
            Column() {
                val bplaytext=state{false}
                Clickable(onClick ={bplaytext.value=true} ) {
                    KText2(it.original,size = statusApp.fontSize)
                }

                KText2(it.translated,size = statusApp.fontSize)
                if(bplaytext.value) playText(bplaytext,it.original)
            }
        }
    }
    }
}

 @Composable
 fun playText(bplayText:MutableState<Boolean>,txt:String){
     val context = ContextAmbient.current
    lateinit var t1:TextToSpeech
     t1 = TextToSpeech(
         context,
         TextToSpeech.OnInitListener { status ->
             if (status != TextToSpeech.ERROR) {
                 t1.setLanguage(Locale.forLanguageTag("ru"))
             }
         })
     Dialog(onCloseRequest = {t1.shutdown(); bplayText.value=false}){
         Box(modifier = Modifier.fillMaxWidth(),backgroundColor = Color.Green){
             Text("HOLA NENG $txt")
             Button(onClick = {t1.speak(txt,TextToSpeech.QUEUE_FLUSH,null)}) {
                 Text("talk")
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
            val radioOptions = listOf("en", "es", "it")
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
data class LOriginalTrans(val lOriginalTrans:MutableList<OriginalTrans> = mutableListOf<OriginalTrans>())

/*@Composable
fun viewShort(article: KArticle,  statusApp: StatusApp) {
    val trans3 = state { LOriginalTrans() }
    onCommit(statusApp.lang) { getTranslation2(article, trans3, statusApp) }
    //getTranslation2(article, trans3, statusApp)
    Box() {
           when(statusApp.currentStatus){
               is AppStatus.Loading -> {Box(modifier = Modifier.fillMaxSize(),gravity = Alignment.Center,backgroundColor = Color.Transparent  ) {
                   CircularProgressIndicator()
               } }
               is AppStatus.Idle->drawListOriginalTranslated(article,loriginalTranslate = trans3.value,statusApp = statusApp)
               is AppStatus.Error->{ Text("Error view Short  ${(statusApp.currentStatus as AppStatus.Error).sError}")}
           }
    }
}*/




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

fun search(statusApp: StatusApp) {
    statusApp.currentStatus = AppStatus.Loading()
    GlobalScope.launch(Dispatchers.Main) {
        KA.articles = getArticles()

        val sb=StringBuilder()
        KA.articles.forEach{ sb.append(it.title) }
        val rt=translate(sb.toString(),statusApp.lang)
        val q=(rt as ResultTranslation.ResultList).Lorigtrans.lOriginalTrans


        //val JJ=KA.articles.zip(q,{arle,cu->OriginalTransLink(arle,cu)})
        val JJ=KA.articles.zip(q,{a,c->OriginalTransLink(a,c.translated)})
        println(JJ)
        statusApp.currentStatus = AppStatus.Idle()
    }
}

fun getLTransArticles(tt:MutableState<List<OriginalTransLink>>,statusApp: StatusApp){
    statusApp.currentStatus = AppStatus.Loading()
    GlobalScope.launch(Dispatchers.Main) {
        KA.articles = getArticles()

        val sb=StringBuilder()
        KA.articles.forEach{ sb.append(it.title) }
        val rt=translate(sb.toString(),statusApp.lang)
        val q=(rt as ResultTranslation.ResultList).Lorigtrans.lOriginalTrans


        //val JJ=KA.articles.zip(q,{arle,cu->OriginalTransLink(arle,cu)})
        val JJ=KA.articles.zip(q,{a,c->OriginalTransLink(a,c.translated)})
        tt.value=JJ
        println(JJ)
        statusApp.currentStatus = AppStatus.Idle()
        //return tt.value
    }
}

 sealed class ResultTranslation{
        class ResultList(val Lorigtrans:LOriginalTrans):ResultTranslation()
        class Error(val sError:String):ResultTranslation()
 }

suspend fun translate(text:String,lang:String):ResultTranslation{
    //var RS:ResultTranslation by lazy
    /*val lOT=LOriginalTrans()
    val ql=splitLongText(text)
    lOT.lOriginalTrans.add(OriginalTrans("n chunks: ${ql.size}","translated size 1 $lang"))
    ql.forEach {
        lOT.lOriginalTrans.add(OriginalTrans("${it.length} ","translated size 1 $lang"))
        lOT.lOriginalTrans.add(OriginalTrans("$it ","translated 1 $lang"))
    }*/

   //lOT.lOriginalTrans.add(OriginalTrans("->original text size ${text.length} ","translated 1 $lang"))
   // lOT.lOriginalTrans.add(OriginalTrans("original 2 ","translated 2 $lang "))
   // lOT.lOriginalTrans.add(OriginalTrans("chunks ${translateLongText(text,lang).size} ","translated 3 $lang "))
    //val RT=ResultTranslation.ResultList(lOT)
    //val RT=ResultTranslation.ResultList()
    val lot=LOriginalTrans()
    return try {
        //gettranslatedText(text,lang)
        val slT=splitLongText(text)
        slT.forEach {
            val RT2=gettranslatedText(it,lang)
            val rl=RT2 as ResultTranslation.ResultList
            lot.lOriginalTrans.addAll(rl.Lorigtrans.lOriginalTrans)
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
  /* val lOT=LOriginalTrans()
   lOT.lOriginalTrans.add(OriginalTrans("original 1 ","translated 1 $lang"))
    lOT.lOriginalTrans.add(OriginalTrans("original 2 ","translated 2 $lang "))
    lOT.lOriginalTrans.add(OriginalTrans("original 3 ","translated 3 $lang "))
    throw Exception("caca de la vaca")
   //   ResultTranslation.Error("cagada pastoret")
    ResultTranslation.ResultList(lOT)*/
   // lOT
   // println("url->$text")
    var url =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + "ru" + "&tl=" + "$lang" + "&dt=t&q=" + URLEncoder.encode(
            text,
            "utf-8"
        )
    val d = Jsoup.connect(url).ignoreContentType(true).get().text()
    val t = JSONArray(d)


    val lOriginalTrans=LOriginalTrans()

    val qsm = t.getJSONArray(0)
    for (i in 0 until qsm.length()) {
        val l = qsm.getJSONArray(i)
        val originalTrans=OriginalTrans(l.getString(1),l.getString(0))
        lOriginalTrans.lOriginalTrans.add(originalTrans)
    }
    ResultTranslation.ResultList(lOriginalTrans)
}
fun separateParagrafs(text: String): List<String> {
    val sP = text.split(". ")
    return sP
}

fun getTranslationLink(originalTransLink: OriginalTransLink, trans: MutableState<LOriginalTrans>, statusApp: StatusApp) {
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

fun getTranslation2(
    article: KArticle,
    trans: MutableState<LOriginalTrans>,
    statusApp: StatusApp
) {
    println("getTranslation2")
    statusApp.currentStatus = AppStatus.Loading()
    GlobalScope.launch(Dispatchers.Main) {
        val sTotalOriginal = article.title + ". " + article.desc
        //val sall = gettranslatedText(sTotalOriginal, statusApp.lang)
        val sall = translate(sTotalOriginal, statusApp.lang)
        when(sall){
            is ResultTranslation.ResultList->{trans.value=sall.Lorigtrans; statusApp.currentStatus = AppStatus.Idle() }
            is ResultTranslation.Error->{statusApp.currentStatus=AppStatus.Error(sall.sError)}
        }
       // trans.value = sall
       // statusApp.currentStatus = AppStatus.Idle()
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