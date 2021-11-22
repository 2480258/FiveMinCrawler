package core.request.cookie

import kotlinx.coroutines.Deferred

interface CookieResolveTarget{
    fun <Ret> sync(func : suspend () -> Deferred<Ret>) : Deferred<Ret>
}