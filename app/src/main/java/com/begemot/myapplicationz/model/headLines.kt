package com.begemot.myapplicationz.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.begemot.knewscommon.*
//import com.begemot.myapplicationz.App.Companion.sApp
import com.begemot.myapplicationz.AppStatus
import com.begemot.myapplicationz.KCache
import com.begemot.myapplicationz.KProvider
import com.begemot.myapplicationz.StatusApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import timber.log.Timber
import java.lang.Exception

class headLines() {
    val lHeadLines: MutableState<THeadLines> = mutableStateOf<THeadLines>(THeadLines())

    var scrollposHL = 0
    val currChapter :MutableState<Int> = mutableStateOf(0)

    val dataHeadlines: Long
        get() = this.lHeadLines.value.datal

    val listHL: List<OriginalTransLink>
        get() = this.lHeadLines.value.lhl

    fun reinicializeHeadLines() {
        lHeadLines.value = THeadLines()
        //scrollposHL = 0
        //currChapter.value = 0 //loadSelectedChapter()
    }

    fun loadSelectedChapter(sApp: StatusApp):Int{
        if(sApp::currentNewsPaper ==null) return 0
        val ii= KCache.loadFromCache("${sApp.currentNewsPaper.handler}.CCH")
        if(ii.isEmpty()){
            Timber.d("ii empty")
            return 0
        }
        else{
            val u=ii.toInt()
            Timber.d("ii not empty $u")
            return u
        }

    }

    fun storeLastSelectedChapter(sApp:StatusApp,index:Int){
        currChapter.value=index
        KCache.storeInCache("${sApp.currentNewsPaper.handler}.CCH",index.toString())
    }

    suspend fun getLines(sApp: StatusApp,np:NewsPaper)= withContext(Dispatchers.IO) {
        Timber.d("${sApp.currentNewsPaper.handler} olang  ${sApp.currentNewsPaper.olang}  trans lang ${sApp.userlang}")
        if (lHeadLines.value.lhl.isNotEmpty()) return@withContext //If they are already loaded ....
        sApp.currentStatus.value = AppStatus.Loading
        lHeadLines.value = lHeadLines.value.copy(lhl = emptyList())
        //delay(2000)
        val resp = KProvider.getHeadLines(sApp.getHeadLineParameters())
        when (resp) {
            is KResult2.Success -> {
                sApp.currentStatus.value = AppStatus.Idle
                lHeadLines.value = resp.t
                Timber.d(lHeadLines.value.lhl.print("getlines"))
                currChapter.value=loadSelectedChapter(sApp)
            }
            is KResult2.Error -> {
                Timber.d("SERVER ERROR!!!")
                sApp.currentStatus.value =
                    AppStatus.Error("getLines ERROR\n${resp.msg}", Exception(resp.msg))
            }
        }
    }

    suspend fun checkUpdates(sApp: StatusApp)=withContext(Dispatchers.IO) {
        Timber.d("enter ${sApp.status()}")
        sApp.currentStatus.value = AppStatus.Loading
        val resp = KProvider.checkUpdates2(sApp.getHeadLineParameters())
        when (resp) {
            is KResult2.Success -> {
                Timber.d("changes '${resp.t}'")
                if (resp.t.datal != 0L) {
                    lHeadLines.value = resp.t
                    sApp.vm.msg.setMsg(sApp, "New Headlines  !!")
                    storeLastSelectedChapter(sApp,0) //?? Vols dir?
                } else sApp.vm.msg.setMsg(sApp, "No new Headlines  !!")
                sApp.currentStatus.value = AppStatus.Idle
            }
            is KResult2.Error -> {
                Timber.d("Aresp error : ${resp.msg}")
                sApp.currentStatus.value =
                    AppStatus.Error("CHECK UPDATES ERROR\n${resp.msg}", Exception(resp.msg))
            }
        }
    }
}
