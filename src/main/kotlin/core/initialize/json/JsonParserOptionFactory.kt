package core.initialize.json

import core.engine.ConfigController
import core.engine.DirectIOImpl
import core.engine.transaction.prepareRequest.preParser.PreParserImpl
import core.engine.transaction.serialize.postParser.PostParserImpl
import core.engine.transaction.serialize.postParser.RequestFactory
import core.export.ConfigControllerImpl
import core.initialize.ParseOption
import core.initialize.RequesterFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class JsonOptionFormat (
    val requestFormat : JsonRequesterCompFormat,
    val parseFormat : JsonPrePostParserFormat,
    val exportFormat : JsonExportParserFormat
        ){

}

class JsonParserOptionFactory(
    val jsonString: String,
    val factory : List<RequesterFactory>
) {
    val format : JsonOptionFormat
    val option : ParseOption
    init {
        format = Json.decodeFromString<JsonOptionFormat>(jsonString)
        option = getOption(factory)
    }

    private fun getOption(factories : Iterable<RequesterFactory>) : ParseOption {
        val config = ConfigControllerImpl()
        val io = DirectIOImpl(config)

        return ParseOption(
            PreParserImpl(format.parseFormat.globalCondition.build(), format.parseFormat.pages.map {
                it.buildPrePage()
            }, format.parseFormat.attributeRequester.build()),
            PostParserImpl(format.parseFormat.pages.map {
                it.buildPostPage()
            }),
            format.exportFormat.build(io),
            format.requestFormat.build(factories, io)
        )
    }
}