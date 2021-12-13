package fivemin.core.engine.session

import arrow.core.*
import fivemin.core.engine.SessionToken
import fivemin.core.engine.UniqueKey
import fivemin.core.engine.UniqueKeyRepository
import fivemin.core.exclusiveSingleOrNone
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock




class UniqueKeyRepositoryImpl constructor(private val set : Option<ArchivedSessionSet>): UniqueKeyRepository {

    private val dic :  MutableMap<SessionToken, UniqueKeyOwnership> = mutableMapOf()
    private val list : MutableMap<UniqueKeyOwnership, MutableList<UniqueKeyState>> = mutableMapOf()
    private val lock : ReentrantLock = ReentrantLock()


    private fun findGlobalExceptSelf(ownership : UniqueKeyOwnership, key : UniqueKey) : Validated<Throwable, Option<UniqueKeyState>>{
        if(set.fold({false}, {it.isConflict(key)})){
            return UniqueKeyDuplicateException().invalid()
        }

        val lst = list.filter {
            it.key != ownership
        }

        var ret =  lst.map {
            findFromStateList(it.value, key)
        }

        var res = ret.map {
            if(it.isInvalid){
                return@findGlobalExceptSelf it
            }
            else{
                it.toOption().flatten()
            }
        }.filterOption().exclusiveSingleOrNone()

        return res
    }

    private fun findFromSelf(self : UniqueKeyOwnership, key : UniqueKey) : Validated<Throwable, Option<UniqueKeyState>>{
        return list[self].toOption().map {
            it.exclusiveSingleOrNone() {
                it.key == key
            }.toEither()
        }.toEither {
            Throwable() //TODO: Specify exception
        }.flatten().toValidated()
    }

    private fun findFromStateList(states : List<UniqueKeyState>, key : UniqueKey) : Validated<Throwable, Option<UniqueKeyState>>{
        return states.exclusiveSingleOrNone {  it.key == key }
    }

    private fun addOrUpdateKey(handle : UniqueKeyOwnership, key : UniqueKey){
        lock.withLock{
            var global = findGlobalExceptSelf(handle, key)

            var isUnique = global.fold({throw it}, {
                it.isNotEmpty()
            })

            if(isUnique){
                throw UniqueKeyDuplicateException()
            }

            var lst = list[handle].toOption()

            if(lst.isEmpty()){
                list[handle] = mutableListOf()
            }

            findFromSelf(handle, key)
                .fold({list[handle]!!.add(UniqueKeyState(key))},
                    {x -> x.map{
                        it.increaseDuplicationCount()
                    }})
        }
    }

    private fun getOwnership(token : SessionToken) : UniqueKeyOwnership {
        return lock.withLock{
            return dic[token].toOption().fold(
                {var os = UniqueKeyOwnership.Create()
                    dic[token] = os
                    os},
                {x -> x})
        }
    }

    override fun addAlias(token: SessionToken, key: UniqueKey) {
        lock.withLock{
            var os = getOwnership(token)
            addOrUpdateKey(os, key)
        }
    }

    override fun transferOwnership(src: SessionToken, dest: SessionToken) {
        lock.withLock{
            var s = dic[src]!!
            dic[dest] = s
            dic.remove(src)
        }
    }

    fun export(detachables : Iterable<SessionToken>) : ArchivedSessionSet{
        lock.withLock{
            var lst = detachables.filter {x ->
                dic.contains(x)
            }

            return ArchivedSessionSet(lst.map{
                ArchivedSession(list[dic[it]]!!.map{it.key})
            })
        }
    }

    class UniqueKeyState constructor(val key : UniqueKey){
        private val maxDuplication : Int = 3
        private var duplicateCount : Int = 1

        fun increaseDuplicationCount(){
            if(duplicateCount >= maxDuplication){
                throw RetryCountMaxedException()
            }

            duplicateCount++
        }



    }

    class UniqueKeyOwnership private constructor(val tokenNumber: Int)
    {
        companion object
        {
            private var LastUsed : Int = -1
            private val lock = ReentrantLock()

            fun Create() : UniqueKeyOwnership
            {
                return lock.withLock {
                    LastUsed++;
                    return UniqueKeyOwnership(LastUsed)
                }
            }
        }
    }
}

class RetryCountMaxedException : Exception(){

}

class UniqueKeyDuplicateException : Exception(){

}