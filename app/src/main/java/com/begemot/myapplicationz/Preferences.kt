package com.begemot.myapplicationz


//import androidx.ui.text.Locale

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
//import androidx.compose.foundation.layout.ColumnScope.gravity
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
//import androidx.compose.ui.VerticalAlignmentLine
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


import com.begemot.kclib.KButtonBar
import com.begemot.kclib.KHeader
import com.begemot.kclib.KText2
import com.begemot.kclib.KWindow

import timber.log.Timber
import kotlin.collections.HashMap


class PrefsParams {
    val localFontSize: MutableState<Int> = mutableStateOf(prefs.fontSize)
    val tabstate: MutableState<Int> = mutableStateOf(prefs.preftab)
    val localPitch: MutableState<Float> = mutableStateOf(prefs.pitch)
    val localSpeechrate: MutableState<Float> = mutableStateOf(prefs.speechrate)
    val selectLocales: MutableState<Boolean> = mutableStateOf(false)
    val localCurrentLang: MutableState<String> = mutableStateOf(prefs.kLang)
    val localSelectedLang: MutableState<String> = mutableStateOf(prefs.selectedLang)
    val localromanize: MutableState<Int> = mutableStateOf(prefs.romanize)
}

fun printStat(msg: String, prefParams: PrefsParams, statusApp: StatusApp) {
    Timber.d("printStat : $msg")
    Timber.d("localCurrentLang ${prefParams.localCurrentLang.value}")
    Timber.d("localSelectedLang ${prefParams.localSelectedLang.value}")
    Timber.d("statusApp.lang  ${statusApp.lang}")
    Timber.d("localCurrent Romanize ${prefParams.localromanize.value}")
}


@ExperimentalLayout
@Composable
fun editPreferences(sApp: StatusApp) {
    Timber.d("editprefs")
    val prefParams = remember { PrefsParams() }
    printStat("On Enter editPreferences ", prefParams, sApp)
    Dialog(onDismissRequest = { sApp.selectLang.value = false }) {
        KWindow() {
            KHeader(txt = "Settings", onClick = { sApp.selectLang.value = false })
            tabPreferences(sApp, prefParams)
            KButtonBar {
                Button(onClick = {
                    //statusApp.fontSize.value = prefParams.localFontSize.value
                    //prefs.fontSize = prefParams.localFontSize.value
                    prefs.fontSize = sApp.fontSize.value
                    prefs.pitch = prefParams.localPitch.value
                    prefs.speechrate = prefParams.localSpeechrate.value
                    sApp.selectLang.value = false
                    prefs.kLang = prefParams.localCurrentLang.value
                    if (sApp.lang != prefs.kLang) sApp.vm.reinicializeHeadLines() //If lang changes force reload <-
                    sApp.lang = prefs.kLang
                    prefs.romanize = prefParams.localromanize.value
                    sApp.romanized=prefs.romanize
                    printStat("After OK edit Preferences ", prefParams, sApp)

                }) {
                    Text("Ok")
                }
                if (prefParams.tabstate.value == 0) {
                    Button(onClick = {
                        prefParams.selectLocales.value = true
                    }) { Text("+ Languages") }
                }
                if (prefParams.tabstate.value == 2) {
                    Button(onClick = {
                        if (prefParams.tabstate.value == 2) {
                            prefParams.localPitch.value = 1.0f
                            prefParams.localSpeechrate.value = 1.0f
                            prefs.speechrate = 1.0f
                            prefs.pitch = 1.0f
                        }
                    }) {
                        Text("default")
                    }
                }
            }
        }
    }
}


fun prefTitles(sApp: StatusApp, prefParams: PrefsParams): List<String> {
    /*val latinized = listOf("zh", "zh-TW")
    val yes = latinized.find { it -> prefParams.localCurrentLang.value.equals(it) }
    return if (yes != null) listOf("Language", "Text", "Sound", "Latin")
    else listOf("Language", "Text", "Sound")*/
    return listOf("Language", "Text", "Sound", "Latin")
}

@ExperimentalLayout
@Composable
fun tabPreferences(sApp: StatusApp, prefParams: PrefsParams) {
    val titles = prefTitles(sApp, prefParams)
    Column {
        TabRow(selectedTabIndex = prefParams.tabstate.value) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = prefParams.tabstate.value == index,
                    content = { Text(title) },
                    onClick = {
                        prefParams.tabstate.value = index
                        prefs.preftab = prefParams.tabstate.value
                    }
                )
            }
        }
        Box(
            modifier = Modifier.preferredHeight(210.dp).fillMaxWidth().padding(10.dp),
            contentAlignment = Alignment.Center
            //gravity = Alignment.Center
        ) {
            when (prefParams.tabstate.value) {
                0 -> tabLanguage(sApp, prefParams)
                1 -> tabText(sApp, prefParams.localFontSize)
                2 -> tabSound(prefParams)
                3 -> tabRomanization(sApp, prefParams)
            }
        }
    }
}

@Composable
fun tabRomanization(sApp: StatusApp, prefParams: PrefsParams) {
    val radioOptions = listOf("Simple", "Complete", "None")
    //var indx = radioOptions.indexOf("None")
    var indx = prefParams.localromanize.value
    var selectedOption by remember { mutableStateOf(radioOptions[indx]) }

    val onSelectedChange = { text: String ->
        selectedOption = text
        prefParams.localromanize.value = radioOptions.indexOf(text)
        //  printStat("before on change current lang", prefParams, statusApp)
        //  prefParams.localCurrentLang.value = text.substringAfterLast("(").substringBefore(")")
        //  printStat("after on change current lang", prefParams, statusApp)
    }


    Column {
        radioOptions.forEach { text ->
            Row(Modifier
                .fillMaxWidth()
                .selectable(
                    selected = (text == selectedOption),
                    onClick = { onSelectedChange(text) }
                )
                .padding(horizontal = 8.dp)
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = { onSelectedChange(text) }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 26.dp)
                )
            }
        }
    }
}


@ExperimentalLayout
@Composable
fun tabLanguage(statusApp: StatusApp, prefParams: PrefsParams) {
    //val selectLocales = state { false }
    val context = AmbientContext.current
    if (prefParams.selectLocales.value) localesDlg(prefParams, statusApp)
    val getOptions = getSelectedLang()
    //Timber.d("getOptions size ${getOptions.size}  content: $getOptions")

    // val localFontSize= state{statusApp.fontSize}

    //val radioOptions = listOf("en", "es", "it","ur","zh","ca","zh-TW")
    val radioOptions = getOptions.map { it -> "${it.value}  (${it.key})" }.sorted()
    //val r= lKLocales.find { it.acronim==statusApp.lang }
    //var indx=radioOptions.indexOf("${r?.displayName}  (${r?.acronim})")
    //Timber.d("tab language ${statusApp.lang}  ${getOptions[statusApp.lang]}")
    val v = getOptions[statusApp.lang]
    val z = getOptions.getKey(v!!)
    val comb = "$v  ($z)"
    var indx = radioOptions.indexOf(comb)


    //Timber.d("indx $indx   statusApp.lang=${statusApp.lang}  getOptions= $getOptions")
    //Timber.d("${getOptions[statusApp.lang]} $comb")
    if (indx == -1) indx = 0
    //        val (selectedOption, onOptionSelected) = state { radioOptions[indx] }
    var selectedOption by remember { mutableStateOf(radioOptions[indx]) }
    val onSelectedChange = { text: String ->
        selectedOption = text
        printStat("before on change current lang", prefParams, statusApp)
        prefParams.localCurrentLang.value = text.substringAfterLast("(").substringBefore(")")
        printStat("after on change current lang", prefParams, statusApp)
    }

    Column {
        radioOptions.forEach { text ->
            Row(Modifier
                .fillMaxWidth()
                .selectable(
                    selected = (text == selectedOption),
                    onClick = { onSelectedChange(text) }
                )
                .padding(horizontal = 8.dp)
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = { onSelectedChange(text) }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 26.dp)
                )
            }
        }
    }

}

@Composable
fun tabText(statusApp: StatusApp, localFontSize: MutableState<Int>) {
    Column() {
        KText2("Text Size", size = statusApp.fontSize.value)
        var sliderPosition = mutableStateOf(statusApp.fontSize.value.toFloat())
        Slider(
            value = sliderPosition.value,
            //onValueChange={sliderPosition.value=it;localFontSize.value=it.toInt()},
            onValueChange = { sliderPosition.value = it;statusApp.fontSize.value = it.toInt() },
            valueRange = 10f..35f
            //    color=Color.Black
        )
    }
}

@Composable
fun tabSound(prefParams: PrefsParams) {
    val sliderPitch: MutableState<Float> = mutableStateOf(prefParams.localPitch.value)
    val sliderRate: MutableState<Float> = mutableStateOf(prefParams.localSpeechrate.value)
    Column(modifier = Modifier.padding(1.dp), verticalArrangement = Arrangement.Center) {
        Text("Pitch")
        Slider(
            value = sliderPitch.value,
            onValueChange = { sliderPitch.value = it;prefParams.localPitch.value = it },
            valueRange = 0.1f..2.0f
        )
        Text("Speech Rate")
        Slider(
            value = sliderRate.value,
            onValueChange = { sliderRate.value = it;prefParams.localSpeechrate.value = it },
            valueRange = 0.1f..2.0f
        )
    }
}

@ExperimentalLayout
@Preview
@Composable
fun tps() {
    val selectLang: MutableState<Boolean> = mutableStateOf(true)
    //editPreferences(sApp = StatusApp(Screens.StartUpScreen,Screens.QuitScreen) )
}


fun Ok(prefParams: PrefsParams, statusApp: StatusApp) {
    printStat("Enter OK select langs ", prefParams, statusApp)
    val lSelectedLocales = getLocalesZ().filter { it -> it.checked }
    val lnames = lSelectedLocales.map { it.acronim }
    val s = lnames.joinToString(",")
    prefs.selectedLang = lnames.joinToString(",")
    prefParams.selectLocales.value = false
    statusApp.lang = prefParams.localCurrentLang.value
    printStat("Leaving OK select langs ", prefParams, statusApp)
    //  Timber.d("selectedLang : ${prefs.selectedLang}")
}

fun setSelectedLang() {
    val lAllLocales = getLocalesZ()
    prefs.selectedLang.split(",").forEach {
        lAllLocales.find { lang -> it.equals(lang.acronim) }?.checked = true
    }
}

//fun getSelectedLang():List<KLocale>{
/*
Returns a bi-map of the acronim/displayname stored in prefs SELECTEDLANG
if prefs is empty sets Local.current.language as selected in AllLocales and adds it to the map
if prefs doesen't contain Local it add it's to the map and selects it at AllLocales
and checks=true all prefs in AllLocales
 */

fun getSelectedLang(): BiHashMap<String, String> {
    val lS = mutableListOf<KLocale>()
    val lAllLocales = getLocalesZ()

    //  val r=Locale.getDefault()
    //  val kLocale = lAllLocales.find { it->it.acronim.equals(Locale.getDefault().language) }
    //  kLocale?.checked=true
    val lSelected = prefs.selectedLang.split(",")  //"ca,en" ...

    //if(lSelected.find {it->it.equals(Locale.getDefault().language) }==null){
    //        lS.add(kLocale!!)
    //    }
    lSelected.forEach {
        val l = lAllLocales.find { kloc -> it.equals(kloc.acronim) }
        if (l != null) {
            l.checked = true
            lS.add(l)
        }
    }


    /*Timber.d("selected lang prefs:${s}")
    val q=s.split(",")
    prefs.selectedLang.split(",").forEach{
        val l=lAllLocales.find { lang->it.equals(lang.acronim) }
        if(l!=null){ l.checked=true; lS.add(l)}
    }
    if(lS.find{a->a.acronim.equals(Locale.current.language)}==null){
        lS.add(lAllLocales.find{a->a.acronim.equals(Locale.current.language)}!!)
    }*/
    val map: BiHashMap<String, String> = BiHashMap()
    lS.forEach {
        map.put(it.acronim, it.displayName)
    }
    Timber.d("getSelectedLang $map")
    return map
    // return lS
}

/*enum class ToggleableState{
    All,Selected,UnSelected
}*/

@ExperimentalLayout
@Composable
fun localesDlg(prefParams: PrefsParams, statusApp: StatusApp) {
    val context = ContextAmbient.current
    //var lselected by remember {mutableStateOf(true)}
    var lselected by mutableStateOf(false)
    printStat("Enter localesDlg", prefParams, statusApp)
    Dialog(onDismissRequest = { prefParams.selectLocales.value = false }) {
        val lKLocale = getLocalesZ2()
        KWindow(size = 265) {
            KHeader(txt = "Select languages", onClick = { prefParams.selectLocales.value = false })
            //,border= BorderStroke(2.dp, Color.Black)
            Column(Modifier.preferredHeight(300.dp).fillMaxWidth(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {


                    //Box(backgroundColor = MaterialTheme.colors.secondaryVariant,modifier = Modifier.fillMaxWidth(1f)) {
                    Row(Modifier.padding(5.dp)) {

                        Text("Unselected")
                        Switch(
                            checked = lselected,
                            onCheckedChange = { lselected = it },
                            Modifier.padding(horizontal = 10.dp)
                        )
                        //Checkbox(checked = lselected, onCheckedChange = { lselected = !lselected} )
                        Text("Selected")
                    }

                }
                //ScrollableColumn() {
                LazyColumnFor(items = lKLocale, itemContent = {
                    if (lselected && it.checked) {
                        rowlang(it = it, statusApp = statusApp, context = context, prefParams)

                    }
                    if (!lselected && !it.checked) {
                        rowlang(it = it, statusApp = statusApp, context = context, prefParams)
                    }
                })
                // }
                /*lKLocale.forEach {
                        if(lselected && it.checked) {
                            rowlang(it = it, statusApp = statusApp, context =context,prefParams  )

                        }
                        if(!lselected && !it.checked){
                            rowlang(it = it, statusApp = statusApp, context =context ,prefParams )
                        }
                    }
                }*/
            }
            KButtonBar {
                //Button(onClick = {/*scr.scrollTo(200f)*/}) { Text("HolaX")}
                Button(onClick = {
                    Ok(
                        prefParams,
                        statusApp
                    )/*;prefParams.selectLocales.value=false*/
                }) { Text("Ok") }
            }
        }

    }
}

@Composable
fun rowlang(it: KLocale, statusApp: StatusApp, context: Context, prefParams: PrefsParams) {
    Row(Modifier.padding(start = 10.dp, top = 5.dp).fillMaxWidth(1.0f)) {
        var checked = mutableStateOf(it.checked)
        Checkbox(
            checked = checked.value,
            onCheckedChange = { c ->
                val count = lKLocales.count { it.checked }
                if (count == 8 && c) {
                    Toast.makeText(
                        context,
                        "Max number of selected languages reached",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    it.checked = c
                    checked.value = !checked.value
                    Timber.d("on checkchange ${prefParams.localCurrentLang.value}")
                    //if (it.acronim.equals(statusApp.lang)) {
                    if (it.acronim.equals(prefParams.localCurrentLang.value)) {
                        it.checked = true
                        checked.value = true
                        Toast.makeText(
                            context,
                            "You can't remove current selected language (${it.acronim})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
        Text(it.toString(), Modifier.padding(start = 3.dp))
    }
}


data class KLocale(
    val displayName: String = "",
    val acronim: String = "",
    var checked: Boolean = false
) {
    override fun toString(): String {
        return "$displayName ($acronim)"
    }
}

private val lKLocales = listOf(
    KLocale("Afrikaans", "af"),
    KLocale("Albanian", "sq"),
    KLocale("Amharic", "am"),
    KLocale("Arabic", "ar"),
    KLocale("Armenian", "hy"),
    KLocale("Azerbaijani", "az"),
    KLocale("Basque", "eu"),
    KLocale("Belarusian", "be"),
    KLocale("Bengali", "bn"),
    KLocale("Bosnian", "bs"),
    KLocale("Bulgarian", "bg"),
    KLocale("Catalan", "ca"),
    KLocale("Cebuano", "ceb"),
    KLocale("Chinese (Simplified)", "zh"),
    KLocale("Chinese (Traditional)", "zh-TW"),
    KLocale("Corsican", "co"),
    KLocale("Croatian", "hr"),
    KLocale("Czech", "cs"),
    KLocale("Danish", "da"),
    KLocale("Dutch", "nl"),
    KLocale("English", "en"),
    KLocale("Esperanto", "eo"),
    KLocale("Estonian", "et"),
    KLocale("Finnish", "fi"),
    KLocale("French", "fr"),
    KLocale("Frisian", "fy"),
    KLocale("Galician", "gl"),
    KLocale("Georgian", "ka"),
    KLocale("German", "de"),
    KLocale("Greek", "el"),
    KLocale("Gujarati", "gu"),
    KLocale("Hausa", "ha"),
    KLocale("Haitian Creole", "ht"),
    KLocale("Hawaiian", "haw"),
    KLocale("Hebrew", "he"), //or iw
    KLocale("Hindi", "hi"),
    KLocale("Hmong", "hmn"),
    KLocale("Hungarian", "hu"),
    KLocale("Icelandic", "is"),
    KLocale("Igbo", "ig"),
    KLocale("Indonesian", "id"),
    KLocale("Irish", "ga"),
    KLocale("Italian", "it"),
    KLocale("Japanese", "ja"),
    KLocale("Javanese", "jv"),
    KLocale("Kannada", "kn"),
    KLocale("Kazakh", "kk"),
    KLocale("Khmer", "km"),
    KLocale("Kinyarwanda", "rw"),
    KLocale("Korean", "ko"),
    KLocale("Kurdish", "ku"),
    KLocale("Kyrgyz", "ky"),
    KLocale("Lao", "lo"),
    KLocale("Latin", "la"),
    KLocale("Latvian", "lv"),
    KLocale("Lithuanian", "lt"),
    KLocale("Luxembourgish", "lb"),
    KLocale("Macedonian", "mk"),
    KLocale("Malagasy", "mg"),
    KLocale("Malay", "ms"),
    KLocale("Malayalam", "ml"),
    KLocale("Maltese", "mt"),
    KLocale("Maori", "mi"),
    KLocale("Marathi", "mr"),
    KLocale("Myanmar (Burmese)", "my"),
    KLocale("Mongolian", "mn"),
    KLocale("Nepali", "ne"),
    KLocale("Nyanja (Chichewa)", "ny"),
    KLocale("Norwegian", "no"),
    KLocale("Odia (Oriya)", "or"),
    KLocale("Pashto", "ps"),
    KLocale("Persian", "fa"),
    KLocale("Polish", "pl"),
    KLocale("Portuguese  Brazil", "pt"),
    KLocale("Punjabi", "pa"),
    KLocale("Romanian", "ro"),
    KLocale("Russian", "ru"),
    KLocale("Samoan", "sm"),
    KLocale("Scots Gaelic", "gd"),
    KLocale("Serbian", "sr"),
    KLocale("Sesotho", "st"),
    KLocale("Shona", "sn"),
    KLocale("Sinhala (Sinhalese)", "si"),
    KLocale("Sindhi", "sd"),
    KLocale("Slovak", "sk"),
    KLocale("Slovenian", "sl"),
    KLocale("Somali", "so"),
    KLocale("Spanish", "es"),
    KLocale("Sundanese", "su"),
    KLocale("Swahili", "sw"),
    KLocale("Swedish", "sv"),
    KLocale("Tajik", "tg"),
    KLocale("Tamil", "ta"),
    KLocale("Tatar", "tt"),
    KLocale("Telugu", "te"),
    KLocale("Thai", "th"),
    KLocale("Turkish", "tr"),
    KLocale("Turkmen", "tk"),
    KLocale("Ukrainian", "uk"),
    KLocale("Urdu", "ur"),
    KLocale("Uyghur", "ug"),
    KLocale("Uzbek", "uz"),
    KLocale("Vietnamese", "vi"),
    KLocale("Welsh", "cy"),
    KLocale("Xhosa", "xh"),
    KLocale("Yiddish", "yi"),
    KLocale("Yoruba", "yo"),
    KLocale("Zulu", "zu")
)

class BiHashMap<K, V> : HashMap<K, V>() {
    private var rMap: MutableMap<V, K> = HashMap()
    override fun put(key: K, value: V): V? {
        rMap[value] = key
        return super.put(key, value)
    }

    fun getKey(target: V): K? {
        return rMap[target]
    }
}

fun getLocalesZ(): List<KLocale> {
    return lKLocales
}

fun getLocalesZ2(): List<KLocale> {
    lKLocales.forEach { it.checked = false }
    // val kLocale = lKLocales.find { it->it.acronim.equals(Locale.getDefault().language) }
    // kLocale?.checked=true
    val lSelected = prefs.selectedLang.split(",")  //"ca,en" ...

    //if(lSelected.find {it->it.equals(Locale.getDefault().language) }==null){
    //    lS.add(kLocale!!)
    //}
    lSelected.forEach {
        val l = lKLocales.find { kloc -> it.equals(kloc.acronim) }
        if (l != null) {
            l.checked = true

        }
    }
    return lKLocales
}


