package com.begemot.myapplicationz.screens

import android.content.Context
//import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import timber.log.Timber
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import com.begemot.myapplicationz.*

import com.begemot.myapplicationz.layout.ListModifier
import com.begemot.myapplicationz.layout.myCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers

//data class NewsPapers(val Name:String,val Desc:String,val resid:Int,val screen:Screens,val newsProvider:INewsPaper)

/*val lNewsPapers=listOf(
  //  NewsPapers("RT Novesti","Russian News",R.drawable.ic_rt_logo_logotyp_us2,Screens.ListHeadLines,RT),
  //  NewsPapers("The Guardian","British News",R.drawable.ic_the_guardian,Screens.ListHeadLines,Guardian),
  //  NewsPapers("Süddeutsche Zeitung","German News",R.drawable.ic_sz_plus_logo,Screens.ListHeadLines,SZ)
)*/

@ExperimentalMaterialApi
@Composable
fun newsPapersScreen(sApp: StatusApp) {
    sApp.currentBackScreen = Screens.QuitScreen
//    Timber.d(sApp.status())
    when (val status = sApp.currentStatus.value) {
        is AppStatus.Idle -> draw_newsPapers(sApp)
        is AppStatus.Error -> displayError(status.sError, status.e, sApp)
        is AppStatus.Loading -> {}
        else -> {}
    }
}


@ExperimentalMaterialApi
@Composable
fun draw_newsPapers(sApp: StatusApp) {
    Timber.d("Composable")
    //val i=sApp.selectedNews.value   //to trigger recomposition when selected news changes!!!
    val context = LocalContext.current
    val cs= CoroutineScope(Dispatchers.IO)
    //sApp.setMsg(" Newspapers-> ${sApp.vm.newsPapers.lNewsPapers.size}")

    sApp.vm.headLines.reinicializeHeadLines()
    val (shape, setShape) = mutableStateOf(CircleShape)
    val scrollState: ScrollState = rememberScrollState(sApp.vm.newsPapers.iFirstVisibleItem)
    // LazyColumn(){
    //         items(sApp.vm.lNewsPapers, itemContent = {

    Column(modifier = Modifier
        .verticalScroll(state = scrollState)
        .then(ListModifier())) {
        sApp.vm.newsPapers.lNewsPapers.forEachIndexed { index, it ->
            val scope= rememberCoroutineScope()
            myCard(
                //shape = RoundedCornerShape(8.dp),
                //elevation = 7.dp,
                onClik = {
                     Timber.w("on click going Headlines Screen with: $it")
                    sApp.currentScreen.value = Screens.HeadLinesScreen
                    sApp.currentBackScreen = Screens.NewsPapersScreen
                    sApp.currentNewsPaper = it
                    sApp.currentStatus.value = AppStatus.Loading
                    sApp.vm.newsPapers.iFirstVisibleItem = scrollState.value
                },
                mod = Modifier
                    //.fillMaxWidth()
                    .padding(0.dp)

                    //.background(Color.Blue)


            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    //Box(modifier = Modifier.preferredHeight(64.dp).preferredWidth(120.dp)) {
                    //Timber.d(" -> CURRENT SELECTED NEWS $index")//  $sApp.currentNewPreferences.value?.selectedNews")
       //             Timber.d("$index")
                    Surface(
                        color = if(sApp.selectedNews!=index) Color.LightGray else Color.Gray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp).pointerInput(Unit){
                            detectTapGestures(
                                onDoubleTap = {  ita->
                                        if(sApp.selectedNews==index) {
                                            Timber.d("O A")
                                            sApp.selectedNews = -1
                                           // sApp.currentNewPreferences.selectedNews=-1
                                        }
                                        else {
                                            Timber.d("O B")
                                            sApp.selectedNews = index
                                            //sApp.currentNewPreferences.selectedNews=index
                                        }
                                              },
                                onTap={  ita->
                                    if (it.url.isNotEmpty())
                                        OpenBrowser(context, it.url)
                                }
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val img= produceState(initialValue = ImageBitmap(10,10)){
                            val l=KProvider.getImage(it.logoName)
                            //if(it.logoName.equals("vilaweb.jpg")) delay(5000)
                            if(l!=null) value=l
                        }
                        Image(
                            bitmap = img.value, //loadNetworkImage(url = it.logoName).value,//img.value,//KCache.getBitmapImage2(it.logoName).value?: ImageBitmap(10,10),
                            modifier = Modifier
                                .height(64.dp)
                                .width(120.dp)
                                .padding(horizontal = 10.dp),
                            contentScale = ContentScale.Fit,
                            contentDescription = ""
                        )
                    }
                    Column(
                        Modifier
                            .padding(start = 6.dp)
                            .fillMaxSize()) {
                        Text(it.name)
                        Text(it.desc)
                    }
                }
            }
        }
    }
}

fun OpenBrowser(ctx:Context,url:String){
    if(url.equals("none")) return
    Timber.d("Open Browser  $url")
    val openURL = Intent(android.content.Intent.ACTION_VIEW)
    openURL.data = Uri.parse(url)
    startActivity(ctx,openURL,null)
}

//Max 242 261 161