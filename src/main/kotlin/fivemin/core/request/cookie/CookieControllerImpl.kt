package fivemin.core.request.cookie

import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import fivemin.core.engine.PerformedRequesterInfo


class CookieControllerImpl(val gradiant : List<CookieSyncGradiant>) : CookieResolveTargetFactory {

    val cookieDic : MutableMap<PerformedRequesterInfo, CookieRepository> = mutableMapOf()

    override fun create(info: PerformedRequesterInfo, cookieRepo: CookieRepository) : CookieResolveTarget {
        cookieDic[info] = cookieRepo

        return CookieResolveTargetImpl(cookieRepo, OutwardPolicy(gradiant.filter {
            it.source == info
        }.flatMap {
            it.destination.map {
                CookieRepositoryProxyImpl(this, it)
            }
        }))
    }

    override fun getReference(info: PerformedRequesterInfo): Validated<Throwable, CookieRepository> {
        if(cookieDic.containsKey(info)){
            return cookieDic[info]!!.valid()
        }

        return NotRecognizedCookieSyncExcepion(info.toString()).invalid()
    }

}

