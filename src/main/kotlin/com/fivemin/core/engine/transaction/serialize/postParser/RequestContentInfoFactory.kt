package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import com.fivemin.core.engine.*
import java.net.URI

interface RequestContentInfoFactory<in Document : Request> {
    suspend fun get(trans: FinalizeRequestTransaction<Document>): RequestContentInfo
}

class RequestContentInfoFactoryImpl<Document : Request>(private val factories: Iterable<RequestFactory>) :
    RequestContentInfoFactory<Document> {
    override suspend fun get(trans: FinalizeRequestTransaction<Document>): RequestContentInfo {
        var ret = factories.map {
            it.get(trans)
        }.filterOption()

        return RequestContentInfo(ret)
    }
}

interface RequestFactory {
    suspend fun <Document : Request> get(trans: FinalizeRequestTransaction<Document>): Option<RequestLinkInfo>
}

class ExtAttrRequestFactory(private val attributeTargetName: String, private val selector: LinkSelector) :
    RequestFactory {

    private val extractor: LinkExtractor = LinkExtractImpl()

    private fun create(info: Iterable<LinkExtractedInfo>, token: RequestToken, parentURI: URI): RequestLinkInfo {
        return RequestLinkInfo(
            attributeTargetName,
            info.map {
                HttpRequestImpl(
                    Some(token),
                    it.absoluteURI,
                    RequestType.ATTRIBUTE,
                    PerRequestHeaderProfile(none(), Some(it.referrer), parentURI.toOption(), it.absoluteURI),
                    it.additionalTag
                )
            },
            InitialOption()
        )
    }

    override suspend fun <Document : Request> get(trans: FinalizeRequestTransaction<Document>): Option<RequestLinkInfo> {
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

        return RequestLinkInfo(
            attributeTargetName,
            info.map {
                HttpRequestImpl(
                    Some(token),
                    it.absoluteURI,
                    RequestType.LINK,
                    PerRequestHeaderProfile(none(), Some(it.referrer), parentURI.toOption(), it.absoluteURI),
                    it.additionalTag
                )
            },
            InitialOption(parseOption = parseOption)
        )
    }

    override suspend fun <Document : Request> get(trans: FinalizeRequestTransaction<Document>): Option<RequestLinkInfo> {
        return trans.result.map {
            extractor.extract(it, Some(selector)).map {
                create(it, trans.request.token, trans.request.target)
            }
        }.flatten().orNull().toOption()
    }
}

data class RequestContentInfo(val linkInfo: Iterable<RequestLinkInfo>)

data class RequestLinkInfo(val name: String, val requests: Iterable<HttpRequest>, val option: InitialOption)
