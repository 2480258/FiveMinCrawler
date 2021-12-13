package fivemin.core.engine.transaction

import arrow.core.Option
import arrow.core.filterOption
import arrow.core.toOption
import fivemin.core.engine.*
import java.net.URI
import java.util.*

class TagBuilder <in Trans : Transaction<Document>, out Document : Request>
    (private val selector : Option<Iterable<TagSelector>>) {
    fun build(init : InitialTransaction<Request>): TagRepository{
        var ret = selector.fold({listOf()}, {
            it.map{
                it.build(init.request.target)
            }
        }).filterOption()

        return TagRepositoryImpl(ret.toOption(), init.tags.toOption())
    }
}
data class TagSelector(val name : String, val regex : Regex, val flag : EnumSet<TagFlag>){
    @OptIn(ExperimentalStdlibApi::class)
    fun build(uri : URI) : Option<Tag> {

        return regex.find(uri.toString()).toOption().map{
            Tag(flag, name, it.value)
        }
    }
}