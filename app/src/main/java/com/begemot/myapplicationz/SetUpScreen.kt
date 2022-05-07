package com.begemot.myapplicationz

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    Timber.w("hey ya")
    //return
    resfreshWraper(true) {
        val scrollState: ScrollState = rememberScrollState(0)
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .border(2.dp, color = Color.Yellow)
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(Color.Transparent)
            ) {
                sApp.vm.msg.LMSG.forEach {
                    Text(
                        it,
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .background(Color.Gray),
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 20.sp,
                        
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
            if(sApp.shallIquit) {
                Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) {
                    Button(onClick = {
                        //moveTaskToBack(true)
                        android.os.Process.killProcess(android.os.Process.myPid())
                        System.exit(1)
                    }) {
                        Text("QUIT")
                    }
                }
            }
        }
    }

}

@Preview
@Composable
fun UTThemes() {
    val lS=listOf("one","two","tree")
    Box(
        Modifier
            .background(Color.Blue)
            .fillMaxSize(1f)) {
        Column(){
            lS.forEach{
                Text(it)
            }
        }
        Column(Modifier.align(Alignment.BottomCenter)) {
            Text("HI")
        }

    }
}
