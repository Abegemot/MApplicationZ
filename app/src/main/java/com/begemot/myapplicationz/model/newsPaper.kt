package com.begemot.myapplicationz.model

import com.begemot.knewscommon.*
import com.begemot.myapplicationz.App.Companion.sApp
import com.begemot.myapplicationz.AppStatus
import com.begemot.myapplicationz.KProvider
import com.begemot.myapplicationz.StatusApp
import kotlinx.coroutines.delay
//import io.ktor.util.*
import timber.log.Timber
import java.lang.Exception
import kotlin.system.measureTimeMillis


class NewsPapers() {
    private var newsPapers = NewsPaperVersion()//    : MutableState<NewsPaperVersion> = mutableStateOf<NewsPaperVersion>(NewsPaperVersion())
    val lNewsPapers: List<NewsPaper>
        get() = this.newsPapers.newspaper
    val Npversion: Int
        get() = this.newsPapers.version
//    val iFirstVisibleItem : MutableState<Int> = mutableStateOf(0)
    var iFirstVisibleItem  = 0

    override fun toString(): String {
        //return "X version ${newsPapers.version} newspapers ${newsPapers.newspaper}"
        return "$newsPapers" //"${newsPapers.toString2()}"
    }

    suspend fun getNewsPapers2():KResult<Unit>{
        var nr:KResult<Unit> = KResult(Result.success(Unit))
        val t= measureTimeMillis {
            val np = KProvider.getNewsPapers()
            np.res.onSuccess {
                newsPapers = it
                nr=KResult(Result.success(Unit),np.srv_time,np.cli_time,"getNewsPapers2")
            }
            .onFailure {
                Timber.e("2 resp error  : exception -> ${np.logInfo()}")
                sApp.currentStatus.value = AppStatus.Error("getLocalNewsPapers Error: ${np.logInfo()}", null)
                nr=KResult<Unit>(Result.failure(it),np.srv_time,np.cli_time,"getNewsPapers2")
            }
        }
        Timber.d("iner time ($t) ms")
        return nr
    }
    suspend fun checkUpdates(sApp: StatusApp, version:Int=0): Boolean {
        val nP=KProvider.getNewsPapersUpdates(version)//sApp.vm.newsPapers.Npversion)
        if(nP.res.isSuccess){
            val NP=nP.res.getOrThrow()
            if(NP.version==0) {
                Timber.d("resp succes : No updates = ${NP}")
                return false
              }else{
                sApp.snack("News Papers Updated")
                Timber.d(" Updates  version=${NP.version}")
                newsPapers = NP
                return true
            }
        }
        else{
            sApp.setMsg2("getNewsPapersUpdates ERROR e->${nP.logInfo()} ")  //??
            sApp.snack("can't connect! no news updated")
            Timber.e("resp error : msg->${nP.logInfo()}  " )
            //sApp.currentStatus.value = AppStatus.Error("checkUpdates newspapers ${nP.logInfo()}", null)
            return false
        }
    }
}

//Max 134 169 91