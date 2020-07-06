        package com.begemot.myapplicationz

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.ui.foundation.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber

object SZ :INewsPaper{
    override val olang: String
        get() = "de"
    override val nameFile: String
        get() = "suddeutche.ks"

    override fun getName(e: Title): String {
        return when(e){
            Title.ARTICLE   ->"SZ Article"
            Title.HEADLINES ->"SZ Headlines"
            Title.NAME->"Süddeutsche Zeitung"
        }
    }

    override suspend fun getTranslatedArticle(
        originalTransLink: OriginalTransLink,
        statusApp: StatusApp
    ): MutableList<OriginalTrans> {
        Timber.d("->gettranslationlink")
        statusApp.currentStatus = AppStatus.Loading
        val original = getSZJSoupArticle(originalTransLink)
        val sall = translate2(original, statusApp.lang,"de")
        // val i=1/0
        Timber.d("<-get translated text")
        return sall
    }

    override suspend fun getHeadLines(statusApp: StatusApp): List<OriginalTransLink> {

        val L= mutableListOf<OriginalTransLink>(OriginalTransLink(KArticle("hola","link","desc"),"translated1"))
        val LA=getSZJSoupHeadlines()

        val sb = StringBuilder()
        LA.forEach { sb.append(it.title) }
        val rt = translate2(sb.toString(), statusApp.lang,"de")    //<-- suspend function runing on IO
        //val q = (rt as ResultTranslation.ResultList).Lorigtrans

        //if(LA.size!=rt.size) throw Exception("LA.size ${LA.size} !=rt.size ${rt.size}")
        val JJ = LA.zip(rt, { a, c -> OriginalTransLink(a, c.translated) })
        val lhd= mutableListOf<OriginalTransLink>()
        lhd.addAll(JJ)
        //Timber.d(LA.toString())
        return lhd
    }
}


suspend fun getSZJSoupArticle(originalTransLink: OriginalTransLink) = withContext(Dispatchers.IO) {
    Timber.d("get Article link= ${originalTransLink.kArticle.link}")
    fun transArticleintro(el: Element): String {
        //println("el $el")
        return el.text()
    }
    val doc = Jsoup.connect(originalTransLink.kArticle.link).get()
    //println(originalTransLink.kArticle.link)
    var art = doc.select("p.css-0")
    val l1 = art.map { it -> "${originalTransLink.kArticle.title}"+transArticleintro(it) }
    l1.joinToString(separator = " ")
}



suspend fun getSZJSoupHeadlines():List<KArticle> = withContext(Dispatchers.IO) {
    fun transFigure(el: Element): KArticle {
       // Timber.d(el.html())
        val title=el.select("h3.sz-teaser__title").text().replace(".",",")//+". "
        val title2 = el.text().replace(".",",")//+". "
        val link =  el.attr("href")

       if(el.text().isEmpty()) return KArticle()
       // else return KArticle(title, link, "")
        //Timber.d("----->$title")
        return KArticle("["+el.select("h3.sz-teaser__title").text().replace(".",",")  +"]. ", link, "")

//        return KArticle(el.select("h3.sz-teaser__title").text().replace(".",",")  +". ", link, "")
    }
   /* fun transSection(el: Element): KArticle {
        val title = el.select("h3").text()//+". "
        // val desc = el.select("p").text()
        val link = el.select("h3").select("a[href]").attr("abs:href")
        return KArticle("["+title+"]. ", link, "")
    }*/

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


