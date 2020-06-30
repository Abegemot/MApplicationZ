package com.begemot.myapplicationz

import androidx.compose.MutableState
import com.begemot.myapplicationz.Screens.FullArticle

enum class Title{
    HEADLINES,ARTICLE,NAME
}

interface INewsPaper{
    val olang:String
    suspend fun getTranslatedArticle(originalTransLink: OriginalTransLink, statusApp: StatusApp):MutableList<OriginalTrans>
    suspend fun getHeadLines(statusApp: StatusApp):MutableList<OriginalTransLink>
    fun getlines(lhd: MutableState<MutableList<OriginalTransLink>>, statusApp: StatusApp):Unit=get_HeadLines(lhd,statusApp, ::getHeadLines)
    fun getName(e:Title):String
    fun linkToArticleScreen():(otl:OriginalTransLink)->Screens=::FullArticle
    fun linkToHeadLinesScreen():Screens=Screens.ListHeadLines
    fun getArticle(originalTransLink:OriginalTransLink, trans: MutableState<MutableList<OriginalTrans>>, statusApp: StatusApp)= get_Article(originalTransLink,trans,statusApp,::getTranslatedArticle)
}


//sealed class Screens2:INewsPaper

  object Guardian:INewsPaper{
      override val olang: String
          get() = "en"

    override suspend fun getTranslatedArticle(
        originalTransLink: OriginalTransLink,
        statusApp: StatusApp
    ): MutableList<OriginalTrans> {
          val lhd= mutableListOf<OriginalTrans>()
          lhd.add(OriginalTrans("guardian 1 article original","trans article guardian1"))
          lhd.add(OriginalTrans("guardian 2 article original","trans article guardian2"))
          lhd.add(OriginalTrans("guardian 3 article original","trans article guardian3"))
          return lhd
    }

    override suspend fun getHeadLines(statusApp: StatusApp): MutableList<OriginalTransLink> {
        val lhd= mutableListOf<OriginalTransLink>()
        lhd.add(OriginalTransLink(KArticle("guardian1","link","desc"),"trans guardian1"))
        lhd.add(OriginalTransLink(KArticle("guardian2","link","desc"),"trans guardian2"))
        return lhd

    }

    override fun getName(e:Title): String {
         return when(e){
              Title.ARTICLE   ->"GU Article"
              Title.HEADLINES ->"GU Headlines"
              Title.NAME->"The Guardian"
          }
      }


  }



