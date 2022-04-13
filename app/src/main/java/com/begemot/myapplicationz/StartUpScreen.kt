package com.begemot.myapplicationz

import kotlinx.coroutines.flow.Flow
//import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan
import timber.log.Timber
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

@Composable
fun startUpScreen(sApp: StatusApp) {
    val status = sApp.currentStatus.value

    Timber.d("statusApp =$sApp")
    when (status) {
        //is AppStatus.Loading -> waiting()
        is AppStatus.Error -> displayError(status.sError, status.e, sApp)
        is AppStatus.Loading -> {
            Column(Modifier.padding(10.dp).fillMaxSize()) {
                CircularProgressIndicator()
                FlowComponent(sflow = myfirstFlow(sApp))
            }
        }
        is AppStatus.Idle -> sApp.currentScreen.value = Screens.NewsPapersScreen
    }
}


@Composable
fun FlowComponent(sflow: Flow<String>) {
    val stringListFlow: Flow<List<String>> = remember(sflow) {
        sflow.scan(emptyList()) { kstrings, s -> kstrings + s }
    }
    val sl: List<String> by stringListFlow.collectAsState(emptyList())
    Column(Modifier.fillMaxHeight(1.0f)) {
        sl.forEach {
            //Text(it)
            Timber.d(it)
        }
    }
}


fun myfirstFlow(statusApp: StatusApp) = flow<String> {
    try {
        emit("Checking Connection")
        if (isOnline(App.lcontext)) emit("...conection ok")  //throw(Exception("Not Online"))
        else emit("no conection, can't work")

        /* Timber.d("installed ver  ${prefs.installedver}")
         prefs.installedver=0
         Timber.d("installed ver  ${prefs.installedver}")
         KCache.deleteFiles()

         */


        //val lf=KCache.listFiles()
        //if(lf.size==0) emit("no files")
        //else{
        //    lf.forEach{it->emit("  $it")}
        //}

        if (!isInstalled()) {
            emit("First Install")
            KCache.setUp()
            emit("Obtaining last version")
            emit("checking Images")
            statusApp.vm.newsPapers.checkUpdates(statusApp)
            emit("end install")
            delay(2000)
            statusApp.currentStatus.value = AppStatus.Idle
            // statusApp.currentScreen=Screens.ListNewsPapers
        } else {
            allReadyInstalled(statusApp = statusApp)
            emit("end start up")
            statusApp.vm.msg.setMsg(statusApp, "End Start Up 2")

            //sendMessage("End Start Up 2")
            //statusApp.sMessage="End start up"
            //statusApp.currentStatus=AppStatus.Idle
        }

    } catch (e: Exception) {
        statusApp.currentStatus.value = AppStatus.Error("ondima", e)
    }

    //emit("uuu"+getNewsPapers(null))
}




suspend fun allReadyInstalled(statusApp: StatusApp) {
    //val scope=statusApp.vm.viewModelScope
    val scope = CoroutineScope(Job() + Dispatchers.IO)
    Timber.d("BEgin")
    scope.launch {
        try {
            statusApp.vm.newsPapers.getNewsPapers()
            if (!statusApp.vm.newsPapers.checkUpdates(statusApp)) statusApp.vm.msg.setMsg(
                statusApp,
                "No News Papers Updates"
            )
            //      sendMessage("No News Papers Updates")
        } catch (e: Exception) {
            Timber.d("WWWWWWWWWWWWWWWWWWWWWW  $e")
        }
        //Timber.d("END ALLREADY INSTALLED COROUTINE")
    }
    Timber.d("END")
}
suspend fun isInstalled():Boolean{
    //KCache.deleteFiles()
    //KCache.setUp()

    return if(KCache.fileExistsAndNotEmpty("knews.json","")) {
        try {
            App.sApp.vm.newsPapers.getNewsPapers()
            Timber.d("INSTALLED")
            true
        } catch (e: Exception) {
            Timber.e("NOT INSTALLED $e")
            false
        }
    }else{
        Timber.e("knews.json does not exist")
        false
    }


    /* if(KCache.fileExistsAndNotEmpty("knews.json","")){
         try{
             App.sApp.vm.newsPapers.getLocalNewsPapers(App.sApp)
         }catch (e:Exception){
             Timber.e("NOT INSTALLED")
             return false
         }
         Timber.d("ALREADY INSTALLED")
         return true
     }
     return false*/
}
