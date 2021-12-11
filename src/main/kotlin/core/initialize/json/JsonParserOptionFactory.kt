package core.initialize.json

import core.engine.ConfigController
import core.engine.DirectIOImpl
import core.engine.transaction.prepareRequest.preParser.PreParserImpl
import core.engine.transaction.serialize.postParser.RequestFactory
import core.export.ConfigControllerImpl
import core.initialize.ParseOption
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
    val factory : List<RequestFactory>
) {
    val format : JsonOptionFormat
    val option : ParseOption
    init {
        format = Json.decodeFromString<JsonOptionFormat>(jsonString)
        option =
    }

    private fun getOption(factories : Iterable<RequestFactory>) : ParseOption {
        val config = ConfigControllerImpl()
        val io = DirectIOImpl(config)

        return ParseOption(
            PreParserImpl()
        )
    }
}