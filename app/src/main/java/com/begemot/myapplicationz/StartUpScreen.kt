package com.begemot.myapplicationz

import androidx.compose.*
import androidx.compose.foundation.*

import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.NewsPaper
import com.begemot.knewscommon.toJListNewsPaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
//import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan
import timber.log.Timber
import androidx.compose.foundation.layout.*
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

@Composable
fun startUpScreen(statusApp:StatusApp) {
    val lnp = state<List<NewsPaper>> { mutableListOf() }

    val status=statusApp.currentStatus
    Timber.d("statusApp $status")
    when(status) {
        //is AppStatus.Loading -> waiting()
        is AppStatus.Error -> displayError(status.sError, status.e)
        is AppStatus.Loading -> {


            ScrollableColumn(Modifier.padding(10.dp)) {
                CircularProgressIndicator()
                FlowComponent(sflow = myfirstFlow(statusApp))
            }
        }
    }
    //lnp.value=getNewsPapers()
    //Box() {
    //    Text("Hola marica!")
    //    FlowComponent(sflow = myfirstFlow())

     //   Text("${lnp.value.size}")
     //   lnp.value.forEach {
     //       Text("${it.handler} ${it.name} ${it.olang} ${it.title} ${it.logoname}")
     //   }
     //   Box(Modifier.fillMaxHeight().fillMaxWidth()) {
     //       Text(text = "sopa")

      //      val lnk2 =
      //          "https://storage.googleapis.com/knews1939.appspot.com/Images/The_Guardian.png"
      //      val lnk1 = "https://storage.googleapis.com/knews1939.appspot.com/Images/rt-logo.png"
      //      val lnk3 =    "https://storage.googleapis.com/knews1939.appspot.com/Images/sz-plus-logo.png"
            // CoilImageWithCrossfade(data = lnk1
            //     ,contentScale = ContentScale.Fit,modifier = Modifier.preferredWidth(100.dp).preferredHeight(150.dp)
            // )
           /* CoilImage(
                data = lnk1,
                contentScale = ContentScale.Fit,
                modifier = Modifier.preferredWidth(100.dp).preferredHeight(50.dp)
            )

            CoilImage(
                data = lnk2,
                contentScale = ContentScale.Fit,
                modifier = Modifier.preferredWidth(100.dp).preferredHeight(50.dp)
            )
            CoilImage(
                data = lnk3,
                contentScale = ContentScale.Fit,
                modifier = Modifier.preferredWidth(100.dp).preferredHeight(50.dp)
            )
*/
            //CoilImage(data=lnk2)
            //CoilImage(data=lnk3)
            //Text(text = "boba2")
            //Image(painter = )

            //val p=Painter()
            //val l=KCache.findImageInCache(App.lcontext,"sz-plus-logo.png")
           //     val l=KCache.findImageInCache(App.lcontext,"Nologo2.png")
           // val s=BitmapFactory.decodeByteArray(l,0,l.size)
           // Image(s.asImageAsset(), modifier = Modifier.preferredWidth(100.dp).preferredHeight(50.dp))

            //Text(text = "xboba")

        //}
        /*val im= ImageAsset(100,10,ImageAssetConfig.Alpha8)
        im.asAndroidBitmap()

        val v=ImageView(this)
        v.setImageBitmap()*/
        //  }
        


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
    Box(Modifier.fillMaxHeight()){
        sl.forEach {
            Text(it)
        }
    }
}


fun myfirstFlow(statusApp: StatusApp)=flow<String>{
   try {
       emit("Checking Connection")
       if(isOnline(App.lcontext))  emit("...conection ok")  //throw(Exception("Not Online"))

       else emit("no conection, can't work")
delay(500)
       emit("Geting News papers form Server")
       val lkn=KNews().getNewsPapers()
       emit("Storing News Papers to cache")
       KCache.storeInCache2(App.lcontext,"knews.json",toJListNewsPaper(lkn).str)
       emit("Check directory Images")
       KCache.makeImagesDir(App.lcontext)
       emit("Updating Images")
       lkn.forEach{
           if(KCache.fileExists(App.lcontext,it.logoname,"/Images")) Timber.d("Exist ${it.logoname}")
           else{
               Timber.d("Does not Exist ${it.logoname}")
               val ba=KNews().getImage("Images/${it.logoname}")
               if(ba.found) Timber.d("${it.logoname} found with size  ${ba.bresult.size}")
               else Timber.d("/${it.logoname}/ not found")
               KCache.storeImageInCache(App.lcontext,it.logoname,ba.bresult)
           }
       }
       val ba=KNews().getImage("Images/Nologo2.png")
       KCache.storeImageInCache(App.lcontext,"Nologo2.png",ba.bresult)
       emit("End update Images  ${lkn.size}")
       delay(1000)
       KProvider2.setNewsPapers(lkn)
       statusApp.currentScreen=Screens.ListNewsPapers

   } catch (e: Exception) {
       statusApp.currentStatus=AppStatus.Error("ondima",e)
   }

    //emit("uuu"+getNewsPapers(null))
}

suspend fun getNewsPapers(lvp: MutableState<List<NewsPaper>>?):Unit  {
    //lateinit var lnp:List<NewsPaper>
   // GlobalScope.launch(Dispatchers.IO) {
    //delay(3000)
//    val np=KNews().getNewsPapers()
    Timber.d("get news paper")
    KCache.listFiles()
    KCache.deleteFiles()
    KCache.listFiles()
    val lkn=KNews().getNewsPapers()
    Timber.d(lkn.toString())
    val s=toJListNewsPaper(lkn).str
    KCache.storeInCache2(App.lcontext,"knews.json",s)
    KCache.makeImagesDir(App.lcontext)
    lkn.forEach{
             if(KCache.fileExists(App.lcontext,it.logoname,"/Images")) Timber.d("Exist ${it.logoname}")
             else{
                 Timber.d("Does not Exist ${it.logoname}")
                 val ba=KNews().getImage("Images/${it.logoname}")
                 if(ba.found) Timber.d("${it.logoname} found with size  ${ba.bresult.size}")
                 else Timber.d("/${it.logoname}/ not found")
                 KCache.storeImageInCache(App.lcontext,it.logoname,ba.bresult)
             }
    }
    val ba=KNews().getImage("Images/Nologo2.png")
    KCache.storeImageInCache(App.lcontext,"Nologo2.png",ba.bresult)
    KCache.listFiles()



    /*Timber.d("que passa neng")
    val l=KCache.findImageInCache(App.lcontext,"sz-plus-logo.png")
    if(l.size==0){
        Timber.d("Image not Found")
        val ba=KNews().getImage("Images/sz-plus-logo.png")
        if(ba.found){
           Timber.d("size image from server : ${ba.bresult.size}")
            KCache.storeImageInCache(App.lcontext,"sz-plus-logo.png",ba.bresult)
        }else Timber.d("Not found in server ${ba.bresult.size}")

    }else{
        Timber.d("Image found with size=${l.size}")
    }
    //      lvp.value=(KNews().getNewsPapers())
   // }*/

}