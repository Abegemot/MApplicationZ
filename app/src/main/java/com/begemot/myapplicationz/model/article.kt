package com.begemot.myapplicationz.model
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.*
import com.begemot.myapplicationz.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.lang.Exception



//@Serializable
//class BookMark(val iBookMark: Int =0)


@Serializable
class BookMark2(){
    val bkMap: HashMap<Int,Int> = HashMap<Int,Int>()

    fun toggleBookMark(i:Int,q:articleHandler){
        if(bkMap[i]==null) bkMap[i]=i
        else bkMap.remove(i)
        KCache.storeInCache(q.nameFileBooMarksAndLast()+".BKM",
            kjson.encodeToString(BookMark2.serializer(),this))
    }
    fun isBookMark(i:Int):Boolean{
        if(bkMap[i]==null) return false
        else return true
    }

}

class articleHandler(val nphandler:String="", val link:String="", val tlang:String=""){
    fun nameFileArticle():String {
        return "Articles/$nphandler${link.takeLast(15).replace("/","")}$tlang"
    }
    fun nameFileBooMarksAndLast():String{
        return "Articles/$nphandler${link.takeLast(15).replace("/","")}"
    }
    fun status():String{
        return "($nphandler $link $tlang)"
    }
}

class article {
    val qarticleHandler: MutableState<articleHandler> = mutableStateOf(articleHandler())
    val lArticle: MutableState<List<OriginalTrans>> = mutableStateOf(emptyList())
    val iInitialItem: MutableState<Int> = mutableStateOf(0)
    val bookMarks: MutableState<BookMark2> = mutableStateOf(BookMark2())
    var listState: LazyListState? = null
    var currentArticle = 0
    private val _nIndex = MutableStateFlow(0)
    val nIndex: StateFlow<Int> = _nIndex
    fun setNIndex(i: Int) {
        _nIndex.value = i
    }

    var holdingItem: MutableState<Int> = mutableStateOf(0)

    fun status(): String {
        return "qarticleHandler = ${qarticleHandler.value.status()}"
    }

    fun reinizializeArticle2() {
        lArticle.value = emptyList()
        listState = null
    }

    suspend fun reinicializeArticle(q: articleHandler, alistState: LazyListState) {
        qarticleHandler.value = q
        lArticle.value = emptyList()
        loadBookMarks()
        listState = alistState
    }

    suspend fun loadBookMarks() {
        //Timber.d("...........Load start")
        val lP2 = KCache.load<BookMark2>("${qarticleHandler.value.nameFileBooMarksAndLast()}.BKM")
        bookMarks.value = lP2
        //Timber.d("Book Marks ${bookMarks.value.bkMap}")
        val ii = KCache.load<Int>("${qarticleHandler.value.nameFileBooMarksAndLast()}.LIN")
        iInitialItem.value = ii
        Timber.d("Loaded Book Marks ${bookMarks.value.bkMap} initial Item = ${iInitialItem.value}")
        //Timber.d("........Load end")
    }

    fun storeLastIndex(index: Int) {
        KCache.storeInCache(
            qarticleHandler.value.nameFileBooMarksAndLast() + ".LIN",
            index.toString()
        )
    }


    suspend fun getTransArt(ah: articleHandler, sApp: StatusApp) {
        sApp.currentStatus.value = AppStatus.Loading
        val resp = KProvider.getArticle(ah)
        Timber.d("(s) NewsPaper ${ah.nphandler} chapter/link ${ah.link}  lang ${ah.tlang} time elapsed ${resp.timeInfo()}")
        Timber.d("sapp.current link ${sApp.currentLink} sapp current chapter ${sApp.vm.headLines.currChapter.value}")
        Timber.d("Article handler ${sApp.vm.article.qarticleHandler.value.nameFileArticle()}")
        when (resp) {
            is KResult3.Success -> {
                //Timber.d("size ${resp.t.size}")
                sApp.currentStatus.value = AppStatus.Idle
                lArticle.value = resp.t
                //         Timber.d(lArticle.value.print("article"))
                if (resp.t.size == 0) {
                    lArticle.value = listOf(OriginalTrans("Article only for subscriptors"))
                }
                if (sApp.currentNewsPaper.mutable) {
                    checkUpdatedArticle(sApp, ah)
                }
            }
            is KResult3.Error -> {
                Timber.d("error")
                sApp.currentStatus.value = AppStatus.Error(resp.msg, Exception(resp.msg))
            }
        }
    }


    suspend fun checkUpdatedArticle(sApp: StatusApp, ah: articleHandler) {
        val d = KCache.getFileDate(ah.nameFileArticle())
        val gart = GetArticle(ah.nphandler, ah.tlang, ah.link, d)

        when (val r = KNews().getUpdatedArticle(gart)) {
            is KResult3.Success -> {
                if (r.t.size == 0) Timber.d("NO UPDATES")
                else {
                    Timber.d("ARTICLE UPDATED")
                    sApp.snack("Article Updated!!")
                    lArticle.value=r.t
                    KCache.storeInCache(ah.nameFileArticle(), toJStr(r.t))
                }
            }
            is KResult3.Error -> {}
        }

    }
}

//Max 177