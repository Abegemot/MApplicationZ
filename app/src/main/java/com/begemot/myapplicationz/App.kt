 package com.begemot.myapplicationz

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.begemot.knewscommon.KindOfNews
import com.begemot.myapplicationz.App.Companion.sApp
import com.google.android.play.core.ktx.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.slf4j.impl.HandroidLoggerAdapter
//import org.slf4j.impl.HandroidLoggerAdapter
import timber.log.Timber
import java.lang.Exception
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

 val logger = KotlinLogging.logger {}

 //cli 11:44.608 ERROR  [main] KClient.invoke->(KClient.kt:285) debug   enabled
 class LineNumberDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        val st =if(Thread.currentThread().name[0]=='D') "DD${Thread.currentThread().name.substringAfter("DefaultDispatcher")}" else "${Thread.currentThread().name}"
        val s="NKZ [$st] (${element.fileName}:${element.lineNumber}) ${element.methodName}"
        return s
    }
 }
  class App:Application(){

      companion object{
         lateinit var lcontext: Context
         lateinit var sApp2:StatusApp
         val sApp:StatusApp by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED)  {  createsApp()   }
         val fPath:String by lazy { "${lcontext.filesDir.absolutePath}/" }
      }

     override fun onCreate() {
        //System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
         HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG
         HandroidLoggerAdapter.ANDROID_API_LEVEL = Build.VERSION.SDK_INT;
         HandroidLoggerAdapter.APP_NAME = "NKZ" //"ZGB"

        super.onCreate()
        lcontext = applicationContext

        if(BuildConfig.DEBUG){
            //System.setProperty("kotlinx.coroutines.debug", "on" )
            System.setProperty(kotlinx.coroutines.DEBUG_PROPERTY_NAME,"on")
            Timber.plant(LineNumberDebugTree())
        }
        Timber.d("START ALL (App.kt:102)")
        //checkWifi(App.instance)
        KCache.fP= fPath
        //KCache.makeDir("MP3")
        setUP()
        Timber.d("END ALL")
     }




     //@ExperimentalPathApi
    fun setUP(){
        Timber.d(" --->begin setUP".padStart(65,' '))
        val scope= CoroutineScope(IO)
        //sApp.setMsg2("SETUP")
        scope.launch(IO +CoroutineName("setup")) {

            sApp.currentStatus.value = AppStatus.Loading
            //delay(3000)
            //      KCache.deleteFiles()
            Timber.d("setUP coroutine begin")

            val newInstall = if (sApp.userid.isEmpty()) {
                Timber.d("User ID not set :New Installation")
                sApp.userid=(UUID.randomUUID().toString())
                true
            } else false

            val res = if (newInstall) NewInstalation() else  LoadInstalation()
            if(res){
                sApp.currentStatus.value = AppStatus.Idle
                if(sApp.selectedNews >-1) {
                    sApp.currentNewsPaper = sApp.vm.newsPapers.lNewsPapers[sApp.selectedNews]
                    sApp.vm.headLines.getLines(sApp, sApp.currentNewsPaper)
                    if(sApp.currentNewsPaper.kind==KindOfNews.BOOK)
                        sApp.setCurrentScreen(Screens.FullArticleScreen(sApp.vm.headLines.getCurrentChapter()))
                    if(sApp.currentNewsPaper.kind==KindOfNews.SONGS)
                        sApp.setCurrentScreen(Screens.SongScreen(sApp.vm.headLines.getCurrentChapter()))
                }
                else {
                    sApp.setCurrentScreen(Screens.NewsPapersScreen)
                }
            }
            //Timber.d("${sApp.status()}")
            sApp.vm.msg.cls() //+-
            Timber.d("    end Set Up<---".padStart(65,' '))
        }
    }

     suspend fun NewInstalation():Boolean{
        var OK=true
         val scope=CoroutineScope(IO +CoroutineName("NEWINSTALLATION"))
         val j=scope.launch(eHandler) {
             Timber.d("BEGIN NEW INSTALLATION")
             sApp.setMsg2("NEW INSTALLATION")
             delay(1000)
             sApp.setMsg2("SETING USER")
             delay(1000)
             KCache.setUp()
             sApp.setMsg2("SETING DIRECTORIES")
             delay(1000)
             sApp.setMsg2("GETING NEWS PAPERS")
             sApp.vm.newsPapers.checkUpdates(sApp)
             Timber.d("END NEW INSTALATION")
             sApp.setMsg2("END INSTALLATION")
             sendmail("New Instalation ${sApp.userid}")
         }
         j.invokeOnCompletion { throwable->
             if(throwable !=null){
                 OK=false
                 Timber.e("ERROR: ${throwable.message}")
                 sApp.setMsg2("SORRY CAN'T INSTALL PPLICATION RETRY LATER")
                 sApp.shallIquit=true
             }
         }
         j.join()

         return OK
     }

     val eHandler= CoroutineExceptionHandler{_,exception->
         Timber.e("eHandler1  $exception")
     }

suspend fun checkServerUpdates() {
    //return
    Timber.d("CHECK FOR SERVER UPDATES current version=${sApp.vm.newsPapers.Npversion}")
    sApp.vm.viewModelScope.launch(IO + CoroutineName("POSTUPDATE")) {
        sApp.vm.newsPapers.checkUpdates(sApp, sApp.vm.newsPapers.Npversion)
    }
}

     suspend fun LoadInstalation():Boolean{
         //delay(3000)
         Timber.e("BEGIN LOAD INSTALLATION !!!!!")
         var OK=true
         sApp.setMsg2("LOAD INSTALLATION")

         val p= executeListOfAsyncFuncs2<Unit>(listOf(toFN2<Unit>(sApp.vm.toneAndPitchMap::loadToneandPitch2 ),toFN2<Unit>(App.sApp.vm.newsPapers::getNewsPapers2)))
         if(p.res.isSuccess) Timber.d("xEND ${p.logInfo()}")
         if(p.res.isFailure) {
             Timber.e("END ${p.logInfo()}")
             OK=false
         }
         if(OK){
             checkServerUpdates()
         }else{
             sApp.setMsg2("CAN'T START APP")
             sApp.setMsg2("ERROR LOADING APP!!!")
             Timber.e("ERROR LOADING APP !!")
         }
         Timber.e("END LOAD INSTALLATION !!!!!!!!! ${sApp.status()}")
         sApp.setMsg2("END LOAD INSTALATION")
         //delay(1000)
         return OK
     }
}




 @OptIn(ExperimentalTime::class)
 fun createsApp():StatusApp{
     val ss:StatusApp
     val t= measureTimeMillis {
         runBlocking(IO) {
              Timber.d("init sApp")
             val prefs:KNewsPrefs //=KNewsPrefs()
             val tm= measureTimedValue {
                 prefs = PreferencesNEW.newPrefsFlow.first()
             }
             Timber.d("got prefs in (${tm.duration.inWholeMilliseconds}) ms prefs=$prefs" )
             //delay(10)
             ss = StatusApp(
                 Screens.SetUpScreen,
                 Screens.QuitScreen,
                    AppStatus.Loading,
                 prefs //PreferencesNEW.newPrefsFlow.first()
             )//getCurrentPrefsNews())
         }
     }
     Timber.d("sApp created in ($t) ms")
     return ss
 }

 fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager != null) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Timber.i("Internet ${NetworkCapabilities.TRANSPORT_CELLULAR}")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Timber.i("Internet ${NetworkCapabilities.TRANSPORT_WIFI}")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Timber.i("Internet ${NetworkCapabilities.TRANSPORT_ETHERNET}")
                return true
            }
        }
    }
    return false
}

 class DelegateMutables2< T : Any>(t: T) : ReadWriteProperty<Any?,T> {
     var  s:MutableState<T> = mutableStateOf(t)
     override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        // Timber.e("GET VALUE from ${property.name} : ${s.value}")
         return s.value
     }
     override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        // Timber.e("SET NEW VALUE =  $value current value=${s.value}")
         if(s.value==value){
             // Timber.e("SKIP VALUE!!! $value")
             return
         }
         s.value=value
         PreferencesNEW.updatePrefKey<T>(property.name,value)

         //Timber.d("HOSTI TU TIU !!!!!!!!!!!!!!")
     }
 }

 class KNewsPrefs(val selectedNews:Int=-1,
                  val userid:String="",
                  val fontsize:Int=20,
                  val lang:String="",
                  val ktheme:Int=2,
                  val preftab:Int=0,
                  val selectedLangs:String=Locale.getDefault().language,
                  val romanize:Int=2) {
     override fun toString(): String {
         return  "selectedNews $selectedNews ,fontSize $fontsize ,lang $lang, ktheme $ktheme, preftab $preftab, selectedLangs $selectedLangs, romanize $romanize, userid $userid"
     }
 }

 class PreferencesNEW(){
    private val Context.dataStore:DataStore<Preferences> by preferencesDataStore(
        name = "KNewsPrefsFile"
    )
    object PrefKeys{
        val SELECTEDNEWS:Preferences.Key<Int>  = intPreferencesKey(::SELECTEDNEWS.name)
        val USERID: Preferences.Key<String> = stringPreferencesKey(::USERID.name)
        val FONTSIZE: Preferences.Key<Int> = intPreferencesKey(::FONTSIZE.name)
        val LANG: Preferences.Key<String> = stringPreferencesKey(::LANG.name)
        val KTHEME:Preferences.Key<Int> = intPreferencesKey(::KTHEME.name)
        val PREFTAB:Preferences.Key<Int> = intPreferencesKey(::PREFTAB.name)
        val SELECTEDLANGS: Preferences.Key<String> = stringPreferencesKey(::SELECTEDLANGS.name)
        val ROMANIZE:Preferences.Key<Int> = intPreferencesKey(::ROMANIZE.name)
    }
    companion object TT{
        val newPrefsFlow: Flow<KNewsPrefs> =    App.lcontext.dataStore.data
            .map { preferences ->
                val selectedNews=preferences[PrefKeys.SELECTEDNEWS]?:-1
                val userid=preferences[PrefKeys.USERID]?:""
                val fontsize=preferences[PrefKeys.FONTSIZE]?: 20
                val lang=preferences[PrefKeys.LANG]?: Locale.getDefault().language
                val ktheme=preferences[PrefKeys.KTHEME]?: 2
                val preftab=preferences[PrefKeys.PREFTAB]?: 0
                val selectedLangs=preferences[PrefKeys.SELECTEDLANGS]?: Locale.getDefault().language
                val romanize=preferences[PrefKeys.ROMANIZE]?: 2
                val np=KNewsPrefs(selectedNews,userid,fontsize,lang,ktheme, preftab, selectedLangs,romanize)
                np
            }

        val n= mapOf<String,Preferences.Key<*>>(
             KNewsPrefs::selectedNews.name to PrefKeys.SELECTEDNEWS,
             KNewsPrefs::userid.name to PrefKeys.USERID,
             KNewsPrefs::fontsize.name to PrefKeys.FONTSIZE,
             KNewsPrefs::lang.name to PrefKeys.LANG,
             KNewsPrefs::ktheme.name to PrefKeys.KTHEME,
             KNewsPrefs::preftab.name to PrefKeys.PREFTAB,
             KNewsPrefs::selectedLangs.name to PrefKeys.SELECTEDLANGS,
             KNewsPrefs::romanize.name to PrefKeys.ROMANIZE
            )

        fun<T>  stringToPrefKey(c: String):Preferences.Key<T>{
             val pk=n[c]
             if(pk==null) {
                 Timber.e("$c Key not found!!")
                 throw Exception("$c not Found Add Key in KNewsPrefs !!")
             }
            // Timber.d("key=${pk.name}")
             return pk as Preferences.Key<T>
        }

        suspend fun getNPrefs():KNewsPrefs{
            return newPrefsFlow.first()
        }

        fun<T> updatePrefKey(nameKey:String , t: Any, msg:String=""){
            val c = stringToPrefKey<T>(nameKey)
            sApp.vm.viewModelScope.launch(IO +CoroutineName("datastore")) {
                Timber.d("update key [$c]  with [$t] msg=$msg")
                App.lcontext.dataStore.edit { preferences ->
                    preferences[c] = t as T
                }
            }
        }
    }
}

//Max 321,353,382,399,427,438,443,470, 549, 563, 313, 515,562, 366,387,403,420,535,540,367,351,376