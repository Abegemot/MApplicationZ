 package com.begemot.myapplicationz

//import androidx.ui.text.Locale
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.util.Log.DEBUG
import androidx.lifecycle.viewModelScope
//import androidx.ui.intl.Locale
import com.begemot.myapplicationz.App.Companion.sApp
//import io.ktor.util.*
//import io.ktor.util.*
import kotlinx.coroutines.*

import timber.log.Timber
import java.util.*
//import kotlin.io.path.ExperimentalPathApi
//import mu.KotlinLogging

 //private val logger = KotlinLogging.logger {}

 class LineNumberDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        val head="*NKZ [${Thread.currentThread().name}](${element.fileName}:${element.lineNumber})->".padEnd(28,' ')
        val tail="${element.className}.${element.methodName}".substring(27)
        return "${head} $tail"
    }
}

 class App:Application(){
    companion object {
        val sApp by lazy { StatusApp(Screens.SetUpScreen,Screens.QuitScreen) }
        lateinit var instance:App
        val lcontext by lazy { App.instance }
    }

  //  @ExperimentalPathApi
    override fun onCreate() {
        super.onCreate()
        instance=this
        if(BuildConfig.DEBUG){

            Timber.plant(LineNumberDebugTree())
            //Timber.plant(Timber.DebugTree())
        }
        //logger.debug { "JO PRIMER" }
        Timber.d("begin  ${sApp.status()}")
        //checkWifi(App.instance)
        setUP()
        Timber.d("end  ${sApp.status()}")
        //sApp.setMsg2("App end OnCreate")

    }


    //@ExperimentalPathApi
    fun setUP(){
        //val scope=sApp.vm.viewModelScope+Dispatchers.IO
        val scope= CoroutineScope(Dispatchers.IO)
        sApp.setMsg2("SETUP")

        scope.launch(Dispatchers.IO) {
            sApp.currentStatus.value = AppStatus.Loading
            delay(1000)
      //      KCache.deleteFiles()
            Timber.d("setUP coroutine begin")

            val newInstall = if(prefs.userId.equals("")){
                Timber.d("User ID not set")
                prefs.userId=UUID.randomUUID().toString()
                true
            }else  false
            //newInstall=true

            if(newInstall) { sApp.setMsg2("SETING USER"); delay(1000)}
            KCache.setUp()
            if(newInstall) { sApp.setMsg2("SETING DIRECTORIES");delay(1000)}

            /*val lf=KCache.listAllFiles()
            lf.forEach{
                Timber.d(it)
            }*/
            if(newInstall) { sApp.setMsg2("SETING TONE AND PITCH"); delay(1000)}
            sApp.vm.toneAndPitchMap.load()


            sApp.vm.newsPapers.getLocalNewsPapers(sApp)

            //sApp.setMsg2("newsPapers = ${sApp.vm.newsPapers.toString().substring(0,10)}...")

            if(sApp.vm.newsPapers.Npversion==0){
                if(newInstall) { sApp.setMsg2("FETCHING NEWSPAPERS LIST")}
                sApp.vm.newsPapers.checkUpdates(sApp)
                if(newInstall) { sApp.setMsg2("NEWS PAPERS LOADED");delay(1000) }

            }else{
                sApp.currentScreen.value=Screens.NewsPapersScreen
                scope.launch {
                    sApp.setMsg2("CHEKING NEWS PAPER UPDATES = ${sApp.vm.newsPapers.toString().substring(0,10)}...")
                    sApp.vm.newsPapers.checkUpdates(sApp)
                }
            }
            Timber.d("end setUP coroutine)")
            // delay(4000)
            //sApp.setMsg2("LEAVING SET UP coroutines")

            if(newInstall) { sApp.setMsg2("INSTALATION OK");delay(1000)}
            sApp.vm.msg.cls()
            sApp.currentStatus.value = AppStatus.Idle
            sApp.currentScreen.value = Screens.NewsPapersScreen
            if(newInstall) sendmail("New Instalation",false)
            Timber.d("END SETUP")

        }
        //sApp.setMsg2("LEAVING SETUP")
    }

}
val prefs: Preferences by lazy {
      Preferences(App.lcontext)

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


class Preferences(context:Context){
    companion object{
        private const val PREFS_FILENAME = "RTPrefs"
        private const val FONT_SIZE = "fontsize"
        private const val LANG = "language"
        private const val USERID = "userid"
        private const val SELECTEDLANG = "selectedlang"
        private const val KTHEME = "ktheme"
        private const val PREFTAB = "preftab"
        private const val ROMANIZE = "romanize"
    }
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    var fontSize:Int
    get()=sharedPrefs.getInt(FONT_SIZE,20)
    set(value)=sharedPrefs.edit().putInt(FONT_SIZE,value).apply()

    var ktheme:Int
        get()=sharedPrefs.getInt(KTHEME,2)
        set(value)=sharedPrefs.edit().putInt(KTHEME,value).apply()

    var preftab:Int
        get()=sharedPrefs.getInt(PREFTAB,0)
        set(value)=sharedPrefs.edit().putInt(PREFTAB,value).apply()

    var kLang:String
    get()=""+sharedPrefs.getString(LANG, Locale.getDefault().language)
    set(value) = sharedPrefs.edit().putString(LANG,value).apply()

    var userId:String
    get()=""+sharedPrefs.getString(USERID,"")
    set(value) = sharedPrefs.edit().putString(USERID,value).apply()

    var selectedLang:String
    get()=""+sharedPrefs.getString(SELECTEDLANG,Locale.getDefault().language)
    set(value)=sharedPrefs.edit().putString(SELECTEDLANG,value).apply()

    var romanize:Int
        get() = sharedPrefs.getInt(ROMANIZE,2)
        set(value) = sharedPrefs.edit().putInt(ROMANIZE,value).apply()
}


 suspend fun startUp(){
     if(isInstalled()){
          Timber.d("already INSTALLED")
         sApp.vm.msg.setMsg2("IS INSTALLED")
         val scope=sApp.vm.viewModelScope+Dispatchers.IO
           scope.launch {
               delay(2000)
               sApp.vm.newsPapers.checkUpdates(sApp) //sApp.vm.msg.setMsg(sApp,"No News Papers Updates")
               //sApp.vm.msg.setMsg(sApp,"News Papers Updates")

               //Timber.d("END CHECK UPDATES")
           }
           //Timber.d("END INSTALLED")
     }
     else{
        //Timber.d("NOT INSTALLED")
         sApp.vm.msg.setMsg2("NOT INSTALLED")
         Timber.d("new instalation")
         KCache.setUp()
         sApp.vm.msg.setMsg2("AFTER CACHE SET UP")
         runBlocking {
             sApp.vm.newsPapers.checkUpdates(sApp)
             sApp.setMsg2("AFTER CHECK UPDATES NP->${sApp.vm.newsPapers.lNewsPapers.size}")
         }
         sApp.currentStatus.value = AppStatus.Idle
         sApp.currentScreen.value = Screens.NewsPapersScreen


     }


 }


 suspend fun isInstalled():Boolean{
     //KCache.deleteFiles()
     //KCache.setUp()
     if(KCache.fileExistsAndNotEmpty("knews.json","")){
         try{
             sApp.vm.newsPapers.getLocalNewsPapers(sApp)
         }catch (e:Exception){
             return false
         }
         return true
     }
     return false
 }

