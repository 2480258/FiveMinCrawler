package core.engine

import arrow.core.Option
import java.net.URI

interface Taggable {
    val tags: TagRepository
}

interface Request : Taggable {
    val token: RequestToken
    val parent: Option<RequestToken>
    val target: URI
    val requestType: RequestType
    val documentType: DocumentType

    fun copyWith(newTarget: Option<URI>, tags: Option<TagRepository>): Request
}

interface HttpRequest : Request {
    val headerOption: PerRequestHeaderProfile
}

enum class RequestType {
    LINK, ATTRIBUTE
}

enum class DocumentType {
    DEFAULT, NATIVE_HTTP
}
