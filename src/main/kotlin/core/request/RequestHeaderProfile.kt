package core.request

import arrow.core.Option
import arrow.core.Some
import arrow.core.none

enum class AcceptHeaderType{
    DEFAULT, IMAGE, VIDEO, AUDIO, SCRIPTS, CSS
}

interface AcceptHeaderPolicy{
    fun getHeader(headerType : AcceptHeaderType) : Option<String>
}

class FirefoxAcceptHeaderPolicyImpl : AcceptHeaderPolicy{
    override fun getHeader(headerType: AcceptHeaderType): Option<String> {
        return when(headerType){
            AcceptHeaderType.DEFAULT -> Some("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.5")
            AcceptHeaderType.IMAGE -> Some("image/webp,*/*")
            AcceptHeaderType.CSS -> Some("text/css,*/*;q=0.1")
            AcceptHeaderType.SCRIPTS -> Some("*/*")
            AcceptHeaderType.VIDEO -> none()
            AcceptHeaderType.AUDIO -> Some("audio/webm,audio/ogg,audio/wav,audio/*;q=0.9,application/ogg;q=0.7,video/*;q=0.6,*/*;q=0.5")
            else -> none()
        }
    }

}

class RequestHeaderProfile(
    val acceptEncoding : Option<String> = Some("gzip, deflate, br"),
    val acceptLanguage : Option<String> = Some("en-US,en;q=0.5"),
    val connection : Option<String> = Some("Keep-Alive"),
    val dnt : Option<Boolean> = Some(true),
    val te : Option<String> = Some("trailers"),
    val userAgent : Option<String> = none()){}

class PerRequestHeaderProfile : RequestHeaderProfile{

}