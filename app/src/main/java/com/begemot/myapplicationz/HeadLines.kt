  package com.begemot.myapplicationz

import androidx.compose.*
import androidx.compose.foundation.Box
import androidx.compose.foundation.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.state
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

import com.begemot.kclib.KText2
import com.begemot.knewscommon.OriginalTransLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.reflect.KSuspendFunction1


@Composable
fun headlinesScreen(statusApp: StatusApp) {
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





fun get_HeadLines(lhd: MutableState<MutableList<OriginalTransLink>>, statusApp: StatusApp, zgetLines: KSuspendFunction1<StatusApp, List<OriginalTransLink>>) {
    Timber.d("->getLHeadlines")
    GlobalScope.launch(Dispatchers.Main) {
        statusApp.currentStatus = AppStatus.Loading
        statusApp.nItems=0
        val resp = exWithException<List<OriginalTransLink>, String> {
            //zgetLHeadLines(statusApp)
            zgetLines(statusApp)
        }
        when(resp) {
            is KResult.Succes -> {
                Timber.d("SUCCES")
                //lhd.clear()
                //lhd.addAll(resp.t)
                lhd.value=resp.t.toMutableList()  //to be amended!!
                statusApp.nItems=lhd.value.size
                statusApp.currentStatus = AppStatus.Idle
            }
            is KResult.Error -> { statusApp.currentStatus = AppStatus.Error(resp.msg, resp.e) }
        }

    }
    Timber.d("<-getLHeadlines")
}
