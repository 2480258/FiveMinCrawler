package com.fivemin.core.request.cookie

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CookieResolveTargetImpl(val repo: CookieRepository, val outPolicy: OutwardPolicy) : CookieResolveTarget {
    val mutex = Mutex()
    override suspend fun <Ret> sync(func: suspend () -> Deferred<Ret>): Deferred<Ret> {
        return mutex.withLock {
            var ret = func()
            outPolicy.syncTo(repo)

            ret
        }
    }
}
