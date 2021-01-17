package com.begemot.myapplicationz


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

import com.begemot.knewscommon.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import io.ktor.util.*

class VM : ViewModel() {
    private val newsPapers: newsPapers = newsPapers()
    val lNewsPapers: List<NewsPaper>
        get() = this.newsPapers.newsPapers.value.newspaper
    val Npversion: Int
        get() = this.newsPapers.newsPapers.value.version

    fun setNPapers(npv: NewsPaperVersion) {
        newsPapers.newsPapers.value = npv
    }

    @KtorExperimentalAPI
    suspend fun checkNPUpdates(sApp: StatusApp): Boolean {
        return newsPapers.checkUpdates(sApp)
    }

    fun getNewsPapers(sApp: StatusApp) {
        newsPapers.getNewsPapers(sApp)
    }


    private val headLines: headLines = headLines()
    val dataHeadlines: Long
        get() = this.headLines.lHeadLines.value.datal

    val listHL: List<OriginalTransLink>
        get() = this.headLines.lHeadLines.value.lhl


    val msg = mesages()
    var scrollposHL = 0

    fun setTHL(thl: THeadLines) {
        headLines.lHeadLines.value = thl
    }

    suspend fun getLines(sApp: StatusApp) {
        headLines.getLines(sApp)
    }

    suspend fun checkHeadLinesUpdates(sApp: StatusApp) {
        headLines.checkUpdates(sApp)
    }

    fun reinicializeHeadLines() {
        setTHL(THeadLines())
    }

    val tCache = TransCache()

}


class mesages() {
    private val _message = MutableStateFlow<String>("ll")
    val mesage: StateFlow<String> get() = _message
    fun setMsg(sApp: StatusApp, sAux: String) {
        Timber.d("setMsg!! $sAux")
        sApp.visibleInfoBar = true //¿?
        _message.value = sAux
    }
}


class headLines() {
    val lHeadLines: MutableState<THeadLines> = mutableStateOf<THeadLines>(THeadLines())

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
                sApp.vm.setTHL(resp.t)

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
                    sApp.vm.setTHL(resp.t)
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

class newsPapers() {
    //val newsPapers: MutableState<List<NewsPaper>> = mutableStateOf<List<NewsPaper>>(emptyList())
    val newsPapers: MutableState<NewsPaperVersion> =
        mutableStateOf<NewsPaperVersion>(NewsPaperVersion())

    //val lHeadLines: MutableState<THeadLines> = mutableStateOf<THeadLines>(THeadLines())
    fun getNewsPapers(statusApp: StatusApp) {
        val resp = exWithException<NewsPaperVersion, String> {
            KProvider.getNewsPapers()
        }
        when (resp) {
            is KResult.Success -> {
                Timber.d("resp succes : ${resp.t}")
                statusApp.vm.setNPapers(resp.t)
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
            KProvider.getNewsPapersUpdates(statusApp.vm.Npversion)
        }
        when (resp) {
            is KResult.Success -> {
                Timber.d("resp succes : updates = ${resp.t}")
                statusApp.currentStatus.value = AppStatus.Idle

                return if (resp.t.version == 0) false else {
                    statusApp.vm.setNPapers(resp.t)
                    true
                }
            }
            is KResult.Error -> {
                Timber.d("resp error : msg->${resp.msg}   e->${resp.e}")
                statusApp.currentStatus.value = AppStatus.Error("checkUpdates newspapers", resp.e)
                // throw(Exception("exc getNewsPapersUpdates  ${resp.msg}"))

            }
        }
        return false
    }
}

open class  TText{
    open fun getText():String{
        return ""
    }
    fun getPinYin():Pinyin{
        return Pinyin()
    }
}

sealed class TransClass:TText(){

    class WithPinYin(val lPy: ListPinyin):TransClass(){
        override fun getText():String {
            val x=lPy.lPy.fold(""){sum, element -> "$sum ${element.w}"}
            return x
            //return "With PinYin"+(lPy.fold(""){sum, element -> "$sum ${((element.first.w}"})
            //return "With PinYin  ${lPy.lPy.toString()}"
        }
    }

    class NoPinYin(val txt:List<String>):TransClass(){
        override fun getText():String{return txt.joinToString(" ") }
    }
    class NoTrans():TransClass()
}


class TransCache {
    private val mT4 = mutableMapOf<String,TransClass>()

    fun getTrans3(orig: String, olang: String, tlang: String): TransClass {
        Timber.d("orig $orig olang $olang tlang $tlang")
        val key = orig + tlang
        val x = mT4[key]
        if (x != null) return x
        val a = gettranslatedTextX(orig, olang, tlang)
        if (a.isEmpty()) return TransClass.NoTrans()
        else {
            if (tlang.equals("zh")) {
                mT4[key] = TransClass.WithPinYin(ListPinyin(getPinying(a)))
                return mT4[key]!!
            }else
                Timber.d("ZZZZZZZZZZZZZZZZZZZZZ  a='$a'")
                mT4[key] = TransClass.NoPinYin(listOf(a))  //listOf(Pinyin(a, ""))
        }
        return TransClass.NoPinYin(listOf(a)) //listOf(Pinyin(a, ""))
    }
}

fun gettranslatedTextX(txt: String, olang: String, tlang: String): String {
    Timber.d("getTranslatedTextX $txt olang $olang tlang $tlang")
    try {
        val r=gettranslatedText(txt, olang, tlang)[0].translated
        //Timber.d("Translation->$r")
        return r
        //return translatePayString(txt,olang,tlang)
        //return "$txt  & translated  "
    } catch (e: java.lang.Exception) {
        Timber.d("Exception $e")
        return translatePayString(txt,olang,tlang)
        return "Error in google trans try it later"
    }
    return ""
}