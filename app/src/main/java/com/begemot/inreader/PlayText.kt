package com.begemot.inreader

import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.begemot.inreader.model.TText
import com.begemot.inreader.model.TransClass
import com.begemot.kclib.FlowRowX
import com.begemot.kclib.KWindow2
import com.begemot.knewscommon.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*


class PlayTextParams(val sApp: StatusApp) {
    var larrselected by mutableStateOf(false)
    var rarrselected by mutableStateOf(false)
    var oLang by mutableStateOf("")
    var tLang by mutableStateOf("")
    var searching by mutableStateOf(false)

    var tTranslated by mutableStateOf(TText())
    var tSelected by mutableStateOf("")
    var tOriginal by mutableStateOf("")

    fun anyarrowSelected(): Boolean = larrselected || rarrselected
    fun status():String="pTP-> olang=$oLang tlang=$tLang  selected='$tSelected' translated='$tTranslated'"
}


lateinit var t2: TextToSpeech

fun initSpeak(lan: String) {
    Timber.d("INIT SPEAK!!!!!  lan $lan  ")
    var tstatus = ""
    var result = 0
    var msg = ""
    t2 = TextToSpeech(
        App.lcontext,
        TextToSpeech.OnInitListener { status ->
            tstatus = status.toString()
            if (status != TextToSpeech.ERROR) {
                //Timber.d("SETLANGUAGE  TEXTTOSPEECH  zlan $zlan lan $lan")
                result = t2.setLanguage(Locale.forLanguageTag(lan))
                if (result == TextToSpeech.LANG_MISSING_DATA) msg = "Missing data"
                if (result == TextToSpeech.LANG_NOT_SUPPORTED) msg = "Lang not supported"
            }
        })
    Timber.d("end Init speak  $msg")
}

fun setlangSpeak(lan: String, pTP: PlayTextParams) {
    Timber.d("lan :$lan pTP.olang:${pTP.oLang}  pTP.tLang:${pTP.tLang}")
    val lng=pTP.sApp.vm.tLang.getLang(lan)
    var msg = ""
    val result = t2.setLanguage(Locale.forLanguageTag(lan))
    if (result == TextToSpeech.LANG_MISSING_DATA) msg = "Missing data"
    if (result == TextToSpeech.LANG_NOT_SUPPORTED) msg = "Lang not supported"
    t2.setSpeechRate(lng.speed)
    t2.setPitch(lng.tone)
}


fun spc(txt: String, lan: String, pTP: PlayTextParams) {
    Timber.d("txt $txt  lang $lan")
    setlangSpeak(lan,pTP)
    t2.speak(txt, TextToSpeech.QUEUE_FLUSH, null, null)
    Timber.d("end spc $lan")
}


@Composable
fun PlayText22(
    bplayText: MutableState<Boolean>,
    transclass: TransClass,
    sApp: StatusApp,
    original: Boolean
) {
    val pTP = remember { PlayTextParams(sApp) }

    if(pTP.oLang.isEmpty()) {
        pTP.oLang = if (original) sApp.currentNewsPaper.olang else sApp.lang
        pTP.tLang = if (original) sApp.lang else sApp.currentNewsPaper.olang
    }

    pTP.tOriginal = transclass.getText()
    //pTP.tSelected=""
    initSpeak( if (original)  pTP.oLang else  pTP.tLang )

    Dialog(onDismissRequest = { t2.shutdown(); bplayText.value = false; sApp.vm.tLang.save() }) {
        KWindow2(4) {
            resfreshWraper2(loading = pTP.searching) {
                SelectableTextAA(pTP,transclass)
                BottomPlayText(pTP)
                DrawTranslatedText(pTP)
            }
        }
    }
}

enum class ClikedSoundPitch {
    None, Left, Right
}

//.border(BorderStroke(1.dp, Color.Green))
@Composable
fun BottomPlayText(
    pTP: PlayTextParams
) {
    var csoundpitch = remember { mutableStateOf(ClikedSoundPitch.None) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.LightGray))
    ) {
        SoundControl(pTP)
        if (pTP.anyarrowSelected())
            SoundPitch(sp = csoundpitch.value, pTP)
    }
}


@Composable
fun SoundControl(pTP: PlayTextParams) {
    val colorSpeaker = if(pTP.sApp.kt.value.theme.isLight) Color.Black else Color.White
    val tup = if (pTP.tSelected.equals("")) pTP.tOriginal else pTP.tSelected
    val tintL = if(pTP.larrselected) Color(0xFFEC407A ) else colorSpeaker
    val tintR = if(pTP.rarrselected) Color(0xFFEC407A ) else colorSpeaker
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .clickable(onClick = { spc(tup, pTP.oLang, pTP) })
        ) {
            Icon(painterResource(id = R.drawable.ic_volume_up24px),contentDescription = "",tint=tintL)
            Text("(${pTP.oLang})")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(alignment = Alignment.Center)
        ) {
            IconToggleButton(
                checked = pTP.larrselected,
                onCheckedChange = {
                    pTP.larrselected = it
                    pTP.rarrselected = false
                }) {
                val tint by animateColorAsState(
                    if (pTP.larrselected) Color(0xFFEC407A) else Color(
                        0xFFB0BEC5
                    )
                )
                Icon(painterResource(id = R.drawable.ic_arrow_back_24px), tint = tint,contentDescription = "")
            }

            Icon(painterResource(id = R.drawable.ic_music_note_24px),contentDescription = "")
            if (!pTP.tTranslated.getText().equals("")) {
                IconToggleButton(
                    checked = pTP.rarrselected,
                    onCheckedChange = {
                        pTP.rarrselected = it
                        pTP.larrselected = false
                    }) {
                    val tint by animateColorAsState(
                        if (pTP.rarrselected) Color(0xFFEC407A) else Color(
                            0xFFB0BEC5
                        )
                    )
                    Icon(painterResource(id = R.drawable.ic_arrow_forward_24px), tint = tint,contentDescription = "")
                }
            }
        }
        if (!pTP.tTranslated.getText().equals("")) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(alignment = Alignment.CenterEnd)
                    .clickable(onClick = { spc(pTP.tTranslated.getText(), pTP.tLang, pTP) })
            ) {
                SelectableLang(pTP)
                Text("(${pTP.tLang})")
                Icon(painterResource(id = R.drawable.ic_volume_up_24px_1__1_),tint=tintR,contentDescription = "")
            }
        }
    }
    //DisposableEffect(Unit) { onDispose(onDisposeEffect = { /*vaaTODO*/ }) }
}


@Composable
fun SelectableLang(pTP: PlayTextParams){
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val lselectedLang=pTP.sApp.vm.jLang.getSelectedLangs(pTP.oLang,pTP.tLang)
    Box() {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            lselectedLang.forEach { it ->
                DropdownMenuItem(onClick = {
                    pTP.tLang = it.acronim
                    expanded=false
                    transtext(pTP,scope)

                    Timber.d("l2-->${pTP.tLang}")
                }) {
                    //Timber.d("tostring->${pTP.tLang}")
                    Text(it.toString())
                }
            }
        }
    }
}

fun transtext(pTP: PlayTextParams,scope: CoroutineScope){
    Timber.d(pTP.status())
    pTP.searching = true
    scope.launch(Dispatchers.IO) {
        pTP.tTranslated = pTP.sApp.vm.tCache.getTrans3(pTP.tSelected, pTP.oLang, pTP.tLang)
        Timber.d("translated=${pTP.tTranslated}")
        //delay(1000)
        pTP.searching = false
    }

}


@Composable
fun SoundPitch(sp: ClikedSoundPitch, pTP: PlayTextParams) {
    val cl= if(pTP.larrselected) pTP.oLang else pTP.tLang
    Timber.d("CL ---> $cl")

    val curlang = pTP.sApp.vm.tLang.getLang(cl)

    val sliderPitch: MutableState<Float> = remember { mutableStateOf( curlang.tone) }
    val sliderRate: MutableState<Float> = remember  { mutableStateOf(curlang.speed) }

    //Timber.d("Selected lang  $curlang   sliderPitch=${sliderPitch.value} sliderTone=${sliderRate.value}")
    val l="${sliderPitch.value} ${sliderRate.value}"

    Timber.d("sound pitch")
    Column(modifier = Modifier.padding(1.dp), verticalArrangement = Arrangement.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tone",modifier = Modifier.width(50.dp))
            Slider(
                value = curlang.tone,//sliderPitch.value, //curlang.tone, // ,
                onValueChange = {
                     sliderPitch.value = it
                     curlang.tone=it
                     pTP.sApp.vm.tLang.setChanged()
                     Timber.d("onvaluechange $it") },
                valueRange = 0.1f..2.0f
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Speed",modifier = Modifier.width(50.dp))
            Slider(
                value = curlang.speed,//sliderRate.value,//curlang.speed,
                onValueChange = { curlang.speed = it; sliderRate.value = it ;Timber.d("onvaluechange $it") },
                valueRange = 0.1f..2.0f
            )
        }
}
    }


    //.border(BorderStroke(1.dp, Color.Yellow))

    @Composable
    fun DrawTranslatedText(
        pTP: PlayTextParams,

    ) {
        Timber.d(pTP.status())
        val scrollState: ScrollState = rememberScrollState(0)
        Box(modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 250.dp)) {
            val tt = pTP.tTranslated
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)) {
                when (tt) {
                    is TransClass.WithPinYin -> {
                        Timber.d("DrawPinYin")
                        DrawPy(lPy = tt.lPy, sApp = pTP.sApp)
                    }
                    is TransClass.NoPinYin -> {
                        Timber.d("DrawNoPinYin  ${tt.getText()}")
                        TextX(tt.getText(), pTP.sApp.fontSize.value, true)
                    }
                    else -> {
                    }
                }
            }
        }
    }

    //.border(BorderStroke(1.dp, Color.Green))

    @Composable
    fun SelectableTextAA(pTP: PlayTextParams,transclass: TransClass) {
        Box(
            contentAlignment = Alignment.CenterStart, modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
        ) {
            val scrollState: ScrollState = rememberScrollState(0)
            Column(modifier=Modifier.verticalScroll(scrollState)){
                when (transclass) {
                    is TransClass.WithPinYin ->  SelectableTextPy(pTP,transclass.lPy)
                    is TransClass.NoPinYin   ->  SelectableText(pTP,transclass.lStr.joinToString(" "))
                    }
                }
            }
    }

@Composable
fun SelectableText(pTP: PlayTextParams,txt: String) {
        val scope = rememberCoroutineScope()
        Timber.d("$txt")
        val lSo = remember { ListSelectableObjects(txt.split(" "))}
        FlowRowX(mainAxisSpacing = 2.dp) {
            lSo.lSO.forEach {
                TextX(
                    it.x.first,
                    pTP.sApp.fontSize.value,
                    it.x.second.value,
                    Modifier.clickable( onClick = { onClickText2(pTP,lSo,it,scope)} )
                )
            }
        }
        //pTP.tTranslated = b.value
    }


    @Composable
    fun SelectableTextPy(pTP: PlayTextParams,lPy: List<Pinyin>) {
        val scope = rememberCoroutineScope()
        Timber.d("lPy  size ${lPy.size}")
        val lSo = remember { ListSelectableObjects(lPy) }
        FlowRowX(mainAxisSpacing = 2.dp) {
            lSo.lSO.forEach {
                Column(
                    Modifier//.border(BorderStroke(0.dp, Color.LightGray))
                        .padding(start = 2.dp, end = 2.dp)
                        .clickable(onClick = { onClickText2(pTP, lSo, it, scope) }),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextX(
                        it.getObject().w,
                        pTP.sApp.fontSize.value,
                        it.isselected(),
                    )
                    TextX(
                        it.getObject().r.replace("\\s".toRegex(), ""),
                        pTP.sApp.fontSize.value,
                        it.isselected(),
                    )
                }
            }
        }
}


@Composable
fun DrawPy(lPy: List<Pinyin>, sApp: StatusApp) {
        FlowRowX(mainAxisSpacing = 2.dp) {
            lPy.forEach {
                Column(
                    Modifier//.border(BorderStroke(0.dp, Color.LightGray))
                        .padding(start = 2.dp, end = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextX(
                        it.w,
                        sApp.fontSize.value,
                        true,
                        Modifier.clickable(onClick = { })
                    )
                    TextX(
                        it.r.replace("\\s".toRegex(), ""),
                        sApp.fontSize.value,
                        true,
                        Modifier.clickable(onClick = { })
                    )
                }
            }
        }
}

@Composable
fun TextX(
        txt: String,
        size: Int,
        selected: Boolean,
        modifier: Modifier = Modifier
    ) {
        Box(modifier = Modifier.padding(1.dp, 0.dp, 1.dp, 0.dp)) {
            if (selected) Text(
                text = txt,
                style = MaterialTheme.typography.h5,
                fontSize = size.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = modifier
            )
            else Text(
                text = txt,
                style = MaterialTheme.typography.h5,
                fontSize = size.sp,
                fontStyle = FontStyle.Normal,
                modifier = modifier
            )

        }
    }

fun <T> prevOrNextSelectedQ2(
    z: ListSelectableObjects<T>, //List<Pair<T, MutableState<Boolean>>>,
    x: SelectableObject<T>       //Pair<T, MutableState<Boolean>>
): Boolean {
     with(z.lSO){
//         Timber.d("List of Selectable Objects size ${size} ${toString()}")
//         Timber.d("Selectable Object : ${x.getObject().toString()}  ${x.isselected()}")
         var prev = false
         var next = false
         val iclickedtext = indexOf(x)
         if (iclickedtext == -1 || iclickedtext==0) prev = false
         else prev = get(iclickedtext - 1).isselected()
         if (iclickedtext + 1 == size) next = false
         else next = get(iclickedtext + 1).isselected()
         return prev || next
     }
}

class SelectableObject<T>(x:T){
    val x:Pair<T, MutableState<Boolean>> = Pair(x,mutableStateOf(false))
    fun isselected():Boolean = x.second.value
    fun setSelectState(state:Boolean){x.second.value=state}
    fun getObject():T =  x.first
    fun getString() : String = if (x.first is Pinyin) (x.first as Pinyin).w   else x.first as String  //yes no maybe

}

class ListSelectableObjects<T>(l:List<T>){
    val lSO = l.map{ it->SelectableObject(it) }
    fun unselectAll(){ lSO.forEach { it.setSelectState(false) }}
    fun getSelectedText():String {
        return lSO.filter { it.isselected() }.fold("") { sum, element -> "$sum ${element.getString()}" }
    }
}

fun <T>  onClickText2(
    pTP:PlayTextParams,
    lSo: ListSelectableObjects<T>,  //ListSelectableObjects<T>
    sO: SelectableObject<T>, // Pair<T, MutableState<Boolean>>,   //SelectableObject<T>
    scope:CoroutineScope
) {
    Timber.d("I've clicked: '${sO.getObject().toString()}'   Ttxt selected->${sO.isselected()} pTP orig=${pTP.oLang}  pTP trans=${pTP.tLang}")
    val currentstateselected = sO.isselected()
    if (prevOrNextSelectedQ2(lSo, sO)) {
        Timber.d("prev or next selected:  ")
        if (currentstateselected)  lSo.unselectAll()
        else sO.setSelectState(true)
    } else {
//        Timber.d("prev or next NOT selected -> Unselect All")
        lSo.unselectAll()
        sO.setSelectState(!currentstateselected)
//        Timber.d("txt selected? ${sO.isselected()}=${sO.getString()}")
    }
    val tt=lSo.getSelectedText()
    pTP.tSelected = tt
    Timber.d("text selected (the fold) -> $tt")
    scope.launch(Dispatchers.IO) {
        pTP.searching = true
        val sPinyin = pTP.sApp.vm.tCache.getTrans3(tt, pTP.oLang, pTP.tLang)
        Timber.d(" getTranstext = ${sPinyin.getText()}")
        pTP.tTranslated = when(sPinyin){
            is TransClass.WithPinYin -> if(sO.isselected()) TransClass.WithPinYin(sPinyin.lPy) else TransClass.NoTrans()
            is TransClass.NoPinYin   -> if(sO.isselected()) TransClass.NoPinYin(sPinyin.lStr) else TransClass.NoTrans()
            is TransClass.NoTrans    -> TransClass.NoTrans()
        }
        //delay(1000)
        //pTP.tTranslated = bbb.value
        pTP.searching = false
    }
}

//Max 528  440
// Max 718
