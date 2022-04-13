package com.begemot.myapplicationz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewModelScope
//import com.begemot.inreader.layout.FlowRowX
import com.begemot.knewscommon.*

import com.begemot.myapplicationz.App.Companion.sApp
import com.begemot.myapplicationz.screens.ArticleScreen
import com.begemot.myapplicationz.screens.headlinesScreen
import com.begemot.myapplicationz.screens.newsPapersScreen
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
//import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.debounce
//import androidx.datastore.preferences

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val REQUEST_UPDATE = 100
private const val APP_UPDATE_TYPE_SUPPORTED = AppUpdateType.IMMEDIATE

val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)


class MainActivity : ComponentActivity() {
//    lateinit var myApp:App
   // lateinit var sApp: StatusApp
  //  val preferenceDataStore: DataStore<Preferences> by lazy {
  //      createDataStore(name = "profile")
  //  }

    //@ExperimentalMaterialApi
    //@ExperimentalComposeUiApi

    @OptIn(ExperimentalMaterialApi::class,ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         //Timber.d("CREATE MAIN ACTIVITY---------------${App.sApp.status()}-------------------")
         Timber.d("CREATE MAIN ACTIVITY--------------- Set Up on his way current Screen ${sApp.currentScreen.value}")

       val t=runBlocking(Dispatchers.Default) {
            //val j=launch {  TestC() }
            //j.join()
            //TestC4()
           // Timber.d("Done!  ")
        }
        //TestC4()
        //TestC5()
        // GlobalScope.launch { TestC4() }
        //val s=GlobalScope.async {  TestC() }
        Timber.d("AFTER TESTC ")


        //App.setActivity(this.applicationContext)
         setContent { newsReaderApp(App.sApp) }
         window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
         sApp.vm.viewModelScope.launch(IO+CoroutineName("GOOGLEUPDATE")) {
             checkForAppUpdates()
         }

    }



    private fun checkForAppUpdates() {
        //1
        Timber.d("CHECK FOR APPLICATION UPDATES")
        val appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        val appUpdateInfo = appUpdateManager.appUpdateInfo
        appUpdateInfo.addOnSuccessListener {
            //2
            Timber.d("LISTENER")
            handleUpdate(appUpdateManager, appUpdateInfo)

        }
        Timber.d("END APPLICATION UPDATES")
    }
    private fun handleUpdate(manager: AppUpdateManager, info: Task<AppUpdateInfo>) {
            Timber.d("HANDLE APPLICATION UPDATES")
            handleImmediateUpdate(manager, info)
    }


    private fun handleImmediateUpdate(manager: AppUpdateManager, info: Task<AppUpdateInfo>) {
         Timber.d("HANDLE IMMEDIATE UPDATES  ${info.result.updateAvailability()}")
        //1
        manager.startUpdateFlowForResult(info.result, AppUpdateType.IMMEDIATE, this, REQUEST_UPDATE)

        if ((info.result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                    //2
                    info.result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) &&
            //3
            info.result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            //4
            manager.startUpdateFlowForResult(info.result, AppUpdateType.IMMEDIATE, this, REQUEST_UPDATE)
        }

    }


    override fun onDestroy() {
        Timber.d(App.sApp.status())
        App.sApp.vm.toneAndPitchMap.save()
        //sApp.currentScreen.value=Screens.NewsPapersScreen
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        Timber.d("------")
        super.onSaveInstanceState(outState, outPersistentState)
        App.sApp.vm.toneAndPitchMap.save()
    }


    override fun onBackPressed() {
        val sc=CoroutineScope(Dispatchers.Main+CoroutineName("onBackPressed"))
        Timber.d("Going back to ${sApp.currentBackScreen} from ${sApp.currentScreen.value}  ${App.sApp.status()}")
   //     Timber.d("${App.sApp.status()}")
        if(App.sApp.currentScreen.value is Screens.FullArticleScreen){
            if(App.sApp.modeBookMark) {
                App.sApp.modeBookMark=false
                sc.launch {
                    Timber.d("holdingItem->${App.sApp.vm.article.holdingItem.value}")
                    App.sApp.vm.article.listState?.scrollToItem(App.sApp.vm.article.holdingItem.value)
                }
                return
            }
            App.sApp.vm.article.reinizializeArticle2()

        }

        if (App.sApp.currentBackScreen == Screens.QuitScreen) {
            finish()
            //App.sApp.currentScreen.value=Screens.NewsPapersScreen
        }else
            App.sApp.currentScreen.value = App.sApp.currentBackScreen
        App.sApp.currentStatus.value = AppStatus.Idle
        //
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

/*object SStatusApp {
    val fontSize = mutableStateOf(prefs.fontSize)
    var lang by mutableStateOf(prefs.kLang)
}

fun pos(s: SStatusApp) {
    Timber.d(s.lang)
}*/


class StatusApp(
    currentScreen: Screens,
    var currentBackScreen: Screens,
    currentStatus: AppStatus = AppStatus.Loading,
    //val cNPrefs by mutableStateOf(KNewsPrefs())
    var currentNewPreferences: KNewsPrefs,


    ) {

    var shallIquit by mutableStateOf(false)
    var currentLink by mutableStateOf("")

    val fontSize = mutableStateOf(currentNewPreferences.fontsize)


    var userlang by mutableStateOf(currentNewPreferences.lang)  //prefs.kLang)

    //val kt = mutableStateOf(kTheme.values()[prefs.ktheme])
    val kt = mutableStateOf(kTheme.values()[currentNewPreferences.ktheme])

    val selectedNews = mutableStateOf(currentNewPreferences.selectedNews)

    val currentStatus = mutableStateOf(currentStatus)

    val currentScreen = mutableStateOf(currentScreen)

    lateinit var currentNewsPaper: NewsPaper
    val userID by lazy { currentNewPreferences.userid }

    var visibleInfoBar by mutableStateOf(false)
    val vm = VM()
    var selectLang = mutableStateOf(false)

    var romanized by mutableStateOf(currentNewPreferences.romanize)

    var modeBookMark by mutableStateOf(false)
    var arethereBookMarks by mutableStateOf(false)

    fun setNewPrefs(knp:KNewsPrefs){
        currentNewPreferences=knp
    }


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
                                                                 Current Lang   ${userlang}
        """.padStart(50, 'u')
    }

    fun getNP():String{
        if(!::currentNewsPaper.isInitialized) return "not set"
        return currentNewsPaper.name
    }

    fun getLangTxt():String{
        if(currentScreen.value==Screens.NewsPapersScreen) return "  ($userlang)"
        return if(::currentNewsPaper.isInitialized) "  (${currentNewsPaper.olang})->($userlang)" else "  ($userlang)"
    }

    fun status(): String =
        """Appstatus->  
           current screen: ${currentScreen.value} 
           status: ${currentStatus.value}
           bkscreen: ${currentBackScreen}
           user lang $userlang
           user: ${userID}
           newPrefs ${sApp.currentNewPreferences} 
           lanpitch ${sApp.vm.toneAndPitchMap}
           newsPapersList ${sApp.vm.newsPapers}
           currentNewPaper ${if(::currentNewsPaper.isInitialized) "$currentNewsPaper" else "NULL"}
           selectedNews  ${selectedNews.value}
           headlines   ${sApp.vm.headLines}<-end Appstatus"""

    fun getHeadLineParameters(): GetHeadLines {
        return GetHeadLines(
            currentNewsPaper.handler,
            userlang,
            vm.headLines.dataHeadlines
        )
    }
    fun setCurrentBookMark(i:Int){

    }


}


@Composable
fun setat(){
    //SetUpScreen(sApp = App.sApp)
    //Text("SETAT")
}


//@ExperimentalComposeUiApi
//@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun newsReaderApp(sApp: StatusApp) {
    Timber.d("Runing!!  current screen ${sApp.currentScreen.value}")
    val ds = DrawerState(initialValue = DrawerValue.Closed)
    val scaffoldState = remember { ScaffoldState(ds, SnackbarHostState()) }
    val contactdialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    //if(sApp.currentScreen.value==Screens.SetUpScreen) { setat(); return }
    MaterialTheme(colors = sApp.kt.value.theme, typography = appTypography) {
        if(sApp.currentScreen.value==Screens.SetUpScreen){
            /*Box(Modifier.width(100.dp).height(100.dp).background(Color.Transparent)){
                Text("POS")
            }*/
            //return@MaterialTheme
            SetUpScreen(sApp)
            return@MaterialTheme
        }
//        return@MaterialTheme
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
    TopAppBar(modifier = Modifier.height(77.dp), elevation = 2.dp) {
        Column(
            Modifier
                //.width(276.dp)
                .weight(1f)
                .fillMaxHeight()
                .align(Alignment.CenterVertically),

            //.padding(start = 5.dp, top = 10.dp)
            //.border(BorderStroke(1.dp,Color.White))
            //verticalArrangement = Arrangement.SpaceEvenly
        )
        {
            Row(
               // verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    //.height(34.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                 //   .padding(vertical = 5.dp)
                //    .align(Alignment.CenterVertically)
               //     .border(BorderStroke(1.dp,Color.Magenta))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.align(
                    Alignment.CenterVertically)) {
                    val st =
                        if (ls[0].length < 10) MaterialTheme.typography.h6 else if (ls[0].length < 19) MaterialTheme.typography.h6 else MaterialTheme.typography.body1
                    Text(
                        text = ls[0],
                        style = st,
                        //modifier = Modifier.alignBy(
                        //    alignmentLine = FirstBaseline
                        //),
                        fontWeight = FontWeight.Bold
                    )

                //Column(horizontalAlignment = Alignment.End,modifier = Modifier.align(
                //    Alignment.CenterEnd)) {
                    Text(
                        text = sApp.getLangTxt(),
                        style = MaterialTheme.typography.caption,
                        //modifier = Modifier.alignBy(
                        //    alignmentLine = FirstBaseline
                        // ),
                        fontWeight = FontWeight.Bold,
                        //textAlign = VerticalAlignmentLine
                        //textAlign = TextAlign.End
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.Top,

                modifier = Modifier
                    //.height(30.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 2.dp)

                    //.border(BorderStroke(1.dp,Color.Yellow))
            ) {
                Text(
                    ls[1],
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,

                )
            }
            //Text("left column 1 un dos tres sopar al mati")

        }
        Column(
            Modifier
                //.border(1.dp,Color.Black)
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
        " News papers"
    )
    if (sApp.currentScreen.value == Screens.HeadLinesScreen) {
        var date =
            "Head Lines (${sApp.vm.headLines.listHL.size})   ${strfromdateasLong(sApp.vm.headLines.dataHeadlines)}"
        if (sApp.currentNewsPaper.kind == KindOfNews.BOOK) date =
            " Chapters (${sApp.vm.headLines.listHL.size})"
        return listOf(sApp.currentNewsPaper.name, date)
    }
    if (sApp.currentScreen.value is Screens.FullArticleScreen) {
        var sNameWork = sApp.currentNewsPaper.name
        if (sApp.currentNewsPaper.kind == KindOfNews.BOOK) {
            if (sApp.modeBookMark) return listOf(
                sNameWork,
                " Chapter ${sApp.currentLink} Bookmarks (${sApp.vm.article.bookMarks.value.bkMap.size})"
            )
            else return listOf("$sNameWork", " Chapter ${sApp.currentLink}  (${1+sApp.vm.article.iInitialItem.value} / ${sApp.vm.article.lArticle.value.size})")
        } else {
            if (sApp.modeBookMark) return listOf(
                sNameWork,
                " Article Bookmarks (${sApp.vm.article.bookMarks.value.bkMap.size})"
            )
            else return listOf("${sApp.currentNewsPaper.name}", " Article ")

        }
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
                sApp.currentNewPreferences.ktheme = sApp.kt.value.ordinal
                Timber.d("on change theme prev theme $sprevTheme current theme ${sApp.kt.value.name} ")
            },
            Modifier.size(sicon)
        ) {
            Icon(Icons.Filled.Favorite,contentDescription = "")
        }
        IconButton(onClick = { sApp.selectLang.value = true }, Modifier.size(sicon)) {
            Icon(Icons.Filled.Settings,contentDescription = null)
        }

        if(sApp.currentScreen.value is Screens.FullArticleScreen && !sApp.modeBookMark){
            IconButton(onClick = {
                scope.launch {
                     sApp.vm.article.listState?.scrollToItem(sApp.vm.article.iInitialItem.value)

                }
            },Modifier.size(sicon)){
                Icon(
                    Icons.Filled.Home,
                    contentDescription = null,
                )
            }
        }

        if(sApp.currentScreen.value is Screens.FullArticleScreen && sApp.arethereBookMarks && !sApp.modeBookMark){
            IconButton(onClick = {
                scope.launch {
                    sApp.modeBookMark = true
                    sApp.vm.article.holdingItem.value = sApp.vm.article.listState?.firstVisibleItemIndex ?: 0
                    sApp.vm.article.listState?.scrollToItem(0)
                }
                                 },Modifier.size(sicon)){
                Icon(
                    painterResource(id = R.drawable.ic_bookmark_border_black_24dp),
                    contentDescription = null,
                                    )
            }

        }

        if (sApp.currentScreen.value == Screens.HeadLinesScreen && sApp.currentNewsPaper.kind==KindOfNews.NEWS)
            IconButton(onClick = {
                scope.launch {
                    //sApp.vm.checkUpdates(sApp)
                    sApp.setMsg("checking updates")
                    sApp.vm.headLines.checkUpdates(sApp)
                }
            }, Modifier.size(sicon)) {
                Icon(Icons.Filled.Refresh,contentDescription = "")
            }
    }
}

@Composable
fun MessageBar(sApp: StatusApp) {
    val value: String by sApp.vm.msg.mesage.collectAsState("")
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
    Timber.d("${sApp.currentScreen.value}")
    if (contactdialog.value) contactDialog(contactdialog)
    if (sApp.selectLang.value) editPreferences(sApp)
    Surface {
        when (val s = sApp.currentScreen.value) {
            is Screens.NewsPapersScreen ->   newsPapersScreen(sApp)
            is Screens.HeadLinesScreen -> headlinesScreen(sApp)
            is Screens.FullArticleScreen -> ArticleScreen(s.originalTransLink, sApp)
            is Screens.SetUpScreen ->{}//SetUpScreen(sApp)
            is Screens.QuitScreen -> SetUpScreen(sApp) //newsPapersScreen(sApp)
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
                Text(sAux)
                Timber.d("END ERROR")
            }
        }
    }
}

//Max val 664,726,754,720




