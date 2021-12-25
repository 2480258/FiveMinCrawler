package fivemin.core.engine.session

import fivemin.core.engine.UniqueKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
data class ArchivedSessionSet constructor(private val set : List<ArchivedSession>){
    fun isConflict(key : UniqueKey) : Boolean{
        return set.any { it -> it.isConflict(key)}
    }
}

@Serializable
data class ArchivedSession constructor(private val set : List<UniqueKey>){
    fun isConflict(key : UniqueKey) : Boolean{
        return set.any{
            it.equals(key)
        }
    }
}