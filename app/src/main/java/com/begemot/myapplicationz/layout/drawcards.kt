package com.begemot.myapplicationz.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.begemot.kclib.FlowRowX
import com.begemot.kclib.KText2
import com.begemot.knewscommon.ListPinyin
import com.begemot.myapplicationz.StatusApp
import timber.log.Timber

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
    Timber.d("DrawPinYin")//->${lPy.lPy}")

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
    if(!translated.isEmpty())
        KText2(txt = translated, size = sApp.fontSize.value)
    //}
}




@Composable
fun DrawPinynSimple(translated: String, sApp: StatusApp, lPy: ListPinyin) {
    Timber.d("drawPinyin Simple")//  ${sApp.romanized}")
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