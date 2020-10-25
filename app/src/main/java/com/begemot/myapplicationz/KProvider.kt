  package com.begemot.myapplicationz

import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.reflect.KSuspendFunction0

object KProvider{
    suspend fun getHL(
        nameFile: String,
        olang: String,
        tlang: String,
        getOriginalHDLines: KSuspendFunction0<List<KArticle>>
    ):List<OriginalTransLink>{
        val olt=KCache.findInCache(App.lcontext, nameFile + tlang)
        if(olt.isNotEmpty()) return olt
        val nolt= getOriginalHDLines()
        Timber.d("nolt  ${nolt.size}")


        val lt=getTranslatedHeadlines(nolt, olang, tlang)
        KCache.storeInCache(App.lcontext, nameFile + tlang, lt)
        return lt
    }
}


object KProvider2{
    private  var lNP:List<NewsPaper> = emptyList()
    fun getNewsPapers(mlNewsPapers: MutableList<NewsPaper>):List<NewsPaper>{

        mlNewsPapers.clear()
        mlNewsPapers.addAll(lNP)
        return mlNewsPapers
        //return lNP
    }
    fun setNewsPapers(tl:List<NewsPaper>){
        lNP=tl
    }
    suspend fun getHeadLines(statusApp: StatusApp):THeadLines{
        Timber.d("getHeadLines ${statusApp.currentNewsPaper.handler}")
        val getHeadLines=GetHeadLines(statusApp.currentNewsPaper.handler,statusApp.lang,0L)
        val nameFile="/Headlines/${statusApp.currentNewsPaper.handler}${statusApp.lang}"
        val sthl=KCache.findInCache2(nameFile)
        if(sthl.length==0){ //No existeix headline
            Timber.d("No existeix headlines")
            val thl2=KNews().getHeadLines(getHeadLines)
            KCache.storeInCache3(nameFile, toStrFromTHeadlines(thl2))
            return thl2

        }else{
            Timber.d("FROM CACHE!!! ")
            val thdl=fromStrToTHeadLines(sthl)
            val getHeadLines=GetHeadLines(statusApp.currentNewsPaper.handler,statusApp.lang,thdl.datal)
            checkUpdates(getHeadLines,thdl)
            Timber.d("BEFORE RETURN GETHEADLINES")
            return thdl
        }
    }

    suspend fun checkUpdates(ghl:GetHeadLines,thl:THeadLines){
        val nameFile="/Headlines/${ghl.handler}${ghl.tlang}"
        val scope= CoroutineScope(Job()+Dispatchers.IO )
        scope.launch {
            Timber.d("start check updates")
            val thl2=KNews().getHeadLines(ghl)
            if(thl2.datal!=0L) {
                KCache.storeInCache3(nameFile, toStrFromTHeadlines(thl2))
                //thl=thl2
                thl.datal=thl2.datal
                thl.lhl=thl2.lhl
                Timber.d("HEADLINES HAVE CHANGED")
            }else{
                Timber.d("HEADLINES HAVEN'T CHANGED")
            }
            //delay(2000L)
            Timber.d("end check updates")
        }
    }


}