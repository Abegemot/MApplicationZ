package com.begemot.myapplicationz


import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.begemot.knewsclient.*
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.model.articleHandler
import kotlinx.coroutines.*
//import io.ktor.util.*
import timber.log.Timber
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

object KProvider {

    //suspend fun getLocalNewsPapers(): NewsPaperVersion = fromStrToNewsPaperV(KCache.loadFromCache("knews.json"))

    @OptIn(ExperimentalTime::class)
    suspend fun getNewsPapers():KResult3<NewsPaperVersion> {
            val sFileName = "zknews.json"
            val np = measureTimedValue {
                KCache.load<NewsPaperVersion>(sFileName)
            }
            if (np.value.newspaper.isNotEmpty()) {
                Timber.d("loaded local News papers in (${np.duration.inWholeMilliseconds} ms)")
                return   KResult3.Success(np.value,"KProvider.getNewsPapers local", np.duration.inWholeMilliseconds, 0)
            }
            if (np.value.version == 0) {
                val res= KNews().getNewsPapersWithVersion3(0)
                Timber.d("Z :${res.msg()}")
                return res
                }
             Timber.d("return from getNewsPapers")
           return KResult3.Success(np.value)
    }

    suspend fun getNewsPapersUpdates(currentver: Int): KResult3<NewsPaperVersion> {
        val nP = KNews().getNewsPapersWithVersion3(currentver)
        //checkImages(nP.newspaper)
        if (nP is KResult3.Success) {
            if (nP.t.version == 0) {
                Timber.d("no updates current version ${currentver}")
                //return nP
            } else {
                Timber.d("news papers updates found")
                KCache.storeInCache("knews.json", toStr(nP.t))
                //return nP
            }
        }
        return nP
    }




    suspend fun getImage(nameImg:String): ImageBitmap?{
        return withContext(Dispatchers.IO+CoroutineName("GETIMG")){ getImage2(nameImg) }
    }

    suspend fun  getImage2(nameImg:String,rec:Int=0): ImageBitmap?{
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
        if(ba is KResult3.Success){
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
        }
        return null
    }


    @OptIn(ExperimentalTime::class)
    suspend fun getHeadLines(getHeadLines: GetHeadLines): KResult3<THeadLines>
    {
        val nameFile=getHeadLines.AndroidNameFile()
        val(hl,time) = measureTimedValue {
             KCache.load<THeadLines>(nameFile)
        }
        if(hl.lhl.isNotEmpty()) {  Timber.d("Found in cache in (${time.inWholeMilliseconds}) ms"); return KResult3.Success(hl,"localHeadLines",time.inWholeMilliseconds)}
        return when(val hl2=KNews().getHeadLines(getHeadLines)){
            is KResult3.Success -> { KCache.storeInCache(nameFile,toStr(hl2.t)); hl2 }
            is KResult3.Error   ->  hl2
        }
    }

    suspend fun checkUpdates2(ghl:GetHeadLines):KResult3<THeadLines> {
        val nameFile = "Headlines/${ghl.handler}${ghl.tlang}"
        val thl2 = KNews().getHeadLines(ghl)
        Timber.d("check updates ${thl2.timeInfo()}")
        if (thl2 is KResult3.Error) return thl2
        if (thl2 is KResult3.Success) {
            if (thl2.t.datal != 0L) {
                KCache.removeHeadLinesOf(ghl.handler)
                KCache.storeInCache(nameFile, toStrFromTHeadlines(thl2.t))
                Timber.d(
                    "end HEADLINES HAVE CHANGED data original  ${strfromdateasLong(ghl.datal)}  new data ${
                        strfromdateasLong(
                            thl2.t.datal
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
                    }  new data ${strfromdateasLong(thl2.t.datal)}"
                )
                return KResult3.Success(THeadLines())
            }
        } else {
            return thl2
        }
    }

    suspend fun  getArticle(articleHandler: articleHandler):KResult3<List<OriginalTrans>>{
        lateinit var art:List<OriginalTrans>
        val elapsed = measureTimeMillis{
            art=KCache.load<List<OriginalTrans>>(articleHandler.nameFileArticle())
        }
        if(art.isNotEmpty()) return KResult3.Success(art,"localArticle",elapsed)
        val resp = KNews().getArticle(articleHandler.nphandler, articleHandler.link, articleHandler.tlang)
        if(resp is KResult3.Success){
            KCache.storeInCache(articleHandler.nameFileArticle(),toStr<List<OriginalTrans>>(resp.t))
        }
        return resp
    }
}

//Max 214 219 240 267 273 283 306 167