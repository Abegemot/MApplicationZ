 package com.begemot.myapplicationz

//import androidx.ui.text.Locale
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.viewModelScope
//import androidx.ui.intl.Locale
import com.begemot.myapplicationz.App.Companion.sApp
import kotlinx.coroutines.*

import timber.log.Timber
import java.util.*

class LineNumberDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        return "NKZ (${element.fileName}:${element.lineNumber})#${element.methodName}"
    }
}
class App:Application(){
    companion object {
        lateinit var instance:App
        lateinit var lcontext:Context
        val sApp by lazy { StatusApp(Screens.NewsPapersScreen,Screens.QuitScreen) }
    }

    override fun onCreate() {
        super.onCreate()
        instance=this
        //SystemClock.sleep(4000)
        if(BuildConfig.DEBUG){
            Timber.plant(LineNumberDebugTree())
        }
        Timber.d("appstart  ${sApp.status()}")
        lcontext=App.instance
        //checkWifi(App.instance)
        if(prefs.userId.equals("")){
            Timber.d("User ID not set")
            prefs.userId=UUID.randomUUID().toString()
        }



        val currentAppLocale =    java.util.Locale.getDefault().language
        val cl=java.util.Locale(currentAppLocale).displayLanguage
  //      Timber.d("User ID = ${prefs.userId}  app locale: $cl")
        startUp()
        Timber.d("end  ${sApp.status()}")

    }
}
val prefs: Preferences by lazy {
      Preferences(App.instance)

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
        private const val PITCH = "pitch"
        private const val SPEECHRATE = "speechrate"
        private const val ROMANIZE = "romanize"
    }
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    var fontSize:Int
    get()=sharedPrefs.getInt(FONT_SIZE,20)
    set(value)=sharedPrefs.edit().putInt(FONT_SIZE,value).apply()

    var pitch:Float
        get()= sharedPrefs.getFloat(PITCH,1.0f)
        set(value)=sharedPrefs.edit().putFloat(PITCH,value).apply()

    var speechrate:Float
        get()= sharedPrefs.getFloat(SPEECHRATE,1.0f)
        set(value)=sharedPrefs.edit().putFloat(SPEECHRATE,value).apply()

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

 fun startUp(){
     if(isInstalled()){
          //Timber.d("INSTALLED")
           val scope=sApp.vm.viewModelScope+Dispatchers.IO
           scope.launch {
               sApp.vm.checkNPUpdates(sApp) //sApp.vm.msg.setMsg(sApp,"No News Papers Updates")
               //sApp.vm.msg.setMsg(sApp,"News Papers Updates")

               //Timber.d("END CHECK UPDATES")
           }
           //Timber.d("END INSTALLED")
     }
     else{
        //Timber.d("NOT INSTALLED")
         Timber.d("new instalation")
         KCache.setUp()
         runBlocking {
             sApp.vm.checkNPUpdates(sApp)
         }
         sApp.currentStatus.value = AppStatus.Idle
         sApp.currentScreen.value = Screens.NewsPapersScreen


     }


 }


 fun isInstalled():Boolean{
     //KCache.deleteFiles()
     //KCache.setUp()
     if(KCache.fileExists("knews.json","")){
         try{
             sApp.vm.getNewsPapers(sApp)
         }catch (e:Exception){
             return false
         }
         return true
     }
     return false
 }

