package core.engine.transaction.export

import arrow.core.Validated
import arrow.core.toOption
import arrow.core.valid
import core.engine.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class JsonExportAdapter(private val fileNameExp : TagExpression, private val exportHandleFactory: ExportHandleFactory) : ExportAdapter {
    override fun parse(request: Request, info: Iterable<ExportAttributeInfo>): Iterable<Validated<Throwable, ExportHandle>> {
        var ret = info.map{ x ->
            x.element.match({ y ->
                Triple(x.tagRepo, x.info, y.body)
            }, { null })
        }

        var tags = ret.filter {
            it != null && it.second.isList
        }.map{ x ->
            Tag(EnumSet.of(TagFlag.NONE), x!!.second.info.name, x.third)
        }

        var tagrepo = ret.map{
            Triple(fileNameExp.build(TagRepositoryImpl(tags.toOption(), it!!.first.toOption())), it.second, it.third)
        }.groupBy {
            it.first
        }

        var result = tagrepo.map{
            Pair(it.key, convertToJson(it.value.map{
                Pair(it.second, it.third)
            }))
        }

        return result.map{
            exportHandleFactory.create(it.first, it.second).valid() //TODO Fix
        }
    }

    @Serializable
    data class JsonExportObject(val single : Map<String, String>, val multi : Map<String, Iterable<String>>)

    private fun convertToJson(data : Iterable<Pair<ExportAttributeLocator, String>>) : String{
        var single = data.filter {
            !it.first.isList
        }.associate {
            Pair(it.first.info.name, it.second)
        }



        var multi = data.filter {
            it.first.isList
        }.groupBy {
            it.first.info.name
        }.asIterable().associate{
            Pair(it.key, it.value.sortedBy {
                it.first.index.fold({0}, {x -> x})
            }.map{
                it.second
            }.toList())
        }

        var ret = JsonExportObject(single, multi)

        return Json.encodeToString(ret)
    }
}