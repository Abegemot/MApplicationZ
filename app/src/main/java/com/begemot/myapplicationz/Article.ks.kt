package com.begemot.myapplicationz

import androidx.compose.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction2

fun get_Article(
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
