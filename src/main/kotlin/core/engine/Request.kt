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

class DefaultRequest(
    override val tags: TagRepository,
    override val parent: Option<RequestToken>,
    override val target: URI,
    override val requestType: RequestType,

) : Request {
    override val token: RequestToken = RequestToken.create()
    override val documentType: DocumentType = DocumentType.DEFAULT

    override fun copyWith(newTarget: Option<URI>, newtags: Option<TagRepository>): Request {
        return DefaultRequest(
            newtags.fold({ tags }, { it }),
            parent,
            newTarget.fold({ target }, { it }),
            requestType,
            documentType
        )
    }
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
