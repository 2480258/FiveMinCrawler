package fivemin.core.initialize.json

import fivemin.core.engine.ConfigController
import fivemin.core.engine.DirectIOImpl
import fivemin.core.engine.transaction.prepareRequest.preParser.PreParserImpl
import fivemin.core.engine.transaction.serialize.postParser.PostParserImpl
import fivemin.core.engine.transaction.serialize.postParser.RequestFactory
import fivemin.core.export.ConfigControllerImpl
import fivemin.core.initialize.ParseOption
import fivemin.core.initialize.RequesterFactory
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