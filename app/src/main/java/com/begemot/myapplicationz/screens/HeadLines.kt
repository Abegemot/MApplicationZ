 package com.begemot.myapplicationz.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.Box
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.begemot.knewscommon.OriginalTransLink
//import com.begemot.kclib.ZFlowRowX
//import com.begemot.inreader.layout.KFlowRow

import com.begemot.myapplicationz.*
import com.begemot.myapplicationz.R
import com.begemot.myapplicationz.layout.DrawText
import com.begemot.myapplicationz.layout.ListModifier
import com.begemot.myapplicationz.layout.myCard2
import timber.log.Timber



@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun headlinesScreen(sApp: StatusApp) {
    sApp.currentBackScreen = Screens.NewsPapersScreen
//    Timber.d("->${statusApp.status()}")
    Timber.d("HEADLINE SCREEN ${sApp.currentStatus.value}  Current news paper: ${sApp.currentNewsPaper}")
    LaunchedEffect(sApp.lang) {
        Timber.d("LaunchedEffect")
        sApp.vm.headLines.getLines(sApp,sApp.currentNewsPaper)
    }
    val status = sApp.currentStatus.value
    when (status) {
        is AppStatus.Loading -> Draw_Headlines(sApp)
        is AppStatus.Error   -> displayError(status.sError, status.e, sApp)
        is AppStatus.Idle    -> Draw_Headlines(sApp)
        is AppStatus.Refreshing -> {}
    }
//    Timber.d("<- ${status.toString()}")
}



@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun Draw_Headlines(sApp: StatusApp) {
    //val lstate = rememberLazyListState(sApp.vm.headLines.scrollposHL)
    val lstate = rememberLazyListState(sApp.vm.headLines.currChapter.value)
    Timber.d("N Headlines ${sApp.vm.headLines.listHL.size}   <---------------------")
    //if(statusApp.currentStatus==AppStatus.Loading) return
    val cs = rememberCoroutineScope()
    val original = remember { mutableStateOf(true) }
    resfreshWraper(sApp.currentStatus.value == AppStatus.Loading || sApp.currentStatus.value == AppStatus.Refreshing) {
        LazyColumn(state = lstate,modifier = ListModifier()) {
            itemsIndexed(sApp.vm.headLines.listHL) { index, it ->
                myCard2(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.drawColoredShadow(Color.Green)
                        .currentChapter(index, sApp)
                ) {
                    Column(modifier = Modifier ) {
                        val bplaytext = remember { mutableStateOf(false) }
                        TText(bplaytext,it,sApp,index,original,true)
                        if (it.translated.length > 0) {
                            TText(bplaytext,it,sApp,index,original,false)
                        }
                        if (bplaytext.value) {
                            Timber.d("let's play text  original=${original.value}")
                            PlayText(
                                sApp ,
                                bplaytext ,
                                bookmarkable = false ,
                                index =index ,
                                original = original.value,
                                SourcePTP.headlines,
                                lstate
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun TText(bplaytext:MutableState<Boolean>,it: OriginalTransLink,sApp: StatusApp,index: Int,original:MutableState<Boolean>,original2:Boolean){
    val printlink = when(original2) {
        true  -> (it.kArticle.link.isNotEmpty() && it.translated.length==0)
        false -> it.kArticle.link.isNotEmpty()
    }
    val frac = if(printlink) 0.92f else 1.0f
    Box(modifier = Modifier
        .fillMaxWidth()
        .detectDoubleTap(
            dtap = { sApp.vm.headLines.storeLastSelectedChapter(sApp, index) },
            stap = { original.value = original2; bplaytext.value = true }
        )) {
            Row(modifier = Modifier.fillMaxWidth(frac)) {
                if (original2) DrawText(text = it.kArticle.title, lPy = it.romanizedo, sApp = sApp)
                else DrawText(text = it.translated, lPy = it.romanizedt, sApp = sApp)
            }
            if (printlink) {
                Row(
                    modifier = Modifier
                        .align(alignment = Alignment.BottomEnd)
                        .padding(end = 2.dp),verticalAlignment = Alignment.Bottom
                ) {
                    DrawLink(sApp = sApp, index = index, it = it)
                }
            }
    }
}

fun Modifier.detectDoubleTap(dtap:(Unit)->Unit,stap:(Unit)->Unit):Modifier{
    return this.then(Modifier.pointerInput(Unit){
        detectTapGestures (
            onDoubleTap = {
                dtap(Unit)
            },onTap = {
                stap(Unit)
            })
    })
}

fun Modifier.currentChapter(index:Int,sApp: StatusApp): Modifier {
    val selectedChap = sApp.vm.headLines.currChapter.value == index
    var col = Color.Transparent
    if (selectedChap ) col = Color.Red
    return then(Modifier.border(
        BorderStroke(1.dp, col),
        RoundedCornerShape(6.dp)
    ))
}


fun Modifier.drawColoredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = this.drawBehind {
    val transparentColor = android.graphics.Color.toArgb(color.copy(alpha = 0.0f).value.toLong())
    val shadowColor = android.graphics.Color.toArgb(color.copy(alpha = alpha).value.toLong())
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}


@Composable
fun DrawLink(sApp: StatusApp,index:Int,it:OriginalTransLink){
    Icon(
        painterResource(id = R.drawable.ic_link_24px),
        modifier=Modifier.clickable(
            onClick = {
                sApp.vm.headLines.scrollposHL = index
                sApp.currentStatus.value=AppStatus.Loading  //?!
                if(it.kArticle.l2.isEmpty()) {
                    Timber.w("going to full Articl Screen to ${it.kArticle.title}")
                    sApp.currentScreen.value = Screens.FullArticleScreen(it)
                }
                else{
                    Timber.w("going to Song Screen to ${it.kArticle.title}")
                    Timber.w("${sApp.status2()}")
                    sApp.vm.headLines.currChapter.value=index         //!!!!!
                    sApp.currentScreen.value = Screens.SongScreen(it)
                }
            }),contentDescription = ""
    )
}

//Max 256 199