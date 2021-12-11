package core.initialize.json

import arrow.core.Some
import core.engine.DirectIO
import core.engine.PerformedRequesterInfo
import core.engine.transaction.serialize.postParser.RequestFactory
import core.request.*
import core.request.cookie.CookieResolveTargetFactory

data class JsonRequesterCompFormat(
    val engines : List<JsonRequesterEngineFormat>,
    val cookiePolicies : List<JsonRequesterCookieSyncFormat>
) {
    fun build(factories : Iterable<RequestFactory>) : RequesterSelector {
        val cookies = cookiePolicies.map {

        }
    }
}



class JsonRequesterEngineFormat(
    val requesterEngineName : String,
    val type : String,
    val requesters : Iterable<JsonRequesterFormat>
        ){

}



class JsonRequesterFormat (
    val userAgent : String,
    val key : String = "Default"
        ){
    fun build(info : PerformedRequesterInfo, io : DirectIO, factory : CookieResolveTargetFactory) : DefaultRequesterCore{

        return DefaultRequesterCore(RequesterExtraImpl(), info, HttpRequesterConfig(RequesterConfig(factory), RequestHeaderProfile(userAgent = Some(userAgent))))
        return DefaultRequesterCore(info, HttpRequesterConfig(RequesterConfig(factory), RequestHeaderProfile(userAgent = Some(userAgent))))
    }
}