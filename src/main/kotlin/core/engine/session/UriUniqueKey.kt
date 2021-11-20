package core.engine.session

import core.engine.UniqueKey
import java.net.URI

class UriUniqueKey constructor(private val value : URI) : UniqueKey() {

    override fun eq(key: UniqueKey): Boolean {
        if(key is UriUniqueKey) {
            return value.equals(key)
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