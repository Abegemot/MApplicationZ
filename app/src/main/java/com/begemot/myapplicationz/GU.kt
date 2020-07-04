package com.begemot.myapplicationz

import android.os.StrictMode
import androidx.compose.MutableState
import com.begemot.myapplicationz.Screens.FullArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber
import java.io.IOException
import java.net.URLEncoder

enum class Title{
    HEADLINES,ARTICLE,NAME
}

interface INewsPaper{
    val olang:String
    suspend fun getTranslatedArticle(originalTransLink: OriginalTransLink, statusApp: StatusApp):MutableList<OriginalTrans>
    suspend fun getHeadLines(statusApp: StatusApp):List<OriginalTransLink>
    fun getlines(lhd: MutableState<MutableList<OriginalTransLink>>, statusApp: StatusApp):Unit=get_HeadLines(lhd,statusApp, ::getHeadLines)
    fun getName(e:Title):String
    fun linkToArticleScreen():(otl:OriginalTransLink)->Screens=::FullArticle
    fun linkToHeadLinesScreen():Screens=Screens.ListHeadLines
    fun getArticle(originalTransLink:OriginalTransLink, trans: MutableState<MutableList<OriginalTrans>>, statusApp: StatusApp)= get_Article(originalTransLink,trans,statusApp,::getTranslatedArticle)
}


  object Guardian:INewsPaper{
      override val olang: String
          get() = "en"

      override fun getName(e:Title): String {
          return when(e){
              Title.ARTICLE   ->"GU Article"
              Title.HEADLINES ->"GU Headlines"
              Title.NAME->"The Guardian"
          }
      }

      override suspend fun getHeadLines(statusApp: StatusApp): List<OriginalTransLink> {
          //val l=getHeadLines()
          pos1()
          val lj=emptyList<OriginalTransLink>()
          return lj
          //return translateListKArticles(l,statusApp.lang,olang )
         /* val lhd= mutableListOf<OriginalTransLink>()
          lhd.add(OriginalTransLink(KArticle("guardian1","link","desc"),"trans guardian1"))
          lhd.add(OriginalTransLink(KArticle("guardian2","link","desc"),"trans guardian2"))
          return lhd*/

      }
      suspend fun pos1(){
          val apikey="AIzaSyBP1dsYp-jPF6PfVetJWcguNLiFouZ3mjo"
          Timber.d("POS1 A TOPE")
         var sUrl="https://www.googleapis.com/language/translate/v2?key=$apikey&q="+URLEncoder.encode("the mother","utf-8")+
                 "&source=en&target=ca"

          sUrl="https://www.googleapis.com/language/translate/v2"//?key=$apikey"
          val sJ="{ \"q\" [\"Hello Kitty\", \"My tailor is rich\"], \"target\": \"de\" }"

            val s=getWebPagePOSTJS(sUrl,sJ)
          Timber.d("OOOOOHHHH--->${s.toString()}")

      }
      override suspend fun getTranslatedArticle(
          originalTransLink: OriginalTransLink,
          statusApp: StatusApp
      ): MutableList<OriginalTrans> {
          val original= getGUArticle(originalTransLink)
          val sall= translate2(original,statusApp.lang,olang)
          return sall
          /*val lhd= mutableListOf<OriginalTrans>()
          lhd.add(OriginalTrans("guardian 1 article original","trans article guardian1"))
          lhd.add(OriginalTrans("guardian 2 article original","trans article guardian2"))
          lhd.add(OriginalTrans("guardian 3 article original","trans article guardian3"))
          return lhd*/
      }
  }

suspend fun getGUArticle(originalTransLink: OriginalTransLink):String = withContext(Dispatchers.Default){
     val doc= getWebPage(originalTransLink.kArticle.link)
     val art=doc.select("p")
    //val s="${originalTransLink.kArticle.title}"
     val l1 = art.map { it -> it.text() }
    // Timber.d(l1.toString())
     l1.joinToString(separator = " ")
}

suspend fun getHeadLines(): List<KArticle> = withContext(Dispatchers.Default){
    fun trans(el: Element):KArticle{
          //val txt="["+el.text().replace(";",",").replace("?","")+"]. "
        val txt="<p> "+el.text().replace(".",",")
            .replace("?","")
            .replace("!","")
            .replace(";",",")+" «T». "
          return KArticle(el.text()+kTock,el.attr("href"))
    }
        val sURL="http://translate.google.com/translate?hl=es&sl=auto&tl=es&u=https%3A%2F%2Fwww.theguardian.com%2Finternational"
        val sURL2="https://www.theguardian.com/international"
        val doc= getWebPage(sURL)
        val u=doc.select("a.u-faux-block-link__overlay.js-headline-text")
        val l=u.map{it->trans(it)}//.subList(7,40)
        Timber.d("GET HEAD LINES GET HEADLINES")
        Timber.d(doc.html())
        l.forEach{
             Timber.d(it.title)
        }
        l

}

