package com.begemot.myapplicationz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun sendmail(msg:String){
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
                return PasswordAuthentication("marcbegemot@gmail.com", "furgus2000")
            }
        })
    try{
        val mm=MimeMessage(session)
        mm.setFrom(InternetAddress("marcbegemot@gmail.com"))
        mm.addRecipient(Message.RecipientType.TO,InternetAddress("marcha64@yahoo.com"))
        mm.setSubject("NewsReader : ${prefs.userId}")
        mm.setText(msg)
        Transport.send(mm)
    }catch (e:MessagingException){
        e.printStackTrace()
    }
}