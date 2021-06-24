package com.begemot.myapplicationz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber

@Composable
fun SetUpScreen(sApp: StatusApp) {
    Timber.d("hey ya")
    val scrollState: ScrollState = rememberScrollState(0)
    resfreshWraper(true) {
        Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            //sApp.setMsg2("Draw SetUp Screen")
            //Text("SetUpScreen  ${sApp.vm.msg.LMSG.size}")
            sApp.vm.msg.LMSG.forEach {
                Text(
                    it,
                    color = Color.Unspecified,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 10.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun UTThemes() {
    Text("HI")
}
