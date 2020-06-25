package com.begemot.myapplicationz

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.layout.Row
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.preferredWidth
import androidx.ui.material.Button
import androidx.ui.material.Checkbox
import androidx.ui.material.IconButton
import androidx.ui.material.RadioGroup
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.KeyboardArrowDown
import androidx.ui.material.icons.filled.KeyboardArrowUp
import androidx.ui.text.Locale
import androidx.ui.unit.dp
import com.begemot.kclib.KButtonBar
import com.begemot.kclib.KHeader
import com.begemot.kclib.KText2
import com.begemot.kclib.KWindow
import timber.log.Timber

@Composable
fun editPreferences(selectLang: MutableState<Boolean>, statusApp: StatusApp) {
    val selectLocales = state { false }
    if(selectLocales.value) localesDlg(selectLocales)
    Dialog(onCloseRequest = { selectLang.value = false }) {
        val localFontSize= state{statusApp.fontSize}
        KWindow() {
            KHeader(txt = "Settings", onClick = {})
            val radioOptions = listOf("en", "es", "it","ur","zh","ca","zh-TW")
            val indx=radioOptions.indexOf(statusApp.lang)
            val (selectedOption, onOptionSelected) = state { radioOptions[indx] }


            // RadioGroup(options = radioOptions,selectedOption = selectedOption,onSelectedChange = onOptionSelected)
            Row(verticalGravity = Alignment.CenterVertically ) {

                Box(modifier = Modifier.preferredWidth(110.dp)) {
                    RadioGroup(options = radioOptions,selectedOption = selectedOption,onSelectedChange = onOptionSelected)
                }

                Row(modifier = Modifier.padding(4.dp),verticalGravity = Alignment.CenterVertically) {
                    IconButton(onClick = {localFontSize.value=(localFontSize.value)-1}) {
                        Icon(Icons.Filled.KeyboardArrowDown)
                    }
                    KText2("Text Size",size=localFontSize.value)
                    IconButton(onClick = {localFontSize.value=(localFontSize.value)+1}) {
                        Icon(Icons.Filled.KeyboardArrowUp)
                    }

                }

            }
            Button(onClick = {selectLocales.value=true}){ Text("bool") }
            KButtonBar {
                Button(onClick = {
                    // selectedLang.value = selectedOption
                    selectLang.value = false
                    statusApp.lang = selectedOption
                    statusApp.fontSize = localFontSize.value
                    prefs.fontSize = localFontSize.value
                    prefs.kLang = selectedOption
                }) {
                    Text("oK")
                }
            }
        }
    }
    //  }

}



@Composable
fun localesDlg(selectLocales:MutableState<Boolean>){
    Dialog(onCloseRequest = {selectLocales.value=false}) {
        val lKLocale = getLocales()
        KWindow() {
            Box(Modifier.preferredHeight(200.dp).preferredWidth(200.dp)) {
                Text("Locales size: ${lKLocale.size}")
                Button(onClick = {
                    Timber.d("sopa  ${lKLocale[0].checked}")
                }){ Text("boolaZ") }
                VerticalScroller() {
                    lKLocale.forEach {
                        Row(){
                            var checked by state{ false }
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { c -> it.checked = c;checked=!checked }
                                )
                            Text(it.toString())
                        }
                    }
                }
            }
        }
    }
}


fun getLocales():List<KLocale>{
      val lKLocales= listOf(
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
    KLocale("Chinese(Traditional)","zh-TW"),
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
   return lKLocales
}

data class KLocale(val displayLang: String ="",val displayName:String="",var checked:Boolean=false){
    override fun toString(): String {
        return displayLang+" "+displayName
    }
}