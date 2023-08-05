package com.begemot.myapplicationz


import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.begemot.knewsclient.*

import com.begemot.knewscommon.*
//import com.begemot.knewscommon.KResult
import com.begemot.myapplicationz.model.articleHandler
import kotlinx.coroutines.*
//import io.ktor.util.*
import timber.log.Timber
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

object KProvider {

    //suspend fun getLocalNewsPapers(): NewsPaperVersion = fromStrToNewsPaperV(KCache.loadFromCache("knews.json"))

    suspend fun checkMP3(sNameMp3:String):Boolean{
          if(KCache.fileExist("MP3/$sNameMp3")) return true
          return KNews().getMP3(sNameMp3, KCache.getFile("MP3/$sNameMp3"))
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getNewsPapers():KResult<NewsPaperVersion> {
            val sFileName = "knews.json"
            val np = measureTimedValue {
                KCache.load<NewsPaperVersion>(sFileName)
            }
            if (np.value.newspaper.isNotEmpty()) {
                Timber.d("loaded local News papers in (${np.duration.inWholeMilliseconds} ms)")
                return KResult(Result.success(np.value),np.duration.inWholeMilliseconds, sparams = "getLocalNewsPapers")
                //return   KResult3.Success(np.value,"KProvider.getNewsPapers local", np.duration.inWholeMilliseconds, 0)
            }
            if (np.value.version == 0) {
                val res= KNews().getNewsPapersWithVersion3(0)
                Timber.d("Z :${res.logInfo()}")
                return res
                }
             Timber.d("return from getNewsPapers")
             return KResult(Result.success(np.value))
    }

    suspend fun getNewsPapersUpdates(currentver: Int): KResult<NewsPaperVersion> {
        val nP = KNews().getNewsPapersWithVersion3(currentver)
        //checkImages(nP.newspaper)
        nP.res.onSuccess {
            if(it.version==0) Timber.d("no updates current version ${currentver}")
            else{
                Timber.d("news papers updates found")
                KCache.storeInCache("knews.json", toJStr(it))
            }
        }
        nP.res.onFailure {
            Timber.e(it)
        }
        return nP
    }

    suspend fun getImage(nameImg:String): ImageBitmap?{
        return withContext(Dispatchers.IO+CoroutineName("GETIMG")){ getImage2(nameImg) }
    }

    suspend fun  getImage2(nameImg:String,rec:Int=0): ImageBitmap?{
        if(nameImg.isEmpty()) return null
        val img2 = KCache.findImgInFile(nameImg)  //  ImageInFile(nameImg)
        if (img2 != null) {
            KCache.addImageInMemory(nameImg, img2)
            return img2
        }
        if(KNews().getIMG(nameImg, KCache.getFile("Images/$nameImg"))){
            val img3=KCache.findImgInFile(nameImg)
            if(img3!=null){
                KCache.addImageInMemory(nameImg,img3)
                return img3
            }
        }else return null
        return null
    }


    suspend fun  getImage3(nameImg:String,rec:Int=0): ImageBitmap?{
            if(nameImg.isEmpty()) return null
            val img2 = KCache.findImgInFile(nameImg)  //  ImageInFile(nameImg)
            if (img2 != null) {
                KCache.addImageInMemory(nameImg, img2);
                return img2
            } //else return null

        if(rec==4){
            Timber.d("$nameImg leaving recursion with null  rec==$rec")
            return null
        }

        val ba = KNews().getImage2("Images/$nameImg")
        ba.res.onSuccess {
            if(it.bresult.size>0) {
                KCache.storeImageInFile(nameImg, it.bresult)
                KCache.addImageInMemory(nameImg,
                    BitmapFactory.decodeByteArray(it.bresult, 0, it.bresult.size)
                        .asImageBitmap()

                )
                if(rec>0)
                    Timber.d("getImage(${rec+1}) Ok $nameImg got from server and stored .... size ${it.bresult.size} bytes [${ba.timeInfo()}]")
                return KCache.getBitmapImageFromMemCache(nameImg)
            }else {
                val s="$nameImg empty in Server!"
                KCache.writeError(s)
                Timber.e(s)
                return null
            }
        }.onFailure {
            Timber.e("Failed to load  $nameImg ${ba.logInfo()}  ${ba.timeInfo()}")
            KCache.writeError("${nameImg}  NOT FOUND  ${ba.logInfo()}")
            //Timber.d("${nameImg}  NOT FOUND  ${ba.msg}")
            Timber.e("GET IMAGE AGAIN !!! $nameImg  (${rec+2})")
            return getImage2(nameImg,rec+1)
            //return null

        }


       /* if(ba is KResult3.Success){
                if(ba.t.bresult.size>0) {
                    KCache.storeImageInFile(nameImg, ba.t.bresult)
                    KCache.addImageInMemory(nameImg,
                        BitmapFactory.decodeByteArray(ba.t.bresult, 0, ba.t.bresult.size)
                            .asImageBitmap()

                    )
                    if(rec>0)
                        Timber.d("getImage(${rec+1}) Ok $nameImg got from server and stored .... size ${ba.t.bresult.size} bytes [${ba.timeInfo()}]")
                    return KCache.getBitmapImageFromMemCache(nameImg)
                }else {
                    val s="$nameImg empty in Server!"
                    KCache.writeError(s)
                    Timber.e(s)
                    return null
                }
        }
        if(ba is KResult3.Error) {
                Timber.e("Failed to load  $nameImg ${ba.msg}  ${ba.timeInfo()}")
                KCache.writeError("${nameImg}  NOT FOUND  ${ba.msg}")
                //Timber.d("${nameImg}  NOT FOUND  ${ba.msg}")
                Timber.e("GET IMAGE AGAIN !!! $nameImg  (${rec+2})")
                return getImage2(nameImg,rec+1)
                //return null
        }*/
        return null
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getHeadLines(getHeadLines: GetHeadLines): KResult<THeadLines> {
        val nameFile = getHeadLines.AndroidNameFile()
        val (hl, time) = measureTimedValue {
            KCache.load<THeadLines>(nameFile)
        }
        if (hl.lhl.isNotEmpty()) {
            Timber.d("Found in cache in (${time.inWholeMilliseconds}) ms")
//            Timber.d(toJStr(hl.lhl))
            return KResult<THeadLines>(Result.success(hl),-1,time.inWholeMilliseconds,"","")
            //return  //KResult3.Success(hl,"localHeadLines",time.inWholeMilliseconds)
        }
        val z= KNews().getHeadLines(getHeadLines)
        z.res.onSuccess {  KCache.storeInCache(nameFile, toJStr(it)) }
        .onFailure {}
        return z
    }

    suspend fun checkUpdates2(ghl:GetHeadLines): KResult<THeadLines> {
        val nameFile = "Headlines/${ghl.handler}${ghl.tlang}"
        val thl2 = KNews().getHeadLines(ghl)
        //Timber.d("check updates ${thl2.timeInfo()}")
        if(thl2.res.isFailure) return thl2
        thl2.res.onSuccess {
            if (it.datal != 0L) {
                KCache.removeHeadLinesOf(ghl.handler)
                KCache.storeInCache(nameFile, toJStr(it))
                Timber.d(
                    "end HEADLINES HAVE CHANGED data original  ${strfromdateasLong(ghl.datal)}  new data ${
                        strfromdateasLong(
                            it.datal
                        )
                    }"
                )
                return thl2
            } else {
                //Timber.d("check updates ${thl2.timeInfo()}")
                Timber.d(
                    "end HEADLINES HAVEN'T CHANGED checkUpdates2 data original  ${
                        strfromdateasLong(
                            ghl.datal
                        )
                    }  new data ${strfromdateasLong(it.datal)}"
                )
                return KResult(Result.success(THeadLines()))
            }
        }
            return thl2
    }





    suspend fun  getArticle(articleHandler: articleHandler):KResult<List<OriginalTrans>>{
        lateinit var art:List<OriginalTrans>
        val elapsed = measureTimeMillis{
            art=KCache.load<List<OriginalTrans>>(articleHandler.nameFileArticle())
        }
        if(art.isNotEmpty()) return KResult(Result.success(art),elapsed,sparams="loadlocalArticle")
        val resp = KNews().getArticle(articleHandler.nphandler, articleHandler.link, articleHandler.tlang)
        if(resp.res.isSuccess){
            KCache.storeInCache(articleHandler.nameFileArticle(),toJStr<List<OriginalTrans>>(resp.res.getOrThrow())) //Oju !!
        }
        return resp
    }
}

//Max 214 219 240 267 273 283 306 167 172 195 214 251 215 224