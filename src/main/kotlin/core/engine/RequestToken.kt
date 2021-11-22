package core.engine

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class RequestToken private constructor(val tokenNumber: Int)
{
    companion object
    {
        var LastUsed : Int = -1
        val lock = ReentrantLock()

        fun create() : RequestToken
        {
            return lock.withLock {
                LastUsed++;
                return RequestToken(LastUsed)
            }
        }
    }
}