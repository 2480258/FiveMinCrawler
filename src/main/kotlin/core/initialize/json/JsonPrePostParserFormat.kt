package core.initialize.json

import arrow.core.Either
import arrow.core.Option
import arrow.core.none
import arrow.core.toOption
import core.engine.*
import core.engine.transaction.TagSelector
import core.engine.transaction.UriRegexPageCondition
import core.engine.transaction.serialize.postParser.*
import java.util.*

class JsonPrePostParserFormat(
    val bookName: String,
    val globalCondition: JsonPageConditionFormat,
    val pages: JsonParserPageFormat,
    val attributeRequester: JsonParseRequesterFormat
) {
}

class JsonParserPageFormat(
    val pageName: String,
    val condition: JsonPageConditionFormat,
    val internalAttributes: List<JsonParserInternalAttributeFormat> = listOf(),
    val linkAttributes: List<JsonParserLinkAttributeFormat> = listOf(),
    val externalAttributes: List<JsonParserLinkAttributeFormat> = listOf(),
    val targetContainer: JsonParserContainerFormat,
    val tag: List<JsonParserPageTagFormat> = listOf(),
    val targetRequesterEngine: JsonParseRequesterFormat
) {
    val attributeFactory = DocumentAttributeFactoryImpl

    private fun buildExt() : RequestContentInfoFactory<Request>{
        var ext = externalAttributes.map {
            it.buildAsExt()
        }

        return RequestContentInfoFactoryImpl<Request>(ext)
    }

    private fun buildLink() : RequestContentInfoFactory<Request> {
        var lst = linkAttributes.map {
            it.buildAsLink()
        }

        return RequestContentInfoFactoryImpl<Request>(lst)
    }

    private fun buildInte() : InternalContentInfoFactory<Request>{
        var lst = internalAttributes.map{
            it.build()
        }

        return InternalContentInfoFactoryImpl<Request>(lst, attri)
    }
}

class JsonParseRequesterFormat(val targetRequester : String) {
    fun build() : RequestOption{
        return RequestOption(RequesterPreference(RequesterEngineInfo(targetRequester), none()))
    }
}

class JsonParserPageTagFormat (
    val name : String,
    val tagRegex : String,
    val isAlias : Boolean
){
    fun build() : TagSelector{
        var flag = EnumSet.of(TagFlag.CONVERT_TO_ATTRIBUTE)

        if(isAlias){
            flag.add(TagFlag.ALIAS)
        }

        return TagSelector(name, Regex(tagRegex), flag)
    }
}

class JsonParserContainerFormat(val workingSetMode : String) {
    fun build() : ContainerOption{
        return ContainerOption(WorkingSetMode.valueOf(workingSetMode))
    }
}

class JsonParserLinkAttributeFormat(
    val attributeName: String,
    val uriRegex: String? = null,
    val xpath: String? = null,
    val cssSelector: String? = null,
    val destPage: String? = null
) {

    private fun getRegex(): Option<Regex> {
        return uriRegex.toOption().map {
            Regex(it)
        }
    }

    private fun getNav(): ParserNavigator {
        if ((xpath != null) && (cssSelector != null)) {
            throw IllegalArgumentException()
        }

        if (xpath != null) {
            return ParserNavigator(Either.Right(xpath))

        }

        if (cssSelector != null) {
            return ParserNavigator(Either.Left(cssSelector))
        }

        throw IllegalArgumentException()
    }

    fun buildAsExt(): ExtAttrRequestFactory {
        return ExtAttrRequestFactory(attributeName, LinkSelector(getNav(), getRegex()))
    }

    fun buildAsLink() : LinkRequestFactory {
        return LinkRequestFactory(attributeName, LinkSelector(getNav(), getRegex()), destPage.toOption().map { PageName(it) })
    }
}

class JsonParserInternalAttributeFormat(
    val attributeName: String,
    val cssSelector: String? = null,
    val xpath: String? = null,
    var parseMode: String
) {
    private fun getNav(): ParserNavigator {
        if ((xpath != null) && (cssSelector != null)) {
            throw IllegalArgumentException()
        }

        if (xpath != null) {
            return ParserNavigator(Either.Right(xpath))
        }

        if (cssSelector != null) {
            return ParserNavigator(Either.Left(cssSelector))
        }

        throw IllegalArgumentException()
    }

    fun build(): InternalContentParser {
        return InternalContentParser(attributeName, getNav(), TextSelectionMode.valueOf(parseMode))
    }
}

class JsonPageConditionFormat(
    val uriRegex: String
) {
    fun build(): UriRegexPageCondition {
        return UriRegexPageCondition(Regex(uriRegex))
    }
}
