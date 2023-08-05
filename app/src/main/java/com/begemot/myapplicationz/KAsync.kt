/*fun TestC() {

    //Timber.d("Start Test C")
    val kt = KTimer()
    //launch { A() }
    //launch { B() }
    //launch { C() }
    //runBlocking {
    //val sc= CoroutineScope(Default+CoroutineName("WHOLE"))
    runBlocking {
        val sc = CoroutineScope(Dispatchers.IO)

        val j = launch {
            val j1 = execZZ("A", sc) { A() }
            val j2 = execZZ("B", sc) { B() }
            val j3 = execZZ("C", sc) { C() }
            j1.await()
            j2.await()
            j3.await()
            Timber.d("End await  (${kt.getElapsed()}) ms")
        }
        Timber.d("join")
        j.join()
    }

    //j.invokeOnCompletion { Timber.d("END TEST C  (${kt.getElapsed()})ms") }

    //Timber.d("End Test C (${kt.getElapsed()})ms")

}


fun TestC2(){
    val t = measureTimeMillis {
        runBlocking() {
            Timber.d("start tc2")

            val j = launch() {
                //launch { A()}
                launch { A() }
                launch { B() }
                launch { C() }
            }
            //j.children.forEach { it.join() } //Ok
            j.join()   //Ok
        }
    }
    Timber.d("io end testC2 in ($t) ms")
}
fun TestC3(){
    val t = measureTimeMillis {
        runBlocking() {
            Timber.d("start")
            coroutineScope {
                launch { A()}
                launch { B() }
                launch { C() }
            }
        }
    }
    Timber.d("io end testC3 in ($t) ms")
}

fun TestC4(){
    val t = measureTimeMillis {
        runBlocking() {
            Timber.d("start")

            withContext(coroutineContext){}.let {
                withContext(coroutineContext) {
                    launch() { A() }
                    launch { B() }
                    launch { C() }
                }
            }
        }
    }
    //Timber.d("io end testC4 in ($t) ms")
}

fun TestC5(){
    var t=0L
    runBlocking(Dispatchers.IO) {
        t = measureTimeMillis {
            runBlocking() {
                Timber.d("start")
                KLauncher("A",this) { A() }
                KLauncher("B",this) { B() }
                KLauncher("C",this) { C() }
            }
        }
        Timber.d("io end testT5 in ($t) ms")

    }
    Timber.d("io end testT5 THAT'S ALL FOLKS in ($t) ms")
}
*/
package com.begemot.myapplicationz

import com.begemot.knewscommon.KResult
import com.begemot.knewscommon.KTimer
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import timber.log.Timber
import kotlin.reflect.KSuspendFunction0
import kotlin.system.measureTimeMillis


suspend fun C(){ Timber.d("Start C"); delay(3300); throw Exception("ONDIMA") ;Timber.d("end C inside")}
suspend fun B(){ Timber.d("Start B"); delay(370);  Timber.d("end B inside")}
suspend fun A(){ Timber.d("Start A");delay(420);  Timber.d("end A inside")}

suspend fun execZ(msg:String, sc: CoroutineScope, afun:suspend ()->Unit){
   val job1=sc.launch(Dispatchers.IO + CoroutineName(msg)) {
        val t = measureTimeMillis {
            Timber.e("START JOB $msg")
            afun()
        }
        Timber.e("END JOB $msg in ($t) ms")
    }
    job1.join()
}

suspend fun KLauncher2(name:String,cs:CoroutineScope,afun: suspend () -> Unit){
    cs.launch(CoroutineName("KlauncherA")) {
        val t= measureTimeMillis {
            afun()
        }
    }
    //j.join()  // Noooorrrr or paralelisme will be destroyed!!
}


suspend fun KLauncher(name:String,cs:CoroutineScope,afun: suspend () -> Unit){
    cs.launch(CoroutineName("KlauncherA")) {
            val t= measureTimeMillis {
                afun()
            }
        }
    //j.join()  // Noooorrrr or paralelisme will be destroyed!!
}

typealias voidfuncs  = suspend () ->Unit
typealias voidfuncs2<T> = suspend () -> KResult<T>
//typealias lOfAsyncFuncs = List<suspend ()->Unit>

typealias lOfAsyncFuncs = List<Pair<suspend ()->Unit,String>>

typealias lOfAsyncFuncs2<T>   = List<Pair<voidfuncs2<T>,String>>

//val lfo2=listOf(suspend{A()}, suspend{B()}, suspend{->C()})

//fun f(a:List<suspend ()->Unit>,b:String){}

//val vv=listOf(Pair(::A,::A.name), Pair(::B,""))

//val LFO= listOfAsyncFuncs(listOf(suspend{->A()}, suspend{->B()}, suspend{->C()}))

//val lfo=listOf(suspend{->A()}, suspend{->B()}, suspend{->C()})

/*fun listOfAsyncFuncs(x:List<suspend ()->Unit>):String{
    return ""
}
 */

fun<T> toFN2(a: KSuspendFunction0<KResult<Unit>>) : Pair<voidfuncs2<Unit>,String>{
    val p=Pair(a,a.name)
    return p
}

fun toFN(a: KSuspendFunction0<Unit>) : Pair<voidfuncs,String>{
    val p=Pair(a,a.name)
    return p
}


val eHandler= CoroutineExceptionHandler{_,exception->
    Timber.e("eHandler1  $exception")
}


/*suspend inline fun< reified T> launchAsinc(vf:voidfuncs2): KResult3<T> {
    return vf.invoke() as KResult3<T>
}*/




suspend fun<T> Ass2(lfuncs:lOfAsyncFuncs2<Unit>):List<KResult<T>> {

//    Timber.d("ENTERING ASS")
    val scope = CoroutineScope(IO+CoroutineName("ASS"))
    //KTimber("LAUNCHING ASS2")
    var x:List<KResult<Unit>> = emptyList()
    var t=0L
    val j1=scope.launch(eHandler) {
        //Timber.d(FDebug("ENTERING INNER ASS"))
 //       KTimber("ENTERING INNER ASS2",KT.LEVEL.ENTERING)
        var ls: String = ""
        val kt = KTimer()
        val r:KResult<T>
        t = measureTimeMillis {
            x = lfuncs.map { async { it.first.invoke() } }.awaitAll()

        }
//        KTimber("LEAVING INNER ASS WAITING FOR JOIN")
    }
    j1.invokeOnCompletion { throwable->
        if (throwable != null) {
            Timber.e("ASS ERROR: ${throwable}")
        }
        //KTimber("ASS FULLFITED")
    }
    val rr= measureTimeMillis {
        j1.join()
    }
    Timber.d("JOIN TIME=$rr  AWAIT TIME = ($t) ms")

    return x as List<KResult<T>>
}


suspend fun Ass(lfuncs:lOfAsyncFuncs):KResult<Unit> {

//    Timber.d("ENTERING ASS")
    val scope = CoroutineScope(IO+CoroutineName("ASS"))
    Timber.d("LAUNCHING ASS")
    var kr:KResult<Unit>
    var t=0L
    val j1=scope.launch(eHandler) {
        //Timber.d(FDebug("ENTERING INNER ASS"))
        var ls: String = ""
        val kt = KTimer()
        t = measureTimeMillis {
           // try {
                    lfuncs.forEach {
                        ls = it.second
                        KLauncher(ls, this) { it.first.invoke() }
             //   }
            //} catch (e: Exception) {
            //    KTimbere("end job $ls() I've Got You! Job failed : $e in (${kt.getElapsed()})")
            //    kr=KResult3.Error("Ass error from '$ls()' = ${e}", kt.getElapsed())
            }
        }
        Timber.d("LEAVING INNER ASS WAITING FOR JOIN")
    }
    j1.invokeOnCompletion { throwable->
        if (throwable != null) {
            Timber.e("ASS ERROR: ${throwable}")
        }
        Timber.d("ASS FULLFITED")
     }
    val rr= measureTimeMillis {
        j1.join()
    }
    Timber.d("JOIN TIME=$rr  LEAVING ASS TIME IN ASS = ($t) ms")

    return KResult(Result.success(Unit),t,0,"Ass answer")
}

suspend fun<T> executeListOfAsyncFuncs2( lfuncs:lOfAsyncFuncs2<Unit> ):KResult<Unit> {
    var t = 1L
    var rs:List<KResult<Unit>> = emptyList()
    val pJ= CoroutineScope(IO+CoroutineName("asynclistfuncs"))
    Timber.d("begin execute list of functions 1")
    val j1=pJ.launch() {
        t= measureTimeMillis {
             rs=Ass2<Unit>(lfuncs)
        }
    }
    j1.join()

    var OK=true
    rs.forEach {

        it.res.onFailure {   OK=false; Timber.e("Error KError3.msg  ${it}")}
    }
    Timber.d("end execution list of functions ($t) ms ")
    if(OK) return KResult(Result.success(Unit),t, sparams = "executelist")
    else return KResult(Result.failure(Exception("xxxxx")),t, sparams = "vols dir?")
}



suspend fun executeListOfAsyncFuncs( lfuncs:lOfAsyncFuncs):KResult<Unit> {
    var t = 1L
    var dif =1L
    lateinit var R:KResult<Unit>
    val pJ= CoroutineScope(IO+CoroutineName("asynclistfuncs"))
    Timber.d("begin execute list of functions 1")
    val j1=pJ.launch() {
        Timber.d("begin execute list of functions 2")
        t= measureTimeMillis {
            R=Ass(lfuncs)
//            Timber.d("REALLYYYY GEORGE ???  ${R.timeInfo()}")
        }
        //dif=t-R.getclitime()
    }
    j1.join()
    Timber.d("end execution list of functions ($t) ms ass time = ${R.timeInfo()} time expended in Ass = ($dif)ms msg=${R} ")
    // Timber.d("leaving executelistoffuncs ($t) ms")
    //R.setclitime(t)
    return R
}

fun execBlockingLAF(lfuncs:lOfAsyncFuncs):KResult<Unit> {
    return runBlocking {
        executeListOfAsyncFuncs(lfuncs)
    }
}


fun executeListOfAsyncFuncsZ( lfuncs:lOfAsyncFuncs):KResult<Unit> {
    var t = 1L
    val R:KResult<Unit>
     runBlocking(IO+CoroutineName("asynclistfuncs")) {
        Timber.d("begin execute list of functions")
        t= measureTimeMillis {
            R=Ass(lfuncs)
//            Timber.d("REALLYYYY GEORGE ???  ${R.timeInfo()}")
        }
        //val dif=t-R.getclitime()
        Timber.d("end execution list of functions ($t) ms ass time = ${R.timeInfo()} time expended in Ass = (dif)ms msg={R.msg()} ")
    }
   // Timber.d("leaving executelistoffuncs ($t) ms")
    //R.setclitime(t)
    return R
}






//Max 204, 319,214, 343