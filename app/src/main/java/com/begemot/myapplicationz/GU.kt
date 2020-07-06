package com.begemot.myapplicationz

import androidx.compose.MutableState
import com.begemot.myapplicationz.Screens.FullArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Title{
    HEADLINES,ARTICLE,NAME
}

interface INewsPaper{
    val olang:String
    val nameFile:String
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
      override val nameFile: String
          get() = "guardian.ks"

      override fun getName(e:Title): String {
          return when(e){
              Title.ARTICLE   ->"GU Article"
              Title.HEADLINES ->"GU Headlines"
              Title.NAME->"The Guardian"
          }
      }



      override suspend fun getHeadLines(statusApp: StatusApp): List<OriginalTransLink> {
         /* val l=getHeadLines()
          val sl=l.subList(1,4)
          return  getTranslatedHeadlines(sl, olang,statusApp.lang)

          */
          //return fakeLoadHeadLines()
          //return KProvider.getHL(nameFile,::fakeLoadHeadLines)
          return KProvider.getHL(nameFile,olang,statusApp.lang,::getHeadLinesL)

         /* val olt=KCache.findInCache(App.lcontext,nameFile)
          if(olt.isNotEmpty()) return olt
          val nolt= fakeLoadHeadLines()
          KCache.storeInCache(App.lcontext,nameFile,nolt)
          return nolt*/
      }

     suspend fun fakeLoadHeadLines():List<OriginalTransLink>{
          val otl= mutableListOf<OriginalTransLink>()
          otl.add(OriginalTransLink(KArticle("ZZtitle 1","link 1","desc1"),"translated 1"))
          otl.add(OriginalTransLink(KArticle("QQtitle 2","link 2","desc2"),"translated 2"))
          return otl
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

suspend fun getHeadLinesL(): List<KArticle> = withContext(Dispatchers.Default){
    val sURL="https://www.theguardian.com/international"
    val doc= getWebPage(sURL)
    val u=doc.select("a.u-faux-block-link__overlay.js-headline-text")
    return@withContext u.map{ it->KArticle(it.text(),it.attr("href"))}//.subList(7,40)
}

