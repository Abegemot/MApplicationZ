package com.begemot.myapplicationz

import androidx.lifecycle.viewModelScope
import com.begemot.myapplicationz.App.Companion.prefs
import kotlinx.coroutines.*
import timber.log.Timber
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun sendmail(msg:String,popup:Boolean = true){
    Timber.d("XSEND MAIL  $msg")
    if(msg.isEmpty() || msg.isBlank()) return
    val scope= App.sApp.vm.viewModelScope+Dispatchers.IO

    scope.launch(Dispatchers.IO) {
        sendmail2(msg,popup)
    }
}


private suspend fun sendmail2(msg:String,popup: Boolean)  = withContext(Dispatchers.IO){
    val props = System.getProperties()
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.socketFactory.port", "465")
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.port", "465")
    

    val session =  Session.getInstance(props,
        object : Authenticator() {
            //Authenticating the password
            override fun getPasswordAuthentication(): PasswordAuthentication {
                val psw="uyyqteslqjnbqrpa"
                val oldp="furgus2000edmund"
                return PasswordAuthentication("marcbegemot@gmail.com",psw )
            }
        })
    try{
        val mm=MimeMessage(session)
        mm.setFrom(InternetAddress("marcbegemot@gmail.com"))
        mm.addRecipient(Message.RecipientType.TO,InternetAddress("marcha64@yahoo.com"))
        mm.setSubject("NewsReader (${BuildConfig.VERSION_CODE}) : ${prefs.userId} ")
        mm.setText(msg)
        Transport.send(mm)
        Timber.d("aparently send....")
        if(popup) App.sApp.setMsg("message sent")
    }catch (e:MessagingException){
        KCache.writeError("Error sending mail ${e.message}")
        if(popup) App.sApp.setMsg("Sorry Message could not be sent ${e.message}")
        Timber.d("Something went wrong !! $e")
        e.printStackTrace()
    }
}
