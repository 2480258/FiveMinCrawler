package com.fivemin.core.engine.transaction.export

import arrow.core.*
import arrow.core.filterOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import javax.swing.text.Document

interface ExportPage {
    val pageName: String
    fun <Document : Request> isAcceptable(trans: SerializeTransaction<Document>): Boolean
    fun <Document : Request> export(trans: SerializeTransaction<Document>): Iterable<ExportHandle>
}

class ExportPageImpl(override val pageName: String, private val targetAttributeName: Iterable<String>, private val adapter: ExportAdapter) : ExportPage {

    companion object {
        private val logger = LoggerController.getLogger("ExportPageImpl")
    }

    private val specialAttributeTagFactory: SpecialAttributeTagFactory = SpecialAttributeTagFactory()
    override fun <Document : Request> isAcceptable(trans: SerializeTransaction<Document>): Boolean {
        return trans.serializeOption.parseOption.name.name == pageName
    }

    override fun <Document : Request> export(trans: SerializeTransaction<Document>): Iterable<ExportHandle> {
        return adapter.parse(trans.request, parseInfo(trans)).map {
            it.swap().map {
                logger.warn(trans.request, "is not exported due to: ", it.toOption())
            }

            it.orNull().toOption()
        }.filterOption()
    }

    private fun <Document : Request> parseInfo(trans: SerializeTransaction<Document>): Iterable<ExportAttributeInfo> {
        var ret = tagSelect(trans).map {
            Pair(it.info, buildTagAll(trans, it.info.name))
        }

        return ret.flatMap { x ->
            (0 until x.second.count()).map { y ->
                ExportAttributeInfo(ExportAttributeLocator(x.first, y.toOption()), x.second.entries.elementAt(y).key, x.second.entries.elementAt(y).value)
            }
        }
    }

    private fun <Document : Request> tagSelect(trans: SerializeTransaction<Document>): Iterable<DocumentAttribute> {
        return trans.attributes.filter {
            targetAttributeName.contains(it.info.name)
        }
    }

    private fun <Document : Request> buildTagAll(trans: SerializeTransaction<Document>, targetAttributeName: String): Map<DocumentAttributeElement, TagRepository> {
        var ret = trans.attributes.single {
            it.info.name == targetAttributeName
        }

        return (0 until ret.item.count()).associate {
            Pair(ret.item.elementAt(it), buildSpecialTags(trans, targetAttributeName, it))
        }
    }

    private fun <Document : Request> buildSpecialTags(trans: SerializeTransaction<Document>, targetAttributeName: String, targetAttrIdx: Int): TagRepository {
        return specialAttributeTagFactory.build(trans, AttributeLocator(targetAttributeName, targetAttrIdx))
    }
}
