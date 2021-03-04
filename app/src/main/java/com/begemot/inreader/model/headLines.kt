package com.begemot.inreader.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.begemot.inreader.AppStatus
import com.begemot.inreader.KProvider
import com.begemot.inreader.StatusApp
import com.begemot.knewscommon.KResult
import com.begemot.knewscommon.OriginalTransLink
import com.begemot.knewscommon.THeadLines
import com.begemot.knewscommon.exWithException
import kotlinx.coroutines.delay
import timber.log.Timber

class headLines() {
    var lHeadLines: MutableState<THeadLines> = mutableStateOf<THeadLines>(THeadLines())

    val dataHeadlines: Long
        get() = this.lHeadLines.value.datal

    val listHL: List<OriginalTransLink>
        get() = this.lHeadLines.value.lhl

    fun reinicializeHeadLines(){
        lHeadLines.value = THeadLines()
    }


    suspend fun getLines(sApp: StatusApp) {
        Timber.d("${sApp.currentNewsPaper.handler} olang  ${sApp.currentNewsPaper.olang}  trans lang ${sApp.lang}")
        if (lHeadLines.value.lhl.isNotEmpty()) return //If they are already loaded ....
        val resp = exWithException<THeadLines, String> {

            sApp.currentStatus.value = AppStatus.Loading
            // delay(3000 )
            lHeadLines.value = lHeadLines.value.copy(lhl = emptyList())
            KProvider.getHeadLines(sApp.getHeadLineParameters())
        }
        when (resp) {
            is KResult.Success -> {
                //Timber.d("KProvider2.getHeadLines Success! Size resp ${resp.t.lhl.size}")
                sApp.currentStatus.value = AppStatus.Idle
                //Timber.d("resp t ${resp.t}")
                lHeadLines.value=resp.t
                //sApp.vm.setTHL(resp.t)

                //statusApp.nItems=lHeadLines.value.lhl.size
                // statusApp.dataHeadlines=lHeadLines.value.datal
                //return resp.t
            }
            is KResult.Error -> {
                Timber.d("SERVER ERROR!!!")
                //statusApp.currentStatus.value = AppStatus.Error("SERVER ERROR -> ${resp.msg}", resp.e)
                sApp.currentStatus.value = AppStatus.Error("getLines ERROR", resp.e)
            }
        }
    }

    suspend fun checkUpdates(sApp: StatusApp) {
        Timber.d("enter ${sApp.status()}")
        val resp = exWithException<THeadLines, String> {
            // val scope= CoroutineScope(Job()+Dispatchers.IO)
            //scope.launch {
            sApp.currentStatus.value = AppStatus.Loading
            KProvider.checkUpdates2(sApp.getHeadLineParameters())
            //}
        }
        when (resp) {
            is KResult.Success -> {
                Timber.d("changes ${resp.t}")
                if (resp.t.datal != 0L) {
                    lHeadLines.value=resp.t
                    //sApp.vm.setTHL(resp.t)
                    sApp.vm.msg.setMsg(sApp, "New Headlines  !!")
                } else sApp.vm.msg.setMsg(sApp, "No new Headlines  !!")
                sApp.currentStatus.value = AppStatus.Idle

            }
            is KResult.Error -> {
                Timber.d("resp error : ${resp.msg}")
                sApp.currentStatus.value = AppStatus.Error(resp.msg, resp.e)
            }
        }

    }
}
