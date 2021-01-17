package com.begemot.myapplicationz

//import com.google.gson.Gson
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.begemot.knewscommon.Found
import com.begemot.knewscommon.OriginalTransLink
import com.begemot.knewscommon.THeadLines
import com.begemot.knewscommon.fromStrToTHeadLines
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat

private val DD=false


object KCache{
    fun findInCache2(path:String): String{
        //throw Exception("ñññññfindincache2")
        val ctx=App.lcontext
        val  sf="${ctx.filesDir.absolutePath}$path"
//        Timber.d("findincache path  :$path")
        Timber.d("findincache sf    :$sf")
        val file=File(sf)
        var txt=""
        var thd:THeadLines
        try {
            var fis:FileInputStream  =  FileInputStream (File(sf))
            fis.bufferedReader(charset = Charset.defaultCharset()).use {
                txt=it.readText()
            }
            fis.close()
            if(DD) Timber.d("readed txt $txt")
            if(DD) Timber.d("END  read txt   <------")
        } catch (e: Exception) {
            Timber.d("exception $e")
            txt=""
        }
        //ctx.openFileInput(path).use{
        //    txt=it.bufferedReader().use{
        //        it.readText()
        //    }
            //thd= fromStrToTHeadLines(txt)
        //}

        return txt
    }
    fun storeInCache3(sNameFile: String, scontent: String){
        val ctx=App.lcontext
        val  sf="${ctx.filesDir.absolutePath}$sNameFile"
        Timber.d("STORE IN CACHE $scontent")
        Timber.d(sf)

        val fos:FileOutputStream= FileOutputStream(File(sf),false)
        fos.write(scontent.toByteArray())
        fos.close()
        //fos.bufferedWriter().use{
        //    it.write(scontent)
        //}

        //ctx.openFileOutput(sNameFile, Context.MODE_PRIVATE).use{
        //    it.write(scontent.toByteArray())
        //}
        //File(sNameFile).writeText(jasonData)

    }




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


    fun storeImageInCache(sNameFile: String, zimage: ByteArray){
        val ctx=App.lcontext
        Timber.d("STORE IMAGE IN CACHE $sNameFile   with size ${zimage.size}")
        val  sf=ctx.filesDir.absolutePath + "/Images/$sNameFile"
        //ctx.openFileOutput(sNameFile,Context.MODE_PRIVATE).use{
        //    it.write(zimage)
        //}
        val file=File(sf)
        file.writeBytes(zimage)
        Timber.d("end write $sf size ${zimage.size}")
    }

    fun setUp(){
        if(checkImagesDir()) return
        makeDir("Images")
        makeDir("Headlines")
        makeDir("Articles")
    }


    fun makeDir(nameDir:String){
        val ctx=App.lcontext
        val  sf=ctx.filesDir.absolutePath + "/$nameDir"
        val file=File(sf)
        if(file.mkdir()){
            Timber.d("directory Images created")
        }else{
            Timber.d("directory Images not created")
        }
    }
    fun checkImagesDir():Boolean{
        val  sf=App.lcontext.filesDir.absolutePath + "/Images"
        val file=File(sf)
        return file.exists()
    }

    fun findImageInCache(ctx: Context, sNameFile: String):ByteArray{
       // Timber.d(sNameFile)
       // makeImagesDir(ctx)
        lateinit var zimage:ByteArray
        try {
            val  sf=ctx.filesDir.absolutePath + "/Images/${sNameFile}"

            val file=File(sf)
         //   Timber.d("$sf ${file.length()}  ${file.exists()}")
           // zimage=file.readBytes()
            val fis = FileInputStream(file)
            zimage=fis.readBytes()
            fis.close()

            //ctx.openFileInput("/Images/$sNameFile").use{
            //    zimage=it.readBytes()
            //}
           // Timber.d("read image size for $sf is ${zimage.size}")

        } catch (e: Exception) {
            Timber.d("cache exception finding $sNameFile $e")
            return  ByteArray(0)
        }
        return  zimage
    }

    fun getBitmapImage(sNameImg: String): Bitmap {
        val l=findImageInCache(App.lcontext, sNameImg)
        Timber.d("$sNameImg    size ${l.size}")
        val s= BitmapFactory.decodeByteArray(l, 0, l.size)
        return s
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

    fun fileExists(ctx: Context, sNameFile: String, sDirectory: String):Boolean{
        val  sf=ctx.filesDir.absolutePath +"$sDirectory/$sNameFile"
        return File(sf).exists()
    }

    fun fileExists(sNameFile: String, sDirectory: String):Boolean{
        val ctx=App.lcontext
        val  sf=ctx.filesDir.absolutePath +"$sDirectory/$sNameFile"
        return File(sf).exists()
    }

    fun storeInCache2(sNameFile: String, scontent: String){
        val ctx=App.lcontext
        Timber.d("STORE IN CACHE $scontent")
        ctx.openFileOutput(sNameFile, Context.MODE_PRIVATE).use{
            it.write(scontent.toByteArray())
        }
        //File(sNameFile).writeText(jasonData)

    }

    fun loadFromCache(sNameFile: String):String{
        val ctx=App.lcontext
        var txt:String=""
        ctx.openFileInput(sNameFile).use{

            // if(it.fd.valid()) Timber.d("valid  $sNameFile ${it.fd.toString()} ")
            // else Timber.d("No Valid $sNameFile ")
            txt=it.bufferedReader().use{
                it.readText()
            }
        }
        Timber.d("File $sNameFile\n$txt")
        return txt
    }

    fun listFiles():List<String>{
        val lf= mutableListOf<String>()
        listRecursive(File(App.lcontext.filesDir.absolutePath), lf)
        lf.forEach {
            Timber.d(it)
        }
        return lf
    }
    fun removeHeadLinesOf(handler:String){
        val F=File(App.lcontext.filesDir.absolutePath+"/Headlines")
        F.listFiles().filter { it.name.substring(0,handler.length).equals(handler) }
            .forEach{
                Timber.d(it.name)
                it.delete()
            }
    }



    fun listRecursive(fileOrDirectory: File, lf: MutableList<String>) {

        val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) listRecursive(
            child,
            lf
        )
        var d=""
        if(fileOrDirectory.isDirectory) d="(D)"
        lf.add(
            "$d ${fileOrDirectory.name} size ${fileOrDirectory.length()} ${
                sdf.format(
                    fileOrDirectory.lastModified()
                )
            }"
        )
    }


    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
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