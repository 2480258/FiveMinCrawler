package core.engine

import arrow.core.Option
import java.net.URI

class HttpRequestImpl(
    override val parent: Option<RequestToken>,
    override val target: URI,
    override val requestType: RequestType,
    override val headerOption: PerRequestHeaderProfile,
    override val tags: TagRepository
) : HttpRequest {
    override val token : RequestToken = RequestToken.create()
    override val documentType: DocumentType = DocumentType.NATIVE_HTTP

    override fun copyWith(newTarget: Option<URI>, newTags: Option<TagRepository>): HttpRequest {
        return HttpRequestImpl(
            parent,
            newTarget.fold({ target }, { it }),
            requestType,
            headerOption,
            newTags.fold({ tags }, { it })
        )
    }
}