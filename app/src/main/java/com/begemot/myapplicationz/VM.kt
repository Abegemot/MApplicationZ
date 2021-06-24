package com.begemot.myapplicationz


import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.begemot.myapplicationz.model.*

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber


class VM : ViewModel() {
    val newsPapers: newsPapers = newsPapers()

    val headLines: headLines = headLines()

    val article:article = article()

    var scrollposHL = 0

    val msg = mesages()

    val tCache = TransCache()

    val toneAndPitchMap = KLAnguanges()

    val jLang = KKLang()

}


class mesages() {
    private val _message = MutableStateFlow<String>("ll")
    val LMSG = mutableStateListOf("")
    val LS = mutableStateListOf("")
    val mesage: StateFlow<String> get() = _message

    fun setMsg(sApp: StatusApp, sAux: String) {
        Timber.d("setMsg!! $sAux")
        sApp.visibleInfoBar = true //¿?
        _message.value = sAux
    }

    fun setMsg2(sAux: String) {
//        Timber.d("setMsg2 = $sAux")
        LMSG.add(sAux)
    }
    fun cls(){
        LMSG.clear()
    }


}





