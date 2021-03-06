package com.begemot.myapplicationz


import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.model.articleHandler
//import io.ktor.util.*
import timber.log.Timber

object KProvider {

    suspend fun getLocalNewsPapers(): NewsPaperVersion = fromStrToNewsPaperV(KCache.loadFromCache("knews.json"))

    //@KtorExperimentalAPI
    suspend fun getNewsPapersUpdates(currentver: Int): NewsPaperVersion {
        //Preconditions.checkArgument(1==1)
        //throw(Exception("Error geting news papers updates"))
        val nP = KNews().getNewsPapersWithVersion(currentver)
        checkImages(nP.newspaper)
        if (nP.version == 0) {  //no updates
            Timber.d("no updates current version ${currentver}")
            return nP
        } else {
            Timber.d("news papers updates found")
            KCache.storeInCache("knews.json", nP.toStr())


            return nP
        }
    }

    suspend fun checkImages(lnp: List<NewsPaper>) {
        Timber.d("check Images News Paper size ${lnp.size}")
        lnp.forEach {
            if (!KCache.fileExistsAndNotEmpty(it.logoName, "/Images")) {
                val ba = KNews().getImage2("Images/${it.logoName}")
                if(ba is KResult2.Success){
                    KCache.storeImageInCache(it.logoName, ba.t.bresult)
                }
                if(ba is KResult2.Error) {
                    Timber.d("${it.logoName}  NOT FOUND  ${ba.msg}")
                }


            }

        }
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
            Timber.d("RETURN FROM CACHE!!! OK ->n headlines = ${thdl.lhl.size} ")
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
            Timber.d("FOUND IN CACHE")
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