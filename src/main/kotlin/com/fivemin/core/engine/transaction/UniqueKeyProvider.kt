package com.fivemin.core.engine.transaction

import com.fivemin.core.UniqueKeySerializer
import com.fivemin.core.engine.*
import kotlinx.serialization.Serializable
import java.net.URI

@Serializable(with = UniqueKeySerializer::class)
data class StringUniqueKey(val src : String) : UniqueKey() {
    override fun eq(key: UniqueKey): Boolean {
        if(key is StringUniqueKey){
           return src == key.src
        }

        return false
    }

    override fun hash(): Int {
        return src.hashCode()
    }

    override fun toStr(): String {
        return src
    }
}

@Serializable(with = UniqueKeySerializer::class)
data class UriUniqueKey(val uri : URI) : UniqueKey(){
    override fun eq(key: UniqueKey): Boolean {
        if(key is UriUniqueKey)
        {
            return uri == key.uri
        }

        return false
    }

    override fun hash(): Int {
        return uri.hashCode()
    }

    override fun toStr(): String {
        return uri.toString()
    }
}

class StringUniqueKeyProvider : TagUniqueKeyProvider{
    override fun create(doc: TagRepository): Iterable<UniqueKey> {
        return doc.filter { x -> x.isAlias }.map { x -> StringUniqueKey(x.value) }
    }
}

class UriUniqueKeyProvider : DocumentUniqueKeyProvider{
    override fun <Document : Request> create(doc: Document): UniqueKey {
        return UriUniqueKey(doc.target)
    }
}