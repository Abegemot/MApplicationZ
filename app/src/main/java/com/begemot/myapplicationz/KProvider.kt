package com.begemot.myapplicationz

import androidx.compose.runtime.MutableState
import androidx.core.util.Preconditions
import com.begemot.knewsclient.KNews
import com.begemot.knewscommon.*
import io.ktor.util.*
import kotlinx.coroutines.*
import timber.log.Timber

object KProvider {

    fun getNewsPapers(): NewsPaperVersion = fromStrToNewsPaperV(KCache.loadFromCache("knews.json"))

    @KtorExperimentalAPI
    suspend fun getNewsPapersUpdates(currentver: Int): NewsPaperVersion {
        //Preconditions.checkArgument(1==1)
        //throw(Exception("Error geting news papers updates"))
        val nP = KNews().getNewsPapersWithVersion(currentver)
        if (nP.version == 0) {  //no updates
            Timber.d("no updates current version ${currentver}")
            return nP
        } else {
            Timber.d("news papers updates found")
            checkImages(nP.newspaper)
            KCache.storeInCache2("knews.json", nP.toStr())
            return nP
        }
    }

    suspend fun checkImages(lnp: List<NewsPaper>) {
        lnp.forEach {
            if (!KCache.fileExists(it.logoName, "/Images")) {
                val ba = KNews().getImage("Images/${it.logoName}")
                KCache.storeImageInCache(it.logoName, ba.bresult)

            }

        }
    }


    suspend fun getHeadLines(getHeadLines: GetHeadLines): THeadLines = withContext(Dispatchers.IO) {
        val nameFile = "/Headlines/${getHeadLines.handler}${getHeadLines.tlang}"
        //delay(1000)
        val sthl = KCache.findInCache2(nameFile)
        //throw(Exception("patata"))
        if (sthl.length == 0) { //No existeix headline
            Timber.d("No existeix headlines en cache")
            val thl2 = KNews().getHeadLines(getHeadLines)
            Timber.d("patata->${thl2.lhl}")
            KCache.storeInCache3(nameFile, thl2.toStr())
            return@withContext thl2
        } else {
            Timber.d("existeix en Cache  ${sthl.length}")
            val thdl = fromStrToTHeadLines(sthl)
            Timber.d("RETURN FROM CACHE!!! OK ->size hl ${thdl.lhl.size} ")
            return@withContext thdl
        }
    }

    suspend fun checkUpdates2(ghl:GetHeadLines):THeadLines = withContext(Dispatchers.IO) {
        val nameFile = "/Headlines/${ghl.handler}${ghl.tlang}"
        try {
            val thl2 = KNews().getHeadLines(ghl)
            // throw(Exception("patata 1"))
            if (thl2.datal != 0L) {
                KCache.removeHeadLinesOf(ghl.handler)
                KCache.storeInCache3(nameFile, toStrFromTHeadlines(thl2))
                Timber.d("end HEADLINES HAVE CHANGED data original  ${ghl.datal}  new data ${thl2.datal}")
                thl2
            } else {
                Timber.d("end HEADLINES HAVEN'T CHANGED checkUpdates2 data original  ${ghl.datal}  new data ${thl2.datal}")
                THeadLines()
            }

        } catch (e: Exception) {
            throw(Exception("${e}  "))
        }
    }
}