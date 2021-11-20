package core.request.cookie

class CookieResolveTargetImpl(val repo : CookieRepository, val outPolicy : OutwardPolicy) : CookieResolveTarget {
    val lock : Any = Any()

    override fun <Ret> sync(func: () -> Ret): Ret {
        synchronized(lock){
            var ret = func()
            outPolicy.syncTo(repo)

            return ret
        }
    }
}