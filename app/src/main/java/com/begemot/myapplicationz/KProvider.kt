package com.begemot.myapplicationz

import com.begemot.knewscommon.KArticle
import com.begemot.knewscommon.NewsPaper
import com.begemot.knewscommon.OriginalTransLink
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
    lateinit var lNP:List<NewsPaper>
    fun getNewsPapers():List<NewsPaper>{
        return lNP
    }
    fun setNewsPapers(tl:List<NewsPaper>){
        lNP=tl
    }
}