package com.begemot.inreader

import androidx.compose.foundation.*
//import androidx.compose.foundation.Box
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.begemot.inreader.model.TransClass
import com.begemot.kclib.FlowRowX
//import com.begemot.kclib.ZFlowRowX
//import com.begemot.inreader.layout.KFlowRow

import com.begemot.kclib.KText2
import com.begemot.knewscommon.ListPinyin
import timber.log.Timber



@Composable
fun headlinesScreen(statusApp: StatusApp) {
    statusApp.currentBackScreen = Screens.NewsPapersScreen
    Timber.d("->${statusApp.status()}")
    LaunchedEffect(statusApp.lang) {
        statusApp.vm.headLines.getLines(statusApp)
    }
    val status = statusApp.currentStatus.value
    when (status) {
        is AppStatus.Loading -> draw_Headlines(statusApp)
        is AppStatus.Error   -> displayError(status.sError, status.e, statusApp)
        is AppStatus.Idle    -> draw_Headlines(statusApp)
        is AppStatus.Refreshing -> {}
    }
    Timber.d("<- ${status.toString()}")
}



@Composable
fun draw_Headlines(statusApp: StatusApp) {
    val lstate = rememberLazyListState(statusApp.vm.scrollposHL)
    Timber.d("${statusApp.status()}  NItems ${statusApp.vm.headLines.listHL.size}")
    //if(statusApp.currentStatus==AppStatus.Loading) return
    val original = remember { mutableStateOf(true) }
    resfreshWraper(statusApp.currentStatus.value == AppStatus.Loading || statusApp.currentStatus.value == AppStatus.Refreshing) {
        LazyColumn(state = lstate) {
            itemsIndexed(statusApp.vm.headLines.listHL) { index, it ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = 7.dp,
                    modifier = Modifier.padding(2.dp)
                ) {

                    Column() {
                        val bplaytext = remember { mutableStateOf(false) }
                        Column(modifier = Modifier.clickable(onClick = {
                            //Timber.d("CLICKED!!!!!!!")
                            original.value = true; bplaytext.value = true
                        })) {
                            //KText2(txt = it.kArticle.title, size = statusApp.fontSize.value)
                            DrawText(text = it.kArticle.title, lPy =it.romanizedo , sApp =statusApp )
                            //DrawPinyn(translated = it.kArticle.title, sApp =statusApp , lPy =it.romanizedo )
                        }
                        if (it.translated.length > 0) {
                            Box(Modifier.clickable(onClick = {
                                original.value = false; bplaytext.value = true
                            })) {
                                //KText2(txt = it.translated, size = statusApp.fontSize.value)
                                DrawText(text = it.translated, lPy =it.romanizedt , sApp =statusApp )
                               // DrawPinyn(it.translated, sApp = statusApp, it.romanizedt)
                            }

                        }
                        if (it.kArticle.link.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(horizontal = 11.dp)
                              ) {

                                Icon(
                                    //vectorResource(id = R.drawable.ic_link_24px),
                                    painterResource(id =R.drawable.ic_link_24px),
                                    modifier=Modifier.clickable(
                                        onClick = {
                                            statusApp.vm.scrollposHL = index
                                            statusApp.currentScreen.value =
                                                Screens.FullArticleScreen(it)
                                        }),contentDescription = ""

                                )
                            }
                        }
                        if (bplaytext.value) {
                            Timber.d("let's play text  original=${original.value}")
                            if(original.value){
                                if(statusApp.currentNewsPaper.olang.equals("zh"))
                                    PlayText22(bplayText = bplaytext, transclass = TransClass.WithPinYin(it.romanizedo.lPy) , sApp =statusApp , original = true )
                                else
                                PlayText22(bplayText = bplaytext, transclass =TransClass.NoPinYin(it.kArticle.title.split(" ")) , sApp =statusApp , original = true )
                            }else{
                                if(statusApp.lang.equals("zh"))
                                  PlayText22(bplayText = bplaytext, transclass =TransClass.WithPinYin(it.romanizedt.lPy) , sApp =statusApp , original = false )
                                else
                                    PlayText22(bplayText = bplaytext, transclass =TransClass.NoPinYin(it.translated.split(" ")) , sApp =statusApp , original = false )
                            }



                        }//else Timber.d("bplaytext=${bplaytext.value}")
                    }
                }
            }
        }
    }
}

@Composable
fun DrawText(text:String,lPy:ListPinyin,sApp: StatusApp){
    if(lPy.lPy.size>0) DrawPinyn(translated = text, sApp =sApp , lPy =lPy )
    else DrawPinynNone(translated = text, sApp = sApp, lPy =lPy )
}





@Composable
fun DrawPinyn(translated: String, sApp: StatusApp, lPy: ListPinyin) {
//    Timber.d("drawpinyin mode ${sApp.romanized}  olang lang ${sApp.lang}")
    if (sApp.lang != "zh" && sApp.lang != "zh-TW") {
 //       DrawPinynNone(translated, sApp, lPy); return
    }  //To be solved!
    if (sApp.romanized == 0) DrawPinynSimple(translated, sApp, lPy)
    if (sApp.romanized == 1) DrawPinynComplete(translated, sApp, lPy)
    if (sApp.romanized == 2) DrawPinynNone(translated, sApp, lPy)
}



@Composable
fun DrawPinynComplete(translated: String, sApp: StatusApp, lPy: ListPinyin) {
    Timber.d("DrawPinYin->${lPy.lPy}")

    Column(Modifier.padding(start = 6.dp, end = 2.dp)) {
        //KText2(txt = translated, size = sApp.fontSize.value)

        FlowRowX(mainAxisSpacing = 3.dp) {
            lPy.lPy.forEach {
                Column(
                    //Modifier.border(BorderStroke(1.dp, Color.LightGray)).padding(start = 0.dp, end = 0.dp)
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //KText2(it.word,size = sApp.fontSize.value)
                    //KText2(it.romanized,size = sApp.fontSize.value)

                    Text(
                        it.w,
                        style = MaterialTheme.typography.h5,
                        fontSize = sApp.fontSize.value.sp
                        //fontSize = TextUnitType.Sp(sApp.fontSize.value),
                        //textAlign = TextAlign.Center
                        //textAlign = TextAlign.End
                    )
                    Text(
                        it.r.replace("\\s".toRegex(), ""), //fuck white spaces
                        style = MaterialTheme.typography.h5,
                        fontSize = sApp.fontSize.value.sp
                    )
                }
            }
        }
    }
}


@Composable
fun DrawPinynNone(translated: String, sApp: StatusApp, lPy: ListPinyin) {
    //Column() {
    KText2(txt = translated, size = sApp.fontSize.value)
    //}
}


@Composable
fun DrawPinynSimple(translated: String, sApp: StatusApp, lPy: ListPinyin) {
    Timber.d("drawPinyin Simple  ${sApp.romanized}")
    Column() {
        KText2(txt = translated, size = sApp.fontSize.value)
        KText2(txt = getOnlyPinyin(lPy), size = sApp.fontSize.value)
    }
}

fun getOnlyPinyin(lPy: ListPinyin): String {
    Timber.d("lPy  $lPy")
    val strb = StringBuilder()
    lPy.lPy.forEach { it -> strb.append(it.r.replace("\\s".toRegex(), "") + " ") }
    return strb.toString()
}