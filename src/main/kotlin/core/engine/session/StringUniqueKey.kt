package core.engine.session

import core.engine.UniqueKey

class StringUniqueKey constructor(private val value : String) : UniqueKey(){
    override fun eq(key: UniqueKey): Boolean {
        if(key is StringUniqueKey) {
            return value.equals(key)
        }

        return false
    }

    override fun hash(): Int {
        return value.hashCode()
    }

    override fun toStr(): String {
        return value
    }
}