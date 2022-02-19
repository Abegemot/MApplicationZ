  package com.begemot.myapplicationz

import android.graphics.BitmapFactory
import android.text.format.Formatter
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.nio.file.*
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
//import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime

//import kotlin.io

private val DD=false

object KCache{
    val hashmapImages: MutableMap<String,ImageBitmap> = HashMap()

    fun storeInCache(sNameFile: String, scontent: String){
        val sf=App.lcontext.filesDir.absolutePath
        Timber.d("STORE IN CACHE $sf ${scontent.length}")
        try {
            Files.write(Paths.get(sf+"/$sNameFile"),scontent.toByteArray())
            //Files.write(Paths.get(sf+"/ERROR.BGM"),"ok namefile:$sNameFile  size ${scontent.length}\n".toByteArray(),StandardOpenOption.APPEND,StandardOpenOption.CREATE,StandardOpenOption.WRITE)
        } catch (e: Exception) {
            //Files.write(Paths.get(sf+"/ERROR.BGM"),"err ${e.message}\n".toByteArray(),StandardOpenOption.APPEND,StandardOpenOption.CREATE,StandardOpenOption.WRITE)
            writeError("storeInCache $sNameFile ${e.message}")
        }
    }

    fun writeError(serr:String){
        val sf=App.lcontext.filesDir.absolutePath
//        val  sf= App.lcontext.filesDir.absolutePath

        Files.write(Paths.get(sf+"/ERROR.BGM"),"err ${serr}\n".toByteArray(),StandardOpenOption.APPEND,StandardOpenOption.CREATE,StandardOpenOption.WRITE)

    }

    fun readErrors():String{
        val sf=App.lcontext.filesDir.absolutePath
//        val  sf= App.lcontext.filesDir.absolutePath
        if(!fileExistsAndNotEmpty("/ERROR.BGM","")) return "no errors logged"
        return String(Files.readAllBytes(Paths.get(sf+"/ERROR.BGM")))
        //return Files.readAllLines(Paths.get(sf+"/ERROR.BGM")).joinToString { "\n" }
    }

    fun clearErrors(){
        val sf=App.lcontext.filesDir.absolutePath
 //       val  sf= App.lcontext.filesDir.absolutePath
        try {
            Files.delete(Paths.get("$sf/ERROR.BGM"))
        } catch (e: Exception) {
        }
    }


    fun storeImageInFile(sNameFile: String, zimage: ByteArray){
        Timber.d("STORE IMAGE IN CACHE $sNameFile   with size ${zimage.size}")
        if(zimage.size==0) return
        val sf=App.lcontext.filesDir.absolutePath+ "/Images/$sNameFile"
        Files.write(Paths.get(sf),zimage)
        Timber.d("end write $sf size ${zimage.size}")
    }

      fun loadFromCache(sNameFile: String):String{   //= withContext(Dispatchers.IO){
          val sf="${App.lcontext.filesDir.absolutePath}/$sNameFile"
        //val  sf="${App.lcontext.filesDir.absolutePath}/$sNameFile"
        val str=
        try {
            val str=String(Files.readAllBytes(Paths.get(sf)))
            Timber.d("File raw $sf length(${str.length})->' ${str.substring(0,minOf(str.length,29))}....'")
            return  str
        } catch (e: Exception) {
            Timber.e("exception $e <-QQ")
            ""//"error loadFromCache ${e.message}"
        }
        return str
    }

   fun setUp(){
        if(checkDirExist("Images")) return
        makeDir("Images")
        makeDir("Headlines")
        makeDir("Articles")
    }

    fun makeDir(nameDir:String){
        val sf=App.lcontext.filesDir.absolutePath+ "/$nameDir"
        //val  sf=App.lcontext.filesDir.absolutePath + "/$nameDir"
        val file=File(sf)
        if(file.mkdir()){
            Timber.d("directory $nameDir created")
        }else{
            Timber.d("directory $nameDir not created")
        }
    }
    fun checkDirExist(sdirname:String):Boolean{
        val sf=App.lcontext.filesDir.absolutePath+ "/$sdirname"
        //val  sf=App.lcontext.filesDir.absolutePath + "/$sdirname"
        val file=File(sf)
        return file.exists()
    }

    fun addImageInMemory(sNameImg:String,img:ImageBitmap){   //= withContext(Dispatchers.IO){
        //hashmapImages[sNameImg] = img
//        Timber.d("put $sNameImg")
        hashmapImages.put(sNameImg,img)
    }

    suspend fun findImgInFile(sNameFile: String):ImageBitmap? = withContext(Dispatchers.IO){
//        Timber.d("finImgInFile x $sNameFile")
        findImageInFile(sNameFile)
    }


    suspend fun findImgInFile2(sNameFile: String):ImageBitmap?= withContext(Dispatchers.IO) {
        //Timber.d("finImgInFile2 x $sNameFile")
        try {
            val sf=App.lcontext.filesDir.absolutePath+ "/Images/${sNameFile}"
            //val  sf=App.lcontext.filesDir.absolutePath + "/Images/${sNameFile}"
            val zimage = Files.readAllBytes(Paths.get(sf))
            val s = BitmapFactory.decodeByteArray(zimage, 0, zimage.size).asImageBitmap()
            Timber.d("findImgInFile2 ($sNameFile) found  ${zimage.size}")
            s
        //    return@withContext s
        } catch (e: Exception) {
            Timber.e("findImgInFile2($sNameFile) exception reading file  $e")
            null
            //return@withContext null //ByteArray(0)
        }
    }


    fun findImageInFile(sNameFile: String):ImageBitmap? {
        try {
            val sf=App.lcontext.filesDir.absolutePath+ "/Images/${sNameFile}"
            val zimage = Files.readAllBytes(Paths.get(sf))
            val s = BitmapFactory.decodeByteArray(zimage, 0, zimage.size).asImageBitmap()
//            Timber.d("found $sNameFile  ${zimage.size}")
            return s
        } catch (e: Exception) {
            Timber.e("cache exception finding $sNameFile $e")
            return  null //ByteArray(0)
        }
    }

    fun getBitmapImage2(sNameImg: String, ms: MutableState<ImageBitmap>){
        val scope= CoroutineScope(CoroutineName("getImagesCor"))
        scope.launch {
            //Log.d("UPALA","hola")
                Timber.d("$sNameImg")
                val x=getBitmapImageFromMemCache(sNameImg)
                if(x != null) ms.value = x
                else Timber.d("Image Null $sNameImg")
        }
     }

    inline fun getBitmapImageFromMemCache(sNameImg: String): ImageBitmap?{
        //Timber.d("->$sNameImg")
        return hashmapImages[sNameImg]
    }

   /*suspend  fun getBitmapImageFromMemCache2(sNameImg: String): ImageBitmap?= withContext(Dispatchers.IO+CoroutineName("ZOPA")) {
       Timber.d("$sNameImg")
       return@withContext hashmapImages[sNameImg]
    }*/

    /*fun filesInDir(dir:String){
        val  sf="${App.lcontext.filesDir.absolutePath}/$dir"
        val f=Files.list(Paths.get(sf))
        f.filter(  )
    }*/



    fun fileExistsAndNotEmpty(sNameFile: String, sDirectory: String):Boolean{
        val sf=App.lcontext.filesDir.absolutePath+"$sDirectory/$sNameFile"
        //val  sf=App.lcontext.filesDir.absolutePath +"$sDirectory/$sNameFile"
        try {
            val f=Files.size(Paths.get(sf))
            if(f==0L) return false
            return File(sf).exists()
        } catch (e: Exception) {
            Timber.d("$e")
            return false  //Mes pos i no cagues
        }
    }


//    @ExperimentalPathApi
//@ExperimentalPathApi
fun listAllFiles():List<String>{
        val root=App.lcontext.filesDir.absolutePath
        //val root=App.lcontext.filesDir.absolutePath
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

    fun listDirectory(sDir:String):String{
        val sf=App.lcontext.filesDir.absolutePath
        val path=Paths.get("${sf}/$sDir")
        val fd = Files.list(path).collect(Collectors.toList())
        //val s=StringBuilder()
        val l= mutableListOf("")
        //DateFormat
        val df = SimpleDateFormat("dd/MM/yyyy")

        fd.forEach {
            val size=Formatter.formatShortFileSize(App.lcontext,it.fileSize())
            l.add("${it.fileName} (${size}) ${df.format(it.getLastModifiedTime().toMillis())}\n")
            //s.append("(${it.fileSize().toString().padStart(6,'0')}) ${it.fileName}\n")
        }
            //.map(Path::getFileName)
            //.map(Path::toString)
        l.sort()
        return l.joinToString("") //s.toString()
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
                Timber.d("Remove : ${it.name}  ${it.extension}")
                if(!it.extension.equals("BKM") && !it.extension.equals("LIN")) {        //do not remove bookmarks
                    Timber.d("RELLY Remove : ${it.name}")
                    it.delete()
                }
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


//Max 311