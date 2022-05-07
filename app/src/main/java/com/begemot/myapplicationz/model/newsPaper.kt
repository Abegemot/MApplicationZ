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

    suspend fun getNewsPapers2():KResult3<Unit>{
        val nr:KResult3<Unit>
        val t= measureTimeMillis {
            val np = KProvider.getNewsPapers()
            nr=when(np){
                is KResult3.Success->{
                    //statusApp.vm.setNPapers(resp.t)
                    newsPapers = np.t
                    //Timber.d("OK getNewsPapers = provider time ${np.timeInfo()} ${np.t.toString().substring(0,100)}")
                    //nr=np.toUnit()
                    KResult3.Success(Unit,"getNewsPapers2 ",np.clientTime,np.serverTime)
                }
                is KResult3.Error->{
                    Timber.e("2 resp error  : exception -> ${np.msg}")
                    sApp.currentStatus.value = AppStatus.Error("getLocalNewsPapers Error: ${np.msg}", null)
                    KResult3.Error(np.msg,"getNewsPapers2",np.clientTime,np.serverTime)
                }
            }
        }
        Timber.d("iner time ($t) ms")
        return nr
    }


    suspend fun getNewsPapers(){
        //delay(50)
        //sApp.currentStatus.value = AppStatus.Idle

        val t= measureTimeMillis {
            //val np = KProvider.getNewsPapers()
            when(val np=KProvider.getNewsPapers()){
                is KResult3.Success->{
                    //statusApp.vm.setNPapers(resp.t)
                    newsPapers = np.t
                    Timber.d("OK getNewsPapers = provider time ${np.timeInfo()} ${np.t.toString().substring(0,50)}")
                    //delay(500)
                    // sApp.currentStatus.value = AppStatus.Idle
                    //throw Exception("JRONYA QUE JRONYA")
                    //throw  Exception("CACA OF THE COW")
                }
                is KResult3.Error->{
                    Timber.e("2 resp error  : exception -> ${np.msg}")
                    sApp.currentStatus.value = AppStatus.Error("getLocalNewsPapers Error: ${np.msg}", null)
                }
            }
        }
        Timber.d("iner time ($t) ms")
        return
        /**/
//        Timber.e("end getLocalNewsPapers")
        //Timber.d("ÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑÑ")
        //delay(1000)
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
            is KResult3.Success->{
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
            is KResult3.Error->{
                sApp.setMsg2("getNewsPapersUpdates ERROR e->${nP.msg} ")
                Timber.e("resp error : msg->${nP.msg}  " )
                sApp.currentStatus.value = AppStatus.Error("checkUpdates newspapers ${nP.msg}", null)
                return false
            }
        }

    }



}

//Max 134 169