package core.engine

import java.util.*

data class Tag (val flag: EnumSet<TagFlag>, val name: String, val value: String){
    private fun checkFlag(ref: TagFlag) : Boolean{
        return flag.contains(ref)
    }

    val isAlias : Boolean
        get(){
            return checkFlag(TagFlag.ALIAS)
        }

    val isUnique : Boolean
    get(){
        return checkFlag(TagFlag.UNIQUE)
    }

    val convertToAttribute : Boolean
    get() {
        return checkFlag(TagFlag.CONVERT_TO_ATTRIBUTE)
    }

}

enum class TagFlag(val tag : Number){
    NONE(0), UNIQUE(1), ALIAS(0b100), CONVERT_TO_ATTRIBUTE(0b1000)
}
