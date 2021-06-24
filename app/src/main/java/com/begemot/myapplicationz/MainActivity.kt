package com.begemot.myapplicationz

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Refresh
import com.begemot.kclib.*
import timber.log.Timber

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
//import com.begemot.inreader.layout.FlowRowX
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.screens.articleScreen
import com.begemot.myapplicationz.screens.headlinesScreen
import com.begemot.myapplicationz.screens.newsPapersScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
//import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.debounce


class MainActivity : ComponentActivity() {
    lateinit var sApp: StatusApp


    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sApp = App.sApp
        Timber.d(sApp.status())
        setContent { newsReaderApp(sApp) }
    }

    override fun onDestroy() {
        Timber.d("${sApp.status()}")
        sApp.vm.toneAndPitchMap.save()
        sApp.currentScreen.value=Screens.NewsPapersScreen
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        Timber.d("------")
        super.onSaveInstanceState(outState, outPersistentState)
        sApp.vm.toneAndPitchMap.save()
    }

    override fun onBackPressed() {
        Timber.d("${sApp.status()}")
        if(sApp.currentScreen.value is Screens.FullArticleScreen){
            if(sApp.modeBookMark) {
                sApp.modeBookMark=false
                return
            }

        }

        if (sApp.currentBackScreen == Screens.QuitScreen) {   finish(); sApp.currentScreen.value=Screens.NewsPapersScreen;}
        sApp.currentStatus.value = AppStatus.Idle
        sApp.currentScreen.value = sApp.currentBackScreen
    }
}


sealed class Screens {
    object NewsPapersScreen : Screens()
    object HeadLinesScreen : Screens()

    //object StartUpScreen:Screens()
    object SetUpScreen:Screens()
    class FullArticleScreen(val originalTransLink: OriginalTransLink) : Screens()
    object QuitScreen : Screens()

    override fun toString(): String = this.javaClass.simpleName
}

sealed class AppStatus {
    object Idle : AppStatus()
    object Loading : AppStatus()
    object Refreshing : AppStatus()
    class Error(val sError: String="", val e: Exception? = null) : AppStatus()

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

    var modeBookMark by mutableStateOf(false)
    var arethereBookMarks by mutableStateOf(false)

    fun setMsg(sAux: String) {
        vm.msg.setMsg(this, sAux)
    }
    fun setMsg2(sAux: String) {
        vm.msg.setMsg2(sAux)
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

    fun getLangTxt():String{
        if(currentScreen.value==Screens.NewsPapersScreen) return "  ($lang)"
        return if(::currentNewsPaper.isInitialized) "  (${currentNewsPaper.olang})->($lang)" else "  ($lang)"
    }

    fun status(): String =
        "Appstatus->  status: ${currentStatus.value},screen: ${currentScreen.value},bkscreen: ${currentBackScreen},lang: $lang,news paper : ${getNP()}, user: ${userID},"

    fun getHeadLineParameters(): GetHeadLines {
        return GetHeadLines(
            currentNewsPaper.handler,
            lang,
            vm.headLines.dataHeadlines
        )
    }
    fun setCurrentBookMark(i:Int){

    }


}


@Composable
fun setat(){
    SetUpScreen(sApp = App.sApp)
    //Text("SETAT")
}


@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun newsReaderApp(sApp: StatusApp) {



    Timber.d("Runing!!  current screen ${sApp.currentScreen.value}")
    val ds = DrawerState(initialValue = DrawerValue.Closed)
    val scaffoldState = remember { ScaffoldState(ds, SnackbarHostState()) }
    val contactdialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    if(sApp.currentScreen.value==Screens.SetUpScreen) { setat(); return }
    MaterialTheme(colors = sApp.kt.value.theme, typography = appTypography) {
        Scaffold(
            scaffoldState = scaffoldState,
            //  drawerContent = { Text("Drawer content") },
            topBar = {
                MAppBar2(sApp)
            },
            //floatingActionButtonPosition = End,
            floatingActionButton = {
                if (sApp.currentScreen.value == Screens.NewsPapersScreen)
                    ExtendedFloatingActionButton(
                        text = { Text("+") },
                        onClick = {
                            //KCache.removeHeadLinesOf("RT")
                            //KCache.listFiles()
                            contactdialog.value = true
                        }


                    )
            },
            content = { modifier ->

                //Column(modifier = Modifier.fillMaxSize()){
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier
                        .fillMaxWidth(1f)
                        .fillMaxHeight(1f)) {
                        screenDispatcher(contactdialog, sApp)
                    }
                    //.border(2.dp, Color.Cyan)
                    if (sApp.visibleInfoBar)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .fillMaxHeight(0.11f)
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


@Composable
fun MAppBar2(sApp: StatusApp) {
    val ls = getTitles(sApp)
    TopAppBar(modifier = Modifier.height(77.dp), elevation = 200.dp) {
        Column(
            Modifier
                //.width(276.dp)
                .weight(1f)
                .align(Alignment.CenterVertically)
                //.padding(start = 5.dp, top = 10.dp)
                //.border(BorderStroke(1.dp,Color.White))
        )
        {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    //.height(34.dp)
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    //.border(BorderStroke(1.dp,Color.Magenta))
            ) {
                val st = if(ls[0].length<10)  MaterialTheme.typography.h5 else  if(ls[0].length<19) MaterialTheme.typography.h6 else MaterialTheme.typography.body1
                Text(
                    text = ls[0],
                    style = st,
                    modifier = Modifier.alignBy(
                        alignmentLine = FirstBaseline
                    ),
                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = sApp.getLangTxt(),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.alignBy(
                        alignmentLine = FirstBaseline
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    //.height(30.dp)
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    //.border(BorderStroke(1.dp,Color.Yellow))
            ) {
                Text(
                    ls[1],
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
            }
            //Text("left column 1 un dos tres sopar al mati")

        }
        Column(
            Modifier
                .width(80.dp)
                .align(Alignment.CenterVertically),horizontalAlignment = Alignment.End
                //.border(1.dp, Color.Black)
                )
        {
            //Box(alignment = Alignment.Center) {
            appIcons(sApp)
            // }
        }
    }
}

@Composable
fun appIconsC(sApp: StatusApp){

}


fun getTitles(sApp: StatusApp): List<String> {
    if (sApp.currentScreen.value == Screens.NewsPapersScreen) return listOf(
        "INews Reader",
        "News papers"
    )
    if (sApp.currentScreen.value == Screens.HeadLinesScreen) {
        val date = "Head Lines (${sApp.vm.headLines.listHL.size})   ${strfromdateasLong(sApp.vm.headLines.dataHeadlines)}"
        val sAux = sApp.currentNewsPaper.name
        return listOf(sAux, date)
    }
    //if(sApp.currentScreen is Screens.StartUpScreen) return listOf("INews Reader","Start Up")
    if (sApp.currentScreen.value is Screens.FullArticleScreen) {
        var   sAux = sApp.currentNewsPaper.name
        if(sApp.modeBookMark) return listOf("Bookmarks",sAux)
        else return listOf("Article", sAux)
    } else {
        //sApp.currentScreen.value = Screens.NewsPapersScreen
        //Timber.d("current screen :${sApp.currentScreen.value}")
            Timber.d("${sApp.status()}")
        return listOf("INews Reader", "Set Up")
    }

}


//border(BorderStroke(1.dp,Color.Green)).

@Composable
fun appIcons(sApp: StatusApp) {
    val sicon = 38.dp
    val scope = rememberCoroutineScope()
    FlowRowX(
        // modifier = Modifier.border(1.dp,Color.Black)
        //crossAxisSpacing = 25.dp,
        //crossAxisAlignment = FlowCrossAxisAlignment.Center,
        //mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
        // mainAxisSize = SizeMode.Wrap,
        lastLineMainAxisAlignment = MainAxisAlignment.End,

        ) {

        IconButton(
            onClick = {
                val sprevTheme=sApp.kt.value.name
                sApp.kt.value = kTheme.next(sApp.kt.value)
                prefs.ktheme = sApp.kt.value.ordinal
                Timber.d("on change theme prev theme $sprevTheme current theme ${sApp.kt.value.name} ")
            },
            Modifier.size(sicon)
        ) {
            Icon(Icons.Filled.Favorite,contentDescription = "")
        }
        IconButton(onClick = { sApp.selectLang.value = true }, Modifier.size(sicon)) {
            Icon(Icons.Filled.Settings,contentDescription = null)
        }

        if(sApp.currentScreen.value is Screens.FullArticleScreen && sApp.arethereBookMarks && !sApp.modeBookMark){
            IconButton(onClick = {sApp.modeBookMark=true},Modifier.size(sicon)){
                Icon(
                    painterResource(id = R.drawable.ic_bookmark_border_black_24dp),
                    contentDescription = null,
                                    )
            }
        }

        if (sApp.currentScreen.value == Screens.HeadLinesScreen)
            IconButton(onClick = {
                scope.launch {
                    //sApp.vm.checkUpdates(sApp)
                    sApp.setMsg("checking updates")
                    sApp.vm.headLines.  checkUpdates(sApp)
                }
            }, Modifier.size(sicon)) {
                Icon(Icons.Filled.Refresh,contentDescription = "")
            }
    }
}

@Composable
fun MessageBar(sApp: StatusApp) {
    val value: String by sApp.vm.msg.mesage.debounce(0L).collectAsState("")
    if (value.isBlank()) return
    //Timber.d("msg  $value")
    //val visibleInfoBar = remember{ mutableStateOf(true)}
    LaunchedEffect(value) {
        delay(1600)
        //visibleInfoBar.value=false
        sApp.visibleInfoBar = false
        // sApp.currentStatus.value = AppStatus.Idle
        Timber.d("after closing msg ${sApp.status()}")
    }

    val m1 = Modifier
        .padding(horizontal = 15.dp, vertical = 5.dp)
        .fillMaxSize(1f)
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



@ExperimentalComposeUiApi
@Composable
fun contactDialog(contactDialog: MutableState<Boolean>) {
    val context = LocalContext.current

    val s1 = remember{ mutableStateOf("") }
    var txt by remember{ mutableStateOf(TextFieldValue("")) }

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
                    sendmail(s1.value)
                    contactDialog.value = false

                }) { Text(text = "Send") }

            }

        }

    }

}



@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun screenDispatcher(contactdialog: MutableState<Boolean>, sApp: StatusApp) {
    Timber.d(sApp.status())
    if (contactdialog.value) contactDialog(contactdialog)
    if (sApp.selectLang.value) editPreferences(sApp)
    Surface {
        when (val s = sApp.currentScreen.value) {
            is Screens.NewsPapersScreen ->   newsPapersScreen(sApp)
            is Screens.HeadLinesScreen -> headlinesScreen(sApp)
            is Screens.FullArticleScreen -> articleScreen(s.originalTransLink, sApp)
            is Screens.SetUpScreen -> {}//SetUpScreen(sApp)
            is Screens.QuitScreen -> newsPapersScreen(sApp)
        }
    }
}


@Composable
fun displayError(sError: String, e: Exception? = null, sApp: StatusApp) {
    //val msg = getStackExceptionMsg2(e)
    val sAux="${sApp.status()}\n\n$sError"
    Surface(color = MaterialTheme.colors.error) {
        Column(
            modifier = Modifier
                .border(BorderStroke(2.dp, Color.Blue))
                .padding(10.dp)
                .fillMaxSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val img = painterResource(id = R.drawable.icons8_black_cat_48)
                Image(
                    img, modifier = Modifier
                        .height(50.dp)
                        .padding(10.dp)
                        .width(50.dp),contentDescription = ""
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
            val scrollState: ScrollState = rememberScrollState(0)
            Column(modifier=Modifier.verticalScroll(scrollState)) {

                //Text("$sError\n${sApp.status()}\n")
                //Text("$msg\n END STACK TRACE")
                Text(sAux)
                //Timber.d("stackTrace -> $msg")
                Timber.d("END ERROR")
            }
        }
    }
}






