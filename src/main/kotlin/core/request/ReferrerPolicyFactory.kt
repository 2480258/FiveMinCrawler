package core.request

import arrow.core.Option
import java.net.URI

enum class ReferrerPolicy{
    NO_REFERRER,
    UNSAFE_URL,
    ORIGIN,
    STRICT_ORIGIN,
    NO_REFERRER_WHEN_DOWNGRADE,
    SAME_ORIGIN,
    ORIGIN_WHEN_CROSS_ORIGIN,
    STRICT_ORIGIN_WHEN_CROSS_ORIGIN
}

class ReferrerPolicyFactory (private val referrerPolicy : Option<String>, private val source : URI, private val dest : URI){
    private val NO_REFERRER = "no-referrer"
    private val NO_REFERRER_WHEN_DOWNGRADE = "no-referrer-when-downgrade"
    private val ORIGIN = "origin"
    private val ORIGIN_WHEN_CROSS_ORIGIN = "origin-when-cross-origin"
    private val SAME_ORIGIN = "same-origin"
    private val STRICT_ORIGIN = "strict-origin"
    private val STRICT_ORIGIN_WHEN_CROSS_ORIGIN = "strict-origin-when-cross-origin"
    private val UNSAFE_URL = "unsafe-url"

    private val DEFAULT_REFERRER = ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN //Default value for contemporary browsers

    private fun parseReferrerPolicy(referrer : Option<String>) : ReferrerPolicy{
        return referrer.fold({DEFAULT_REFERRER}, {
            return when(it){
                NO_REFERRER -> ReferrerPolicy.NO_REFERRER
                NO_REFERRER_WHEN_DOWNGRADE -> ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE //
                ORIGIN -> ReferrerPolicy.ORIGIN
                ORIGIN_WHEN_CROSS_ORIGIN -> ReferrerPolicy.ORIGIN_WHEN_CROSS_ORIGIN
                SAME_ORIGIN -> ReferrerPolicy.SAME_ORIGIN
                STRICT_ORIGIN -> ReferrerPolicy.STRICT_ORIGIN
                STRICT_ORIGIN_WHEN_CROSS_ORIGIN -> ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                UNSAFE_URL -> ReferrerPolicy.UNSAFE_URL
                else -> DEFAULT_REFERRER
            }
        })
    }
}