package com.begemot.myapplicationz.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.AppStatus
import com.begemot.myapplicationz.KProvider
import com.begemot.myapplicationz.StatusApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception

class headLines() {
    val lHeadLines: MutableState<THeadLines> = mutableStateOf<THeadLines>(THeadLines())

    val dataHeadlines: Long
        get() = this.lHeadLines.value.datal

    val listHL: List<OriginalTransLink>
        get() = this.lHeadLines.value.lhl

    fun reinicializeHeadLines() {
        lHeadLines.value = THeadLines()
    }


    suspend fun getLines(sApp: StatusApp,np:NewsPaper)= withContext(Dispatchers.IO) {
        Timber.d("${sApp.currentNewsPaper.handler} olang  ${sApp.currentNewsPaper.olang}  trans lang ${sApp.lang}")
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
