package com.begemot.myapplicationz.screens

import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.begemot.knewscommon.OriginalTransLink
import com.begemot.knewscommon.milisToMinSec
import com.begemot.knewscommon.milisToMinSecMilis
import com.begemot.myapplicationz.*
import com.begemot.myapplicationz.layout.myCard2
import kotlinx.coroutines.*
import timber.log.Timber

class SongParams{
    lateinit var  mp:MediaPlayer
    var jobTimer        = mutableStateOf(false)
    val initialized     = mutableStateOf(false)
    val txt             = mutableStateOf("")
    val playing         = mutableStateOf(false)
    var duration        = mutableStateOf(0)
    var currTime        = mutableStateOf(0)
    var futureTime      = mutableStateOf(0f)
    var soundloaded     = mutableStateOf(false)
    init {
        Timber.d("INITIALIZE SONG PARAMS")
    }
    override fun toString(): String {
        return "jobTimer=${jobTimer.value} initialized=${initialized.value} playing=${playing.value} duration=${duration.value} currTime=${currTime.value} soundloaded=${soundloaded.value}"
    }
}
private val SP=SongParams()


suspend fun initializeSongScreen(sOriginalTransLink: OriginalTransLink,sApp:StatusApp,listState: LazyListState){
    Timber.d("Start")
    loadSongText(sOriginalTransLink,sApp,listState)
    loadSong(sOriginalTransLink,sApp)
    //sApp.currentStatus.value = AppStatus.Idle
    SP.initialized.value=true
    Timber.d("DID IT ${sOriginalTransLink.kArticle.l2} SOUND? ${sApp.currentStatus.value}")
    Timber.d(SP.toString())
    Timber.d("End")
}

suspend fun loadSongText(sOriginalTransLink: OriginalTransLink,sApp:StatusApp,listState: LazyListState){
     loadArticle(sOriginalTransLink,sApp,listState)
}

fun onEndSong(mp:MediaPlayer?){
    Timber.d("OnEnd Song")
    mp?.seekTo(0)
    SP.currTime.value=0
    stopMp3()
}


suspend fun loadSong(sOriginalTransLink: OriginalTransLink,sApp:StatusApp){
    Timber.d("Start")
    //delay(3000)
    SP.mp = MediaPlayer()
    SP.mp.setOnCompletionListener{mp-> onEndSong(mp)}
    if(KProvider.checkMP3(sOriginalTransLink.kArticle.l2))
    {
        try {
            //   .create(  App.lcontext.applicationContext, Uri.parse(s))
            SP.mp.setDataSource(KCache.getCName("/MP3/${sOriginalTransLink.kArticle.l2}"))
            SP.mp.prepare()
            SP.duration.value =  SP.mp.duration
            SP.soundloaded.value = true
            //SP.mp?.start()
        } catch (e: Exception) {
            SP.soundloaded.value = false
            Timber.e(e)
        }
    }
    else
        SP.txt.value="NOOOOO SOUND CHANGED!! CHANGED!! CHANGED!! CHANGED!! /n "
    Timber.d("End")
}



fun releaseSongScreen(){
    SP.jobTimer.value=false
    SP.playing.value=false
    if(SP.soundloaded.value) SP.mp.release()
    SP.duration.value=0
    SP.currTime.value=0
    SP.futureTime.value=0f
    SP.soundloaded.value = false
    SP.initialized.value = false
    Timber.d(SP.toString())
}


@Composable
fun SongScreen(s:OriginalTransLink, sApp: StatusApp){
    //sApp.vm.KK.showMessage("Hola 1")
    //sApp.vm.KK.showMessage("Hola 2")
    //sApp.vm.KK.showMessage("Hola 3")


    Timber.d("SONG SCREENS ${sApp.currentStatus.value}")
        //sApp.currentStatus.value=AppStatus.Loading
        val lstate = rememberLazyListState(sApp.vm.article.iInitialItem.value)
        sApp.currentBackScreen = Screens.HeadLinesScreen
    LaunchedEffect(sApp.lang) {
        Timber.d("entering launched effect !!! ${sApp.currentStatus.value}")
        initializeSongScreen(s, sApp, lstate)
       // SP.jobTimer.value = true
        sApp.currentStatus.value = AppStatus.Idle
        Timber.d("leaving launched effect")
    }
    DisposableEffect(s) {
            Timber.d("on enter disposable effect")
            onDispose {
                releaseSongScreen()
                Timber.d("onDispose")
            }
        }

        SideEffect {
            Timber.d("Enter SideEffect")
            SP.jobTimer.value = true
            CoroutineScope(Dispatchers.Default).launch { controlMp3Time() }
            Timber.d("Leaving SideEffect")
        }
        resfreshWraper(sApp.currentStatus.value == AppStatus.Loading) {
            Timber.d("SONG STRING COMPOSABLE3 ${sApp.currentStatus.value}")
            //if (sApp.currentStatus.value == AppStatus.Idle)
                DrawSongScreen(sApp, s, lstate)
        }
}

@Preview
@Composable
fun NewMan(){
    Box(Modifier.padding(0.dp)){
        Row(
            Modifier
                .background(Color.Blue)
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
                //.padding(bottom = 50.dp)
        )
        {
            Text("Row1 Blue")
        }
        Row(
            Modifier
                .background(Color.Transparent)
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)//, start = 10.dp,end=10.dp)
                .fillMaxWidth(0.95f)
                .border(2.dp, Color.Green, shape = RoundedCornerShape(52.dp))

        ){
            myCard2() {
                NSound()
            }
        }
        Row(
            Modifier
                .background(Color.Yellow)
                .align(Alignment.CenterEnd)
                .offset(x = -100.dp)
                .padding(50.dp)

        ){
            Text("Row3 Yellow")
            Column() {
                Text("t1")
                Text("t2")
                Text("t3")
            }
        }
    }
}

@Preview
@Composable
fun NSound(){
   /* Text("Row2 Green")
    Column() {
        Text("t1")
        Text("t2")
        Text("t3")
    }*/
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement =  Arrangement.SpaceBetween){

        IconButton(onClick = {  }, enabled = true, modifier = Modifier.border(1.dp,Color.Magenta)) {
            Icon(Icons.Filled.PlayArrow,contentDescription = null)
        }
        IconButton(onClick = {  }, enabled = true ) {
            Icon(Icons.Filled.Pause,contentDescription = null)
        }
        Box() {
            Text(modifier = Modifier.align(Alignment.TopCenter),text= "0.0")
            Slider(value = 100f, valueRange = 0f..200f, onValueChange = {}, modifier = Modifier.padding(top=4.dp, bottom = 4.dp))
            Text(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, top = 4.dp),text="10:24")

        }

    }
}

//@Preview
@Composable
fun DrawSongTest(){
    Column {
        Row(
            Modifier
                //.background(if (SP.initialized.value) Color.Black else Color.Red)
                .fillMaxHeight(0.90F)
                .fillMaxWidth()){
            Text("primera row  ${SP.txt.value}")
        }
        Divider()
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .background(Color.Blue)
                .align(Alignment.CenterHorizontally)

                .clip(AbsoluteRoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp)
                .fillMaxHeight()
                .fillMaxWidth(0.75f)){
            myCard2() {
                DrawSoundBar(::stopMp3,::playMp3,SP.playing.value,SP.soundloaded.value)
            }
            //DrawSoundBar(::stopMp3,::playMp3,SP.playing.value)
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun DrawSongScreen(sApp: StatusApp,originalTransLink: OriginalTransLink,lstate: LazyListState){
    Box(Modifier.background(color = Color.Transparent)) {
        Row(
            Modifier
                //.background(if (SP.initialized.value) Color.Black else Color.Red)
                //.background(Color.Blue)
                .fillMaxHeight(1F)
                .fillMaxWidth(1f))
                {
    //                Text("primera row  ${SP.txt.value}")
    //                if(sApp.currentStatus.value==AppStatus.Idle){
                        Timber.d("DrawArticle       <--------------------------------")
                        drawArticle(sApp = sApp,originalTransLink,lstate,true )
    //                }
           }
        //Divider()
        Row(

            //verticalAlignment = Alignment.Bottom,
            //horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                //.background(MaterialTheme.colors.surface)
                .background(Color.Transparent)
                //.background(if (SP.soundloaded) Color.Green else Color.Blue)
                //.height(75.dp)
                //.padding(horizontal = 50.dp)
                //.fillMaxHeight()
                .padding(bottom = 2.dp)
                .border(1.dp, Color.Gray,shape = RoundedCornerShape(15.dp))
                .fillMaxWidth(0.95f)
        ){
            myCard2 {
                //NSound()
                DrawSoundBar(::playMp3, ::stopMp3, SP.playing.value,SP.soundloaded.value)
            }

        }

    }
}

suspend fun controlMp3Time(){
//    return
    while(SP.jobTimer.value) {
        delay(500)
//        Timber.d("change current time playing=${SP.playing.value}")
        if(SP.playing.value) {
            Timber.d("change current time playing")
            SP.txt.value = "CAOA ${SP.mp.currentPosition.toLong().milisToMinSec()}"
            SP.currTime.value = SP.mp.currentPosition  //SP.mp.currentPosition
        }
    }
    Timber.d("FINISH CONTROL TIME!!!")
}

fun stopMp3(){
    SP.mp.pause()
    SP.playing.value=false
}
fun playMp3(){
    Timber.d("PPPPPPPPPLAYYYY !!!!!")

    SP.mp.start()
    SP.playing.value=true
}



fun getTimeSlider():String{
    val t = if(SP.futureTime.value>0) SP.futureTime.value.toLong() else SP.currTime.value.toLong()
    return t.milisToMinSec()
}

@Preview
@Composable
fun DrawSoundBar(play:()->Unit={},stop:()->Unit={},playing:Boolean=false,loaded:Boolean=false){
    var checked by remember { mutableStateOf(false) }
    val clr=if(SP.duration.value==0 && SP.soundloaded.value) Color.LightGray else Color.Unspecified
    val tint by animateColorAsState(if (checked) Color(0xFFEC407A) else Color(0xFFB0BEC5))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement =  Arrangement.SpaceBetween, modifier = Modifier.background(clr)){

    IconButton(onClick = { play() }, enabled = !playing && loaded) {
        Icon(Icons.Filled.PlayArrow,contentDescription = null)
    }
    IconButton(onClick = { stop() }, enabled = playing && loaded) {
        Icon(Icons.Filled.Pause,contentDescription = null)
    }

    var sliderPosition = if(SP.futureTime.value>0) SP.futureTime.value else SP.currTime.value.toFloat()
        Timber.d(sliderPosition.toString())
    Box() {
        Text(modifier = Modifier.align(Alignment.TopCenter),text= getTimeSlider(), style = MaterialTheme.typography.caption)
        Slider(
            value = sliderPosition,
            onValueChange = {
                Timber.d("SLIDER ON VALUE CHANGE : $it")
                sliderPosition = it
                SP.futureTime.value=it
            },
            valueRange = 0f..SP.duration.value.toFloat(),
            onValueChangeFinished = {
                SP.currTime.value=SP.futureTime.value.toInt()
                SP.mp.seekTo(SP.currTime.value)
                SP.futureTime.value=0f
            },
           // modifier = Modifier.padding(top=4.dp, bottom = 4.dp)
        )
        Text(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 10.dp),text=SP.duration.value.toLong().milisToMinSec(),style = MaterialTheme.typography.caption)
    }
    }
}
