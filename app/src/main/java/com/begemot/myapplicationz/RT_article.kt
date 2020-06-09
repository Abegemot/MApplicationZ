package com.begemot.myapplicationz

import androidx.compose.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber

fun getTranslatedArticle(originalTransLink: OriginalTransLink, trans: MutableState<MutableList<OriginalTrans>>, statusApp: StatusApp){
    GlobalScope.launch (Dispatchers.Main){
        val resp= exWithException<MutableList<OriginalTrans>,String> {
            getTranslatedArticle2(originalTransLink ,statusApp )
        }
        when(resp){
            is KResult.Succes->{trans.value=resp.t;statusApp.currentStatus = AppStatus.Idle}
            is KResult.Error->{statusApp.currentStatus=AppStatus.Error(resp.msg,resp.e)}
        }
    }
}
suspend fun getTranslatedArticle2(originalTransLink: OriginalTransLink, statusApp: StatusApp):MutableList<OriginalTrans>  {
    Timber.d("->gettranslationlink")
    statusApp.currentStatus = AppStatus.Loading
    val original = getArticle(originalTransLink)
    val sall = translate2(original, statusApp.lang,"ru")
   // val i=1/0
    Timber.d("<-get translated text")
    return sall
}

/*fun oldgetTranslatedArticle(originalTransLink: OriginalTransLink, trans: MutableState<MutableList<OriginalTrans>>, statusApp: StatusApp) {
    Timber.d("->gettranslationlink")
    statusApp.currentStatus = AppStatus.Loading
    GlobalScope.launch(Dispatchers.Main) {
        val original = getArticle(originalTransLink)
        //  println("original : $original")
        //val sall = gettranslatedText(original, statusApp.lang)
        val sall = translate(original, statusApp.lang)
        Timber.d("<-get translated text")
        when(sall){
            is ResultTranslation.ResultList->{trans.value=sall.Lorigtrans; statusApp.currentStatus = AppStatus.Idle }
            is ResultTranslation.Error->{statusApp.currentStatus=AppStatus.Error(sall.sError,null)}
        }
        //trans.value = sall
        //statusApp.currentStatus = AppStatus.Idle()
    }
}*/

suspend fun getArticle(originalTransLink: OriginalTransLink) = withContext(Dispatchers.IO) {
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
    val lt= (l1 + l2).joinToString()
    lt
}

