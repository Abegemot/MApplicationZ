package com.begemot.myapplicationz.model

import com.begemot.knewscommon.*
import com.begemot.myapplicationz.AppStatus
import com.begemot.myapplicationz.KProvider
import com.begemot.myapplicationz.StatusApp
//import io.ktor.util.*
import timber.log.Timber


class newsPapers() {
    private var newsPapers = NewsPaperVersion()//    : MutableState<NewsPaperVersion> = mutableStateOf<NewsPaperVersion>(NewsPaperVersion())
    val lNewsPapers: List<NewsPaper>
        get() = this.newsPapers.newspaper
    val Npversion: Int
        get() = this.newsPapers.version

    override fun toString(): String {


        //return "X version ${newsPapers.version} newspapers ${newsPapers.newspaper}"
        return "${newsPapers.toString2()}"
    }

    suspend fun getLocalNewsPapers(sApp: StatusApp) {
        val resp = exWithException<NewsPaperVersion, String> {
            KProvider.getLocalNewsPapers()
        }
        when (resp) {
            is KResult.Success -> {
                //sApp.setMsg("newsPapers.getNewspapers->Ok")
                Timber.d("resp succes : ${resp.t.toString().substring(0,15)}")
                //statusApp.vm.setNPapers(resp.t)
                newsPapers = resp.t
                sApp.currentStatus.value = AppStatus.Idle
            }
            is KResult.Error -> {
                //sApp.setMsg("newsPapers.getNewspapers->failed e->${resp.e} ")
                Timber.d("resp error : msg->${resp.msg}   e->${resp.e}")
                sApp.currentStatus.value = AppStatus.Error("getnewspapers", resp.e)
               // throw(Exception("exc getNewsPapers  ${resp.msg}"))
            }
        }
    }


    //@KtorExperimentalAPI
    suspend fun checkUpdates(sApp: StatusApp): Boolean {
        val resp = exWithException<NewsPaperVersion, String> {
            Timber.d("looking for News Papers Update with currver=${sApp.vm.newsPapers.Npversion}")
            //KProvider.checkImages(sApp.vm.newsPapers.lNewsPapers)
            KProvider.getNewsPapersUpdates(sApp.vm.newsPapers.Npversion)
        }
        when (resp) {
            is KResult.Success -> {
                Timber.d("resp succes : updates = ${resp.t}")
                //sApp.setMsg2("getNewsPapersUpdates OK")

                //sApp.currentStatus.value = AppStatus.Idle
                if (resp.t.version == 0) {
                    //sApp.setMsg2("No News Papers Update")
                    //sApp.setMsg("No News Papers Update")
                    return false
                }
                else {
                    //sApp.setMsg2("News Papers Update")
                    sApp.setMsg("News Papers Update")
                    newsPapers = resp.t
                    return true
                }
            }
            is KResult.Error -> {
                sApp.setMsg2("getNewsPapersUpdates ERROR e->${resp.e} ")
                Timber.d("resp error : msg->${resp.msg}   e->${resp.e}")
                sApp.currentStatus.value = AppStatus.Error("checkUpdates newspapers", resp.e)
            }
        }
        return false
    }

}
