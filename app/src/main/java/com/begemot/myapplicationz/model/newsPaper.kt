package com.begemot.myapplicationz.model

import com.begemot.knewscommon.*
import com.begemot.myapplicationz.AppStatus
import com.begemot.myapplicationz.KProvider
import com.begemot.myapplicationz.StatusApp
//import io.ktor.util.*
import timber.log.Timber


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
        return "${newsPapers.toString2()}"
    }

    suspend fun getLocalNewsPapers(sApp: StatusApp){
        Timber.d("start getLocalNewsPapers")
        when(val np=KProvider.getLocalNewsPapers()){

            is KResult2.Success->{
                Timber.d("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
                Timber.d("2 resp succes : ${np.t.toString().substring(0,15)}")
                //statusApp.vm.setNPapers(resp.t)
                newsPapers = np.t
                Timber.d("2 size news papers: ${newsPapers.newspaper.size}")
                sApp.currentStatus.value = AppStatus.Idle
                //throw  Exception("CACA OF THE COW")
            }
            is KResult2.Error->{
                Timber.e("2 resp error  : exception -> ${np.msg}")
                sApp.currentStatus.value = AppStatus.Error("getLocalNewsPapers Error: ${np.msg}", null)
            }
        }
    }

/*    suspend fun getLocalNewsPapers2(sApp: StatusApp) {
        Timber.e("GETLOCALNEWSPAPERS")
        val resp = exWithException<NewsPaperVersion, String> {
            KProvider.getLocalNewsPapers()
        }
        when (resp) {
            is KResult.Success -> {
                //sApp.setMsg("newsPapers.getNewspapers->Ok")
                Timber.d("resp succes : ${resp.t.toString().substring(0,15)}")
                //statusApp.vm.setNPapers(resp.t)
                newsPapers = resp.t
                Timber.d("size news papers: ${newsPapers.newspaper.size}")
                sApp.currentStatus.value = AppStatus.Idle
            }
            is KResult.Error -> {
                //sApp.setMsg("newsPapers.getNewspapers->failed e->${resp.e} ")
                Timber.e(" resp error  : exception -> ${resp.e}")
                sApp.currentStatus.value = AppStatus.Error("getLocalNewsPapers Error: ${resp.e}", resp.e)
               // throw(Exception("exc getNewsPapers  ${resp.msg}"))
            }
        }
    }
*/

    //@KtorExperimentalAPI
/*    suspend fun checkUpdates2(sApp: StatusApp): Boolean {
        val resp = exWithException<NewsPaperVersion, String> {
            Timber.d("looking for News Papers Update with currver=${sApp.vm.newsPapers.Npversion}")
            //KProvider.checkImages(sApp.vm.newsPapers.lNewsPapers)
            KProvider.getNewsPapersUpdates(sApp.vm.newsPapers.Npversion)
        }
        when (resp) {
            is KResult.Success -> {
                //sApp.setMsg2("getNewsPapersUpdates OK")

                //sApp.currentStatus.value = AppStatus.Idle
                if (resp.t.version == 0) {
                    Timber.d("resp succes : No updates = ${resp.t}")
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
    }*/

    suspend fun checkUpdates(sApp: StatusApp, version:Int=0): Boolean {
        val nP=KProvider.getNewsPapersUpdates(version)//sApp.vm.newsPapers.Npversion)
        when(nP){
            is KResult2.Success->{
                if(nP.t.version==0) {
                    Timber.d("resp succes : No updates = ${nP.t}")
                    return false
                }else{
                    sApp.setMsg("News Papers Update")
                    Timber.d(" Updates  version=${nP.t.version}")
                    newsPapers = nP.t
                    return true
                }
            }
            is KResult2.Error->{
                sApp.setMsg2("getNewsPapersUpdates ERROR e->${nP.msg} ")
                Timber.e("resp error : msg->${nP.msg}  " )
                sApp.currentStatus.value = AppStatus.Error("checkUpdates newspapers ${nP.msg}", null)
                return false
            }
        }

    }



}
