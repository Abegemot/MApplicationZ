 package com.begemot.myapplicationz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber
import kotlin.reflect.KSuspendFunction1

/*fun getRT_Headlines4(lhd:MutableList<OriginalTransLink>, statusApp: StatusApp){
    Timber.d("->getLHeadlines")
    GlobalScope.launch(Dispatchers.Main) {
        statusApp.currentStatus = AppStatus.Loading
        val resp = exWithException<MutableList<OriginalTransLink>, String> {
            getRTHeadLines(statusApp)
        }
        when(resp) {
            is KResult.Succes -> {
                Timber.d("SUCCES")
                lhd.clear()
                lhd.addAll(resp.t); statusApp.currentStatus = AppStatus.Idle
            }
            is KResult.Error -> { statusApp.currentStatus = AppStatus.Error(resp.msg, resp.e) }
        }

    }
    Timber.d("<-getLHeadlines")

}*/
 fun getRT_Headlines(lhd:MutableList<OriginalTransLink>, statusApp: StatusApp){
     get_HeadLines(lhd,statusApp, ::getRTHeadLines)
 }

fun get_HeadLines(lhd:MutableList<OriginalTransLink>, statusApp: StatusApp, zgetLines: KSuspendFunction1<StatusApp, MutableList<OriginalTransLink>>) {
    Timber.d("->getLHeadlines")
    GlobalScope.launch(Dispatchers.Main) {
        statusApp.currentStatus = AppStatus.Loading
        val resp = exWithException<MutableList<OriginalTransLink>, String> {
            //zgetLHeadLines(statusApp)
            zgetLines(statusApp)
        }
        when(resp) {
            is KResult.Succes -> {
                Timber.d("SUCCES")
                lhd.clear()
                lhd.addAll(resp.t); statusApp.currentStatus = AppStatus.Idle
            }
            is KResult.Error -> { statusApp.currentStatus = AppStatus.Error(resp.msg, resp.e) }
        }

    }
    Timber.d("<-getLHeadlines")
}


suspend fun getRTHeadLines(statusApp: StatusApp):MutableList<OriginalTransLink>{
    Timber.d("->zgetLHeadlines")
   //statusApp.currentStatus = AppStatus.Loading
    // GlobalScope.launch(Dispatchers.Main) {
    val LA = getJSoupHeadlines()   //<-- suspend function runing on IO

    Timber.d("-->after jsoup   ${LA.size}")
    LA.forEach{Timber.d("->${it.title}<-")}
    Timber.d("<--after jsoup")

    val sb = StringBuilder()
    LA.forEach { sb.append(it.title) }
    val rt = translate2(sb.toString(), statusApp.lang,"ru")    //<-- suspend function runing on IO
    //val q = (rt as ResultTranslation.ResultList).Lorigtrans

    val JJ = LA.zip(rt, { a, c -> OriginalTransLink(a, c.translated) })
    val lhd= mutableListOf<OriginalTransLink>()
    lhd.addAll(JJ)
    //statusApp.currentStatus = AppStatus.Idle
    // return@launch lhd

    //  }
    Timber.d("<-zgetLHeadlines  ${lhd.size}")
    return lhd
}
suspend fun getJSoupHeadlines():List<KArticle> = withContext(Dispatchers.IO) {
    fun transFigure(el: Element): KArticle {
        val title = el.select("h3").text()+". "
        val link = el.select("a[href]").first().attr("abs:href")
        return KArticle(title, link, "")
    }
    fun transSection(el: Element): KArticle {
        val title = el.select("h3").text()+". "
        // val desc = el.select("p").text()
        val link = el.select("h3").select("a[href]").attr("abs:href")
        return KArticle(title, link, "")
    }

    val s = "https://russian.rt.com/inotv"
    val con= Jsoup.connect(s)
    con.timeout(6000)
    val doc = con.get()
    var art = doc.select("figure")
    val l1 = art.map { it -> transFigure(it) }

    art = doc.select("section.block-white.materials-preview").select("article")
    val l2 = art.map { it -> transSection(it) }
    l1 + l2
}

