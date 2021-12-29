package com.fivemin.core.request.cookie

class OneWayCookieSyncPolicyImpl(destination : List<CookiedRequester>) : CookieSyncPolicy{
    override val outWard : OutwardPolicy

    init{
        outWard = OutwardPolicy(destination.map{
            it.cookieRepository
        })
    }
}