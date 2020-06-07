package com.begemot.myapplicationz

import androidx.compose.Composable
import androidx.ui.foundation.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber

@Composable
fun SZ_headlinesScreen(statusApp: StatusApp){
    statusApp.currentBackScreen=Screens.ListNewsPapers
    Text("Not Implemented")
}

fun getSZ_Headlines(lhd:MutableList<OriginalTransLink>, statusApp: StatusApp){
    get_HeadLines(lhd,statusApp,::getSZHeadLines)

}


suspend fun getSZHeadLines(statusApp: StatusApp):MutableList<OriginalTransLink>{
    val L= mutableListOf<OriginalTransLink>(OriginalTransLink(KArticle("hola","link","desc"),"translated1"))
    val LA=getSZJSoupHeadlines()

    val sb = StringBuilder()
    LA.forEach { sb.append(it.title) }
    val rt = translate2(sb.toString(), statusApp.lang)    //<-- suspend function runing on IO
    //val q = (rt as ResultTranslation.ResultList).Lorigtrans

    val JJ = LA.zip(rt, { a, c -> OriginalTransLink(a, c.translated) })
    val lhd= mutableListOf<OriginalTransLink>()
    lhd.addAll(JJ)


    Timber.d(LA.toString())
    return lhd
}

suspend fun getSZJSoupHeadlines():List<KArticle> = withContext(Dispatchers.IO) {
    fun transFigure(el: Element): KArticle {
        val title = el.text()+". "
        val link =  el.attr("href")
        return KArticle(title, link, "")
    }
    fun transSection(el: Element): KArticle {
        val title = el.select("h3").text()+". "
        // val desc = el.select("p").text()
        val link = el.select("h3").select("a[href]").attr("abs:href")
        return KArticle(title, link, "")
    }

    val s = "https://www.sueddeutsche.de"
    val con= Jsoup.connect(s)
    con.timeout(6000)
    val doc = con.get()
    var art = doc.select("a[href]")
    val l1 = art.map { it -> transFigure(it) }

    //art = doc.select("section.block-white.materials-preview").select("article")
    //val l2 = art.map { it -> transSection(it) }
    //l1 + l2
    l1
}


