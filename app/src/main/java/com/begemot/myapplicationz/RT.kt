   package com.begemot.myapplicationz

import androidx.compose.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber
import kotlin.reflect.KSuspendFunction1
   fun getRTArticle(originalTransLink: OriginalTransLink, trans: MutableState<MutableList<OriginalTrans>>, statusApp: StatusApp){
       get_Article(originalTransLink,trans,statusApp, ::getRTTranslatedArticle  )
   }




   suspend fun getRTTranslatedArticle(originalTransLink: OriginalTransLink, statusApp: StatusApp):MutableList<OriginalTrans>  {
       Timber.d("->gettranslationlink")
       statusApp.currentStatus = AppStatus.Loading
       val original = getRTJSoupArticle(originalTransLink)
       val sall = translate2(original, statusApp.lang,"ru")
       //val i=1/0
       Timber.d("<-get translated text")
       return sall
   }

   suspend fun getRTJSoupArticle(originalTransLink: OriginalTransLink) = withContext(Dispatchers.IO) {
       Timber.d("get Article")
       fun transArticleintro(el: Element): String {
           //println("el $el")
           return el.text()
       }

       val doc = Jsoup.connect(originalTransLink.kArticle.link).get()
       //println(originalTransLink.kArticle.link)
       var art = doc.select("div.article-intro")
       val l1 = art.map { it -> "${originalTransLink.kArticle.title}"+transArticleintro(it) }

       //println("l1 ${l1.size}  $l1")

       val arts = doc.select("div.article-body")
       val l2 = arts.map { it -> transArticleintro(it) }
       //println("l2 ${l2.size} $l2")
       val lt= (l1 + l2).joinToString(separator = " ")
       lt
   }

 fun getRT_Headlines(lhd: MutableState<MutableList<OriginalTransLink>>, statusApp: StatusApp){
     get_HeadLines(lhd,statusApp, ::getRTHeadLines)
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
        val title = "["+el.select("h3").text()+"]. "
        //val title="["+title1.replace('«','-')
           // .replace('»','-')
           // .replace('.',',')
           // .replace('—',' ')
           // .replace('?','?')+"]. "
        val link = el.select("a[href]").first().attr("abs:href")
        return KArticle(title, link, "")
    }
    fun transSection(el: Element): KArticle {
        val title = "["+el.select("h3").text()+"]. "
        //val title="["+title1.replace('«','-')
            //.replace('»','-')
           //.replace('.',',')
           // .replace('—',' ')
           // .replace('?','?')+"]. "

        val link = el.select("h3").select("a[href]").attr("abs:href")
        return KArticle(title, link, "")
    }

    val s = "https://russian.rt.com/inotv"
    val con= Jsoup.connect(s)
    //con.timeout(6000)
    val doc = con.get()
    var art = doc.select("figure")
    val l1 = art.map { it -> transFigure(it) }

    art = doc.select("section.block-white.materials-preview").select("article")
    val l2 = art.map { it -> transSection(it) }
    l1 + l2
    //val ls=(l1+l2).subList(0,3)
    //ls
}

