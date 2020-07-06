package com.begemot.myapplicationz

import android.content.Context
import com.google.gson.Gson
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import kotlin.reflect.KSuspendFunction0

object KProvider{
    suspend fun getHL(
        nameFile:String,olang:String,tlang:String, getOriginalHDLines: KSuspendFunction0<List<KArticle>>
    ):List<OriginalTransLink>{
        val olt=KCache.findInCache(App.lcontext, nameFile)
        if(olt.isNotEmpty()) return olt
        val nolt= getOriginalHDLines()
        Timber.d("nolt  ${nolt.size}")


        val lt=getTranslatedHeadlines(nolt,olang,tlang)
        KCache.storeInCache(App.lcontext, nameFile,lt)
        return lt
    }
}

object KCache{
    fun findInCache(ctx: Context,sNameFile:String):List<OriginalTransLink>{
       // val otl= mutableListOf<OriginalTransLink>()
      // return otl
        var txt: String = ""
        try {
            txt = ""
            val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS")
            val currentTime=System.currentTimeMillis()
            var ds=sdf.format(currentTime)
            Timber.d("current date time $ds")
            val  sf=ctx.filesDir.absolutePath + "/${sNameFile}"
            val file=File(sf)
            val fileTime=file.lastModified()
            var ds2=sdf.format(fileTime)
            Timber.d("file date time $sf $ds2")
            val elapsedminutes=(currentTime-fileTime)/60000
            Timber.d("elapsed minutes :$elapsedminutes")
            if(elapsedminutes>60) return emptyList()

            ctx.openFileInput(sNameFile).use{

                txt=it.bufferedReader().use{
                    it.readText()
                }
            }
        } catch (e: Exception) {
            Timber.d("cache exception $e")
            return emptyList()
        }
        Timber.d("TXT FOUND IN CACHE $txt")

        val obj = Gson().fromJson(txt, ListOriginalTransList::class.java)
        //val json=Json(JsonConfiguration.Stable)
        //val obj=json.parse(OriginalTransLink.serializer().list,txt)
        Timber.d(" FIND IN CACHE START")
        Timber.d(obj.toString())
        Timber.d(" FIND IN CACHE END")
        return obj.lOT
    }


    fun storeInCache(ctx:Context,sNameFile:String, lOtl:List<OriginalTransLink>){

        Timber.d("STORE IN CACHE")
        //val json=Json(JsonConfiguration.Stable)
        //val jasonData=json.stringify(OriginalTransLink.serializer().list,lOtl)
        var gson=Gson()
        var rq=Gson().toJson(ListOriginalTransList(lOtl) )
        ctx.openFileOutput(sNameFile,Context.MODE_PRIVATE).use{
            it.write(rq.toByteArray())
        }
        //File(sNameFile).writeText(jasonData)

    }
}