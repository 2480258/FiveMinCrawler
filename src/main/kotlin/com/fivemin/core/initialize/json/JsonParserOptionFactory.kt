package com.fivemin.core.initialize.json

import com.fivemin.core.engine.DirectIO
import com.fivemin.core.engine.transaction.prepareRequest.preParser.PreParserImpl
import com.fivemin.core.engine.transaction.serialize.postParser.PostParserImpl
import com.fivemin.core.initialize.ParseOption
import com.fivemin.core.initialize.RequesterFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
data class JsonOptionFormat(
    val requestFormat: JsonRequesterCompFormat,
    val parseFormat: JsonPrePostParserFormat,
    val exportFormat: JsonExportParserFormat
)

class JsonParserOptionFactory(
    val jsonString: String,
    val factory: List<RequesterFactory>,
    val io: DirectIO
) {
    val format: JsonOptionFormat
    val option: ParseOption
    init {
        format = Json.decodeFromString<JsonOptionFormat>(jsonString)
        option = getOption(factory)
    }

    private fun getOption(factories: Iterable<RequesterFactory>): ParseOption {
        return ParseOption(
            PreParserImpl(
                format.parseFormat.globalCondition.build(),
                format.parseFormat.pages.map {
                    it.buildPrePage()
                },
                format.parseFormat.attributeRequester.build()
            ),
            PostParserImpl(
                format.parseFormat.pages.map {
                    it.buildPostPage()
                }
            ),
            format.exportFormat.build(io),
            format.requestFormat.build(factories, io)
        )
    }
}
