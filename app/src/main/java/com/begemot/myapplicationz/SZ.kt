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

    Timber.d("-->after jsoup   ${LA.size}")
    LA.forEach{Timber.d("->${it.title}<-")}
    Timber.d("<--after jsoup")

    val sb = StringBuilder()
    LA.forEach { sb.append(it.title) }
    val rt = translate2(sb.toString(), statusApp.lang,"de")    //<-- suspend function runing on IO
    //val q = (rt as ResultTranslation.ResultList).Lorigtrans

    val JJ = LA.zip(rt, { a, c -> OriginalTransLink(a, c.translated) })
    val lhd= mutableListOf<OriginalTransLink>()
    lhd.addAll(JJ)


    //Timber.d(LA.toString())
    return lhd
}

suspend fun getSZJSoupHeadlines():List<KArticle> = withContext(Dispatchers.IO) {
    fun transFigure(el: Element): KArticle {
        Timber.d(el.html())
        val title=el.select("h3.sz-teaser__title").text().replace(".",",")+". "
        val title2 = el.text().replace(".",",")+". "
        val link =  el.attr("href")

       if(el.text().isEmpty()) return KArticle()
       // else return KArticle(title, link, "")
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
    var art = doc.select("a.sz-teaser[href]")
    val l1 = art.map { it -> transFigure(it) }.filter { !it.title.isEmpty() }

    //art = doc.select("section.block-white.materials-preview").select("article")
    //val l2 = art.map { it -> transSection(it) }
    //l1 + l2
    l1
}


