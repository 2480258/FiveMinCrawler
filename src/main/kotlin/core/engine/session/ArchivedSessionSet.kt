package core.engine.session

import core.engine.UniqueKey


data class ArchivedSessionSet constructor(private val set : Iterable<ArchivedSession>){
    fun isConflict(key : UniqueKey) : Boolean{
        return set.any { it -> it.isConflict(key)}
    }

}

data class ArchivedSession constructor(private val set : Iterable<UniqueKey>){
    fun isConflict(key : UniqueKey) : Boolean{
        return set.contains(key)
    }
}