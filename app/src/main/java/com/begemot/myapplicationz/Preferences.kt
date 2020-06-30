package com.begemot.myapplicationz

import android.widget.Toast
import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.graphics.Color
import androidx.ui.intl.Locale
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.KeyboardArrowDown
import androidx.ui.material.icons.filled.KeyboardArrowUp
import androidx.ui.tooling.preview.Preview
//import androidx.ui.text.Locale
import androidx.ui.unit.dp
import com.begemot.kclib.KButtonBar
import com.begemot.kclib.KHeader
import com.begemot.kclib.KText2
import com.begemot.kclib.KWindow
import timber.log.Timber

@ExperimentalLayout
@Composable
fun editPreferences(selectLang: MutableState<Boolean>, statusApp: StatusApp) {
    val selectLocales = state { false }
    if(selectLocales.value) localesDlg(selectLocales,statusApp)
    val getOptions= getSelectedLang()
    Timber.d("getOptions size ${getOptions.size}")
    Dialog(onCloseRequest = { selectLang.value = false }) {
        val localFontSize= state{statusApp.fontSize}
        KWindow() {
            KHeader(txt = "Settings", onClick = {selectLang.value = false})
            //val radioOptions = listOf("en", "es", "it","ur","zh","ca","zh-TW")
            val radioOptions= getOptions.map{it->it.value}.sorted()
              var indx=radioOptions.indexOf(getOptions[statusApp.lang])

            Timber.d("indx $indx   statusApp.lang=${statusApp.lang}  getOptions= $getOptions")
            if(indx==-1) indx=0
            val (selectedOption, onOptionSelected) = state { radioOptions[indx] }
            RadioGroup(options = radioOptions,selectedOption = selectedOption,onSelectedChange = onOptionSelected)

            KText2("Text Size",size=localFontSize.value)
            var sliderPosition by state{statusApp.fontSize.toFloat()}
            Slider(
                value=sliderPosition,
                onValueChange={sliderPosition=it;localFontSize.value=it.toInt()},
                valueRange = 10f..35f
            //    color=Color.Black
            )


            KButtonBar {
                Button(onClick = {selectLocales.value=true}){ Text("+ Languages") }
                Button(onClick = {
                    // selectedLang.value = selectedOption
                    selectLang.value = false
                    statusApp.lang = getOptions.getKey(selectedOption).toString()
                    statusApp.fontSize = localFontSize.value
                    prefs.fontSize = localFontSize.value
                    prefs.kLang =  getOptions.getKey(selectedOption).toString()
                }) {
                    Text("oK")
                }
            }
        }
    }
    //  }

}
@Preview
@Composable
fun atos(){
    Dialog(onCloseRequest = {  }) {
        var state by state { 0 }
        val titles = listOf("TAB 1", "TAB 2", "TAB 3 WITH LOTS OF TEXT")
        Column {
            TabRow(items = titles, selectedIndex = state) { index, text ->
                Tab(
                    text = { Text(text) },
                    selected = state == index,
                    onSelected = { state = index })


            }
            when (state) {
                0 -> tab1()
                1 -> Text("poma")
            }
            Text(
                modifier = Modifier.gravity(Alignment.CenterHorizontally),
                text = "Text tab ${state + 1} selected",
                style = MaterialTheme.typography.body1
            )
        }
    }
}


@Composable
fun tab1(){
    Box(backgroundColor = Color.Green,modifier = Modifier.preferredSize(100.dp)){
        Text("patata"); Text("gos")
    }
}


@ExperimentalLayout

@Composable
fun dlgLocale(statusApp: StatusApp){
    val scr= ScrollerPosition()
   // setSelectedLang()
   KWindow(size = 220){
       KHeader(txt = "Select languages", onClick ={} )
       Box(Modifier.preferredHeight(300.dp), border=Border(2.dp, Color.Black)) {
           VerticalScroller(scrollerPosition = scr) {
               getLocales().forEach {
                   Row() {
                       var checked by state { false }
                       Checkbox(
                           checked = checked,
                           onCheckedChange = {

                               c -> it.checked = c
                               checked = !checked
                               if(it.acronim.equals(statusApp.lang) ){
                                    it.checked = true
                                   checked = true
                               }
                           }
                       )
                       Text(it.toString())
                   }
               }
           }
       }
       KButtonBar {
          Button(onClick = {scr.scrollTo(200f)}) { Text("Hola")}
          Button(onClick = {Ok()}) { Text("Ok")}
       }
   }
}

fun Ok(){
    val lSelectedLocales=getLocales().filter{ it->it.checked}
    val lnames=lSelectedLocales.map{it.acronim}
    val s=lnames.joinToString (",")
    prefs.selectedLang=lnames.joinToString(",")
    Timber.d("selectedLang : ${prefs.selectedLang}")
}

fun setSelectedLang(){
    val lAllLocales= getLocales()
    prefs.selectedLang.split(",").forEach{
        lAllLocales.find { lang->it.equals(lang.acronim) }?.checked=true
    }
}

//fun getSelectedLang():List<KLocale>{
/*
Returns a bi-map of the acronim/displayname stored in prefs SELECTEDLANG
if prefs is empty sets Local.current.language as selected in AllLocales and adds it to the map
if prefs doesen't contain Local it add it's to the map and selects it at AllLocales
and checks=true all prefs in AllLocales
 */

fun getSelectedLang():MyHashMap<String,String>{
    val lS= mutableListOf<KLocale>()
    val lAllLocales= getLocales()
    val kLocale = lAllLocales.find { it->it.acronim.equals(Locale.current.language) }
    kLocale?.checked=true
    val lSelected=prefs.selectedLang.split(",")  //"ca,en" ...

    if(lSelected.find {it->it.equals(Locale.current.language) }==null){
            lS.add(kLocale!!)
        }
       lSelected.forEach{
          val l=lAllLocales.find { kloc->it.equals(kloc.acronim) }
           if(l!=null){
               l.checked=true
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
    val map:MyHashMap<String,String> = MyHashMap()
    lS.forEach{
        map.put(it.acronim,it.displayName)
    }
    return map
   // return lS
}



@ExperimentalLayout
@Composable
fun localesDlg(selectLocales:MutableState<Boolean>,statusApp: StatusApp){
    val context = ContextAmbient.current
    val scr= ScrollerPosition()
    Dialog(onCloseRequest = {selectLocales.value=false}) {
        val lKLocale = getLocales()
        KWindow(size=220) {
            KHeader(txt = "Select languages", onClick ={selectLocales.value=false} )
            //Box(Modifier.preferredHeight(300.dp),border= Border(2.dp, Color.Black)) {
                /*Text("Locales size: ${lKLocale.size}")
                Button(onClick = {
                    Timber.d("sopa  ${lKLocale[0].checked}")
                }){ Text("boolaZ") }*/
                VerticalScroller(scrollerPosition = scr) {
                    Button(onClick = {scr.scrollTo(200f)}) { Text("Hola2")}
                    lKLocale.forEach {
                        Row(){
                            var checked by state{ it.checked }
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { c ->
                                         it.checked = c
                                         checked=!checked
                                        if(it.acronim.equals(statusApp.lang) ){
                                            it.checked = true
                                            checked = true
                                            Toast.makeText(context,"You can't remove current selected language",Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                            Text(it.toString())
                        }
                    }
                }
            //}
            KButtonBar {
                Button(onClick = {scr.scrollTo(200f)}) { Text("HolaX")}
                Button(onClick = {Ok();selectLocales.value=false}) { Text("Ok")}
            }
        }

    }
}


fun getLocales():List<KLocale>{
    return lKLocales
}

class MyHashMap<K,V>:HashMap<K,V>(){
    private var rMap:MutableMap<V,K> = HashMap()
    override fun put(key:K,value:V):V?{
        rMap[value]=key
        return super.put(key, value)
    }
    fun getKey(target:V):K ?{
        return rMap[target]
    }
}

private  val lKLocales= listOf(
    KLocale("Afrikaans","af"),
    KLocale("Albanian","sq"),
    KLocale("Amharic","am"),
    KLocale("Arabic","ar"),
    KLocale("Armenian","hy"),
    KLocale("Azerbaijani","az"),
    KLocale("Basque","eu"),
    KLocale("Belarusian","be"),
    KLocale("Bengali","bn"),
    KLocale("Bosnian","bs"),
    KLocale("Bulgarian","bg"),
    KLocale("Catalan","ca"),
    KLocale("Cebuano","ceb"),
    KLocale("Chinese (Simplified)","zh"),
    KLocale("Chinese (Traditional)","zh-TW"),
    KLocale("Corsican","co"),
    KLocale("Croatian","hr"),
    KLocale("Czech","cs"),
    KLocale("Danish","da"),
    KLocale("Dutch","nl"),
    KLocale("English","en"),
    KLocale("Esperanto","eo"),
    KLocale("Estonian","et"),
    KLocale("Finnish","fi"),
    KLocale("French","fr"),
    KLocale("Frisian","fy"),
    KLocale("Galician","gl"),
    KLocale("Georgian","ka"),
    KLocale("German","de"),
    KLocale("Greek","el"),
    KLocale("Gujarati","gu"),
    KLocale("Hausa","ha"),
    KLocale("Haitian Creole","ht"),
    KLocale("Hawaiian","haw"),
    KLocale("Hebrew","he"), //or iw
    KLocale("Hindi","hi"),
    KLocale("Hmong","hmn"),
    KLocale("Hungarian","hu"),
    KLocale("Icelandic","is"),
    KLocale("Igbo","ig"),
    KLocale("Indonesian","id"),
    KLocale("Irish","ga"),
    KLocale("Italian","it"),
    KLocale("Japanese","ja"),
    KLocale("Javanese","jv"),
    KLocale("Kannada","kn"),
    KLocale("Kazakh","kk"),
    KLocale("Khmer","km"),
    KLocale("Kinyarwanda","rw"),
    KLocale("Korean","ko"),
    KLocale("Kurdish","ku"),
    KLocale("Kyrgyz","ky"),
    KLocale("Lao","lo"),
    KLocale("Latin","la"),
    KLocale("Latvian","lv"),
    KLocale("Lithuanian","lt"),
    KLocale("Luxembourgish","lb"),
    KLocale("Macedonian","mk"),
    KLocale("Malagasy","mg"),
    KLocale("Malay","ms"),
    KLocale("Malayalam","ml"),
    KLocale("Maltese","mt"),
    KLocale("Maori","mi"),
    KLocale("Marathi","mr"),
    KLocale("Myanmar (Burmese)","my"),
    KLocale("Mongolian","mn"),
    KLocale("Nepali","ne"),
    KLocale("Nyanja (Chichewa)","ny"),
    KLocale("Norwegian","no"),
    KLocale("Odia (Oriya)","or"),
    KLocale("Pashto","ps"),
    KLocale("Persian","fa"),
    KLocale("Polish","pl"),
    KLocale("Portuguese  Brazil","pt"),
    KLocale("Punjabi","pa"),
    KLocale("Romanian","ro"),
    KLocale("Russian","ru"),
    KLocale("Samoan","sm"),
    KLocale("Scots Gaelic","gd"),
    KLocale("Serbian","sr"),
    KLocale("Sesotho","st"),
    KLocale("Shona","sn"),
    KLocale("Sinhala (Sinhalese)","si"),
    KLocale("Sindhi","sd"),
    KLocale("Slovak","sk"),
    KLocale("Slovenian","sl"),
    KLocale("Somali","so"),
    KLocale("Spanish","es"),
    KLocale("Sundanese","su"),
    KLocale("Swahili","sw"),
    KLocale("Swedish","sv"),
    KLocale("Tajik","tg"),
    KLocale("Tamil","ta"),
    KLocale("Tatar","tt"),
    KLocale("Telugu","te"),
    KLocale("Thai","th"),
    KLocale("Turkish","tr"),
    KLocale("Turkmen","tk"),
    KLocale("Ukrainian","uk"),
    KLocale("Urdu","ur"),
    KLocale("Uyghur","ug"),
    KLocale("Uzbek","uz"),
    KLocale("Vietnamese","vi"),
    KLocale("Welsh","cy"),
    KLocale("Xhosa","xh"),
    KLocale("Yiddish","yi"),
    KLocale("Yoruba","yo"),
    KLocale("Zulu","zu")
    )


data class KLocale(val displayName: String ="", val acronim:String="", var checked:Boolean=false){
    override fun toString(): String {
        return displayName+" "+acronim
    }
}