package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import fivemin.core.engine.*
import fivemin.core.request.RequestHeaderProfile
import java.net.URI

interface RequestContentInfoFactory<in Document : Request> {
    fun get(trans: FinalizeRequestTransaction<Document>): RequestContentInfo
}

class RequestContentInfoFactoryImpl<Document : Request>(private val factories: Iterable<RequestFactory>) :
    RequestContentInfoFactory<Document> {
    override fun get(trans: FinalizeRequestTransaction<Document>): RequestContentInfo {
        var ret = factories.map {
            it.get(trans)
        }.filterOption()
        
        return RequestContentInfo(ret)
    }
}

interface RequestFactory {
    fun <Document : Request> get(trans: FinalizeRequestTransaction<Document>): Option<RequestLinkInfo>
}

class ExtAttrRequestFactory(private val attributeTargetName: String, private val selector: LinkSelector) :
    RequestFactory {
    
    private val extractor: LinkExtractor = LinkExtractImpl()
    
    private fun create(info: Iterable<LinkExtractedInfo>, token: RequestToken, parentURI: URI): RequestLinkInfo {
        return RequestLinkInfo(attributeTargetName, info.map {
            HttpRequestImpl(
                Some(token),
                it.absoluteURI,
                RequestType.ATTRIBUTE,
                PerRequestHeaderProfile(RequestHeaderProfile(), Some(it.referrer), parentURI, it.absoluteURI),
                it.additionalTag
            )
        }, InitialOption())
    }
    
    override fun <Document : Request> get(trans: FinalizeRequestTransaction<Document>): Option<RequestLinkInfo> {
        return trans.result.map {
            extractor.extract(it, Some(selector)).map {
                create(it, trans.request.token, trans.request.target)
            }
        }.flatten().orNull().toOption()
    }
}

class LinkRequestFactory(
    private val attributeTargetName: String,
    private val selector: LinkSelector,
    private val preDestPage: Option<PageName>
) : RequestFactory {
    private val extractor: LinkExtractor
    
    init {
        extractor = LinkExtractImpl()
    }
    
    private fun create(info: Iterable<LinkExtractedInfo>, token: RequestToken, parentURI: URI): RequestLinkInfo {
        val parseOption = preDestPage.map {
            ParseOption(it)
        }
        
        return RequestLinkInfo(attributeTargetName, info.map {
            HttpRequestImpl(
                Some(token),
                it.absoluteURI,
                RequestType.LINK,
                PerRequestHeaderProfile(RequestHeaderProfile(), Some(it.referrer), parentURI, it.absoluteURI),
                it.additionalTag
            )
        }, InitialOption(parseOption = parseOption))
    }
    
    override fun <Document : Request> get(trans: FinalizeRequestTransaction<Document>): Option<RequestLinkInfo> {
        return trans.result.map {
            extractor.extract(it, Some(selector)).map {
                create(it, trans.request.token, trans.request.target)
            }
        }.flatten().orNull().toOption()
    }
}

data class RequestContentInfo(val linkInfo: Iterable<RequestLinkInfo>)

data class RequestLinkInfo(val name: String, val requests: Iterable<HttpRequest>, val option: InitialOption)