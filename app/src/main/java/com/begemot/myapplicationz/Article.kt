package com.begemot.myapplicationz

//import androidx.compose.Composable
//import androidx.compose.MutableState
import androidx.compose.foundation.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
//import androidx.compose.onCommit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.state
//import androidx.compose.state
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
/*import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Box
import androidx.ui.foundation.clickable
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxHeight
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.material.Card
import androidx.ui.unit.dp*/
import com.begemot.kclib.KText2
import com.begemot.knewscommon.OriginalTrans
import com.begemot.knewscommon.OriginalTransLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction2

@Composable
fun articleScreen(originalTransLink: OriginalTransLink, statusApp: StatusApp){
    //statusApp.currentBackScreen=backScreenFun()
    statusApp.currentBackScreen=statusApp.newsProvider.linkToHeadLinesScreen()
    val trans3 = state { mutableListOf<OriginalTrans>() }
    onCommit(statusApp.lang) {
        statusApp.newsProvider.getArticle(originalTransLink,trans3,statusApp)
       // getArticle(originalTransLink, trans3, statusApp)    //change name to getTranslatedArticle
    }
    val status=statusApp.currentStatus
    when(status){
        is AppStatus.Loading -> waiting()
        is AppStatus.Error-> displayError(status.sError,status.e)
        is AppStatus.Idle->drawArticle(originalTransLink,loriginalTranslate = trans3.value,statusApp = statusApp,olang=statusApp.newsProvider.olang)
    }
}



@Composable
fun drawArticle(originalTransLink: OriginalTransLink, loriginalTranslate:MutableList<OriginalTrans>, statusApp: StatusApp,olang:String){
    // KWindow() {
    val original= state{true}
    LazyColumnItems(items = loriginalTranslate, itemContent = {
        Card(shape= RoundedCornerShape(8.dp),elevation = 7.dp, modifier = Modifier.fillMaxHeight()+ Modifier.padding(2.dp)+ Modifier.fillMaxWidth()) {
            Column() {
                val bplaytext= state{false}
                Box(modifier= Modifier.clickable(onClick = {original.value=true; bplaytext.value=true} )) {
                    KText2(it.original,size = statusApp.fontSize)
                }
                Box(modifier= Modifier.clickable(onClick = {original.value=false; bplaytext.value=true} )) {
                    KText2(it.translated, size = statusApp.fontSize)
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
            is KResult.Succes->{trans.value=resp.t;statusApp.currentStatus = AppStatus.Idle}
            is KResult.Error->{statusApp.currentStatus=AppStatus.Error(resp.msg,resp.e)}
        }
    }
}
