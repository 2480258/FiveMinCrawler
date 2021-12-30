package com.fivemin.core.request.cookie

import arrow.core.*
import com.fivemin.core.engine.PerformedRequesterInfo

class CookieControllerImpl(val gradiant: List<CookieSyncGradiant>) : CookieResolveTargetFactory {

    val cookieDic: MutableMap<PerformedRequesterInfo, CookieRepository> = mutableMapOf()

    override fun create(info: PerformedRequesterInfo, cookieRepo: CookieRepository): CookieResolveTarget {
        cookieDic[info] = cookieRepo

        return CookieResolveTargetImpl(
            cookieRepo,
            OutwardPolicy(
                gradiant.filter {
                    it.source == info
                }.flatMap {
                    it.destination.map {
                        CookieRepositoryProxyImpl(this, it)
                    }
                }
            )
        )
    }

    override fun getReference(info: PerformedRequesterInfo): Either<Throwable, CookieRepository> {
        if (cookieDic.containsKey(info)) {
            return cookieDic[info]!!.right()
        }

        return NotRecognizedCookieSyncExcepion(info.toString()).left()
    }
}
