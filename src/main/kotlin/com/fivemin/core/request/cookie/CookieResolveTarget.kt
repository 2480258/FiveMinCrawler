package com.fivemin.core.request.cookie

import kotlinx.coroutines.Deferred

interface CookieResolveTarget{
    suspend fun <Ret> sync(func : suspend () -> Deferred<Ret>) : Deferred<Ret>
}