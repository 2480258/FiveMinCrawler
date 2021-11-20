package core.engine

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class SessionToken private constructor(val tokenNumber: Int) {
    companion object {
        var LastUsed: Int = -1
        val lock = ReentrantLock()

        fun Create(): SessionToken {
            return lock.withLock {
                LastUsed++;
                return SessionToken(LastUsed)
            }
        }
    }
}