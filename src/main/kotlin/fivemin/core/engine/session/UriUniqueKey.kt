package fivemin.core.engine.session

import fivemin.core.engine.UniqueKey
import java.net.URI

class UriUniqueKey constructor(private val value : URI) : UniqueKey() {
    override fun equals(other: Any?): Boolean {
        if(other != null && other is UniqueKey) {
            return eq(other)
        }
        
        return false
    }
    
    
    override fun eq(key: UniqueKey): Boolean {
        if(key is UriUniqueKey) {
            return value == key.value
        }

        return false
    }

    override fun hash(): Int {
        return value.hashCode()
    }

    override fun toStr(): String {
        return value.toString()
    }
}