package com.begemot.myapplicationz

import android.speech.tts.TextToSpeech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.begemot.kclib.KButtonBar
import com.begemot.kclib.KHeader
import com.begemot.kclib.KWindow
import com.begemot.kclib.Kline
import com.begemot.knewscommon.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

private val searching = mutableStateOf(false)
private var tTranslated by mutableStateOf(TText())
private var tSelected by mutableStateOf("")
private var tOriginal by mutableStateOf("")

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
    if (prefs.speechrate != 1f) t2.setSpeechRate(prefs.speechrate)
    if (prefs.pitch != 1f) t2.setPitch(prefs.pitch)
    Timber.d("end Init speak  $msg")
}

fun setlangSpeak(lan: String) {
    var msg = ""
    val result = t2.setLanguage(Locale.forLanguageTag(lan))
    if (result == TextToSpeech.LANG_MISSING_DATA) msg = "Missing data"
    if (result == TextToSpeech.LANG_NOT_SUPPORTED) msg = "Lang not supported"
}


fun spc(txt: String, lan: String) {
    Timber.d("txt $txt  lang $lan")
    setlangSpeak(lan)
    t2.speak(txt, TextToSpeech.QUEUE_FLUSH, null, null)
    Timber.d("end spc $lan")
}

@ExperimentalLayout
@Composable
fun PlayText22(
    bplayText: MutableState<Boolean>,
    transclass: TransClass,
    sApp: StatusApp,
    original: Boolean
) {
    val olang = sApp.currentNewsPaper.olang
    val tlang = sApp.lang
    var audiolang = ""
    if (original) audiolang = olang
    else audiolang = tlang

    Timber.d("Init play Text : original=$original olang=$olang  tlang=$tlang  audiolang=${audiolang}")
    tOriginal=transclass.getText()
    initSpeak(audiolang)
    Dialog(onDismissRequest = { t2.shutdown(); bplayText.value = false }) {
        KWindow() {
            resfreshWraper2(loading = searching.value) {
                SelectableTextAA(transclass, sApp, original)
                BottomPlayText(olang,tlang,original)
                    DrawTranslatedText(
                        original = original,
                        sApp = sApp,
                        olang = olang,
                        tlang = tlang,
                        audiolang = audiolang
                    )
            }
        }
    }
}

//.border(BorderStroke(1.dp, Color.Green))
@Composable fun BottomPlayText(olang: String,tlang: String,original: Boolean){
    val l1=if(original) olang else tlang
    val l2=if(!original) olang else tlang
    val kk= tTranslated.getText()
    val zz= tSelected
    val tup=if(tSelected.equals("")) tOriginal else tSelected
    Timber.d("text translated  ->$kk   text to translate ->$zz")
    Timber.d("toriginal $tOriginal")
    Box(modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Color.LightGray)), contentAlignment = Alignment.CenterEnd) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(alignment = Alignment.CenterStart).clickable(onClick = { spc(tup,l1) })
        ) {
              Icon(vectorResource(id = R.drawable.ic_volume_up_24px))
              Text("($l1)")
        }
        if (!tTranslated.getText().equals("")) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(alignment = Alignment.CenterEnd).clickable(onClick = { spc(tTranslated.getText(),l2) })
            ) {
                Text("($l2)")
                Icon(vectorResource(id = R.drawable.ic_volume_up_24px))
            }
        }
    }
}
//.border(BorderStroke(1.dp, Color.Yellow))
@ExperimentalLayout
@Composable fun DrawTranslatedText(original: Boolean, sApp: StatusApp, olang:String, tlang:String, audiolang:String){
    Box(modifier = Modifier.fillMaxWidth()) {
        val tt = tTranslated
        when (tt) {
            is TransClass.WithPinYin -> {
                Timber.d("DrawPinYin")
                DrawPy(lPy = tt.lPy,sApp = sApp)
            }
            is TransClass.NoPinYin -> {
                Timber.d("DrawNoPinYin  ${tt.getText()}")
                TextX(tt.getText(), sApp.fontSize.value, true)
            }
            else -> {

            }
        }
    }
}

fun <T> SelectableListOfT(l:List<T>):List<Pair<T,MutableState<Boolean>>>{
        return l.map { it->Pair(it, mutableStateOf(false))}
}

open class Selectable(){
    open fun getText():String{
        return ""
    }

}

sealed class SelectableListOfItems():Selectable(){
       class WithPinYin(val lSelectablePy:List<Pair<Pinyin,MutableState<Boolean>>>):SelectableListOfItems()
       class NoPinYin(val lSelectableString:List<Pair<String,MutableState<Boolean>>>):SelectableListOfItems()
}

//.border(BorderStroke(1.dp, Color.Green))
@ExperimentalLayout
@Composable
fun SelectableTextAA(transclass: TransClass, sApp: StatusApp, original: Boolean) {
    Box(contentAlignment = Alignment.CenterStart,modifier = Modifier.fillMaxWidth().preferredHeightIn(max=250.dp)) {
        ScrollableColumn() {


            when (transclass) {
                is TransClass.WithPinYin -> {
                    Timber.d("withpinyin")
                    SelectableTextPy(transclass.lPy, sApp, original)
                }
                is TransClass.NoPinYin -> {
                    Timber.d("Nopinyin")
                    SelectableText(transclass.txt.joinToString(" "), null, sApp, original)
                }
            }
        }
    }
}

@ExperimentalLayout
@Composable
fun SelectableText(txt: String, lPy: ListPinyin?, sApp: StatusApp, original: Boolean) {
    val b = remember { mutableStateOf(TText()) }
    val lTxt = remember { SelectableListOfT(txt.split(" ")) }
    FlowRow(mainAxisSpacing = 2.dp) {
        lTxt.forEach {
            TextX(
                it.first,
                sApp.fontSize.value,
                it.second.value,
                Modifier.clickable(onClick = { onClickText(lTxt, it, sApp,  original,b) })
            )
        }
    }
    tTranslated=b.value
}

@ExperimentalLayout
@Composable
fun SelectableTextPy(lPy: ListPinyin, sApp: StatusApp, original: Boolean) {
    Timber.d("lPy  size ${lPy.lPy.size}")
    val b = remember { mutableStateOf(TText()) }
    val lTxt = remember { SelectableListOfT(lPy.lPy) }
    FlowRow(mainAxisSpacing = 2.dp) {
        lTxt.forEach {
            Column(
                Modifier//.border(BorderStroke(0.dp, Color.LightGray))
                    .padding(start = 2.dp, end = 2.dp).clickable(onClick = { onClickText(lTxt, it, sApp,  original,b,true) }),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextX(
                    it.first.w,
                    sApp.fontSize.value,
                    it.second.value,
                   // Modifier.clickable(onClick = { onClickText(lTxt, it, sApp,  original,b,true) })
                )
                TextX(
                    it.first.r.replace("\\s".toRegex(), ""),
                    sApp.fontSize.value,
                    it.second.value,
                    //Modifier.clickable(onClick = { onClickTextPy(lTxt, it, sApp, t, original) })
                )
            }
        }
    }
    tTranslated=b.value
}

@ExperimentalLayout
@Composable
fun DrawPy(lPy: ListPinyin, sApp: StatusApp) {

    FlowRow(mainAxisSpacing = 2.dp) {
        lPy.lPy.forEach {
            Column(
                Modifier//.border(BorderStroke(0.dp, Color.LightGray))
                    .padding(start = 2.dp, end = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextX(
                    it.w,
                    sApp.fontSize.value,
                    true,
                    Modifier.clickable(onClick = {  })
                )
                TextX(
                    it.r.replace("\\s".toRegex(), ""),
                    sApp.fontSize.value,
                    true,
                    Modifier.clickable(onClick = {  })
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
            fontSize = TextUnit.Companion.Sp(size),
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
        else Text(
            text = txt,
            style = MaterialTheme.typography.h5,
            fontSize = TextUnit.Companion.Sp(size),
            fontStyle = FontStyle.Normal,
            modifier = modifier
        )

    }
}

fun <T> prevOrNextSelectedQ(z : List<Pair<T,MutableState<Boolean>>>,x:Pair<T,MutableState<Boolean>>):Boolean{
    var prev = false
    var next = false
    val iclickedtext = z.indexOf(x)
    if (iclickedtext == 0) prev = false
    else prev = z[iclickedtext - 1].second.value
    if (iclickedtext + 1 == z.size) next = false
    else next = z[iclickedtext + 1].second.value
    return prev || next
}

fun <T>  onClickText(
    mtl: List<Pair<T, MutableState<Boolean>>>,
    txt: Pair<T, MutableState<Boolean>>,
    sApp: StatusApp,
    original: Boolean,
    bbb:MutableState< TText >,
    pinyin:Boolean=false
) {
    Timber.d("I've clicked: '${txt.first}'   Ttxt selected->${txt.second.value}")
    searching.value = true
    val currentstateselected = txt.second.value
    val prevornextselected = prevOrNextSelectedQ(mtl, txt)
    if (prevornextselected) {
        if (currentstateselected) mtl.forEach { it.second.value = false }
        else txt.second.value = true
    } else {
        mtl.forEach { it.second.value = false }
        //t.value= emptyList()
        txt.second.value = !currentstateselected
    }
    //if(T is Pinyin)
    Timber.d("prev or next selected:  $prevornextselected")
    val tt=if(pinyin)
            mtl.filter { it.second.value }.fold("") { sum, element -> "$sum ${((element.first) as Pinyin).w}" }
    else    mtl.filter { it.second.value }.fold("") { sum, element -> "$sum ${element.first}" }

    //if(pinyin)  tSelected=SelectableListOfItems.WithPinYin(mtl as List<Pair<Pinyin, MutableState<Boolean>>>)
    //else        tSelected=SelectableListOfItems.NoPinYin(mtl as List<Pair<String, MutableState<Boolean>>>)
    tSelected=tt

    Timber.d("text selected->$tt")
    Timber.d("and the reduction is: (the fold)  $tt")
    GlobalScope.launch {
        val sPinyin =
            if (original) sApp.vm.tCache.getTrans3(tt, sApp.currentNewsPaper.olang, sApp.lang)
            else sApp.vm.tCache.getTrans3(tt, sApp.lang, sApp.currentNewsPaper.olang)
        Timber.d(" pinyin getText->${sPinyin.getText()}")

        when (sPinyin) {
            is TransClass.NoTrans -> {
                Timber.d("No TRANS   xxxxxZ")
                bbb.value=TransClass.NoTrans()

            }
            is TransClass.WithPinYin -> {
                Timber.d("WithPinYin!!!")
                if (txt.second.value) {
                    bbb.value= TransClass.WithPinYin(sPinyin.lPy)
                } else bbb.value = TransClass.NoTrans()//Pinyin()
                Timber.d("CAn't believe it ${bbb.value.getText()}")
            }
            is TransClass.NoPinYin -> {
                Timber.d("¡¡¡¡---NoPinYin---!!!")
                if (txt.second.value) {
                    bbb.value=TransClass.NoPinYin(sPinyin.txt)
                    Timber.d("No PinYin --> CAn't believe it ${bbb.value.getText()}")
                } else bbb.value = TransClass.NoTrans()//Pinyin()
                //Timber.d("NoPinYin   t.value: ${t.value}")
            }
        }
        tTranslated=bbb.value
        searching.value = false
    }
}

//Max 528  440

/*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.max

/**
 * A composable that places its children in a horizontal flow. Unlike [Row], if the
 * horizontal space is too small to put all the children in one row, multiple rows may be used.
 *
 * Note that just like [Row], flex values cannot be used with [FlowRow].
 *
 * Example usage:
 *
 * @sample androidx.compose.foundation.layout.samples.SimpleFlowRow
 *
 * @param mainAxisSize The size of the layout in the main axis direction.
 * @param mainAxisAlignment The alignment of each row's children in the main axis direction.
 * @param mainAxisSpacing The main axis spacing between the children of each row.
 * @param crossAxisAlignment The alignment of each row's children in the cross axis direction.
 * @param crossAxisSpacing The cross axis spacing between the rows of the layout.
 * @param lastLineMainAxisAlignment Overrides the main axis alignment of the last row.
 */
@ExperimentalLayout
@Composable
fun FlowRow(
    mainAxisSize: SizeMode = SizeMode.Wrap,
    mainAxisAlignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisAlignment: FlowCrossAxisAlignment = FlowCrossAxisAlignment.Start,
    crossAxisSpacing: Dp = 0.dp,
    lastLineMainAxisAlignment: FlowMainAxisAlignment = mainAxisAlignment,
    content: @Composable () -> Unit
) {
    Flow(
        orientation = LayoutOrientation.Horizontal,
        mainAxisSize = mainAxisSize,
        mainAxisAlignment = mainAxisAlignment,
        mainAxisSpacing = mainAxisSpacing,
        crossAxisAlignment = crossAxisAlignment,
        crossAxisSpacing = crossAxisSpacing,
        lastLineMainAxisAlignment = lastLineMainAxisAlignment,
        content = content
    )
}

/**
 * A composable that places its children in a vertical flow. Unlike [Column], if the
 * vertical space is too small to put all the children in one column, multiple columns may be used.
 *
 * Note that just like [Column], flex values cannot be used with [FlowColumn].
 *
 * Example usage:
 *
 * @sample androidx.compose.foundation.layout.samples.SimpleFlowColumn
 *
 * @param mainAxisSize The size of the layout in the main axis direction.
 * @param mainAxisAlignment The alignment of each column's children in the main axis direction.
 * @param mainAxisSpacing The main axis spacing between the children of each column.
 * @param crossAxisAlignment The alignment of each column's children in the cross axis direction.
 * @param crossAxisSpacing The cross axis spacing between the columns of the layout.
 * @param lastLineMainAxisAlignment Overrides the main axis alignment of the last column.
 */
@ExperimentalLayout
@Composable
fun FlowColumn(
    mainAxisSize: SizeMode = SizeMode.Wrap,
    mainAxisAlignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisAlignment: FlowCrossAxisAlignment = FlowCrossAxisAlignment.Start,
    crossAxisSpacing: Dp = 0.dp,
    lastLineMainAxisAlignment: FlowMainAxisAlignment = mainAxisAlignment,
    content: @Composable () -> Unit
) {
    Flow(
        orientation = LayoutOrientation.Vertical,
        mainAxisSize = mainAxisSize,
        mainAxisAlignment = mainAxisAlignment,
        mainAxisSpacing = mainAxisSpacing,
        crossAxisAlignment = crossAxisAlignment,
        crossAxisSpacing = crossAxisSpacing,
        lastLineMainAxisAlignment = lastLineMainAxisAlignment,
        content = content
    )
}

/**
 * Used to specify the alignment of a layout's children, in cross axis direction.
 */
enum class FlowCrossAxisAlignment {
    /**
     * Place children such that their center is in the middle of the cross axis.
     */
    Center,
    /**
     * Place children such that their start edge is aligned to the start edge of the cross axis.
     */
    Start,
    /**
     * Place children such that their end edge is aligned to the end edge of the cross axis.
     */
    End,
}

typealias FlowMainAxisAlignment = MainAxisAlignment

/**
 * Layout model that arranges its children in a horizontal or vertical flow.
 */
@Composable
@OptIn(InternalLayoutApi::class)
private fun Flow(
    orientation: LayoutOrientation,
    mainAxisSize: SizeMode,
    mainAxisAlignment: FlowMainAxisAlignment,
    mainAxisSpacing: Dp,
    crossAxisAlignment: FlowCrossAxisAlignment,
    crossAxisSpacing: Dp,
    lastLineMainAxisAlignment: FlowMainAxisAlignment,
    content: @Composable () -> Unit
) {
    fun Placeable.mainAxisSize() =
        if (orientation == LayoutOrientation.Horizontal) width else height
    fun Placeable.crossAxisSize() =
        if (orientation == LayoutOrientation.Horizontal) height else width

    Layout(content) { measurables, outerConstraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        val constraints = OrientationIndependentConstraints(outerConstraints, orientation)

        val childConstraints = if (orientation == LayoutOrientation.Horizontal) {
            Constraints(maxWidth = constraints.mainAxisMax)
        } else {
            Constraints(maxHeight = constraints.mainAxisMax)
        }

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.toIntPx() +
                placeable.mainAxisSize() <= constraints.mainAxisMax

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                crossAxisSpace += crossAxisSpacing.toIntPx()
            }
            sequences += currentSequence.toList()
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

            currentSequence.clear()
            currentMainAxisSize = 0
            currentCrossAxisSize = 0
        }

        for (measurable in measurables) {
            // Ask the child for its preferred size.
            val placeable = measurable.measure(childConstraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacing.toIntPx()
            }
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.mainAxisSize()
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.crossAxisSize())
        }

        if (currentSequence.isNotEmpty()) startNewSequence()

        val mainAxisLayoutSize = if (constraints.mainAxisMax != Constraints.Infinity &&
            mainAxisSize == SizeMode.Expand
        ) {
            constraints.mainAxisMax
        } else {
            max(mainAxisSpace, constraints.mainAxisMin)
        }
        val crossAxisLayoutSize = max(crossAxisSpace, constraints.crossAxisMin)

        val layoutWidth = if (orientation == LayoutOrientation.Horizontal) {
            mainAxisLayoutSize
        } else {
            crossAxisLayoutSize
        }
        val layoutHeight = if (orientation == LayoutOrientation.Horizontal) {
            crossAxisLayoutSize
        } else {
            mainAxisLayoutSize
        }

        layout(layoutWidth, layoutHeight) {
            sequences.fastForEachIndexed { i, placeables ->
                val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                    placeables[j].mainAxisSize() +
                        if (j < placeables.lastIndex) mainAxisSpacing.toIntPx() else 0
                }
                val arrangement = if (i < sequences.lastIndex) {
                    mainAxisAlignment.arrangement
                } else {
                    lastLineMainAxisAlignment.arrangement
                }
                //
                // Handle vertical direction
                val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
                arrangement.arrange(
                    mainAxisLayoutSize,
                    childrenMainAxisSizes,
                    this@Layout,
                    mainAxisPositions
                )
                placeables.fastForEachIndexed { j, placeable ->
                    val crossAxis = when (crossAxisAlignment) {
                        FlowCrossAxisAlignment.Start -> 0
                        FlowCrossAxisAlignment.End ->
                            crossAxisSizes[i] - placeable.crossAxisSize()
                        FlowCrossAxisAlignment.Center ->
                            Alignment.Center.align(
                                IntSize.Zero,
                                IntSize(
                                    width = 0,
                                    height = crossAxisSizes[i] - placeable.crossAxisSize()
                                ),
                                LayoutDirection.Ltr
                            ).y
                    }
                    if (orientation == LayoutOrientation.Horizontal) {
                        placeable.place(
                            x = mainAxisPositions[j],
                            y = crossAxisPositions[i] + crossAxis
                        )
                    } else {
                        placeable.place(
                            x = crossAxisPositions[i] + crossAxis,
                            y = mainAxisPositions[j]
                        )
                    }
                }
            }
        }
    }
}

/**
 * Used to specify how a layout chooses its own size when multiple behaviors are possible.
 */
//
enum class SizeMode {
    /**
     * Minimize the amount of free space by wrapping the children,
     * subject to the incoming layout constraints.
     */
    Wrap,
    /**
     * Maximize the amount of free space by expanding to fill the available space,
     * subject to the incoming layout constraints.
     */
    Expand
}

/**
 * Used to specify the alignment of a layout's children, in main axis direction.
 */
@OptIn(InternalLayoutApi::class)
enum class MainAxisAlignment(internal val arrangement: Arrangement.Vertical) {
    // (soboleva) support RTl in Flow
    // workaround for now - use Arrangement that equals to previous Arrangement
    /**
     * Place children such that they are as close as possible to the middle of the main axis.
     */
    Center(Arrangement.Center),

    /**
     * Place children such that they are as close as possible to the start of the main axis.
     */
    Start(Arrangement.Top),

    /**
     * Place children such that they are as close as possible to the end of the main axis.
     */
    End(Arrangement.Bottom),

    /**
     * Place children such that they are spaced evenly across the main axis, including free
     * space before the first child and after the last child.
     */
    SpaceEvenly(Arrangement.SpaceEvenly),

    /**
     * Place children such that they are spaced evenly across the main axis, without free
     * space before the first child or after the last child.
     */
    SpaceBetween(Arrangement.SpaceBetween),

    /**
     * Place children such that they are spaced evenly across the main axis, including free
     * space before the first child and after the last child, but half the amount of space
     * existing otherwise between two consecutive children.
     */
    SpaceAround(Arrangement.SpaceAround);
}

 */