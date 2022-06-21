package com.begemot.myapplicationz


import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.begemot.myapplicationz.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber


class VM : ViewModel() {
    val newsPapers: NewsPapers = NewsPapers()

    val headLines: headLines = headLines()

    val article:article = article()

    val msg = mesages()

    val tCache = TransCache()

    val toneAndPitchMap = KLAnguanges()

    val jLang = KKLang()

    val KK=KskFold(this.viewModelScope)

}

class KskFold(val sc: CoroutineScope){
    var job: Job? = null
    var scafoldstate:ScaffoldState?=null
    fun setscafoldstate(scs:ScaffoldState){ scafoldstate=scs}

    init {
        if(job!=null){
            job?.cancel()
            job=null
        }
    }


    fun showMessage(sMsg:String){
        if(job!=null) {job?.cancel(); job=null}
        job=sc.launch {
            Timber.d("SOWWWW  MEEESSSSSSAAAGEEE $sMsg!")
            scafoldstate?.snackbarHostState?.showSnackbar(
                sMsg
            )
        }

    }


}


class mesages() {
    val LMSG = mutableStateListOf("")
    fun setMsg2(sAux: String) {
        LMSG.add(sAux)
    }
    fun cls(){
        LMSG.clear()
    }
}


//Max 56 87


