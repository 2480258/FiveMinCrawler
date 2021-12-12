package core.request.cookie

import kotlinx.coroutines.Deferred

class CookieResolveTargetImpl(val repo : CookieRepository, val outPolicy : OutwardPolicy) : CookieResolveTarget {
    val lock : Any = Any()

    override fun <Ret> sync(func: () -> Deferred<Ret>): Deferred<Ret> {
        synchronized(lock){
            var ret = func()
            outPolicy.syncTo(repo)

            return ret
        }
    }
}