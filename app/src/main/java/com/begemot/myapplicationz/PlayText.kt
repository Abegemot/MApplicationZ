package com.begemot.myapplicationz

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState


import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.begemot.myapplicationz.model.TText
import com.begemot.myapplicationz.model.TransClass
import com.begemot.kclib.FlowRowX
import com.begemot.kclib.KWindow2
import com.begemot.kclib.kTheme
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.screens.OpenBrowser
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt

private val sizeicon=29.dp


enum class SourcePTP{
    headlines,article
}

class PlayTextParams(val sApp: StatusApp,val bookmarkable: Boolean,index:Int=0,val original: Boolean,val source:SourcePTP,val listState: LazyListState) {
    var larrselected by mutableStateOf(false)
    var rarrselected by mutableStateOf(false)
    var oLang by mutableStateOf(if (original) sApp.currentNewsPaper.olang else sApp.lang)
    var tLang by mutableStateOf(if (original) sApp.lang else sApp.currentNewsPaper.olang)
    var isSoundParamsEnabled by mutableStateOf(false)
    var tXXTransClass by mutableStateOf(TText())

    var searching by mutableStateOf(false)

    var tTranslated by mutableStateOf(TText())
    var tSelected by mutableStateOf("")
    var tOriginal by mutableStateOf("")
    //var bookmarkable by mutableStateOf(bookmarkable)
    var aIndex by mutableStateOf(index)
    //    lSo.value=ListSelectableObjects(txt.split(" "))
    //var lSOt by mutableStateOf(ListSelectableObjects(sApp.vm.article.lArticle.value[aIndex].original.split(" ")))

    var lSOnpy by mutableStateOf(ListSelectableObjects(listOf("")))
    var lSOpy by mutableStateOf(ListSelectableObjects(listOf(Pinyin())))

    var transclassNPY by mutableStateOf(TransClass.NoPinYin())


    fun anyarrowSelected(): Boolean = larrselected || rarrselected
    fun status():String="pTP-> olang=$oLang tlang=$tLang  selected='$tSelected' translated='${tTranslated.getText()}'"
    fun setlSOt(){lSOnpy=(ListSelectableObjects(sApp.vm.article.lArticle.value[aIndex].original.split(" ")))}

    enum class scroll{
        BACK,FORWARD
    }

    fun canIscrollOne(s:scroll):Boolean{
        if(s == scroll.FORWARD){
            if(source==SourcePTP.headlines){
                return aIndex < sApp.vm.headLines.lHeadLines.value.lhl.size
            }
            if(source==SourcePTP.article){
                return aIndex < sApp.vm.article.lArticle.value.size
            }
        }
        if(s == scroll.BACK){
            if(source==SourcePTP.headlines){
                return aIndex > 0
            }
            if(source==SourcePTP.article){
                return aIndex > 0
            }
        }
        return false
    }

    fun setIndex(i:Int){
        tTranslated=TText()
        tSelected = ""


        aIndex=i
        tXXTransClass= getTransClass(sApp,aIndex,original,source)
        tOriginal = tXXTransClass.getText()

        if(tXXTransClass is TransClass.WithPinYin){
            lSOpy = ListSelectableObjects((tXXTransClass as TransClass.WithPinYin).lPy)
        }
        if(tXXTransClass is TransClass.NoPinYin){
            lSOnpy = ListSelectableObjects((tXXTransClass as TransClass.NoPinYin).lStr)
        }
    }
    fun setIndex2(i:Int){
        aIndex=i
    }

    init{
        setIndex(index)
    }
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
    val lng=pTP.sApp.vm.toneAndPitchMap.getLang(lan)
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


fun getTransClass(sApp: StatusApp,index:Int,original: Boolean,source: SourcePTP):TransClass {
    Timber.d("gettransclass of $index")

    if(source==SourcePTP.article) {

        val origtrans = sApp.vm.article.lArticle.value[index]

        if (original) {
            if (sApp.currentNewsPaper.olang.equals("zh"))
                return TransClass.WithPinYin(origtrans.romanizedo.lPy)
            else
                return TransClass.NoPinYin(origtrans.original.split(" "))
        } else {
            if (sApp.lang.equals("zh"))
                return TransClass.WithPinYin(origtrans.romanizedt.lPy)
            else
                return TransClass.NoPinYin(origtrans.translated.split(" "))
        }
    }
    if(source==SourcePTP.headlines) {

        val origtranslink = sApp.vm.headLines.listHL[index]

        if (original) {
            if (sApp.currentNewsPaper.olang.equals("zh"))
                return TransClass.WithPinYin(origtranslink.romanizedo.lPy)
            else
                return TransClass.NoPinYin(origtranslink.kArticle.title.split(" "))
        } else {
            if (sApp.lang.equals("zh"))
                return TransClass.WithPinYin(origtranslink.romanizedt.lPy)
            else
                return TransClass.NoPinYin(origtranslink.translated.split(" "))
        }
    }
    return TransClass.NoPinYin(listOf(""))
}

fun scrollparent(pTP: PlayTextParams,cS: CoroutineScope){
    cS.launch() {
        pTP.sApp.vm.article.listState?.scrollToItem(pTP.aIndex)
    }
}
suspend fun scrollparent2(pTP: PlayTextParams){
    //CoroutineScope(Dispatchers.Main).launch {
    //    Timber.d("$I")
    //    cS.launch {
            pTP.listState?.scrollToItem(pTP.aIndex,)
            //pTP.sApp.vm.article.listState?.scrollToItem(pTP.aIndex)
    //    }

  //  }
    //cS.launch() {
    //    pTP.sApp.vm.article.listState?.scrollToItem(I)
    //}
}



@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun PlayText(sApp: StatusApp,bplayText: MutableState<Boolean>,bookmarkable:Boolean,index:Int,original: Boolean,source: SourcePTP,listState: LazyListState,cS:CoroutineScope?=null){
    //Timber.d("play text A -> $index")
    val pTP = remember { PlayTextParams(sApp,bookmarkable,index,original,source,listState) }
    val cS1 = rememberCoroutineScope()
    //Timber.d("play text B -> ${pTP.aIndex}")
    initSpeak( if (original)  pTP.oLang else  pTP.tLang )
    Dialog(onDismissRequest = {
        //scrollparent(pTP,cS1)
        Timber.d("dissmis PlayText")
        t2.shutdown()
        bplayText.value = false
        sApp.vm.toneAndPitchMap.save()

    },properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Timber.d("passed playtext")
        KWindow2(4) {
           resfreshWraper2(loading = pTP.searching) {
                   SelectableTextAA(pTP)
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
    pTP: PlayTextParams,

) {
    var csoundpitch = remember { mutableStateOf(ClikedSoundPitch.None) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.LightGray))
            .padding(vertical = 3.dp)
    ) {
        SoundControl(pTP)
        if (pTP.anyarrowSelected())
            SoundPitch(sp = csoundpitch.value, pTP)
    }
}


@Composable
fun BookMarkIcon(pTP: PlayTextParams){
    //var isBookMark by remember { mutableStateOf(pTP.sApp.vm.article.bookMarks.value.isBookMark(pTP.aIndex)) }
    var isBookMark = pTP.sApp.vm.article.bookMarks.value.isBookMark(pTP.aIndex)
    val colorNoBookMarks = if(kTheme.fromInt(pTP.sApp.ktheme).theme.isLight) Color.Black else Color.White
    val cs=rememberCoroutineScope()
    if(pTP.bookmarkable) {
        Box(modifier = Modifier.clickable {
                pTP.sApp.vm.article.bookMarks.value.toggleBookMark(pTP.aIndex,pTP.sApp.vm.article.qarticleHandler.value)
                isBookMark=pTP.sApp.vm.article.bookMarks.value.isBookMark(pTP.aIndex)
                val l=pTP.sApp.vm.article.lArticle.value
                pTP.sApp.vm.article.lArticle.value= emptyList()
                pTP.sApp.vm.article.lArticle.value = l
                pTP.sApp.arethereBookMarks = !pTP.sApp.vm.article.bookMarks.value.bkMap.isEmpty()


            //cs.launch {
            //    pTP.sApp.vm.article.currentArticle++
            //    pTP.sApp.vm.article.listState?.scrollToItem(pTP.sApp.vm.article.currentArticle)
            //}
        } ) {
            val tintHL = if (isBookMark) Color.Cyan
            else colorNoBookMarks
            if(pTP.sApp.currentNewsPaper.kind==KindOfNews.BOOK) {
                Icon(
                    painterResource(id = R.drawable.ic_bookmark_border_black_24dp),
                    contentDescription = null,
                    tint = tintHL,
                    modifier = Modifier.size(sizeicon)
                )
            }
        }
    }
}


@Composable
fun LeftLangSpeaker(pTP: PlayTextParams,tup:String,tintL:Color){
    Row(modifier = Modifier.clickable(onClick = {spc(tup, pTP.oLang, pTP)}),horizontalArrangement = Arrangement.Start) {
        Icon(
            painterResource(id = R.drawable.ic_volume_up24px),
            contentDescription = "",
            tint = tintL,
            modifier = Modifier.size(sizeicon)
        )
        Text("(${pTP.oLang})",Modifier.align(Alignment.CenterVertically))
    }

}

@Composable
fun RightLangSpeacker(pTP: PlayTextParams,tintR:Color){
         Row(
            modifier = Modifier
                .clickable(onClick = { spc(pTP.tTranslated.getText(), pTP.tLang, pTP) }),
            ) {
            Text("(${pTP.tLang})", Modifier.align(Alignment.CenterVertically))
            Icon(
                painterResource(id = R.drawable.ic_volume_up_24px_1__1_), tint = tintR,
                contentDescription = "",
                modifier = Modifier.size(sizeicon)
            )
        }
}

@Composable
fun SoundSelector(pTP: PlayTextParams){
        val ctx=LocalContext.current

        IconButton(
            onClick = { OpenGoogle(ctx, pTP, false) },
            modifier = Modifier.padding(end=10.dp).size(sizeicon)
        ) {
            Image(
                painterResource(id = R.drawable.googlesearchc),
                contentDescription = "Localized description",
               // modifier = Modifier.padding(horizontal=20.dp)
            )
        }

        IconToggleButton(
            checked = pTP.larrselected,
            modifier=Modifier.size(sizeicon),
            onCheckedChange = {
                pTP.larrselected = it
                pTP.rarrselected = false
            }) {
            val tint by animateColorAsState(
                if (pTP.larrselected) Color(0xFFEC407A) else Color(
                    0xFFB0BEC5
                )
            )
            if(pTP.isSoundParamsEnabled)
            Icon(painterResource(id = R.drawable.ic_arrow_back_24px),
                tint = tint,contentDescription = "",
                modifier=Modifier.size(sizeicon)
            )
        }

        Icon(painterResource(id = R.drawable.ic_music_note_24px),contentDescription = "", modifier= Modifier
            .size(sizeicon)
            .clickable {
                pTP.isSoundParamsEnabled = !pTP.isSoundParamsEnabled
                pTP.rarrselected = false
                pTP.larrselected = false
            })



            IconToggleButton(
                checked = pTP.rarrselected,
                onCheckedChange = {
                    pTP.rarrselected = it
                    pTP.larrselected = false
                },Modifier.size(sizeicon)) {
                val tint by animateColorAsState(
                    if (pTP.rarrselected) Color(0xFFEC407A) else Color(
                        0xFFB0BEC5
                    )
                )
                if(pTP.isSoundParamsEnabled)
                Icon(painterResource(id = R.drawable.ic_arrow_forward_24px),
                    tint = tint,contentDescription = "",
                    modifier=Modifier.size(sizeicon)
                )
            }
    IconButton(onClick = { OpenGoogle(ctx,pTP,true)  },modifier = Modifier.padding(start= 10.dp).size(sizeicon)) {
        Image(painterResource(id =R.drawable.google_translate_logo ), contentDescription = "Localized description")//,modifier = Modifier.size(sizeicon+10.dp))
    }

}


fun OpenGoogle(ctx:Context,pTP: PlayTextParams,trans:Boolean){
    val olang = pTP.sApp.currentNewsPaper.olang
    val tlang = pTP.tLang
    val stext=pTP.tSelected
    val lnk = if(trans) "https://translate.google.com/?sl=$olang&tl=$tlang&text=$stext&op=translate"
              else "https://www.google.com/search?q=$stext"
    OpenBrowser(ctx,lnk)
}


@Composable
fun SoundControl(pTP: PlayTextParams) {
    val crs= rememberCoroutineScope()//  CoroutineScope(Dispatchers.IO)
    var currentJob by remember { mutableStateOf<Job?>(null) }
    val colorSpeaker = if(kTheme.fromInt(pTP.sApp.ktheme).theme.isLight) Color.Black else Color.White
    val tup = if (pTP.tSelected.equals("")) pTP.tOriginal else pTP.tSelected
    val tintL = if(pTP.larrselected) Color(0xFFEC407A ) else colorSpeaker
    val tintR = if(pTP.rarrselected) Color(0xFFEC407A ) else colorSpeaker

        Box(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.align(alignment = CenterStart)) {
            /*if(pTP.aIndex>0) Icon(painterResource(id = R.drawable.ic_navigate_before_24px),contentDescription = null /*modifier = Modifier.width(75.dp)*/,
                Modifier.clickable {
                    currentJob?.cancel()
                    currentJob=crs.launch {
                        pTP.setIndex(pTP.aIndex-1)
                        //pTP.aIndex--
                        //pTP.setlSOt()
                        Timber.d("Index:   ${pTP.aIndex}")
                        //ls?.animateScrollToItem(pTP.aIndex)
                     //   ls?.scrollToItem(pTP.aIndex)
                    }
                }
            )*/

            BookMarkIcon(pTP)
            LeftLangSpeaker(pTP, tup, tintL)
        }
        Row(modifier = Modifier.align(alignment = Center)){
            SoundSelector(pTP)
        }
        Row(modifier = Modifier.align(alignment = CenterEnd)) {
                SelectableLang(pTP)
                Spacer(Modifier.width(5.dp))
                RightLangSpeacker(pTP, tintR)
            /*Icon(painterResource(id = R.drawable.ic_navigate_next_black_24dp),contentDescription = null /*modifier = Modifier.width(75.dp)*/,
                Modifier.clickable {
                    if(pTP.canIscrollOne(PlayTextParams.scroll.FORWARD)) {
                   // if (pTP.aIndex < pTP.sApp.vm.article.lArticle.value.size) {
                        crs.launch(){
                            Timber.d("set index-> ${pTP.aIndex}")
                            pTP.setIndex(pTP.aIndex + 1)
                            //ls?.animateScrollToItem(pTP.aIndex)
                           // ls?.scrollToItem(pTP.aIndex)
                        }
                    }
                }
            )*/
            }
        }
}




@Composable
fun SelectableLang(pTP: PlayTextParams){
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val lselectedLang=pTP.sApp.vm.jLang.getSelectedLangs(pTP.oLang,pTP.tLang)
    Box() {
        IconButton(onClick = { expanded = true },modifier = Modifier.size(sizeicon)) {
            Icon(Icons.Default.MoreVert, contentDescription = "Localized description",modifier = Modifier.size(sizeicon))
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

    val curlang = pTP.sApp.vm.toneAndPitchMap.getLang(cl)

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
                     pTP.sApp.vm.toneAndPitchMap.setChanged()
                     Timber.d("onvaluechange $it") },
                valueRange = 0.1f..2.0f
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Speed",modifier = Modifier.width(50.dp))
            Slider(
                value = curlang.speed,//sliderRate.value,//curlang.speed,
                onValueChange = {
                    curlang.speed = it; sliderRate.value = it
                    Timber.d("onvaluechange $it")
                    pTP.sApp.vm.toneAndPitchMap.setChanged()
                    },
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
            .background(MaterialTheme.colors.surface)
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
                        TextX(tt.getText(), pTP.sApp.fontsize, true)
                    }
                    else -> {
                    }
                }
            }
        }
    }

    //.border(BorderStroke(1.dp, Color.Green))

    @Composable
    fun SelectableTextAA(pTP: PlayTextParams) {
        val cs= rememberCoroutineScope()
           dragablesquare(pTP,cs) {
                Box(
                    contentAlignment = Alignment.CenterStart, modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                    //.border(3.dp, Color.Magenta)
                ) {
                    val scrollState: ScrollState = rememberScrollState(0)
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        when (pTP.tXXTransClass) {
                            is TransClass.WithPinYin -> SelectableTextPy(pTP)
                            is TransClass.NoPinYin -> SelectableText(pTP)
                        }
                    }
                }
           }
    }

@Composable
fun dragablesquare(pTP: PlayTextParams,cs2:CoroutineScope, children: @Composable () -> Unit){
    var offsetX by remember{ mutableStateOf(0f)}
    val cs = rememberCoroutineScope()
    Box() {
        //Text("true")
           Row(modifier = Modifier
               .align(CenterStart)
               .padding(start = 5.dp)){
               //Text("Previous")
               Icon(painterResource(id = R.drawable.ic_navigate_before_24px),
                   contentDescription = "",
                   modifier=Modifier.size(sizeicon)
               )
           }
           Row(modifier = Modifier
               .align(CenterEnd)
               .padding(end = 5.dp)){
               //Text("Next")
               Icon(painterResource(id = R.drawable.ic_navigate_next_black_24dp),
                   contentDescription = "",
                   modifier=Modifier.size(sizeicon)
               )
           }
        Box(Modifier
            //.background(Color.Blue)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .draggable(
                enabled = true,
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    offsetX += delta
                },
                onDragStopped = {

                    Timber.d("Offset at release : $offsetX")
                    if (offsetX < -80f) {
                        if (pTP.canIscrollOne(PlayTextParams.scroll.FORWARD)) {
                            cs.launch {
                                //pTP.aIndex = pTP.aIndex + 1
                                //pTP.sApp.vm.article.currentArticle++
                                //pTP.sApp.vm.article.listState?.scrollToItem(pTP.sApp.vm.article.currentArticle)

                                pTP.setIndex(pTP.aIndex + 1)
                                scrollparent2(pTP)

                            }
                        }
                    } else if (offsetX > 100f) {

                        if (pTP.canIscrollOne(PlayTextParams.scroll.BACK)) {
                            cs.launch {
                                //pTP.sApp.vm.article.currentArticle--
                                //pTP.sApp.vm.article.listState?.scrollToItem(pTP.sApp.vm.article.currentArticle--)

                                pTP.setIndex(pTP.aIndex - 1)
                                scrollparent2(pTP)
                                //pTP.aIndex = pTP.aIndex - 1
                            }
                        }

                    }
                    offsetX = 0f
                },
                onDragStarted = { }
            )
        ) {
            children()
        }
    }
}


@Composable
fun SelectableText(pTP: PlayTextParams) {
        //Timber.d("BB->$txt")
        val scope = rememberCoroutineScope()

        FlowRowX(mainAxisSpacing = 2.dp) {
            pTP.lSOnpy.lSO.forEach {
                TextX(
                    it.x.first,
                    pTP.sApp.fontsize,
                    it.x.second.value,
                    Modifier.clickable( onClick = { onClickText2(pTP,pTP.lSOnpy,it,scope)} )
                )
            }
        }
}


    @Composable
    fun SelectableTextPy(pTP: PlayTextParams) {
        val scope = rememberCoroutineScope()
        FlowRowX(mainAxisSpacing = 2.dp) {
             pTP.lSOpy.lSO.forEach {
                Column(
                    Modifier//.border(BorderStroke(0.dp, Color.LightGray))
                        .padding(start = 2.dp, end = 2.dp)
                        .clickable(onClick = { onClickText2(pTP, pTP.lSOpy, it, scope) }),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextX(
                        it.getObject().w,
                        pTP.sApp.fontsize,
                        it.isselected(),
                    )
                    TextX(
                        it.getObject().r.replace("\\s".toRegex(), ""),
                        pTP.sApp.fontsize,
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
                        sApp.fontsize,
                        true,
                        Modifier.clickable(onClick = { })
                    )
                    TextX(
                        it.r.replace("\\s".toRegex(), ""),
                        sApp.fontsize,
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

class ListSelectableObjects<T>(l:List<T> = emptyList()){

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
// Max 661 770 808 810 832!!
//Max 528  440
// Max 718
