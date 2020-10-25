 package com.begemot.myapplicationz

import androidx.compose.foundation.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.begemot.kclib.KText2
import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.KResult
import com.begemot.knewscommon.OriginalTrans
import com.begemot.knewscommon.OriginalTransLink
import com.begemot.knewscommon.exWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.reflect.KSuspendFunction2

 @Composable
fun articleScreen(originalTransLink: OriginalTransLink, statusApp: StatusApp){
    //statusApp.currentBackScreen=backScreenFun()
    Timber.d("${originalTransLink.kArticle.link}")
    statusApp.currentBackScreen=Screens.ListHeadLines
    val trans3 = remember { mutableStateOf<List<OriginalTrans>>(emptyList()) }
    launchInComposition(statusApp.lang){
        trans3.value=getTransArt(statusApp,originalTransLink)
    }

    val status=statusApp.currentStatus
    when(status){
        is AppStatus.Loading -> waiting()
        is AppStatus.Error-> displayError(status.sError,status.e,statusApp)
        is AppStatus.Idle->drawArticle(originalTransLink,loriginalTranslate = trans3.value,statusApp = statusApp,olang=statusApp.currentNewsPaper.olang)
    }
}

suspend fun getTransArt(statusApp: StatusApp,otl:OriginalTransLink):List<OriginalTrans>{
    Timber.d("getTransArt ${statusApp.currentNewsPaper.handler}  ${statusApp.lang}")
    val resp= exWithException<List<OriginalTrans>,String> {
        statusApp.currentStatus=AppStatus.Loading
        KNews().getArticle(statusApp.currentNewsPaper.handler,statusApp.lang,otl.kArticle.link)
    }
    when(resp){
        is KResult.Success->{statusApp.currentStatus=AppStatus.Idle; return resp.t}
        is KResult.Error->{statusApp.currentStatus=AppStatus.Error(resp.msg,resp.e)}
    }
    //statusApp.currentStatus=AppStatus.Loading
    //val ls=KNews().getArticle(statusApp.currentNewsPaper.handler,statusApp.lang,otl.kArticle.link)
    //statusApp.currentStatus=AppStatus.Idle
    return emptyList()
}


@Composable
fun drawArticle(originalTransLink: OriginalTransLink, loriginalTranslate:List<OriginalTrans>, statusApp: StatusApp,olang:String){
    // KWindow() {
    val original= state{true}
    LazyColumnFor(items = loriginalTranslate, itemContent = {
        Card(shape= RoundedCornerShape(8.dp),elevation = 7.dp, modifier = Modifier.padding(2.dp).fillParentMaxWidth()) {
            Column() {
                val bplaytext= state{false}
                Box(modifier= Modifier.clickable(onClick = {original.value=true; bplaytext.value=true} )) {
                    KText2(it.original,size = statusApp.fontSize.value)
                }
                Box(modifier= Modifier.clickable(onClick = {original.value=false; bplaytext.value=true} )) {
                    KText2(it.translated, size = statusApp.fontSize.value)
                }
                if(bplaytext.value){
                    if(original.value) playText(bplaytext,it.original,statusApp,olang)
                    else playText(bplaytext,it.translated,statusApp,statusApp.lang  )
                }
            }
        }
    })
    //   }
}
fun   get_Article(
    originalTransLink: OriginalTransLink, trans: MutableState<MutableList<OriginalTrans>>, statusApp: StatusApp,
    gettransArticle: KSuspendFunction2<OriginalTransLink, StatusApp, MutableList<OriginalTrans>>
){
    GlobalScope.launch (Dispatchers.Main){
        val resp= exWithException<MutableList<OriginalTrans>,String> {
            //getRTTranslatedArticle(originalTransLink ,statusApp )
            gettransArticle(originalTransLink,statusApp)
        }
        when(resp){
            is KResult.Success->{trans.value=resp.t;statusApp.currentStatus = AppStatus.Idle}
            is KResult.Error->{statusApp.currentStatus=AppStatus.Error(resp.msg,resp.e)}
        }
    }
}
