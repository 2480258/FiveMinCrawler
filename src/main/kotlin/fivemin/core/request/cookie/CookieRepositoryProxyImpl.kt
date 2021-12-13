package fivemin.core.request.cookie

import arrow.core.Validated
import arrow.core.flatten
import fivemin.core.engine.PerformedRequesterInfo
import java.net.HttpCookie

class CookieRepositoryProxyImpl(private val solver : CookieRepositoryReferenceSolver, private val dest : PerformedRequesterInfo) :
    CookieRepository {
    var cache : Validated<Throwable, CookieRepository>? = null

    override fun getAllCookies(): Validated<Throwable, Iterable<HttpCookie>> {
        return getRepo().toEither().map{
            it.getAllCookies().toEither()
        }.flatten().toValidated()
    }

    private fun getRepo() : Validated<Throwable, CookieRepository> {
        if(cache == null){
            cache = solver.getReference(dest)
        }

        return cache!!
    }

    override fun download(repo : CookieRepository){
        getRepo().map{
            it.download(repo)
        }
    }

    override fun reset(){
        getRepo().map {
            it.reset()
        }
    }
}