package com.begemot.myapplicationz.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.begemot.kclib.KText2
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.*
import com.begemot.myapplicationz.layout.DrawText
import com.begemot.myapplicationz.layout.ListModifier
import com.begemot.myapplicationz.layout.myCard
import com.begemot.myapplicationz.model.articleHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import timber.log.Timber


@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun ArticleScreen(originalTransLink: OriginalTransLink, sApp: StatusApp) {
    val lstate= rememberLazyListState(sApp.vm.article.iInitialItem.value)
    val q = articleHandler(sApp.currentNewsPaper.handler,originalTransLink.kArticle.link,sApp.lang)
    Timber.d("${sApp.currentNewsPaper.desc} ${q.nameFileArticle()} link ->${originalTransLink.kArticle.link} l2->${originalTransLink.kArticle.l2}  initial position ${sApp.vm.article.iInitialItem.value}")
    Timber.d(sApp.status2())
    sApp.currentBackScreen = Screens.HeadLinesScreen

    LaunchedEffect(sApp.lang) {
        sApp.currentStatus.value = AppStatus.Loading
        sApp.vm.article.reinicializeArticle(q,lstate)
        sApp.currentLink = originalTransLink.kArticle.link
        sApp.arethereBookMarks = !sApp.vm.article.bookMarks.value.bkMap.isEmpty()
        sApp.vm.article.getTransArt(q,sApp)
        //delay(2000)
        lstate.scrollToItem(sApp.vm.article.iInitialItem.value)
    }
    val status = sApp.currentStatus.value
    when (status) {
        is AppStatus.Loading -> drawArticle(sApp = sApp,originalTransLink,lstate )
        is AppStatus.Error -> displayError(status.sError, status.e, sApp)
        is AppStatus.Idle -> drawArticle(sApp = sApp,originalTransLink,lstate)
    }
}

suspend fun loadArticle(originalTransLink: OriginalTransLink, sApp: StatusApp,lstate:LazyListState){
    Timber.d(sApp.status2())
    sApp.currentStatus.value = AppStatus.Loading
    val q = articleHandler(sApp.currentNewsPaper.handler,originalTransLink.kArticle.link,sApp.lang)
    sApp.vm.article.reinicializeArticle(q,lstate)
    sApp.currentLink = originalTransLink.kArticle.link
    sApp.arethereBookMarks = !sApp.vm.article.bookMarks.value.bkMap.isEmpty()
    sApp.vm.article.getTransArt(q,sApp)
}


fun bookMark2(index:Int,sApp: StatusApp):Modifier {
    val bkmark = sApp.vm.article.bookMarks.value.isBookMark(index)
    val initial = (sApp.vm.article.iInitialItem.value == index)
    var col = Color.Transparent
    if (bkmark && initial) col = Color.Magenta
    if (bkmark && !initial) col = Color.Cyan
    if (!bkmark && initial) col = Color.Red
    return Modifier.border(  //then
        BorderStroke(1.dp, col),
        RoundedCornerShape(6.dp)
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun drawArticle(sApp: StatusApp,otl:OriginalTransLink, lstate: LazyListState,skip:Boolean=false) {
    val lItems = sApp.vm.article.lArticle.value
    Timber.d("Start Draw  initial position ${sApp.vm.article.iInitialItem.value} nItems=${lItems.size}")
    val cs = rememberCoroutineScope()
    LaunchedEffect(key1 = sApp.vm.article.iInitialItem, block ={lstate.scrollToItem(sApp.vm.article.iInitialItem.value)} )
    resfreshWraper(sApp.currentStatus.value == AppStatus.Loading,skip) {
        val original = remember { mutableStateOf(true) }
        LazyColumn(state = lstate,modifier = ListModifier()) {
            itemsIndexed(lItems) { index, it ->
                if(sApp.modeBookMark) {
                    if (sApp.vm.article.bookMarks.value.isBookMark(index) ) {
                        KText2(txt = "$index",size=sApp.fontsize)
                        articleCard2(index, sApp, original, it, cs, lstate)
                    }
                }
                else
                  articleCard2(index , sApp  , original  , it ,cs,lstate )
             }
        }
    }
    Timber.d("End Draw")
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun articleCard2(index:Int,sApp: StatusApp,original:MutableState<Boolean>,it:OriginalTrans,cs:CoroutineScope,lstate: LazyListState){
    myCard(
        //shape = RoundedCornerShape(8.dp),
        //elevation = 1.dp,
        mod = bookMark2(index = index, sApp =sApp ), //Modifier
            //.bookMark(index, sApp),
            //.padding(2.dp)
            //.fillMaxWidth(),//.fillParentMaxWidth()//.bookMark()
        onClik={}
    ) {
        val bplaytext = remember { mutableStateOf(false) }
        Column() {
        DrawArticleOT(cs = cs, sApp =sApp , index =index , original =original , bplaytext = bplaytext, lstate =lstate , it =it,true )
        DrawArticleOT(cs = cs, sApp =sApp , index =index , original =original , bplaytext = bplaytext, lstate =lstate , it =it ,false)
        if (bplaytext.value) {
               LaunchedEffect(key1 = index){
                lstate.scrollToItem(index)
               }
            PlayText(sApp , bplaytext  , true , index  , original.value  , SourcePTP.article,lstate,cs  )
        }

     }
  }
}


@Composable
fun DrawArticleOT(cs: CoroutineScope, sApp: StatusApp, index: Int, original: MutableState<Boolean>, bplaytext: MutableState<Boolean>, lstate: LazyListState, it: OriginalTrans,original2:Boolean){
    Column(modifier = Modifier
        .fillMaxWidth()
        .pointerInput(Unit) {
            detectTapGestures(

                onDoubleTap = {
                    cs.launch {
                        if (!sApp.modeBookMark) onDoubleTapcard(sApp, index)
                    }
                },
                onTap = {
                    Timber.d("single tap $index")
                    onclikcard(sApp, cs, index, original, bplaytext, onClickBookMark = {
                        val cv = CoroutineScope(Dispatchers.Main)
                        cv.launch {
                            lstate.scrollToItem(it)
                        }
                    }, original2)
                }

            )
        }

    ) {
        if(original2) DrawText(text = it.original, lPy = it.romanizedo, sApp = sApp)
        else DrawText(text = it.translated, lPy = it.romanizedt, sApp = sApp)
    }

}

fun onclikcard(sApp: StatusApp, cs: CoroutineScope, index: Int,original: MutableState<Boolean>, bplaytext:MutableState<Boolean>,onClickBookMark:(Int)->Unit,original2: Boolean){
    if (sApp.modeBookMark) {
        Timber.d("will scrooll to $index")
        sApp.modeBookMark = false
        //cs.launch {
            //val idx=350
            //sApp.vm.article.lzls.animateScrollToItem(index)
            //sApp.vm.article.iInitialItem.value=index
            onClickBookMark(index)
            //ls.animateScrollToItem(index)
            //Timber.d("scrooll to $index ${sApp.vm.article.lzls.firstVisibleItemIndex}")

        //}

        //return@clickable
    } else {

        original.value = original2; bplaytext.value = true
    }

}

suspend fun onDoubleTapcard(sApp: StatusApp,index:Int){
    Timber.d("double tap  $index")
    sApp.vm.article.iInitialItem.value=index
    sApp.vm.article.storeLastIndex(index)
    //sApp.vm.article.lzls.scrollToItem(index)

}

//Max 222 256 206 211 191