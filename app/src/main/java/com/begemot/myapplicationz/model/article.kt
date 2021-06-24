package com.begemot.myapplicationz.model
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.AppStatus
import com.begemot.myapplicationz.KCache
import com.begemot.myapplicationz.KProvider
import com.begemot.myapplicationz.StatusApp
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.lang.Exception


//fun nameFilefromLink(np:String,link:String,tlang:String):String{
//    return "Articles/$np${link.takeLast(15).replace("/","")}$tlang"
//}

@Serializable
class BookMark(val iBookMark: Int =0)


@Serializable
class BookMark2(){
    val bkMap: HashMap<Int,Int> = HashMap<Int,Int>()

    fun toggleBookMark(i:Int,q:articleHandler){
        if(bkMap[i]==null) bkMap[i]=i
        else bkMap.remove(i)
        KCache.storeInCache(q.nameFileArticle()+".BKM",
            kjson.encodeToString(BookMark2.serializer(),this))
    }
    fun isBookMark(i:Int):Boolean{
        if(bkMap[i]==null) return false
        else return true
    }

}


/*suspend fun findBookMark(file:String):Int{
      val lP = KCache.loadFromCache(file)
      if(lP.isEmpty()) return 1
      val l = Json.decodeFromString <BookMark>(lP)
      return l.iBookMark
}

fun storeBookMark(file:String,iBookMark: Int){
    val sAux=Json.encodeToString(BookMark.serializer(), BookMark(iBookMark))
    KCache.storeInCache(file,sAux)
}*/

class articleHandler(val nphandler:String="", val link:String="", val tlang:String=""){
    fun nameFileArticle():String {
        return "Articles/$nphandler${link.takeLast(15).replace("/","")}$tlang"
    }
}

class article {
    val qarticleHandler:MutableState<articleHandler> = mutableStateOf(articleHandler())
    val lArticle : MutableState<List<OriginalTrans>> = mutableStateOf(emptyList())
    val iInitialItem :MutableState<Int> = mutableStateOf(0)
    val bookMarks:MutableState<BookMark2> = mutableStateOf(BookMark2())

    suspend fun reinicializeArticle(q:articleHandler) {
        qarticleHandler.value = q
        lArticle.value        = emptyList()
       // bookMarks.value       =
        loadBookMarks()
    }

    suspend fun loadBookMarks(){
        val lP = KCache.loadFromCache(qarticleHandler.value.nameFileArticle()+".BKM")
        if(lP.isEmpty()) bookMarks.value=BookMark2()
        else {
            val l = kjson.decodeFromString<BookMark2>(lP)
            bookMarks.value = l
        }
        //return l.iBookMark }
    }


    /*fun isbookmark(i:Int):Boolean{
        if(i==1) return true
        return false
    }*/

   /* fun  saveBookMark(i:Int,sApp: StatusApp,link: String){
        val s = articleHandler(sApp.currentNewsPaper.handler,link,sApp.lang).nameFileArticle()+".BKM"
        //val s = nameFilefromLink(sApp.currentNewsPaper.handler,link,sApp.lang)+".BKM"
        //storeBookMark(s,i)
    }

    fun addBookMark(i:Int){

    }*/


    suspend fun getTransArt(ah: articleHandler,sApp: StatusApp) {

//        suspend fun getTransArt(sApp: StatusApp, otl: OriginalTransLink) {
        Timber.d("getTransArt ${ah.nphandler}  ${ah.tlang}")
        sApp.currentStatus.value = AppStatus.Loading
        //iInitialItem.value = findBookMark(ah.nameFileArticle()+".BKM")
        //iInitialItem.value = findBookMark(nameFilefromLink(ah.nphandler,ah.link,ah.tlang)+".BKM")
        //throw Exception("BOOM")
        //val q=articleHandler(sApp.currentNewsPaper.handler,otl.kArticle.link,sApp.lang)
        val resp=KProvider.getArticle(ah)
        //val resp = KNews().getArticle(sApp.currentNewsPaper.handler, otl.kArticle.link, sApp.lang)
        Timber.d("getTransArt ${resp.timeInfo()}")
        when (resp) {
            is KResult2.Success -> {
                Timber.d("size ${resp.t.size}")
                sApp.currentStatus.value = AppStatus.Idle
                lArticle.value = resp.t
                  Timber.d(lArticle.value.print("article"))
                if(resp.t.size==0){
                     lArticle.value=listOf(OriginalTrans("Article only for subscriptors"))
                }
            }
            is KResult2.Error -> {
                Timber.d("error")
                sApp.currentStatus.value = AppStatus.Error(resp.msg, Exception(resp.msg))
            }
        }
    }
}