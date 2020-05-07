package com.begemot.myapplicationz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.setContent
import com.begemot.kclib.KText
import com.begemot.kclib.KWindow
import com.begemot.kclib.kTheme

import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.ui.tooling.preview.Preview
import com.begemot.kclib.Kline

fun sopa(ctx:Activity){
    println("I am sopa")
    println("${ctx.localClassName}<---calling package")
    val I= Intent(ctx,posActivity::class.java)
    println("I am sopa 2 ")
   //I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    println("I am sopa 3.")
    //ctx.application.startActivity(I,null)
    if(!ctx.isFinishing)
    startActivity(ctx,I,null)

}


class posActivity:AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        println("UUUUUUUUUUUUUUUUUUUUJJJJJJJJJJJJJJJJJJ")
        super.onCreate(savedInstanceState, persistentState)
             println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ")
            setContent{TT()}

    }
}

@Preview
@Composable
fun TT(){
    println("-->TT()<--")
    val kt = state{ kTheme.DARK}
    KWindow(kt.value,270){
        KText(txt = "SARANDONGA 2")
        Kline()
    }
}