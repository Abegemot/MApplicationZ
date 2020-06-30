  package com.begemot.myapplicationz

import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Icon
import androidx.ui.foundation.clickable
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.*
import androidx.ui.material.Card
import androidx.ui.res.vectorResource
import androidx.ui.unit.dp
import com.begemot.kclib.KText2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.reflect.KSuspendFunction1

@Composable
fun headlinesScreen(statusApp: StatusApp,afun:(otl:OriginalTransLink)->Screens,getlines:(lhd:MutableState<MutableList<OriginalTransLink>>, statusApp: StatusApp)->Unit,olang:String) {
    statusApp.currentBackScreen=Screens.ListNewsPapers
    Timber.d("->headlines screen")
    val lHeadlines = state{ mutableListOf<OriginalTransLink>()}
   // remember{ modelListOf<OriginalTransLink> }
    //val lHeadlines = remember( getlines(mutableListOf<OriginalTransLink>,statusApp))
    onCommit(statusApp.lang){
        Timber.d("on commit  headlines screen  ${lHeadlines.value.size}")
        //if(lHeadlines.value.size==0)
        //getRT_Headlines(lHeadlines.value,statusApp)
        getlines(lHeadlines,statusApp)
        Timber.d("after commit  headlines screen")
    }
    Timber.d("headlines size ${lHeadlines.value.size}")
    val status=statusApp.currentStatus
    when(status){
        is AppStatus.Loading  -> waiting()
        is AppStatus.Error    -> displayError(status.sError,status.e)
        is AppStatus.Idle     -> draw_Headlines(loriginalTransLink =lHeadlines.value , statusApp =statusApp,afun=afun,olang=olang)
    }
    Timber.d("<-headlines screen ${status.toString()}")
}


  @Composable
  fun headlinesScreen2(statusApp: StatusApp) {
      statusApp.currentBackScreen=Screens.ListNewsPapers
      Timber.d("->headlines screen")
      val lHeadlines = state{ mutableListOf<OriginalTransLink>()}
      // remember{ modelListOf<OriginalTransLink> }
      //val lHeadlines = remember( getlines(mutableListOf<OriginalTransLink>,statusApp))
      onCommit(statusApp.lang){
          Timber.d("on commit  headlines screen  ${lHeadlines.value.size}")
          //if(lHeadlines.value.size==0)
          //getRT_Headlines(lHeadlines.value,statusApp)
          statusApp.newsProvider.getlines(lHeadlines,statusApp )
          //getlines(lHeadlines,statusApp)
          Timber.d("after commit  headlines screen")
      }
      Timber.d("headlines size ${lHeadlines.value.size}")
      val status=statusApp.currentStatus
      when(status){
          is AppStatus.Loading  -> waiting()
          is AppStatus.Error    -> displayError(status.sError,status.e)
          is AppStatus.Idle     -> draw_Headlines(loriginalTransLink =lHeadlines.value , statusApp =statusApp,afun=statusApp.newsProvider.linkToArticleScreen(),olang=statusApp.newsProvider.olang)
      }
      Timber.d("<-headlines screen ${status.toString()}")
  }



@Composable
fun draw_Headlines(
    loriginalTransLink: MutableList<OriginalTransLink>,
    statusApp: StatusApp,
    afun:(otl:OriginalTransLink)->Screens,
    olang:String

) {
    val original= state{true}
    LazyColumnItems(items = loriginalTransLink, itemContent = {
        Card(shape = RoundedCornerShape(8.dp),elevation = 7.dp, modifier = Modifier.fillMaxHeight().padding(2.dp) )  {
            Column() {
                val bplaytext= state{false}
                Box(modifier = Modifier.clickable(onClick ={original.value=true; bplaytext.value=true  } )){
                    KText2(txt = it.kArticle.title, size = statusApp.fontSize)
                }
                Box(Modifier.clickable(onClick = {original.value=false; bplaytext.value=true  }) ){
                    KText2(txt = it.translated, size = statusApp.fontSize)
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        vectorResource(id = R.drawable.ic_link_24px),modifier = Modifier.padding(0.dp,0.dp,15.dp,3.dp).clickable(
                            onClick = { statusApp.currentScreen = afun(it) }
                        ))
                }
                if(bplaytext.value){
                    if(original.value) playText(bplaytext,it.kArticle.title,statusApp,olang)
                    else   playText(bplaytext,it.translated,statusApp,statusApp.lang)
                }
            }
        }
    })
}





fun get_HeadLines(lhd: MutableState<MutableList<OriginalTransLink>>, statusApp: StatusApp, zgetLines: KSuspendFunction1<StatusApp, MutableList<OriginalTransLink>>) {
    Timber.d("->getLHeadlines")
    GlobalScope.launch(Dispatchers.Main) {
        statusApp.currentStatus = AppStatus.Loading
        statusApp.nItems=0
        val resp = exWithException<MutableList<OriginalTransLink>, String> {
            //zgetLHeadLines(statusApp)
            zgetLines(statusApp)
        }
        when(resp) {
            is KResult.Succes -> {
                Timber.d("SUCCES")
                //lhd.clear()
                //lhd.addAll(resp.t)
                lhd.value=resp.t
                statusApp.nItems=lhd.value.size
                statusApp.currentStatus = AppStatus.Idle
            }
            is KResult.Error -> { statusApp.currentStatus = AppStatus.Error(resp.msg, resp.e) }
        }

    }
    Timber.d("<-getLHeadlines")
}
