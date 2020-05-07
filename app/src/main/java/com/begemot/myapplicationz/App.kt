package com.begemot.myapplicationz

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class App:Application(){
    companion object {
        lateinit var instance:App
    }

    override fun onCreate() {
        super.onCreate()
        instance=this
    }
}
val prefs: Preferences by lazy {
    Preferences(App.instance)
}

class Preferences(context:Context){
    companion object{
        private const val PREFS_FILENAME ="RTPrefs"
        private const val FONT_SIZE ="fontsize"
        private const val LANG ="language"
    }
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    var fontSize:Int
    get()=sharedPrefs.getInt(FONT_SIZE,20)
    set(value)=sharedPrefs.edit().putInt(FONT_SIZE,value).apply()

    var kLang:String
    get()=sharedPrefs.getString(LANG,"es")
    set(value) = sharedPrefs.edit().putString(LANG,value).apply()

}