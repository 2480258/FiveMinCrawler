package core.engine

import arrow.core.Option
import core.request.*
import java.net.URI

class PerRequestHeaderProfile(val requestHeaderProfile: RequestHeaderProfile, referrerPolicy : Option<String>, src : URI, dest : URI, headerType: AcceptHeaderType){
    private val acceptHeaderPolicy : AcceptHeaderPolicy
    private val referrerPolicyFactory : ReferrerPolicyFactory

    val referrer : Option<URI>
    val accept : Option<String>

    init{
        acceptHeaderPolicy = FirefoxAcceptHeaderPolicyImpl()
        referrerPolicyFactory = ReferrerPolicyFactory()

        referrer = referrerPolicyFactory.extractReferrer(src, dest, referrerPolicy)
        accept = acceptHeaderPolicy.getHeader(headerType)
    }
}