package fivemin.core.initialize.json

import fivemin.core.engine.DirectIO
import fivemin.core.engine.transaction.export.*

@kotlinx.serialization.Serializable
data class JsonExportParserFormat (
    val bookName : String,
    val pages : List<JsonExportPageFormat>
        ){
    fun build(io : DirectIO) : ExportParser{
        return ExportParserImpl(pages.map {
            it.build(io, bookName)
        })
    }
}

@kotlinx.serialization.Serializable
data class JsonExportPageFormat (
    val pageName : String,
    val targetAttributeName : List<String>,
    val adapter : JsonExportAdapterFormat
        ){
    fun build(io : DirectIO, bookName : String) : ExportPage {
        return ExportPageImpl(pageName, targetAttributeName, adapter.build(io, bookName))
    }
}

@kotlinx.serialization.Serializable
data class JsonExportAdapterFormat(
    val mode : String,
    val fileNameTagExp : String
) {

    val JSON_ADAPTER = "Json"
    val BIN_ADAPTER = "Binary"

    fun build(io : DirectIO, bookName: String) : ExportAdapter{
        var factory = ExportHandleFactoryImpl(io, bookName)

        if(mode == JSON_ADAPTER){
            return JsonExportAdapter(TagExpression(fileNameTagExp), factory)
        }
        if(mode == BIN_ADAPTER){
            return BinaryExportAdapter(TagExpression(fileNameTagExp), factory)
        }
        else{
            throw IllegalArgumentException()
        }
    }
}