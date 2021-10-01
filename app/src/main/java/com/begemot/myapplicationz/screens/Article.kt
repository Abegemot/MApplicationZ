package com.begemot.myapplicationz.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.begemot.kclib.KText2
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.*
import com.begemot.myapplicationz.layout.DrawText
import com.begemot.myapplicationz.layout.ListModifier
import com.begemot.myapplicationz.layout.myCard
import com.begemot.myapplicationz.model.articleHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import timber.log.Timber


@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun ArticleScreen(originalTransLink: OriginalTransLink, sApp: StatusApp) {
    val lstate= rememberLazyListState(sApp.vm.article.iInitialItem.value)

    Timber.d("link ->${originalTransLink.kArticle.link}  ${sApp.vm.article.iInitialItem.value}")
    sApp.currentBackScreen = Screens.HeadLinesScreen

    LaunchedEffect(sApp.userlang) {
        sApp.currentStatus.value = AppStatus.Loading
        val q = articleHandler(sApp.currentNewsPaper.handler,originalTransLink.kArticle.link,sApp.userlang)
        sApp.vm.article.reinicializeArticle(q,lstate)
        sApp.currentLink = originalTransLink.kArticle.link

        sApp.arethereBookMarks = !sApp.vm.article.bookMarks.value.bkMap.isEmpty()
        sApp.vm.article.getTransArt(q,sApp)
        //delay(2000)
        lstate.scrollToItem(sApp.vm.article.iInitialItem.value)
        //sApp.vm.article.lzls.scrollToItem(sApp.vm.article.iInitialItem.value)
        Timber.d("current last Index ${sApp.vm.article.iInitialItem.value}")
    }
    Timber.d("setting rememberLazyListStateTo : ${sApp.vm.article.iInitialItem.value}")

    //val lstate= remember{ LazyListState(sApp.vm.article.iInitialItem.value) }
    //Timber.d("lstate=${lstate.firstVisibleItemIndex}")
    val status = sApp.currentStatus.value
    when (status) {
        is AppStatus.Loading -> drawArticle(sApp = sApp,originalTransLink,lstate )
        is AppStatus.Error -> displayError(status.sError, status.e, sApp)
        is AppStatus.Idle -> drawArticle(sApp = sApp,originalTransLink,lstate)
    }
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
fun drawArticle(sApp: StatusApp,otl:OriginalTransLink, lstate: LazyListState) {
    Timber.d("zz  initial position ${sApp.vm.article.iInitialItem.value}")
    //val lstate= rememberLazyListState(sApp.vm.article.iInitialItem.value)
    //lstate.scrollToItem(sApp.vm.article.iInitialItem.value)
    val cs = rememberCoroutineScope()

//    val i=sApp.vm.article.nIndex.collectAsState().value
//    Timber.d("i $i")
//    LaunchedEffect(key1 = sApp.vm.article.iInitialItem, block ={lstate.scrollToItem(sApp.vm.article.iInitialItem.value)} )
    LaunchedEffect(key1 = sApp.vm.article.iInitialItem, block ={lstate.scrollToItem(sApp.vm.article.iInitialItem.value)} )
    resfreshWraper(sApp.currentStatus.value == AppStatus.Loading) {
        val original = remember { mutableStateOf(true) }
        val lItems = sApp.vm.article.lArticle.value
        // val lstate = remember{ sApp.lzls }
        LazyColumn(state = lstate,modifier = ListModifier()) {
            itemsIndexed(lItems) { index, it ->
                if(sApp.modeBookMark) {
                    if (sApp.vm.article.bookMarks.value.isBookMark(index) ) {
                        KText2(txt = "$index",size=sApp.fontSize.value)
                        articleCard2(index, sApp, original, it, cs, lstate)
                    }
                }
                else
                  articleCard2(index , sApp  , original  , it ,cs,lstate )
             }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun articleCard2(index:Int,sApp: StatusApp,original:MutableState<Boolean>,it:OriginalTrans,cs:CoroutineScope,lstate: LazyListState){
    //val sc= rememberCoroutineScope()
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
            cs.launch {
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
                    },original2)
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

//Max 222 256