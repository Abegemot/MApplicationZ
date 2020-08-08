package com.begemot.myapplicationz

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.begemot.kclib.*
import com.begemot.knewscommon.OriginalTransLink
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    //val sApp=StatusApp(Screens.ListNewsPapers,Screens.ListNewsPapers)
    val sApp=StatusApp(Screens.StartUpScreen,Screens.QuitScreen)

    @ExperimentalLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {     newsReaderApp(sApp) }

    }
    override fun onBackPressed() {
        //super.onBackPressed()
        //finish()
        //exitProcess(0)
        //sApp.currentScreen = sApp.currentBackScreen//Screens.ListHeadlines
        Timber.d("current ${sApp.currentScreen}  back ${sApp.currentBackScreen}")
        if(sApp.currentBackScreen==Screens.QuitScreen) finish()
        sApp.currentScreen = sApp.currentBackScreen
    }
}



sealed class Screens {
    object ListNewsPapers : Screens()
    object ListHeadLines:Screens()
    object StartUpScreen:Screens()
    class FullArticle(val originalTransLink: OriginalTransLink) : Screens()
    object QuitScreen:Screens()
}

sealed class AppStatus {
    object Idle : AppStatus()
    object Loading : AppStatus()
    class Error(val sError: String,val e: Exception?=null) : AppStatus()
}

class StatusApp(
    currentScreen:Screens,
    currentBackScreen:Screens,
    currentStatus: AppStatus=AppStatus.Loading,
   // nItems:Int=0,
    var fontSize: Int = prefs.fontSize,
    var lang: String = prefs.kLang

     )
{
         var currentScreen by mutableStateOf(currentScreen)
         var currentStatus by mutableStateOf(currentStatus)
         var currentBackScreen by mutableStateOf(currentBackScreen)
         var nItems by mutableStateOf(0)
         lateinit var   newsProvider:INewsPaper
    }

@ExperimentalLayout
@Composable
fun newsReaderApp(sApp: StatusApp){
    val ds=DrawerState(initialValue = DrawerValue.Closed, AnimationClockAmbient.current)
    val scaffoldState=remember{ ScaffoldState(ds ) }
    val kt = state { kTheme.values()[prefs.ktheme]  }
    val selectLang = state { false }
    val contactdialog = state { false }

    MaterialTheme(colors = kt.value.theme,typography = appTypography) {
        Scaffold(
            scaffoldState = scaffoldState,
            //  drawerContent = { Text("Drawer content") },
            topBar = {
                TopAppBar(
                    title = { title(statusApp = sApp) },
                    actions = {
                        IconButton(onClick = { kt.value = kTheme.next(kt.value); prefs.ktheme=kt.value.ordinal }
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

@ExperimentalLayout
@Composable
fun contactDialog(contactDialog: MutableState<Boolean>) {
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


@ExperimentalLayout
@Composable
fun screenDispatcher(selectLang: MutableState<Boolean>,contactdialog:MutableState<Boolean>,sApp:StatusApp){
    if(contactdialog.value) contactDialog(contactdialog)
    if (selectLang.value) editPreferences(selectLang, sApp)
    Box() {
        Surface {
            when (val s = sApp.currentScreen) {
                is Screens.ListNewsPapers -> newsPapersScreen(sApp)
                is Screens.ListHeadLines  -> headlinesScreen(sApp)
                is Screens.FullArticle    -> articleScreen(s.originalTransLink,sApp)
                is Screens.StartUpScreen  -> startUpScreen(sApp)
            }
        }
    }

}


 @Composable
 fun title(statusApp: StatusApp){
     Column() {
         Text(text = "News Reader",style = MaterialTheme.typography.h5)
         var s=""
         if(statusApp.nItems>0) s=" (${statusApp.nItems})"
         val currScreen=statusApp.currentScreen
         val sAux=when(currScreen){
             is Screens.ListNewsPapers->"News papers"
             is Screens.ListHeadLines -> statusApp.newsProvider.getName(Title.HEADLINES)+s
             is Screens.FullArticle ->statusApp.newsProvider.getName(Title.ARTICLE)
             is Screens.StartUpScreen ->"Start up"
             is Screens.QuitScreen->""
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
                    img, modifier =Modifier.height(50.dp)
                        .width(50.dp)
                            //Modifier.tag(tag = "centerImage")
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
 fun playText(bplayText:MutableState<Boolean>, txt:String, statusApp: StatusApp, lan:String){
     val context = ContextAmbient.current
     var msg by state{""}
    lateinit var t1:TextToSpeech

     var result by state{0}
     var tstatus by state{""}
     val l=Locale.forLanguageTag(lan)
     t1 = TextToSpeech(
         context,
         TextToSpeech.OnInitListener { status ->
             tstatus=status.toString()

             if (status != TextToSpeech.ERROR) {
                result= t1.setLanguage(Locale.forLanguageTag(lan))
                if(result==TextToSpeech.LANG_MISSING_DATA) msg="Missing data"
                if(result==TextToSpeech.LANG_NOT_SUPPORTED) msg="Lang not supported"

             }

         })
     //pos="voices ${t1.voices.size}"
     Dialog(onCloseRequest = {t1.shutdown(); bplayText.value=false}){

        // t1.setSpeechRate(0.7f)
        // t1.setPitch(0.5f)
         //Box(modifier = Modifier.fillMaxWidth(),backgroundColor = Color.Green){
         KWindow() {
              KText2(txt,size = statusApp.fontSize)
              Row(verticalGravity = Alignment.CenterVertically) {
                 IconButton(onClick = { t1.speak(txt, TextToSpeech.QUEUE_FLUSH, null, null) }) {
                    Icon(vectorResource(id = R.drawable.ic_volume_up_24px))
                 }
                 Text("($lan) $msg")
              }
         }
     }

 }


//data class KArticle(val title: String = "", val link: String = "", val desc: String = "")
//data class OriginalTransLink(val kArticle: KArticle,val translated: String)
//data class OriginalTrans(val original:String="",val translated:String="")
inline class ListOriginalTransList(val lOT:List<OriginalTransLink>)





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






