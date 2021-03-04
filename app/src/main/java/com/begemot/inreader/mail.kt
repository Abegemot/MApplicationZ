package com.begemot.inreader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun sendmail(msg:String){
    Timber.d("SEND MAIL")
    GlobalScope.launch(Dispatchers.Main) {
        sendmail2(msg)
    }
}


suspend fun sendmail2(msg:String)  = withContext(Dispatchers.IO){
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
                return PasswordAuthentication("marcbegemot@gmail.com", "furgus2000edmund")
            }
        })
    try{
        val mm=MimeMessage(session)
        mm.setFrom(InternetAddress("marcbegemot@gmail.com"))
        mm.addRecipient(Message.RecipientType.TO,InternetAddress("marcha64@yahoo.com"))
        mm.setSubject("NewsReader : ${prefs.userId}")
        mm.setText(msg)
        Transport.send(mm)
        Timber.d("aparently send....")
    }catch (e:MessagingException){
        Timber.d("Something went wrong !! $e")
        e.printStackTrace()
    }
}