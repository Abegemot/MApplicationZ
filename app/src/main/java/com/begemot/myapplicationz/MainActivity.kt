package com.begemot.myapplicationz

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.FabPosition.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.Icon
import androidx.compose.material.icons.filled.Refresh
import com.begemot.kclib.*
import timber.log.Timber
import java.util.*

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.begemot.knewscommon.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
//import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.debounce
import org.intellij.lang.annotations.JdkConstants


class MainActivity : AppCompatActivity() {
    lateinit var sApp: StatusApp

    @ExperimentalLayout
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sApp = App.sApp
        Timber.d("${sApp.status()}")
        setContent { newsReaderApp(sApp) }
    }

    override fun onBackPressed() {
        Timber.d("on back pressed ${sApp.status()}")
        if (sApp.currentBackScreen == Screens.QuitScreen) finish()
        sApp.currentStatus.value = AppStatus.Idle
        sApp.currentScreen.value = sApp.currentBackScreen
    }
}


sealed class Screens {
    object NewsPapersScreen : Screens()
    object HeadLinesScreen : Screens()

    //object StartUpScreen:Screens()
    class FullArticleScreen(val originalTransLink: OriginalTransLink) : Screens()
    object QuitScreen : Screens()

    override fun toString(): String = this.javaClass.simpleName
}

sealed class AppStatus {
    object Idle : AppStatus()
    object Loading : AppStatus()
    object Refreshing : AppStatus()
    class Error(val sError: String, val e: Exception? = null) : AppStatus()

    override fun toString() = this.javaClass.simpleName
}

object SStatusApp {
    val fontSize = mutableStateOf(prefs.fontSize)
    var lang by mutableStateOf(prefs.kLang)
}

fun pos(s: SStatusApp) {
    Timber.d(s.lang)
}


class StatusApp(
    currentScreen: Screens,
    currentBackScreen: Screens,
    currentStatus: AppStatus = AppStatus.Loading,
) {
    val fontSize = mutableStateOf(prefs.fontSize)
    var lang by mutableStateOf(prefs.kLang)


    val currentStatus = mutableStateOf(currentStatus)

    var currentBackScreen = currentBackScreen
    val currentScreen = mutableStateOf(currentScreen)

    lateinit var currentNewsPaper: NewsPaper
    val userID by lazy { prefs.userId }

    var visibleInfoBar by mutableStateOf(false)
    val vm = VM()
    val kt = mutableStateOf(kTheme.values()[prefs.ktheme])
    var selectLang = mutableStateOf(false)

    var romanized by mutableStateOf(prefs.romanize)

    fun setMsg(sAux: String) {
        vm.msg.setMsg(this, sAux)
    }

    override fun toString(): String {
        return """
                                                                 Current Screen ${currentScreen.toString()}
                                                                 Current Back Screen ${currentBackScreen}
                                                                 Current Status ${currentStatus}
                                                                 Current Lang   ${lang}
        """.padStart(50, 'u')
    }

    fun getNP():String{
        if(!::currentNewsPaper.isInitialized) return "not set"
        return currentNewsPaper.name
    }

    fun status(): String =
        "status->  screen: ${currentScreen.value},bkscreen: ${currentBackScreen},status: ${currentStatus.value},lang: $lang,news paper : ${getNP()}, user: ${userID},"

    fun getHeadLineParameters(): GetHeadLines {
        return GetHeadLines(
            currentNewsPaper.handler,
            lang,
            vm.dataHeadlines
        )
    }
}


@ExperimentalMaterialApi
@ExperimentalLayout
@Composable
fun newsReaderApp(sApp: StatusApp) {
    Timber.d("Runing!!")
    val ds = DrawerState(initialValue = DrawerValue.Closed, AnimationClockAmbient.current)
    val scaffoldState = remember { ScaffoldState(ds, SnackbarHostState()) }
    val contactdialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MaterialTheme(colors = sApp.kt.value.theme, typography = appTypography) {
        Scaffold(
            scaffoldState = scaffoldState,
            //  drawerContent = { Text("Drawer content") },
            topBar = {
                MAppBar2(sApp)
            },
            floatingActionButtonPosition = End,
            floatingActionButton = {
                if (sApp.currentScreen.value == Screens.NewsPapersScreen)
                    ExtendedFloatingActionButton(
                        text = { Text("+") },
                        onClick = {
                            //KCache.removeHeadLinesOf("RT")
                            KCache.listFiles()
                            contactdialog.value = true
                        }


                    )
            },
            bodyContent = { modifier ->

                //Column(modifier = Modifier.fillMaxSize()){
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth(1f).fillMaxHeight(1f)) {
                        screenDispatcher(contactdialog, sApp)
                    }
                    //.border(2.dp, Color.Cyan)
                    if (sApp.visibleInfoBar)
                        Box(
                            modifier = Modifier.fillMaxWidth(1f).fillMaxHeight(0.11f)
                                .align(Alignment.BottomStart)
                        ) {
                            //Box(){
                            MessageBar(sApp)
                        }
                }
                // }

            }
        )
    }

}

//border(BorderStroke(1.dp,Color.White)).
//border(BorderStroke(1.dp,Color.Gray)).
//border(BorderStroke(1.dp,Color.Magenta)).

@ExperimentalLayout
@Composable
fun MAppBar2(sApp: StatusApp) {
    val ls = getTitles(sApp)
    TopAppBar(modifier = Modifier.height(77.dp), elevation = 200.dp) {
        Column(Modifier.width(276.dp).padding(start = 5.dp, top = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(34.dp).fillMaxWidth()
            ) {
                Text(
                    text = ls[0],
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.alignBy(
                        alignmentLine = FirstBaseline
                    ),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "  (${sApp.lang})",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.alignBy(
                        alignmentLine = FirstBaseline
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.height(30.dp).fillMaxWidth()
            ) {
                Text(
                    ls[1],
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
            }
            //Text("left column 1 un dos tres sopar al mati")

        }
        Column(Modifier.width(80.dp).align(Alignment.CenterVertically)) {
            //Box(alignment = Alignment.Center) {
            appIcons(sApp)
            // }
        }
    }
}


fun getTitles(sApp: StatusApp): List<String> {
    if (sApp.currentScreen.value == Screens.NewsPapersScreen) return listOf(
        "INews Reader",
        "News papers"
    )
    if (sApp.currentScreen.value == Screens.HeadLinesScreen) {
        val date =
            "Head Lines (${sApp.vm.listHL.size})   ${strfromdateasLong(sApp.vm.dataHeadlines)}"
        val sAux = sApp.currentNewsPaper.name
        return listOf(sAux, date)
    }
    //if(sApp.currentScreen is Screens.StartUpScreen) return listOf("INews Reader","Start Up")
    if (sApp.currentScreen.value is Screens.FullArticleScreen) {
        val sAux = sApp.currentNewsPaper.name
        return listOf("Article", sAux)
    } else {
        sApp.currentScreen.value = Screens.NewsPapersScreen
        Timber.d("current screen :${sApp.currentScreen.value}")
        return listOf("", "")
    }

}


//border(BorderStroke(1.dp,Color.Green)).
@ExperimentalLayout
@Composable
fun appIcons(sApp: StatusApp) {
    val sicon = 38.dp
    val scope = rememberCoroutineScope()
    FlowRow(
        //crossAxisSpacing = 25.dp,
        //crossAxisAlignment = FlowCrossAxisAlignment.Center,
        //mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
        // mainAxisSize = SizeMode.Wrap,
        lastLineMainAxisAlignment = MainAxisAlignment.End,

        ) {

        IconButton(
            onClick = {
                Timber.d("on chacge theme click ${sApp.status()}")
                sApp.kt.value = kTheme.next(sApp.kt.value)
                prefs.ktheme = sApp.kt.value.ordinal
            },
            Modifier.size(sicon)
        ) {
            Icon(Icons.Filled.Favorite)
        }
        IconButton(onClick = { sApp.selectLang.value = true }, Modifier.size(sicon)) {
            Icon(Icons.Filled.Settings)
        }
        if (sApp.currentScreen.value == Screens.HeadLinesScreen)
            IconButton(onClick = {
                scope.launch {
                    //sApp.vm.checkUpdates(sApp)
                    sApp.setMsg("checking updates")
                    sApp.vm.checkHeadLinesUpdates(sApp)
                }
            }, Modifier.size(sicon)) {
                Icon(Icons.Filled.Refresh)
            }
    }
}

@Composable
fun MessageBar(sApp: StatusApp) {
    val value: String by sApp.vm.msg.mesage.debounce(0L).collectAsState("")
    if (value.isBlank()) return
    Timber.d("msg  $value")
    //val visibleInfoBar = remember{ mutableStateOf(true)}
    LaunchedEffect(value) {
        delay(1500)
        //visibleInfoBar.value=false
        sApp.visibleInfoBar = false
        // sApp.currentStatus.value = AppStatus.Idle
        Timber.d("after closing msg ${sApp.status()}")
    }
    val m1 = Modifier.padding(horizontal = 15.dp, vertical = 5.dp).fillMaxSize(1f)
//    Column(modifier = Modifier.fillMaxSize(1f).border(BorderStroke(1.dp,Color.Blue)),verticalArrangement = Arrangement.Center) {
    Column(modifier = Modifier.fillMaxSize(1f), verticalArrangement = Arrangement.Center) {
        Card(
            shape = RoundedCornerShape(6.dp),
            modifier = m1,
            elevation = 12.dp,
            border = BorderStroke(Dp.Hairline, MaterialTheme.colors.error),
            backgroundColor = MaterialTheme.colors.onBackground
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    value,
                    color = MaterialTheme.colors.background,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}


@ExperimentalLayout
@Composable
fun contactDialog(contactDialog: MutableState<Boolean>) {
    val context = ContextAmbient.current

    val s1 = mutableStateOf("")
    var txt by mutableStateOf(TextFieldValue(""))

    Dialog(onDismissRequest = { contactDialog.value = false }) {
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
fun screenDispatcher(contactdialog: MutableState<Boolean>, sApp: StatusApp) {
    Timber.d(sApp.status())
    if (contactdialog.value) contactDialog(contactdialog)
    if (sApp.selectLang.value) editPreferences(sApp)
    // Box() {
    Surface {
        when (val s = sApp.currentScreen.value) {
            is Screens.NewsPapersScreen -> {//sApp.currentStatus=AppStatus.Loading;
                newsPapersScreen(sApp)
            }
            is Screens.HeadLinesScreen -> headlinesScreen(sApp)
            is Screens.FullArticleScreen -> articleScreen(s.originalTransLink, sApp)
            //               is Screens.StartUpScreen  -> startUpScreen(sApp)
        }
    }
    // }

}


@Composable
fun displayError(sError: String, e: Exception? = null, sApp: StatusApp) {
    val msg = getStackExceptionMsg(e)
    val sAux="$sError\n${sApp.status()}\n\n$msg\nEND STACK TRACE"
    Surface(color = MaterialTheme.colors.error) {
        Column(
            modifier = Modifier.border(BorderStroke(2.dp, Color.Blue)).padding(10.dp).fillMaxSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val img = imageResource(id = R.drawable.icons8_black_cat_48)
                Image(
                    img, modifier = Modifier.height(50.dp).padding(10.dp)
                        .width(50.dp)
                    //Modifier.tag(tag = "centerImage")
                )
                Column(horizontalAlignment = Alignment.End,modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        sendmail(sAux)
                        sApp.currentStatus.value=AppStatus.Idle
                        sApp.currentScreen.value=sApp.currentBackScreen
                        sApp.setMsg("Message send")

                    }) { Text("Send email") }
                }
            }
            ScrollableColumn() {

                //Text("$sError\n${sApp.status()}\n")
                //Text("$msg\n END STACK TRACE")
                Text(sAux)
                Timber.d("stackTrace -> $msg")
                Timber.d("END ERROR")
            }
        }
    }
}






