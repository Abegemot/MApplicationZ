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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import com.begemot.myapplicationz.*
import com.begemot.myapplicationz.layout.ListModifier
import com.begemot.myapplicationz.layout.myCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    Timber.d(sApp.status())
    when (val status = sApp.currentStatus.value) {
        is AppStatus.Idle -> draw_newsPapers(sApp)
        is AppStatus.Error -> displayError(status.sError, status.e, sApp)
        is AppStatus.Loading -> {}
    }
}


@ExperimentalMaterialApi
@Composable
fun draw_newsPapers(sApp: StatusApp) {
    val context = LocalContext.current

    //sApp.setMsg(" Newspapers-> ${sApp.vm.newsPapers.lNewsPapers.size}")
    Timber.d("Composable ${sApp.status()}")
    sApp.vm.headLines.reinicializeHeadLines()
    val (shape, setShape) = mutableStateOf(CircleShape)
    val scrollState: ScrollState = rememberScrollState(sApp.vm.newsPapers.iFirstVisibleItem)
    // LazyColumn(){
    //         items(sApp.vm.lNewsPapers, itemContent = {
    Column(modifier = Modifier
        .verticalScroll(state = scrollState)
        .then(ListModifier())) {
        sApp.vm.newsPapers.lNewsPapers.forEach {
            val scope= rememberCoroutineScope()
            myCard(
                //shape = RoundedCornerShape(8.dp),
                //elevation = 7.dp,
                onClik = {
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
                    Surface(
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        //val img = remember { mutableStateOf(ImageBitmap(10, 10)) }

                        //LaunchedEffect(key1 = img.value, block ={
                        //    val l= KProvider.getImage(it.logoName)
                        //    if(l!=null)   img.value= l
                        //} )

                        val img= produceState(initialValue = ImageBitmap(10,10)){
                            val l=KProvider.getImage(it.logoName)
                            //if(it.logoName.equals("vilaweb.jpg")) delay(5000)
                            if(l!=null) value=l
                        }
                        //LaunchedEffect(img.value) {
                      /* LaunchedEffect(true){
                                               //launch {
                            //val i=KProvider.getImage(it.logoName)
                            val i=KProvider.getImage(it.logoName )
                            if(i!=null) {
                                Timber.d("IMG NOT NULL")
                                img.value=i

                            }else
                                Timber.d("IMG NULL  !!!!!!!!!!!")
                            //!!!!! img.value = KCache.getBitmapImage(it.logoName)!!
                        }*/
                        //val img=loadNetworkImage(url = it.logoName).value
                        Image(
                            bitmap = img.value, //loadNetworkImage(url = it.logoName).value,//img.value,//KCache.getBitmapImage2(it.logoName).value?: ImageBitmap(10,10),
                            modifier = Modifier
                                .height(64.dp)
                                .width(120.dp)
                                .padding(horizontal = 10.dp)
                                .clickable(onClick = {
                                    if (it.url.isNotEmpty())
                                        OpenBrowser(context, it.url)
                                }
                                ),
                            // modifier = Modifier.size(225.dp).padding(15.dp).drawShadow(8.dp,shape),
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
@Composable
fun loadNetworkImage(
    url: String,

): State<ImageBitmap> {

    // Creates a State<T> with Result.Loading as initial value
    // If either `url` or `imageRepository` changes, the running producer
    // will cancel and will be re-launched with the new inputs.
    return produceState<ImageBitmap>(initialValue = ImageBitmap(10, 10), url) {

        // In a coroutine, can make suspend calls
        val s=KProvider.getImage2(url)
        if(s==null) Timber.d("NULLLL DE LA PUNYETA")
        Timber.d("NOOOOOTTT  NULL")
        s
        // Update State with either an Error or Success result.
        // This will trigger a recomposition where this State is read
        //value = if (image == null) {
        //    Result.Error
        //} else {
                //    Result.Success(image)
        //}
    }
}


fun OpenBrowser(ctx:Context,url:String){
    if(url.equals("none")) return
    Timber.d("Open Browser  $url")
    val openURL = Intent(android.content.Intent.ACTION_VIEW)
    openURL.data = Uri.parse(url)
    startActivity(ctx,openURL,null)
}