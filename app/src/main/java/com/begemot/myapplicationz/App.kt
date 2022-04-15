 package com.begemot.myapplicationz

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.MutableState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.begemot.knewscommon.KResult3
import com.begemot.knewscommon.KTimer
import com.begemot.myapplicationz.App.Companion.sApp
import com.begemot.myapplicationz.PreferencesNEW.TT.ccc2

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

import timber.log.Timber
import java.lang.Exception
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue


class LineNumberDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        val s="NKZ [${Thread.currentThread().name}] (${element.fileName}:${element.lineNumber}) ${element.methodName}"
        val el=element.methodName
        val len=20
        var smethod =""
        if(el.length>len) {
            smethod="Method Name too long > $len"
        }else
        smethod=el.padStart(20,'-')
        val st =if(Thread.currentThread().name[0]=='D') "DD${Thread.currentThread().name.substringAfter("DefaultDispatcher")}" else "${Thread.currentThread().name}"
        val bs="NKZ [$st] (${element.fileName}:${element.lineNumber})".padEnd(54,'-')
        return "$bs$smethod"
       // return "NKZ [$st] (${element.fileName}:${element.lineNumber})".padEnd(84,'-')
       // return "NKZ [${Thread.currentThread().name}] (${element.fileName}:${element.lineNumber})".padEnd(84,'-')
    }
}

 class App:Application(){

      companion object{
         lateinit var lcontext: Context

        @OptIn(InternalCoroutinesApi::class)
        val sApp:StatusApp by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Timber.e("creating sApp by lazy")
            runBlocking(IO) {
                Timber.d("init sApp")
                //delay(10)
                StatusApp(
                    Screens.SetUpScreen,
                    Screens.QuitScreen,
                    AppStatus.Loading,
                    PreferencesNEW.newPrefsFlow.first()
                )//getCurrentPrefsNews())
            }
        }



        val fPath:String by lazy { "${lcontext.filesDir.absolutePath}/" }




      }



     override fun onCreate() {
        super.onCreate()
        lcontext = applicationContext

        if(BuildConfig.DEBUG){
            System.setProperty("kotlinx.coroutines.debug", "on" )
            Timber.plant(LineNumberDebugTree())
        }
        Timber.d("begin Create App ")// ${sApp.status()}")
        //checkWifi(App.instance)



//        Timber.d("fontsize =${App.sApp.fontSize}")
        KCache.fP= fPath  //"${lcontext.filesDir.absolutePath}/"
        //val t= measureTimeMillis {
        //TestC5()
        //--> val s=App.sApp.vm.newsPapers.ge
       // executeListOfAsyncFuncs(Dispatchers.IO, listOf({::sApp.get().vm.newsPapers.getNewsPapers().javaClass.name},{sApp.vm.toneAndPitchMap.load()}))
        Timber.d("  START")

         //val p=executeListOfAsyncFuncsZ(listOf(toFN(StatusApp.vm2.newsPapers::getNewsPapers),toFN(StatusApp.vm2.toneAndPitchMap::load)))
         val p=executeListOfAsyncFuncsZ( listOf(toFN(::A), toFN(::B), toFN(::C)))
         if(p is KResult3.Success) Timber.d("Succes!!")
         if(p is KResult3.VoidSucces) Timber.d("Void Succes !!!")
         else Timber.e("Error :${p.msg()}z")
         Timber.d("    END in ${p.msg()}z")
       // Timber.d(" END (${p.first})ms ${p.second}")
         //  execZ("GETNEWSPAPERS",scope){ sApp.vm.newsPapers.getNewsPapers()}
       //  execZ("GETTONEANDPITCH",scope){sApp.vm.toneAndPitchMap.load()}
            //TestC()
        //}
         //Timber.d("TestC run in ($t) ms")
         //val p=toFN(StatusApp.vm2.newsPapers::getNewsPapers)
         //val q=toFN(StatusApp.vm2.toneAndPitchMap::load)


        setUP()
        Timber.d("end  Create  App  ${sApp.currentNewPreferences.userid}")
     }

     suspend fun TonePitch(){
         sApp.vm.toneAndPitchMap.load()
     }



    //@ExperimentalPathApi
    fun setUP(){
        Timber.d(" --->begin setUP".padStart(65,' '))
        val scope= CoroutineScope(IO)
        sApp.setMsg2("SETUP")

        scope.launch(IO +CoroutineName("setup")) {
            sApp.currentStatus.value = AppStatus.Loading
            //      KCache.deleteFiles()
            Timber.d("setUP coroutine begin")

            val newInstall = if (sApp.currentNewPreferences.userid.isEmpty()) {
                Timber.d("User ID not set")
                sApp.currentNewPreferences.userid=(UUID.randomUUID().toString())
                //PreferencesNEW.updateUserid(UUID.randomUUID().toString())
                true
            } else false

            val res = if (newInstall) NewInstalation() else  UpdateInstalation()
            if(res){
                Timber.d("AFTER UPDATE INSTALATION PASSED")
                sApp.currentStatus.value = AppStatus.Idle
                if(sApp.currentNewPreferences.selectedNews>-1) {
                    sApp.currentNewsPaper = sApp.vm.newsPapers.lNewsPapers[sApp.currentNewPreferences.selectedNews]
                    sApp.vm.headLines.getLines(sApp, sApp.currentNewsPaper)
                    sApp.currentScreen.value = Screens.FullArticleScreen(sApp.vm.headLines.getCurrentChapter())
                }
                else {
                    Timber.d("setting current screen = news papers screen !!")
                    sApp.currentScreen.value = Screens.NewsPapersScreen
                }
            }
            Timber.d("${sApp.status()}")
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
             sendmail("New Instalation ${sApp.userID}")
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


     @OptIn(ExperimentalTime::class)
     suspend fun UpdateInstalation():Boolean{
         Timber.e("BEGIN UPDATE INSTALLATION !!!!!")
         val kt=KTimer()
         var OK=true
         sApp.setMsg2("UPDATE INSTALLATION")
         val t= measureTimeMillis {

             val scope = CoroutineScope(IO + CoroutineName("UINSTALL"))
             Timber.e("START GET NEWS PAPERS AND PITCH IN APP")
             val j = scope.launch(eHandler) {
                 execZ("GETNEWSPAPERS",scope){ sApp.vm.newsPapers.getNewsPapers()}
                 execZ("GETTONEANDPITCH",scope){sApp.vm.toneAndPitchMap.load()}
                 //execZ("TEST EXECZ",scope){delay(1000)}
                 //delay(2000)
             }
             j.invokeOnCompletion { throwable ->
                 if (throwable != null) {
                     OK = false
                     Timber.e("ERROR: ${throwable.message}")
                     sApp.setMsg2("SORRY CAN'T RUN THE APPLICATION RETRY LATER")
                     sApp.shallIquit = true
                 }
                 Timber.e("1 tone and pitch and News Papers LOADED duration: (?) ms")
             }
             Timber.d("JOIN 1")
             j.join()
             Timber.d("JOIN 2")
         }

         Timber.e("2 tone and pitch and News Papers LOADED duration: ($t) ms  (${kt.getElapsed()}) ms")
         //Timber.d("1 ${sApp.status()}")
         if(OK){
             //Timber.d("2 ${sApp.status()}")

             Timber.d("END UPDATE INSTALATION OK, CHECK FOR UPDATES current version=${sApp.vm.newsPapers.Npversion}")
             sApp.vm.viewModelScope.launch(IO +CoroutineName("POSTUPDATE")) {
                 sApp.vm.newsPapers.checkUpdates(sApp, sApp.vm.newsPapers.Npversion)
             }
         }else{
             Timber.e("ERROR LOADING APP !!")
         }

         Timber.e("END UPDATE INSTALLATION !!!!!!!!!")

         return OK
     }
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

 class DelegateMutables< T : Any>(t: MutableState<T>) : ReadWriteProperty<Any?,MutableState<T>> {
     var  s:MutableState<T> = t
     override fun getValue(thisRef: Any?, property: KProperty<*>): MutableState<T> {
         //Timber.e("GET VALUE")
         return s
     }
     override fun setValue(thisRef: Any?, property: KProperty<*>, value: MutableState<T>) {
         //Timber.e("SET VALUE  $value")
         if(s.value==value.value){
             // Timber.e("SKIP VALUE!!! $value")
             return
         }
         s.value=value.value
         PreferencesNEW.upk2(ccc2(property.name),value.value)
         //Timber.d("HOSTI TU TIU !!!!!!!!!!!!!!")
     }
 }

 class DelegatePrefsString<T : Any>(t:T) : ReadWriteProperty<Any?,T> {
      var  s:T=t
     override fun getValue(thisRef: Any?, property: KProperty<*>): T {
         //Timber.e("GET VALUE")
         return s
     }
     override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
         //Timber.e("SET VALUE  $value")
         if(s==value){
            // Timber.e("SKIP VALUE!!! $value")
             return
         }
         s=value
         PreferencesNEW.upk2(ccc2(property.name),value)
         //Timber.d("HOSTI TU TIU !!!!!!!!!!!!!!")
     }
 }



 class KNewsPrefs(selectedNews:Int=-1, userid:String="", fontsize:Int=20, lang:String="", ktheme:Int=2, preftab:Int=0, selectedLangs:String=Locale.getDefault().language,romanize:Int=2) {
     var selectedNews: Int by DelegatePrefsString(selectedNews)
     var userid: String by DelegatePrefsString(userid)
     var fontsize: Int by DelegatePrefsString<Int>(fontsize)
     var lang: String by DelegatePrefsString<String>(lang)
     var ktheme:Int by DelegatePrefsString(ktheme)
     var preftab:Int by DelegatePrefsString(preftab)
     var selectedLangs: String by DelegatePrefsString(selectedLangs)
     var romanize:Int by DelegatePrefsString(romanize)
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
                Timber.e("newprefsflow")
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
        fun  ccc2(c: String):Preferences.Key<*>{
             val pk=n[c]
             if(pk==null)throw Exception("$c not Found Need to add Key in KNewsPrefs !!")
             return pk
        }

        suspend fun getNPrefs():KNewsPrefs{
            return newPrefsFlow.first()
        }

        fun<T> upk2(c: Preferences.Key<T>, t: Any, msg:String=""){
            sApp.vm.viewModelScope.launch(IO +CoroutineName("datastore")) {
                Timber.d("update key [$c]  with [$t] msg=$msg")
                App.lcontext.dataStore.edit { preferences ->
                    preferences[c]=t as T // as String
                }
            }
        }
    }
}

//Max 321,353,382,399,427,438,443,470, 549, 563, 313, 515,562, 366,387