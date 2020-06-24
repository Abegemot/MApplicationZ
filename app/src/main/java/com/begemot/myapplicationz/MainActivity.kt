       package com.begemot.myapplicationz

//import androidx.test.core.app.ApplicationProvider.getApplicationContext
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.*
import androidx.ui.res.imageResource
import androidx.ui.res.vectorResource
import androidx.ui.unit.dp
import com.begemot.kclib.*

import java.util.*
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity() {
    val sApp=StatusApp(Screens.ListNewsPapers,Screens.ListNewsPapers)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // if(BuildConfig.DEBUG){
       //     Timber.plant(Timber.DebugTree())
       // }
        //checkWifi(this.applicationContext)
        //setContent { LoginUi() }

        setContent { newsReaderApp(sApp) }

    }
    override fun onBackPressed() {
        //super.onBackPressed()
        sApp.currentScreen = sApp.currentBackScreen//Screens.ListHeadlines
    }
}

sealed class Screens {
    object RT_ListHeadlines : Screens()
    class  RT_FullArticle(val originalTransLink: OriginalTransLink) : Screens()
    object SZ_ListHeadlines : Screens()
    class  SZ_FullArticle(val originalTransLink: OriginalTransLink) : Screens ()
    object ListNewsPapers : Screens()
}

     inline fun RT_FullArticle(otl:OriginalTransLink):Screens{ return Screens.RT_FullArticle(otl) }
     inline fun SZ_FullArticle(otl:OriginalTransLink):Screens{ return Screens.SZ_FullArticle(otl) }
     inline fun RT_ListHeadlines():Screens{ return Screens.RT_ListHeadlines}
     inline fun SZ_ListHeadlines():Screens{ return Screens.SZ_ListHeadlines}
     //val APOS2 = {otl:OriginalTransLink->Screens.RT_FullArticle(otl)}

sealed class AppStatus {
    object Idle : AppStatus()
    object Loading : AppStatus()
    class Error(val sError: String,val e: Exception?=null) : AppStatus()
}

class StatusApp(
    currentScreen:Screens,
    currentBackScreen:Screens,
    currentStatus: AppStatus=AppStatus.Loading,
    nItems:Int=0,
    var fontSize: Int = prefs.fontSize,
    var lang: String = prefs.kLang

     )
{
         var currentScreen by mutableStateOf(currentScreen)
         var currentStatus by mutableStateOf(currentStatus)
         var currentBackScreen by mutableStateOf(currentBackScreen)
         var nItems by mutableStateOf(0)
    }

@Composable
fun newsReaderApp(sApp: StatusApp){
    val scaffoldState=remember{ScaffoldState()}
    val kt = state { kTheme.DARK  }
    val selectLang = state { false }
    val contactdialog = state { false }

    MaterialTheme(colors = kt.value.theme,typography = appTypography) {
        Scaffold(
            scaffoldState = scaffoldState,
            //  drawerContent = { Text("Drawer content") },
            topAppBar = {
                TopAppBar(
                    title = { title(statusApp = sApp) },
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
            },
            floatingActionButtonPosition = Scaffold.FabPosition.End,
            floatingActionButton = {
                if (sApp.currentScreen == Screens.ListNewsPapers)
                    ExtendedFloatingActionButton(
                        text = { Text("+") },
                        onClick = { contactdialog.value=true  }


                    )
            },
            bodyContent = { modifier ->
                screenDispatcher(selectLang,contactdialog,sApp)

            }
        )
    }

}

@Composable
fun contactDialog(contactDialog:MutableState<Boolean>) {
    val context = ContextAmbient.current
    val s1 = state { "" }
    var txt by state { TextFieldValue("") }

    Dialog(onCloseRequest = { contactDialog.value = false }) {
        KWindow() {
            KHeader(txt = "Contact us", onClick = { contactDialog.value = false })
            KField2(txt = "holax", st = s1)
            //KField("Your Message:", s1)
            //Box(Modifier.preferredHeight(150.dp).fillMaxWidth(),backgroundColor = Color.Red){
            // KField(txt = "lane", st = s1)
            //FilledTextField(modifier = Modifier.fillMaxSize() ,value = txt, onValueChange ={txt=it},label={Text("label")} )

            //      }
            //}
            KButtonBar {
                Button(onClick = {
                    //sendEmail(context)
                    sendmail(s1.value)
                    contactDialog.value = false

                }) { Text(text = "Send") }

            }

        }

    }

}


@Composable
fun screenDispatcher(selectLang: MutableState<Boolean>,contactdialog:MutableState<Boolean>,sApp:StatusApp){
    if(contactdialog.value) contactDialog(contactdialog)
    if (selectLang.value) editPreferences(selectLang, sApp)
    Box() {
        Surface {
            when (val s = sApp.currentScreen) {
                is Screens.ListNewsPapers -> newsPapersScreen2(sApp)
                is Screens.RT_ListHeadlines -> headlinesScreen(sApp, ::RT_FullArticle,::getRT_Headlines)
                is Screens.SZ_ListHeadlines->headlinesScreen(sApp,::SZ_FullArticle,::getSZ_Headlines)
                //is Screens.RT_FullArticle -> RT_articleScreen(s.originalTransLink, sApp)
                is Screens.RT_FullArticle-> articleScreen(
                    originalTransLink = s.originalTransLink,
                    statusApp = sApp,
                    backScreenFun = ::RT_ListHeadlines,
                    getArticle = ::getRTArticle
                )
                is Screens.SZ_FullArticle-> articleScreen(
                    originalTransLink = s.originalTransLink,
                    statusApp = sApp,
                    backScreenFun = ::SZ_ListHeadlines,
                    getArticle = ::getSZArticle
                )

            }

        }
    }

}


 @Composable
 fun title(statusApp: StatusApp){
     Column() {
         Text(text = "News Reader",style = MaterialTheme.typography.h5)

         var s=""
         if(statusApp.nItems>0) s="(${statusApp.nItems})"
         val currScreen=statusApp.currentScreen
         val sAux=when(currScreen){
             is Screens.RT_FullArticle->" RT Article"
             is Screens.RT_ListHeadlines->" RT Headlines  $s"
             is Screens.ListNewsPapers->"News papers"
             is Screens.SZ_ListHeadlines->" SZ Headlines  $s"
             is Screens.SZ_FullArticle -> " SZ Article"
         }
         Text(" $sAux",style = MaterialTheme.typography.subtitle1)
     }
 }


@Composable
 fun waiting(){
    Timber.d("->waiting")
   Box(modifier = Modifier.fillMaxSize(),gravity = Alignment.Center,backgroundColor = Color.Transparent  ) {
       CircularProgressIndicator()
   }
   Timber.d("<-  waiting")
}

@Composable
   fun displayError(sError:String,e: Exception?=null) {
    var msg = "null"
    val sw = StringWriter()
    if (e != null) {
        e.printStackTrace(PrintWriter(sw))
        msg = sw.toString()
    }
    Surface(color = MaterialTheme.colors.error) {


        VerticalScroller() {

            Box(border = Border(2.dp, Color.Blue),padding = 10.dp) {
                val img = imageResource(id = R.drawable.icons8_black_cat_48)
                Image(
                    img, modifier = Modifier.tag(tag = "centerImage")
                        .height(50.dp)
                        .width(50.dp)
                )
                //Icon(vectorResource(id = R.drawable.icons8_black_cat_48))
                //Icon(asset = ImageAsset(R.drawable.icons8_black_cat_48 ,20))
                //ImageAsset(R.drawable.icons8_black_cat_48 ,100)
                Text("Error : $sError")
                Text("stackTrace -> $msg")
                Timber.d("stackTrace -> $msg")
            }
        }
    }
}




 @Composable
 fun playText(bplayText:MutableState<Boolean>, txt:String, statusApp: StatusApp, original:Boolean=true){
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
fun editPreferences(selectLang: MutableState<Boolean>, statusApp: StatusApp) {
    Dialog(onCloseRequest = { selectLang.value = false }) {
        val localFontSize=state{statusApp.fontSize}
        KWindow() {
            KHeader(txt = "Settings", onClick = {})
            val radioOptions = listOf("en", "es", "it","ur","zh","ca")
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






sealed class KResult<T,R>{
    class Succes<T,R>(val t:T):KResult<T,R>()
    class Error<T,R>(val msg:String,val e:Exception?=null):KResult<T,R>()
    object Empty:KResult<Nothing,Nothing>()
}


inline fun <reified T, reified R> exWithException(afun:()->T): KResult<T,R> {
    return try {
           val p=afun()
          KResult.Succes(p)

    }catch(e:Exception){
          KResult.Error("error",e)
    }
}






