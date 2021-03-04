package com.begemot.inreader.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.begemot.inreader.AppStatus
import com.begemot.inreader.KProvider
import com.begemot.inreader.StatusApp
import com.begemot.knewscommon.KResult
import com.begemot.knewscommon.NewsPaper
import com.begemot.knewscommon.NewsPaperVersion
import com.begemot.knewscommon.exWithException
import io.ktor.util.*
import timber.log.Timber


class newsPapers() {
    private var newsPapers=NewsPaperVersion()//    : MutableState<NewsPaperVersion> = mutableStateOf<NewsPaperVersion>(NewsPaperVersion())
    val lNewsPapers: List<NewsPaper>
        get() = this.newsPapers.newspaper
    val Npversion: Int
        get() = this.newsPapers.version

    //val lHeadLines: MutableState<THeadLines> = mutableStateOf<THeadLines>(THeadLines())
    fun getNewsPapers(statusApp: StatusApp) {
        val resp = exWithException<NewsPaperVersion, String> {
            KProvider.getNewsPapers()
        }
        when (resp) {
            is KResult.Success -> {
                Timber.d("resp succes : ${resp.t}")
                //statusApp.vm.setNPapers(resp.t)
                newsPapers = resp.t
                statusApp.currentStatus.value = AppStatus.Idle
            }
            is KResult.Error -> {
                Timber.d("resp error : msg->${resp.msg}   e->${resp.e}")
                statusApp.currentStatus.value = AppStatus.Error("getnewspapers", resp.e)
                throw(Exception("exc getNewsPapers  ${resp.msg}"))
            }
        }
    }


    @KtorExperimentalAPI
    suspend fun checkUpdates(statusApp: StatusApp): Boolean {
        val resp = exWithException<NewsPaperVersion, String> {
            KProvider.getNewsPapersUpdates(statusApp.vm.newsPapers.Npversion)
        }
        when (resp) {
            is KResult.Success -> {
                Timber.d("resp succes : updates = ${resp.t}")
                statusApp.currentStatus.value = AppStatus.Idle
                return if (resp.t.version == 0) false else {
                    newsPapers = resp.t
                    true
                }
            }
            is KResult.Error -> {
                Timber.d("resp error : msg->${resp.msg}   e->${resp.e}")
                statusApp.currentStatus.value = AppStatus.Error("checkUpdates newspapers", resp.e)
            }
        }
        return false
    }
}
