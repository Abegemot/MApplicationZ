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

import com.begemot.knewscommon.KResult3
import com.begemot.knewscommon.KTimer
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import timber.log.Timber
import kotlin.reflect.KSuspendFunction0
import kotlin.system.measureTimeMillis


suspend fun C(){ Timber.d("Start C"); delay(1300); throw Exception("ONDIMA") ;Timber.d("end C inside")}
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



suspend fun KLauncher(name:String,cs:CoroutineScope,afun: suspend () -> Unit){
    val j:Job
    val kt= KTimer()
    var OK=true
    j = cs.launch(CoroutineName("KlauncherA")) {
        val t= measureTimeMillis {
            Timber.d("start job $name")
 //           try {
//                Timber.d("CALLING AFUN")
                 afun()
//            } catch (e: Exception) {
//                Timber.e("Exception $e")
                OK=false
//            }
            Timber.d("end job $name computed in (${kt.getElapsed()}) ms")
        }

    }
    //j.join()  // Noooorrrr or paralelisme will be destroyed!!
    //return j
}

typealias voidfuncs = suspend () ->Unit
typealias lOfAsyncFuncs = List<suspend ()->Unit>

typealias lOfAsyncFuncs22 = List<Pair<suspend ()->Unit,String>>
val lfo2=listOf(suspend{A()}, suspend{B()}, suspend{->C()})

fun f(a:List<suspend ()->Unit>,b:String){}

val vv=listOf(Pair(::A,::A.name), Pair(::B,""))

val LFO= listOfAsyncFuncs(listOf(suspend{->A()}, suspend{->B()}, suspend{->C()}))

val lfo=listOf(suspend{->A()}, suspend{->B()}, suspend{->C()})

fun listOfAsyncFuncs(x:List<suspend ()->Unit>):String{
    return ""
}
fun toFN(a: KSuspendFunction0<Unit>) : Pair<voidfuncs,String>{
    val p=Pair(a,a.name)
    return p
}


val eHandler= CoroutineExceptionHandler{_,exception->
    Timber.e("eHandler1  $exception")
}



suspend fun Ass(lfuncs:lOfAsyncFuncs22):KResult3<Nothing> {

    Timber.d("ENTERING ASS")
    val scope = CoroutineScope( CoroutineName("ASS"))
    Timber.d("LAUNCHING ASS")
    var ls:String=""
    val t= measureTimeMillis {
        try {
            coroutineScope {
                lfuncs.forEach {
                    ls=it.second
                    KLauncher(ls, this) { it.first.invoke() }
                }
            }
        } catch (e: Exception) {

            Timber.e("I've Got You! $ls: $e")
            val d=KResult3.VoidSucces(clientTime = 0)
            return KResult3.Error("Ass error from '$ls()' = ${e}")
        }
    }
    return KResult3.VoidSucces(t)
}


fun executeListOfAsyncFuncsZ( lfuncs:lOfAsyncFuncs22):KResult3<Nothing> {
    var t = 1L
    val R:KResult3<Nothing>
     runBlocking(IO+CoroutineName("asynclistfuncs")) {

        Timber.d("begin execute list of functions")
        val a: Int=11
            t= measureTimeMillis {
                R=Ass(lfuncs)
                Timber.d("REALLYYYY GEORGE ??? $a")
             }
        Timber.d("end execution list of functions ($t) ms ass=$a")
    }
    Timber.d("a leaving executelistoffuncs")
    R.setclitime(t)
    return R
    //return Pair(t,true)
}






//Max 204, 319