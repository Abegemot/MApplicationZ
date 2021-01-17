package com.begemot.myapplicationz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.begemot.kclib.KText2
import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.KResult
import com.begemot.knewscommon.OriginalTrans
import com.begemot.knewscommon.OriginalTransLink
import com.begemot.knewscommon.exWithException

import timber.log.Timber

@ExperimentalLayout
@Composable
fun articleScreen(originalTransLink: OriginalTransLink, sApp: StatusApp) {
    //statusApp.currentBackScreen=backScreenFun()
    Timber.d("${originalTransLink.kArticle.link}")
    sApp.currentBackScreen = Screens.HeadLinesScreen
    val trans3 = remember { mutableStateOf<List<OriginalTrans>>(emptyList()) }
    LaunchedEffect(sApp.lang) {
        trans3.value = getTransArt(sApp, originalTransLink)
    }
    val status = sApp.currentStatus.value
    when (status) {
        //is AppStatus.Loading -> waiting()
        is AppStatus.Loading -> drawArticle(loriginalTranslate = trans3.value, sApp = sApp)
        is AppStatus.Error -> displayError(status.sError, status.e, sApp)
        is AppStatus.Idle -> drawArticle(loriginalTranslate = trans3.value, sApp = sApp)
    }
}

suspend fun getTransArt(sApp: StatusApp, otl: OriginalTransLink): List<OriginalTrans> {
    Timber.d("getTransArt ${sApp.currentNewsPaper.handler}  ${sApp.lang}")
    val resp = exWithException<List<OriginalTrans>, String> {
        sApp.currentStatus.value = AppStatus.Loading
        //throw Exception("BOOM")
        KNews().getArticle(sApp.currentNewsPaper.handler, otl.kArticle.link, sApp.lang)
    }
    when (resp) {
        is KResult.Success -> {
            Timber.d("size ${resp.t.size}"); sApp.currentStatus.value =
                AppStatus.Idle; return resp.t
        }
        is KResult.Error -> {
            Timber.d("error");sApp.currentStatus.value = AppStatus.Error(resp.msg, resp.e)
        }
    }
    return emptyList()
}


@ExperimentalLayout
@Composable
fun drawArticle(loriginalTranslate: List<OriginalTrans>, sApp: StatusApp) {
    val olang = sApp.currentNewsPaper.olang
    resfreshWraper(sApp.currentStatus.value == AppStatus.Loading) {
        val original = mutableStateOf(true)
        LazyColumn() {
            items(loriginalTranslate, itemContent = {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = 7.dp,
                    modifier = Modifier.padding(2.dp).fillParentMaxWidth()
                ) {
                    Column() {
                        val bplaytext = remember { mutableStateOf(false) }
                        Column(modifier = Modifier.clickable(onClick = {
                            original.value = true; bplaytext.value = true
                        })) {
                            DrawText(text = it.original, lPy = it.romanizedo, sApp = sApp)
                            //KText2(it.original, size = sApp.fontSize.value)
                        }
                        Column(modifier = Modifier.clickable(onClick = {
                            original.value = false; bplaytext.value = true
                        })) {
                            //KText2(it.translated, size = sApp.fontSize.value)
                            DrawText(text = it.translated, lPy = it.romanizedt, sApp = sApp)
                            //DrawPinyn(it.translated,sApp = sApp, it.romanizedo)
                        }

                        //Timber.d("bplaytext=${bplaytext.value}")
                        if (bplaytext.value) {
                            //if (original.value) playText(bplaytext, it.original,it.romanized, sApp, true)
                            //else playText(bplaytext, it.translated,it.romanized, sApp, false)
                            if (original.value) {
                                if (sApp.currentNewsPaper.olang.equals("zh"))
                                    PlayText22(
                                        bplayText = bplaytext,
                                        transclass = TransClass.WithPinYin(it.romanizedo),
                                        sApp = sApp,
                                        original = true
                                    )
                                else
                                    PlayText22(
                                        bplayText = bplaytext,
                                        transclass = TransClass.NoPinYin(it.original.split(" ")),
                                        sApp = sApp,
                                        original = true
                                    )
                                //PlayText22(bplayText = bplaytext, transclass =TransClass.NoPinYin(it.original.split(" ")) , sApp =sApp , original = true )
                            } else {
                                if (sApp.lang.equals("zh"))
                                    PlayText22(
                                        bplayText = bplaytext,
                                        transclass = TransClass.WithPinYin(it.romanizedt),
                                        sApp = sApp,
                                        original = false
                                    )
                                else
                                    PlayText22(
                                        bplayText = bplaytext,
                                        transclass = TransClass.NoPinYin(it.translated.split(" ")),
                                        sApp = sApp,
                                        original = false
                                    )
                            }
                        }
                    }
                }
            })
        }
    }
}
