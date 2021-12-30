package com.fivemin.core.engine

import com.fivemin.core.UniqueKeySerializer
import kotlinx.serialization.Serializable

@Serializable(with = UniqueKeySerializer::class)
abstract class UniqueKey {
    protected abstract fun eq(key: UniqueKey): Boolean

    protected abstract fun hash(): Int

    protected abstract fun toStr(): String

    override fun toString(): String {
        return toStr()
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is UniqueKey) {
            return eq(other)
        }

        return false
    }

    override fun hashCode(): Int {
        return hash()
    }
}
