package com.begemot.myapplicationz

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.ui.text.Locale
import timber.log.Timber
import java.util.UUID

class App:Application(){
    companion object {
        lateinit var instance:App
    }

    override fun onCreate() {
        super.onCreate()
        instance=this
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
        //checkWifi(App.instance)
        if(prefs.userId.equals("")){
            prefs.userId=UUID.randomUUID().toString()
        }
        val currentAppLocale = Locale.current.language
        val cl=java.util.Locale(currentAppLocale).displayLanguage
        Timber.d("app locale: $cl")
    }
}
val prefs: Preferences by lazy {
    Preferences(App.instance)

}


//pse pse
fun checkWifi(context: Context){
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
    if(isConnected) println("WIFI conected ")
    else println(" NO WIFI")
}

class Preferences(context:Context){
    companion object{
        private const val PREFS_FILENAME ="RTPrefs"
        private const val FONT_SIZE ="fontsize"
        private const val LANG ="language"
        private const val USERID="userid"
    }
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    var fontSize:Int
    get()=sharedPrefs.getInt(FONT_SIZE,20)
    set(value)=sharedPrefs.edit().putInt(FONT_SIZE,value).apply()

    var kLang:String
    get()=sharedPrefs.getString(LANG,Locale.current.language)
    set(value) = sharedPrefs.edit().putString(LANG,value).apply()

    var userId:String
    get()=sharedPrefs.getString(USERID,"")
    set(value) = sharedPrefs.edit().putString(USERID,value).apply()


}