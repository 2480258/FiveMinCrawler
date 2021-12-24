package fivemin.core.engine.transaction.export

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.right
import arrow.core.toOption
import arrow.core.valid
import fivemin.core.engine.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import java.util.*

class JsonExportAdapter(private val fileNameExp: TagExpression, private val exportHandleFactory: ExportHandleFactory) :
    ExportAdapter {
    override fun parse(
        request: Request,
        info: Iterable<ExportAttributeInfo>
    ): Iterable<Either<Throwable, ExportHandle>> {
        
        var ret = info.map { x ->
            x.element.match({ y ->
                Triple(x.tagRepo, x.info, y.body)
            }, { null })
        }
        
        var tags = ret.filter {
            it != null && it.second.isList
        }.map { x ->
            Tag(EnumSet.of(TagFlag.NONE), x!!.second.info.name, x.third)
        }
        
        var tagrepo = ret.map {
            Triple(fileNameExp.build(TagRepositoryImpl(tags.toOption(), it!!.first.toOption())), it.second, it.third)
        }.groupBy {
            it.first
        }
        
        return tagrepo.map {
            Either.catch {
                exportHandleFactory.create(it.key, convertToJson(it.value.map {
                    Pair(it.second, it.third)
                }))
            }
        }
    }
    
    @Serializable
    data class JsonExportObject(val result: Map<String, JsonElement>)
    
    private fun convertToJson(data: Iterable<Pair<ExportAttributeLocator, String>>): String {
        val json = Json {}
        
        
        var single = data.filter {
            !it.first.isList
        }.associate {
            Pair(it.first.info.name, it.second)
        }
        
        
        var multi = data.filter {
            it.first.isList
        }.groupBy {
            it.first.info.name
        }.asIterable().associate {
            Pair(it.key, it.value.sortedBy {
                it.first.index.fold({ 0 }, { x -> x })
            }.map {
                it.second
            }.toList())
        }
        
        var res = single.plus(multi)
        
        var map = res.map {
            Pair(it.key, json.encodeToJsonElement(serializer(it.value::class.javaObjectType), it.value))
        }.asIterable().toMap()
        
        var ret = JsonExportObject(JsonObject(map))
        
        return Json.encodeToString(ret)
    }
}