package com.begemot.inreader.model

import com.begemot.inreader.KCache
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

    fun save(){
        if(!changed) return
        val sAux= Json.encodeToString(langContainer.serializer(),langContainer("pse",lMap))
        Timber.d(sAux)
        KCache.storeInCache3("/$namefile",sAux)
        changed = false
    }
    fun setChanged(){
        changed=true
    }

    fun load(){
        try {
            val lp= KCache.loadFromCache(namefile)
            val ss= Json.decodeFromString<langContainer>(lp)
            lMap=ss.lMap
        } catch (e: Exception) {
            Timber.d(e)
        }
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