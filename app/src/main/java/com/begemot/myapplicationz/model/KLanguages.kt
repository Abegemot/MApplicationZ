package com.begemot.myapplicationz.model

import com.begemot.myapplicationz.KCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

@Serializable
data class dLang(val lng:String, var tone:Float=1f, var speed:Float=1f,val selected:Boolean=false)


@Serializable
data class langContainer(val name:String = "lang",val lMap:MutableMap<String,dLang> = HashMap())

class KLAnguanges(){
    private var lMap:MutableMap<String,dLang> = HashMap()
    private var changed = false
    private val namefile="lanpitch"
    val size:Int
        get() = lMap.size

    override fun toString(): String {
        return lMap.toString()
    }

    fun save(){
        Timber.d("Save Tone and Pitch")
        if(!changed) {
            Timber.d("Save Tone and Pitch NOT CHANGED NOT SAVED")
            return
        }
        val sAux= Json.encodeToString(langContainer.serializer(),langContainer("pse",lMap))
        Timber.d(sAux)
        KCache.storeInCache("/$namefile",sAux)
        changed = false
    }
    fun setChanged(){
        changed=true
    }



    suspend fun load(){
        //withContext(Dispatchers.IO) {
            Timber.d("KLanguages load start $namefile")
           // delay(1000)
            try {
                val lp = KCache.loadStringFromCache(namefile)
                if (lp.length == 0) return
                val ss = Json.decodeFromString<langContainer>(lp)
                Timber.d("KLanguages load OK end $namefile")
                lMap = ss.lMap

            } catch (e: Exception) {
                //Timber.d("KLanguages load end $namefile")
                Timber.e("KLanguages load exception ${e.message}")
            }
        //}
    }
    /*fun getListSelected():List<dLang>{
        val lselected=lMap.toList()
        val x=lselected.map{x->x.second}
        return x
    }*/
    fun getLang(lng:String):dLang{
        var l=lMap[lng]
        if(l!=null) return l
        l=dLang(lng,1f,1f)
        lMap.put(lng,l)
        return l
    }
}