package fivemin.core.engine

interface UniqueKeyRepository {
    fun addAlias(token : SessionToken, key : UniqueKey)
    fun transferOwnership(src : SessionToken, dest : SessionToken)
}