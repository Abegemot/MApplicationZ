package com.begemot.myapplicationz

import android.content.Context
import com.begemot.knewscommon.KArticle
import com.begemot.knewscommon.OriginalTransLink
//import com.google.gson.Gson
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import kotlin.reflect.KSuspendFunction0



object KCache{
    fun findInCache(ctx: Context, sNameFile: String):List<OriginalTransLink>{
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

        //val obj = Gson().fromJson(txt, ListOriginalTransList::class.java)
        //val json=Json(JsonConfiguration.Stable)
        //val obj=json.parse(OriginalTransLink.serializer().list,txt)
        Timber.d(" FIND IN CACHE START")
        //Timber.d(obj.toString())
        Timber.d(" FIND IN CACHE END")
        //return obj.lOT
        return emptyList()
    }


    fun storeImageInCache(ctx: Context, sNameFile: String, zimage: ByteArray){
        Timber.d("STORE IMAGE IN CACHE")
        val  sf=ctx.filesDir.absolutePath + "/Images/$sNameFile"
        //ctx.openFileOutput(sNameFile,Context.MODE_PRIVATE).use{
        //    it.write(zimage)
        //}
        val file=File(sf)
        file.writeBytes(zimage)
        Timber.d("end write $sf size ${zimage.size}")
    }
    fun makeImagesDir(ctx: Context){
        val  sf=ctx.filesDir.absolutePath + "/Images"
        val file=File(sf)
        if(file.mkdir()){
            Timber.d("directory Images created")
        }else{
            Timber.d("directory Images not created")
        }
    }

    fun findImageInCache(ctx: Context, sNameFile: String):ByteArray{
        makeImagesDir(ctx)
        lateinit var zimage:ByteArray
        try {
            val  sf=ctx.filesDir.absolutePath + "/Images/${sNameFile}"

            val file=File(sf)
            zimage=file.readBytes()
            //ctx.openFileInput(sNameFile).use{
             //   zimage=it.readBytes()
            //}
        } catch (e: Exception) {
            Timber.d("cache exception $e")
            return  ByteArray(0)
        }
        return  zimage
    }

    fun storeInCache(ctx: Context, sNameFile: String, lOtl: List<OriginalTransLink>){

        Timber.d("STORE IN CACHE")
        //val json=Json(JsonConfiguration.Stable)
        //val jasonData=json.stringify(OriginalTransLink.serializer().list,lOtl)
       // var gson=Gson()
       // var rq=Gson().toJson(ListOriginalTransList(lOtl))
        //ctx.openFileOutput(sNameFile, Context.MODE_PRIVATE).use{
        //    it.write(rq.toByteArray())
       // }
        //File(sNameFile).writeText(jasonData)

    }

    fun fileExists(ctx: Context, sNameFile: String,sDirectory:String):Boolean{
        val  sf=ctx.filesDir.absolutePath +"$sDirectory/$sNameFile"
        return File(sf).exists()
    }

    fun storeInCache2(ctx: Context, sNameFile: String,scontent:String){
        Timber.d("STORE IN CACHE $scontent")
        ctx.openFileOutput(sNameFile, Context.MODE_PRIVATE).use{
            it.write(scontent.toByteArray())
        }
        //File(sNameFile).writeText(jasonData)

    }

    fun listFiles(){
        /*App.lcontext.fileList().forEach {

            val  sf=App.lcontext.filesDir.absolutePath + "/${it}"
            val file=File(sf)
            val size=file.length() ///1024
            val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS")
            var ds=sdf.format(file.lastModified())
            Timber.d("Name $it   Data  $ds  Size $size (B)")

        }*/
        val lf= mutableListOf<String>()
        listRecursive(File(App.lcontext.filesDir.absolutePath),lf)
        lf.forEach {
            Timber.d(it)
        }

    }
    fun listRecursive(fileOrDirectory: File,lf:MutableList<String>) {
        val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS")
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) listRecursive(child,lf)
        lf.add("name:${fileOrDirectory.name} size ${fileOrDirectory.length()} last changed ${sdf.format(fileOrDirectory.lastModified())}")
    }


    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(child)
        fileOrDirectory.delete()
    }


    fun deleteFiles(){
        Timber.d("delete recursive all")
        deleteRecursive(File(App.lcontext.filesDir.absolutePath))
       /* App.lcontext.fileList().forEach {
            val  sf=App.lcontext.filesDir.absolutePath + "/${it}"
            //Timber.d("Deleting .... $sf")
            val file=File(sf)
            if(!file.isDirectory){
                Timber.d("Deleting .... $sf")
                App.lcontext.deleteFile(sf)
            }
        }*/

    }

}