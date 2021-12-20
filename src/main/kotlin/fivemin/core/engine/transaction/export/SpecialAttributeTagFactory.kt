package fivemin.core.engine.transaction.export

import arrow.core.toOption
import fivemin.core.engine.*
import java.net.URI
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.extension
import kotlin.io.path.name

class AttributeLocator (val attributeName : String, val attributeIndex : Int){

}

class SpecialAttributeTagFactory {
    interface SpecialTagFactory{
        fun <Document : Request> create(trans : SerializeTransaction<Document>, locator : AttributeLocator) : Tag
    }

    class NameSpecialTagFactory : SpecialTagFactory{
        override fun <Document : Request> create(trans: SerializeTransaction<Document>, locator : AttributeLocator): Tag {
            return Tag(EnumSet.of(TagFlag.NONE), "name", locator.attributeName)
        }
    }

    class IncSpecialTagFactory : SpecialTagFactory{
        override fun <Document : Request> create(
            trans: SerializeTransaction<Document>,
            locator: AttributeLocator
        ): Tag {
            val cnt = trans.attributes.single {
                it.info.name == locator.attributeName
            }.item.count()
            var strcount : Int = 0

            if(cnt != 0){
                strcount = kotlin.math.log10(cnt.toDouble()).toInt() + 1
            }
            else{
                strcount = 1
            }

            var ccount = strcount + 1

            var ret = ("%0$ccount" + "d").format(locator.attributeIndex)

            return Tag(EnumSet.of(TagFlag.NONE), "inc", ret)
        }
    }

    class LastSegSpecialTagFactory : SpecialTagFactory{
        val FallBackName : String = "Data"
        override fun <Document : Request> create(
            trans: SerializeTransaction<Document>,
            locator: AttributeLocator
        ): Tag {
            return Tag(EnumSet.of(TagFlag.NONE), "lastseg", trans.attributes.single {
                it.info.name == locator.attributeName
            }.item.elementAt(locator.attributeIndex).match({""}, {
                getLastSeg(it.target)
            }))
        }

        private fun getLastSeg(t : URI) : String{
            val ext = Paths.get(t).name

            if(ext.isBlank()){
                return FallBackName
            }

            return ext
        }

    }

    class ExtSpecialTagFactory : SpecialTagFactory{
        override fun <Document : Request> create(
            trans: SerializeTransaction<Document>,
            locator: AttributeLocator
        ): Tag {
            return Tag(EnumSet.of(TagFlag.NONE), "ext", trans.attributes.single {
                it.info.name == locator.attributeName
            }.item.elementAt(locator.attributeIndex).match({""},{
                getLastExtension(it.target)
            }))
        }

        private fun getLastExtension(t : URI) : String{
            val ext = Paths.get(t).extension

            return ext
        }
    }

    val factories : Iterable<SpecialTagFactory> = listOf(ExtSpecialTagFactory(), LastSegSpecialTagFactory(), IncSpecialTagFactory(), NameSpecialTagFactory())


    fun <Document : Request> build(trans : SerializeTransaction<Document>, locator: AttributeLocator) : TagRepository{
        var ret = factories.map {
            it.create(trans, locator)
        }

        return TagRepositoryImpl(ret.toOption(), trans.tags.toOption())
    }
}


