package com.begemot.myapplicationz

import android.content.Context
import androidx.compose.foundation.Image
//import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.begemot.knewscommon.THeadLines
import timber.log.Timber
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.ContextAmbient
import androidx.core.content.ContextCompat.startActivity

//data class NewsPapers(val Name:String,val Desc:String,val resid:Int,val screen:Screens,val newsProvider:INewsPaper)

/*val lNewsPapers=listOf(
  //  NewsPapers("RT Novesti","Russian News",R.drawable.ic_rt_logo_logotyp_us2,Screens.ListHeadLines,RT),
  //  NewsPapers("The Guardian","British News",R.drawable.ic_the_guardian,Screens.ListHeadLines,Guardian),
  //  NewsPapers("Süddeutsche Zeitung","German News",R.drawable.ic_sz_plus_logo,Screens.ListHeadLines,SZ)
)*/

@Composable
fun newsPapersScreen(sApp: StatusApp) {
    sApp.currentBackScreen = Screens.QuitScreen
    Timber.d(sApp.status())
    when (val status = sApp.currentStatus.value) {
        is AppStatus.Idle -> draw_newsPapers(sApp)
        is AppStatus.Error -> displayError(status.sError, status.e, sApp)
        is AppStatus.Loading -> {
        }
    }
}


@Composable
fun draw_newsPapers(sApp: StatusApp) {
    val context = AmbientContext.current
    Timber.d("Composable ${sApp.status()}")
    sApp.vm.reinicializeHeadLines()
    val (shape, setShape) = mutableStateOf(CircleShape)
    Column() {
        LazyColumn(){
            items(sApp.vm.lNewsPapers, itemContent = {

                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = 7.dp,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(2.dp)
                        .clickable(onClick = {
                            Timber.d("onClick going to HL")
                            sApp.currentScreen.value = Screens.HeadLinesScreen
                            sApp.currentBackScreen = Screens.NewsPapersScreen
                            sApp.currentNewsPaper = it
                            sApp.currentStatus.value = AppStatus.Loading

                        })
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        //Box(modifier = Modifier.preferredHeight(64.dp).preferredWidth(120.dp)) {
                        Surface(color = Color.Transparent) {
                            Image(
                                bitmap = KCache.getBitmapImage(it.logoName).asImageBitmap(),
                                modifier = Modifier
                                    .preferredHeight(64.dp)
                                    .preferredWidth(120.dp)
                                    .padding(horizontal = 10.dp)
                                    .clickable(onClick = { OpenBrowser(context,it.url)}),
                                // modifier = Modifier.size(225.dp).padding(15.dp).drawShadow(8.dp,shape),
                                contentScale = ContentScale.Fit
                            )


                        }
                        Column(Modifier.padding(start = 6.dp)) {
                            Text(it.name)
                            Text(it.desc)
                        }

                    }
                }
            })
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