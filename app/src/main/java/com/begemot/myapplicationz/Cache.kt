  package com.begemot.myapplicationz

import android.graphics.BitmapFactory
import android.text.format.Formatter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.begemot.knewscommon.fromJStr
import com.begemot.knewscommon.kjson
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import timber.log.Timber
import java.io.File
import java.nio.file.*
import java.text.SimpleDateFormat
import java.util.stream.Collectors
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

  //import kotlin.io

private val DD=false


object KCache{
    val hashmapImages: MutableMap<String,ImageBitmap> = HashMap()
    var fP=""

    fun getCName(sNameFile: String):String = "$fP$sNameFile"

    fun storeInCache(sNameFile: String, scontent: String){
        Timber.d("STORE IN CACHE ${getCName(sNameFile)} ${scontent.length} bytes")
        try {
            Files.write(Paths.get(getCName(sNameFile)),scontent.toByteArray())
        } catch (e: Exception) {
            writeError("storeInCache ${getCName(sNameFile)} ${e.message}")
        }
    }

    fun writeError(serr:String){
        Files.write(Paths.get(getCName("ERROR.BGM")),"err ${serr}\n".toByteArray(),StandardOpenOption.APPEND,StandardOpenOption.CREATE,StandardOpenOption.WRITE)
    }

    fun readErrors():String{
        if(!fileExistsAndNotEmpty("ERROR.BGM","")) return "no errors logged"
        return String(Files.readAllBytes(Paths.get(getCName("ERROR.BGM"))))
        //return Files.readAllLines(Paths.get(sf+"/ERROR.BGM")).joinToString { "\n" }
    }

    fun clearErrors(){
        try {
            Files.delete(Paths.get(getCName("ERROR.BGM")))
        } catch (e: Exception) {
        }
    }


    fun storeImageInFile(sNameFile: String, zimage: ByteArray){
        val sf= getCName( "Images/$sNameFile")
        //Timber.d("STORE IMAGE IN CACHE $sf   with size ${zimage.size}")
        if(zimage.size==0) return
        Files.write(Paths.get(sf),zimage)
        //throw Exception("patata !!!!!")
        //Timber.d("end write $sf size ${zimage.size}")
    }
    fun storeMP3InFile(sNameFile: String,zimage: ByteArray){
        val sf= getCName( "MP3/$sNameFile")
        //Timber.d("STORE IMAGE IN CACHE $sf   with size ${zimage.size}")
        if(zimage.size==0) {  Timber.e("empty mp3 from server $sf"); return }
        Files.write(Paths.get(sf),zimage)
    }


    @OptIn(ExperimentalTime::class)
    //suspend
   suspend inline fun <reified T>   load(sNameFile: String):T{
            //  return T::class.java.newInstance()
            val s = loadStringFromCache(sNameFile)
            if (s.isEmpty()) {
                //Timber.d("loading ${T::class.simpleName} from $sNameFile")
                return when (T::class) {
                    Int::class -> {
                        0 as T
                    }
                    List::class -> {
                        emptyList<T>() as T
                    }
                    else -> {
                        T::class.java.newInstance()
                    }
                }

            }
            return fromJStr<T>(s)
            //val l = kjson.decodeFromString<T>(s)
            //return l
    }

// caller has to deal with empty values
      suspend fun loadStringFromCache(sNameFile: String):String= withContext(Dispatchers.IO+CoroutineName("FileLoader")){   //= withContext(Dispatchers.IO){
        val str=
        try {
            val str=String(Files.readAllBytes(Paths.get(getCName(sNameFile))))
            //Timber.d("File raw $sf length(${str.length})->' ${str.substring(0,minOf(str.length,69))}....'")
            return@withContext str
        } catch (e: Exception) {
            //Timber.e("exception $e")
            ""//"error loadFromCache ${e.message}"
        }
          return@withContext str
    }


    fun loadStringFromCache2(sNameFile: String):String{   //= withContext(Dispatchers.IO){
        val str=
            try {
                val str=String(Files.readAllBytes(Paths.get(getCName(sNameFile))))
                //Timber.d("File raw $sf length(${str.length})->' ${str.substring(0,minOf(str.length,69))}....'")
                return str
            } catch (e: Exception) {
                //Timber.e("exception $e")
                ""//"error loadFromCache ${e.message}"
            }
        return str
    }






    fun findImageInFile(sNameFile: String):ImageBitmap? {
        if(sNameFile.isEmpty()) return null
        try {
            val sf= getCName( "Images/${sNameFile}")
            val zimage = Files.readAllBytes(Paths.get(sf))
            val s = BitmapFactory.decodeByteArray(zimage, 0, zimage.size).asImageBitmap()
//            Timber.d("found $sNameFile  ${zimage.size}")
            return s
        } catch (e: Exception) {
            Timber.e("cache exception finding '$sNameFile' $e")
            return  null //ByteArray(0)
        }
    }



    fun setUp(){
        if(checkDirExist("Images")) return
        makeDir("Images")
        makeDir("Headlines")
        makeDir("Articles")
        makeDir("MP3")
    }

    fun makeDir(nameDir:String){
        val sf= getCName( "/$nameDir")
        val file=File(sf)
        if(file.mkdir()){
            Timber.d("directory $nameDir created")
        }else{
            Timber.d("directory $nameDir not created")
        }
    }
    fun checkDirExist(sdirname:String):Boolean{
        val sf= getCName( "/$sdirname")
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


  /*  suspend fun findImgInFile2(sNameFile: String):ImageBitmap?= withContext(Dispatchers.IO) {
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
    }*/



/*    fun getBitmapImage2(sNameImg: String, ms: MutableState<ImageBitmap>){
        val scope= CoroutineScope(CoroutineName("getImagesCor"))
        scope.launch {
            //Log.d("UPALA","hola")
                Timber.d("$sNameImg")
                val x=getBitmapImageFromMemCache(sNameImg)
                if(x != null) ms.value = x
                else Timber.d("Image Null $sNameImg")
        }
     }*/

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


    fun fileExist(sNameFile:String):Boolean{
        val sf= getCName(sNameFile)
        val b= File(sf).exists()
        Timber.d("file exist $b of $sf")
        return b
    }

    fun getFile(sNameFile: String):File{
        val sf= getCName(sNameFile)
        return File(sf)
    }


    fun getFileDate(sNameFile: String):Long{
        val sf= getCName(sNameFile)
        return File(sf).lastModified()
    }




    fun fileExistsAndNotEmpty(sNameFile: String, sDirectory: String):Boolean{
        val sf= getCName("$sDirectory/$sNameFile")
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
        val root= getCName("")
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
        val path=Paths.get(getCName("/$sDir"))
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
        listRecursive(File(getCName("")), lf)
        lf.forEach {
            Timber.d(it)
        }
        return lf
    }

    fun removeHeadLinesOf(handler:String){
        Timber.d("removeHeadLinesOf:$handler")
        val F=File(getCName("Headlines"))
        F.listFiles()?.filter { it.name.substring(0,handler.length).equals(handler) }
            ?.forEach{
                Timber.d("Remove : ${it.name}")
                it.delete()
            }
    }

    fun removeBookmarks(){
        val F=File(getCName("Articles"))
        F.listFiles()?.filter { it.extension.equals("BKM") }
            ?.forEach{
                Timber.d("Remove : ${it.name}")

                it.delete()
            }
    }

    fun deleteFile(nameFile:String){
        val F=File(getCName("$nameFile"))
        F.delete()
    }

    fun deleteDirectory(dir:String){
        val F=File(getCName("/$dir"))
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
        deleteRecursive(File(getCName("")))
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


//Max 311 342 359 374