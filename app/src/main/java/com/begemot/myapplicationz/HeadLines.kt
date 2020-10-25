  package com.begemot.myapplicationz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Box
import androidx.compose.foundation.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

import com.begemot.kclib.KText2
import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.KResult
import com.begemot.knewscommon.OriginalTransLink
import com.begemot.knewscommon.THeadLines
import com.begemot.knewscommon.exWithException
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.reflect.KSuspendFunction1


@Composable
fun headlinesScreen(statusApp: StatusApp) {
      statusApp.currentBackScreen=Screens.ListNewsPapers
      Timber.d("->headlines screen Composable")
      val lHeadlines = remember{ mutableStateOf<List<OriginalTransLink>>(emptyList()) }
    LaunchedTask(statusApp.lang) {
        lHeadlines.value = getLines(statusApp).lhl
    }
    Timber.d("headlines size ${lHeadlines.value.size} status app   ${statusApp.currentStatus}")
      val status=statusApp.currentStatus
      when(status){
          is AppStatus.Loading  -> waiting()
          is AppStatus.Error    -> displayError(status.sError,status.e,statusApp)
          is AppStatus.Idle     -> draw_Headlines(loriginalTransLink =lHeadlines.value , statusApp =statusApp,olang=statusApp.currentNewsPaper.olang)
      }
      Timber.d("<-headlines screen ${status.toString()}")
}

@KtorExperimentalAPI
suspend fun getLines(statusApp: StatusApp):THeadLines{
      Timber.d("getLines ${statusApp.currentNewsPaper.handler} olang  ${statusApp.currentNewsPaper.olang}  trans lang ${statusApp.lang}")
      //l.clear()
      val resp= exWithException<THeadLines,String> {
          //l.clear()
          statusApp.currentStatus=AppStatus.Loading
          KProvider2.getHeadLines(statusApp)
          //KNews().getHeadLines(namepaper = statusApp.currentNewsPaper.handler,statusApp.lang)
      }
      when(resp){
          is KResult.Success->{
              Timber.d("KProvider2.getHeadLines Success! Size resp ${resp.t.lhl.size}")
              statusApp.currentStatus = AppStatus.Idle
              statusApp.nItems=resp.t.lhl.size
              statusApp.dataHeadlines=resp.t.datal
              return resp.t
          }
          is KResult.Error->{statusApp.currentStatus=AppStatus.Error(resp.msg,resp.e)}
      }
      return THeadLines()
}


@Composable
fun draw_Headlines(
    loriginalTransLink: List<OriginalTransLink>,
    statusApp: StatusApp,
    olang:String
) {
    val original= state{true}
    LazyColumnFor(items = loriginalTransLink, itemContent = {
        Card(shape = RoundedCornerShape(8.dp),elevation = 7.dp, modifier = Modifier.padding(2.dp))  {
            Column() {
                val bplaytext= state{false}
                Box(modifier = Modifier.clickable(onClick ={original.value=true; bplaytext.value=true  } )){
                    KText2(txt = it.kArticle.title, size = statusApp.fontSize.value)
                }
                if(it.translated.length>0){
                Box(Modifier.clickable(onClick = {original.value=false; bplaytext.value=true  }) ){
                    KText2(txt = it.translated, size = statusApp.fontSize.value)
                }
                 }
                if(it.kArticle.link.isNotEmpty()){
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillParentMaxWidth()) {
                    Icon(
                        vectorResource(id = R.drawable.ic_link_24px),modifier = Modifier.padding(0.dp,0.dp,15.dp,3.dp).clickable(
                            onClick = { statusApp.currentScreen = Screens.FullArticle(it) }
                        ))
                }}
                if(bplaytext.value){
                    if(original.value) playText(bplaytext,it.kArticle.title,statusApp,olang)
                    else   playText(bplaytext,it.translated,statusApp,statusApp.lang)
                }
            }
        }
    })
}

/*
fun get_HeadLines(lhd: MutableState<MutableList<OriginalTransLink>>, statusApp: StatusApp, zgetLines: KSuspendFunction1<StatusApp, THeadLines>) {
    Timber.d("->getLHeadlines")
    GlobalScope.launch(Dispatchers.Main) {
        statusApp.currentStatus = AppStatus.Loading
        statusApp.nItems=0
        val resp = exWithException<THeadLines, String> {
            //zgetLHeadLines(statusApp)
            zgetLines(statusApp)
        }
        when(resp) {
            is KResult.Success -> {
                Timber.d("SUCCES")
                //lhd.clear()
                //lhd.addAll(resp.t)
                lhd.value=resp.t.lhl.toMutableList()  //to be amended!!
                statusApp.nItems=lhd.value.size
                statusApp.currentStatus = AppStatus.Idle
            }
            is KResult.Error -> { statusApp.currentStatus = AppStatus.Error(resp.msg, resp.e) }
        }

    }
    Timber.d("<-getLHeadlines")
}*/
