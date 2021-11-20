package core.engine.session

import arrow.core.*
import core.engine.SessionToken
import core.engine.UniqueKey
import core.engine.UniqueKeyRepository
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class UniqueKeyRepositoryImpl constructor(private val set : Either<Unit, ArchivedSessionSet>): UniqueKeyRepository {

    private val dic :  MutableMap<SessionToken, UniqueKeyOwnership> = mutableMapOf()
    private val list : MutableMap<UniqueKeyOwnership, MutableList<UniqueKeyState>> = mutableMapOf()
    private val lock : ReentrantLock = ReentrantLock()


    private fun findGlobalExceptSelf(ownership : UniqueKeyOwnership, key : UniqueKey) : Either<Unit, UniqueKeyState>{
        return set.map { x ->
            if(x.isConflict(key)){
                throw UniqueKeyDuplicateException()
            }

            list.filter { x -> x.key != ownership }
                .map { x -> findFromStateList(x.component2(), key) }.single { x -> x.isRight() }
        }.flatten()
    }

    private fun findFromSelf(self : UniqueKeyOwnership, key : UniqueKey) : Either<Unit, UniqueKeyState>{
        return list[self].rightIfNotNull {  }.map { x -> findFromStateList(x, key) }.flatten()
    }

    private fun findFromStateList(states : List<UniqueKeyState>, key : UniqueKey) : Either<Unit,UniqueKeyState>{
        return states.firstOrNull() { it -> it.Key == key}.rightIfNotNull {  }
    }

    private fun addOrUpdateKey(handle : UniqueKeyOwnership, key : UniqueKey){
        lock.withLock{
            var global = findGlobalExceptSelf(handle, key)

            if(global.isNotEmpty()){
                throw UniqueKeyDuplicateException()
            }

            var lst = list[handle].toOption()

            if(lst.isEmpty()){
                list[handle] = mutableListOf()
            }

            var self = findFromSelf(handle, key)
                .fold({x -> list[handle]!!.add(UniqueKeyState(key))},
                    {x -> x.increaseDuplicationCount()})
        }
    }

    private fun getOwnership(token : SessionToken) : UniqueKeyOwnership {
        return lock.withLock{
            return dic[token].rightIfNotNull { }.fold(
                {x -> var os = UniqueKeyOwnership.Create()
                    dic[token] = os
                    return os},
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
                ArchivedSession(list[dic[it]]!!.map{it.Key})
            })
        }
    }

    class UniqueKeyState constructor(val Key : UniqueKey){
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