package com.begemot.myapplicationz

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.nio.file.*
import java.text.SimpleDateFormat
import java.util.stream.Collectors
//import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.fileSize
//import kotlin.io

private val DD=false

object KCache{
    private val hashmapImages: MutableMap<String,ImageBitmap> = HashMap()

    fun storeInCache(sNameFile: String, scontent: String){
        val  sf= App.lcontext.filesDir.absolutePath
        Timber.d("STORE IN CACHE $sf ${scontent.length}")

        try {
            Files.write(Paths.get(sf+"/$sNameFile"),scontent.toByteArray())
            Files.write(Paths.get(sf+"/ERROR.BGM"),"ok namefile:$sNameFile  size ${scontent.length}\n".toByteArray(),StandardOpenOption.APPEND,StandardOpenOption.CREATE,StandardOpenOption.WRITE)
        } catch (e: Exception) {
            Files.write(Paths.get(sf+"/ERROR.BGM"),"err ${e.message}\n".toByteArray(),StandardOpenOption.APPEND,StandardOpenOption.CREATE,StandardOpenOption.WRITE)

        }
    }

    fun storeImageInCache(sNameFile: String, zimage: ByteArray){
        Timber.d("STORE IMAGE IN CACHE $sNameFile   with size ${zimage.size}")
        val  sf=App.lcontext.filesDir.absolutePath + "/Images/$sNameFile"
        Files.write(Paths.get(sf),zimage)
        Timber.d("end write $sf size ${zimage.size}")
    }

    suspend fun loadFromCache(sNameFile: String):String= withContext(Dispatchers.IO){
        val  sf="${App.lcontext.filesDir.absolutePath}/$sNameFile"
        val str=
        try {
            String(Files.readAllBytes(Paths.get(sf)))
        } catch (e: Exception) {
            Timber.d("exception $e")
            ""//"error loadFromCache ${e.message}"
        }
        Timber.d("File raw $sf ->' ${str.substring(0,minOf(str.length,29))}....'")
        str
    }

   fun setUp(){
        if(checkDirExist("Images")) return
        makeDir("Images")
        makeDir("Headlines")
        makeDir("Articles")
    }

    fun makeDir(nameDir:String){
        val  sf=App.lcontext.filesDir.absolutePath + "/$nameDir"
        val file=File(sf)
        if(file.mkdir()){
            Timber.d("directory $nameDir created")
        }else{
            Timber.d("directory $nameDir not created")
        }
    }
    fun checkDirExist(sdirname:String):Boolean{
        val  sf=App.lcontext.filesDir.absolutePath + "/$sdirname"
        val file=File(sf)
        return file.exists()
    }

    fun findImageInFile(sNameFile: String):ByteArray{
        val zimage:ByteArray
        try {
            val  sf=App.lcontext.filesDir.absolutePath + "/Images/${sNameFile}"
            zimage = Files.readAllBytes(Paths.get(sf))

        } catch (e: Exception) {
            Timber.d("cache exception finding $sNameFile $e")
            return  ByteArray(0)
        }
        return  zimage
    }

    fun getBitmapImage2(sNameImg: String, ms: MutableState<ImageBitmap>){
        val scope= CoroutineScope(CoroutineName("getImagesCor"))
        scope.launch {
            //Log.d("UPALA","hola")
                Timber.d("$sNameImg")
                val x=getBitmapImage(sNameImg)
                if(x != null) ms.value = x
                else Timber.d("Image Null $sNameImg")
        }
     }


   suspend  fun getBitmapImage(sNameImg: String): ImageBitmap?= withContext(Dispatchers.IO) {
        val bmp=hashmapImages[sNameImg]
       //Timber.d("get BitmapImage")
        if(bmp!=null) {
            return@withContext bmp
        }
        val l=findImageInFile( sNameImg)
        Timber.d("$sNameImg    size ${l.size}")
        if(l.size>0) {
            val s = BitmapFactory.decodeByteArray(l, 0, l.size).asImageBitmap()
            hashmapImages[sNameImg] = s
             s
        }
        else  null
    }




    fun fileExistsAndNotEmpty(sNameFile: String, sDirectory: String):Boolean{
        val  sf=App.lcontext.filesDir.absolutePath +"$sDirectory/$sNameFile"
        try {
            val f=Files.size(Paths.get(sf))
            if(f==0L) return false
            return File(sf).exists()
        } catch (e: Exception) {
            return false  //Mes pos i no cagues
        }
    }


//    @ExperimentalPathApi
//@ExperimentalPathApi
fun listAllFiles():List<String>{
        val root=App.lcontext.filesDir.absolutePath
        val path = Paths.get(root)
        var result: List<Path?>



        Files.walk(path).use { walk ->
            result =
                walk.filter { path: Path? ->
                    Files.isRegularFile(
                        path
                    )
                }
                    .collect(Collectors.toList())
        }

        val ls= result.map{it->"${it.toString().substring(root.length)}  ${it?.fileSize()}"}
        return ls
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
        F.listFiles()?.filter { it.name.substring(0,handler.length).equals(handler) }
            ?.forEach{
                Timber.d("Remove : ${it.name}")
                it.delete()
            }
    }

    fun removeBookmarks(){
        val F=File(App.lcontext.filesDir.absolutePath+"/Articles")
        F.listFiles()?.filter { it.extension.equals("BKM") }
            ?.forEach{
                Timber.d("Remove : ${it.name}")

                it.delete()
            }
    }

    fun deleteFile(nameFile:String){
        val F=File(App.lcontext.filesDir.absolutePath+"/$nameFile")
        F.delete()
    }

    fun deleteDirectory(dir:String){
        val F=File(App.lcontext.filesDir.absolutePath+"/$dir")
        F.listFiles()?.filter { it.isFile }
            ?.forEach{
                Timber.d("Remove : ${it.name}")
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
            "$d ${fileOrDirectory.name} (${fileOrDirectory.length()}) ${
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