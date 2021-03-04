package com.begemot.inreader


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.begemot.inreader.model.*

import com.begemot.knewscommon.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import io.ktor.util.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json


class VM : ViewModel() {
    val newsPapers: newsPapers = newsPapers()

    val headLines: headLines = headLines()

    var scrollposHL = 0

    val msg = mesages()

    val tCache = TransCache()

    val tLang = KLAnguanges()

    val jLang = KKLang()

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





