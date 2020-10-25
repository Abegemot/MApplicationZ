 package com.begemot.myapplicationz

import androidx.compose.*
import androidx.compose.foundation.*

import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.NewsPaper
import com.begemot.knewscommon.toJListNewsPaper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
//import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan
import timber.log.Timber
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumnItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.begemot.knewscommon.JListNewsPaper
import com.begemot.knewscommon.fromJsonToList
import kotlinx.coroutines.*

 @Composable
fun startUpScreen(statusApp:StatusApp) {
    val lnp = state<List<NewsPaper>> { mutableListOf() }

    val status=statusApp.currentStatus
    Timber.d("startUpScreen statusApp $status")
    when(status) {
        //is AppStatus.Loading -> waiting()
        is AppStatus.Error -> displayError(status.sError, status.e,statusApp  )
        is AppStatus.Loading -> {


            ScrollableColumn(Modifier.padding(10.dp).fillMaxSize()) {
                CircularProgressIndicator()
                FlowComponent(sflow = myfirstFlow(statusApp))
            }
        }
        is AppStatus.Idle->statusApp.currentScreen=Screens.ListNewsPapers
    }



        launchInComposition {
           // getNewsPapers(lnp)
        }
    //}
}



@Composable
fun FlowComponent(sflow: Flow<String>){
    val stringListFlow: Flow<List<String>> = remember(sflow) {
        sflow.scan(emptyList()) { kstrings, s -> kstrings + s }
    }
    val sl: List<String> by stringListFlow.collectAsState(emptyList())
    Column(Modifier.fillMaxHeight(1.0f)){
        sl.forEach {
            Text(it)
            Timber.d(it)
        }
    }
}


fun myfirstFlow(statusApp: StatusApp)=flow<String>{
   try {
       emit("Checking Connection")
       if(isOnline(App.lcontext))  emit("...conection ok")  //throw(Exception("Not Online"))
       else emit("no conection, can't work")

       //KCache.deleteFiles()
       //val lf=KCache.listFiles()
       //if(lf.size==0) emit("no files")
       //else{
       //    lf.forEach{it->emit("  $it")}
       //}

       if(!isInstalled()){
           emit("First Install")
           KCache.setUp()
           emit("Obtaining last version")
           val (version,listNP)=KNews().getNewsPapersWithVersion(0)
           emit("checking Images")
           checkImages(listNP)
           KCache.storeInCache2("knews.json", toJListNewsPaper(listNP).str)
           KProvider2.setNewsPapers(listNP)
           Timber.d("setting version $version")
           prefs.installedver = version
           emit("end install")
           statusApp.currentStatus=AppStatus.Idle
          // statusApp.currentScreen=Screens.ListNewsPapers
       }
       else {
           allReadyInstalled(statusApp = statusApp)
         /* emit("Already Installed")
          emit("checking updates")
          val (version,listNP)=KNews().getNewsPapersWithVersion(prefs.installedver)
          if(version==0){  //no updates
              emit("no updates current version ${prefs.installedver}")
              val lkn= fromJsonToList(JListNewsPaper(KCache.loadFromCache("knews.json")))
              KProvider2.setNewsPapers(lkn)
          }
          else{
              emit("found updates")
              prefs.installedver=version
              checkImages(listNP)
              KCache.storeInCache2("knews.json", toJListNewsPaper(listNP).str)
              KProvider2.setNewsPapers(listNP)
          }
          emit("end start up")
          //val lkn= fromJsonToList(JListNewsPaper(KCache.loadFromCache(App.lcontext,"knews.json")))
          // statusApp.currentScreen=Screens.ListNewsPapers
           statusApp.currentStatus=AppStatus.Idle*/
           emit("end start up")
           Timber.d("End startup flow")
           //statusApp.currentStatus=AppStatus.Idle
     }

   } catch (e: Exception) {
       statusApp.currentStatus=AppStatus.Error("ondima",e)
   }

    //emit("uuu"+getNewsPapers(null))
}


fun isInstalled():Boolean{
   Timber.d("installed ver: ${prefs.installedver}")
   if(KCache.fileExists("knews.json","")){ Timber.d("EXISTEIX"); return true}
    else { Timber.d("NOEXISTEIX"); return false}
   if(prefs.installedver>0) return true
   return false
}

 suspend fun checkImages(lnp:List<NewsPaper>){
     lnp.forEach{
         if(!KCache.fileExists(it.logoname,"/Images"))
         {
             val ba=KNews().getImage("Images/${it.logoname}")
             KCache.storeImageInCache(it.logoname,ba.bresult)

         }

     }
}


suspend fun allReadyInstalled(statusApp: StatusApp){
    val scope= CoroutineScope(Job() +Dispatchers.IO )
    scope.launch {
        Timber.d("Already Installed")
        Timber.d("checking updates")

        val (version,listNP)=KNews().getNewsPapersWithVersion(prefs.installedver)
        if(version==0){  //no updates
            Timber.d("no updates current version ${prefs.installedver}")
            val lkn= fromJsonToList(JListNewsPaper(KCache.loadFromCache("knews.json")))
            KProvider2.setNewsPapers(lkn)
        }
        else{
            //emit("found updates")
            prefs.installedver=version
            checkImages(listNP)
            KCache.storeInCache2("knews.json", toJListNewsPaper(listNP).str)
            KProvider2.setNewsPapers(listNP)
        }
        Timber.d("end allReadyInstalled")
        //val lkn= fromJsonToList(JListNewsPaper(KCache.loadFromCache(App.lcontext,"knews.json")))
        // statusApp.currentScreen=Screens.ListNewsPapers
        statusApp.currentStatus=AppStatus.Idle
    }
}
