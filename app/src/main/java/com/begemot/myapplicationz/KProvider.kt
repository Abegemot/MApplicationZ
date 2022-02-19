package com.begemot.myapplicationz


import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.begemot.knewsclient.KNews
import com.begemot.knewsclient.envelopeX2
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.model.articleHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
//import io.ktor.util.*
import timber.log.Timber

object KProvider {

    //suspend fun getLocalNewsPapers(): NewsPaperVersion = fromStrToNewsPaperV(KCache.loadFromCache("knews.json"))

    suspend fun getLocalNewsPapers():KResult2<NewsPaperVersion,String>{
        Timber.d("London Calling ----")
        return  envelopeX2 {
            Timber.d("start KProvider.getLocalNewsPaper")
            Pair(fromStrToNewsPaperV(KCache.loadFromCache("knews.json")),0)
        }
    }


    suspend fun getNewsPapersUpdates(currentver: Int): KResult2<NewsPaperVersion,String> {
        val nP = KNews().getNewsPapersWithVersion2(currentver)
        //checkImages(nP.newspaper)
        if (nP is KResult2.Success) {
            if (nP.t.version == 0) {
                Timber.d("no updates current version ${currentver}")
                //return nP
            } else {
                Timber.d("news papers updates found")
                KCache.storeInCache("knews.json", nP.t.toStr())
                //return nP
            }
        }
        return nP
    }



    /*suspend fun getImage3(nameImg:String): ImageBitmap? = withContext(Dispatchers.IO+CoroutineName("getImage")){
        Timber.d("OOOOO OOOOOOOOO  $nameImg")
        val img=KCache.getBitmapImageFromMemCache(nameImg)
        if(img!=null) return@withContext img

        val img2 = KCache.findImageInFile(nameImg)
        if(img2!=null){ KCache.addImageInMemory(nameImg,img2); Timber.d("Found in local file "); return@withContext img2 }
        else Timber.d("Not found in local file")

        val ba = KNews().getImage2("Images/$nameImg")
        if(ba is KResult2.Success){
            Timber.d("Result ok size img ${ba.t.bresult.size}")
            //KCache.storeImageInCache(np.logoName, ba.t.bresult)
            if(ba.t.bresult.size>0) {
                KCache.storeImageInFile(nameImg, ba.t.bresult)
                KCache.addImageInMemory(nameImg,
                    BitmapFactory.decodeByteArray(ba.t.bresult, 0, ba.t.bresult.size)
                        .asImageBitmap()
                )
                Timber.d("return Ok : $nameImg ${ba.t.bresult.size}")
                ba.t.bresult
                val l=KCache.getBitmapImageFromMemCache(nameImg)
                Timber.d("Height ${l?.height}")
                return@withContext l
            }else
                return@withContext null
        }
        if(ba is KResult2.Error) {
            Timber.d("Failed to load  $nameImg")
            KCache.writeError("${nameImg}  NOT FOUND  ${ba.msg}")
            Timber.e("getImage(${nameImg})  ERROR:  ${ba.msg}")
            return@withContext null
        }
        return@withContext null
    }*/

    suspend fun getImage(nameImg:String): ImageBitmap?{
        return withContext(Dispatchers.IO+CoroutineName("GETIMG")){ getImage2(nameImg) }
    }

    suspend fun  getImage2(nameImg:String): ImageBitmap?{

        Timber.d("$nameImg")

        val img=KCache.getBitmapImageFromMemCache(nameImg)
        if(img!=null) return img

        //coroutineScope {
            val img2 = KCache.findImgInFile(nameImg)  //  ImageInFile(nameImg)
            if (img2 != null) {
                KCache.addImageInMemory(nameImg, img2);
                return img2
            } //else return null
        //}
        val ba = KNews().getImage2("Images/$nameImg")
        if(ba is KResult2.Success){
                Timber.d("Result ok size img ${ba.t.bresult.size}")

                if(ba.t.bresult.size>0) {
                    KCache.storeImageInFile(nameImg, ba.t.bresult)
                    KCache.addImageInMemory(nameImg,
                        BitmapFactory.decodeByteArray(ba.t.bresult, 0, ba.t.bresult.size)
                            .asImageBitmap()
                    )
                    return KCache.getBitmapImageFromMemCache(nameImg)
                }else
                    return null
        }
        if(ba is KResult2.Error) {
                Timber.d("Failed to load  $nameImg")
                KCache.writeError("${nameImg}  NOT FOUND  ${ba.msg}")
                Timber.d("${nameImg}  NOT FOUND  ${ba.msg}")
                return null
        }
        return null
    }


    suspend fun getHeadLines(getHeadLines: GetHeadLines): KResult2<THeadLines,String>
    {
        //val nameFile = "Headlines/${getHeadLines.handler}${getHeadLines.tlang}"
        val nameFile=getHeadLines.AndroidNameFile()
        //delay(1000)
        val sthl = KCache.loadFromCache(nameFile)
        //throw(Exception("patata"))
        if (sthl.length == 0) { //No existeix headline en cache
            Timber.d("No existeix headlines en cache")
            val thl2 = KNews().getHeadLines(getHeadLines)

            if(thl2 is KResult2.Error) return thl2
            if(thl2 is KResult2.Success){
                Timber.d(thl2.t.lhl.print("Headlines of ${getHeadLines.handler} from server"))
                Timber.d("found in server-> n headlines = ${thl2.t.lhl.size}")
                KCache.storeInCache(nameFile, thl2.t.toStr())
                return thl2
            }
        } else {
            Timber.d("existeix en Cache  ${sthl.length}")
            val thdl = fromStrToTHeadLines(sthl)
            Timber.d(thdl.lhl.print("Headlines of ${getHeadLines.handler} from cache"))
            Timber.d("RETURN FROM CACHE!!! OK ->n headlines = ${thdl.lhl.size} data headlines=${thdl.datal}")
            return KResult2.Success(thdl)
        }
        return KResult2.Error("nothing")
    }

    suspend fun checkUpdates2(ghl:GetHeadLines):KResult2<THeadLines,String> {
        val nameFile = "Headlines/${ghl.handler}${ghl.tlang}"
//        try {
        val thl2 = KNews().getHeadLines(ghl)
        Timber.d("check updates ${thl2.timeInfo()}")
        if (thl2 is KResult2.Error) return thl2
        // throw(Exception("patata 1"))
        if (thl2 is KResult2.Success) {

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
                return KResult2.Success(THeadLines())
            }
        } else {
            return thl2
        }
    }

    suspend fun  getArticle(articleHandler: articleHandler):KResult2<List<OriginalTrans>,String>{
        val sthl = KCache.loadFromCache(articleHandler.nameFileArticle())
        if(!sthl.isEmpty()){
            Timber.d("FOUND IN CACHE ${articleHandler.link} ${articleHandler.nphandler}")
            return KResult2.Success(JListOriginalTrans(sthl).toList())
        }

        val resp = KNews().getArticle(articleHandler.nphandler, articleHandler.link, articleHandler.tlang)
        if(resp is KResult2.Success){
              val s=resp.t.toJSON()
              KCache.storeInCache(articleHandler.nameFileArticle(),s.str)

        }

        return resp
    }


}