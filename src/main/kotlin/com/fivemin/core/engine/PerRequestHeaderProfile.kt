package com.fivemin.core.engine

import arrow.core.Option
import arrow.core.none
import com.fivemin.core.request.*
import java.net.URI

class PerRequestHeaderProfile(
    val requestHeaderProfile: RequestHeaderProfile,
    referrerPolicy: Option<String>,
    src: URI,
    dest: URI,
    headerType: Option<AcceptHeaderType> = none()
) {
    private val acceptHeaderPolicy: AcceptHeaderPolicy
    private val referrerPolicyFactory: ReferrerPolicyFactory

    val referrer: Option<URI>
    val accept: Option<String>

    init {
        acceptHeaderPolicy = FirefoxAcceptHeaderPolicyImpl()
        referrerPolicyFactory = ReferrerPolicyFactory()

        referrer = referrerPolicyFactory.extractReferrer(src, dest, referrerPolicy)
        accept = acceptHeaderPolicy.getHeader(headerType.fold({ AcceptHeaderType.DEFAULT }, { it }))
    }
}