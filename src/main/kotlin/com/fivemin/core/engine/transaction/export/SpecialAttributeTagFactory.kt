/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core.engine.transaction.export

import arrow.core.toOption
import com.fivemin.core.engine.*
import java.net.URI
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.extension
import kotlin.io.path.name

class AttributeLocator(val attributeName: String, val attributeIndex: Int)

class SpecialAttributeTagFactory {
    interface SpecialTagFactory {
        fun <Document : Request> create(trans: SerializeTransaction<Document>, locator: AttributeLocator): Tag
    }
    
    /**
     * Creates NAME tags with attribute name.
     */
    class NameSpecialTagFactory : SpecialTagFactory {
        override fun <Document : Request> create(
            trans: SerializeTransaction<Document>,
            locator: AttributeLocator
        ): Tag {
            return Tag(EnumSet.of(TagFlag.NONE), "name", locator.attributeName)
        }
    }
    
    /**
     * Creates INC tags with parsed results' indices.
     */
    class IncSpecialTagFactory : SpecialTagFactory {
        override fun <Document : Request> create(
            trans: SerializeTransaction<Document>,
            locator: AttributeLocator
        ): Tag {
            val cnt = trans.attributes.single {
                it.info.name == locator.attributeName
            }.item.count()
            var strcount: Int = 0

            if (cnt != 0) {
                strcount = kotlin.math.log10(cnt.toDouble()).toInt() + 1
            } else {
                strcount = 1
            }

            val ccount = strcount + 1

            val ret = ("%0$ccount" + "d").format(locator.attributeIndex)
            //always creates one more zeros than number of digits. It helps windows sorts well.

            return Tag(EnumSet.of(TagFlag.NONE), "inc", ret)
        }
    }
    
    /**
     * Creates LASTSEG tags with last segments of URL.
     */
    class LastSegSpecialTagFactory : SpecialTagFactory {
        val FallBackName: String = "Data"
        override fun <Document : Request> create(
            trans: SerializeTransaction<Document>,
            locator: AttributeLocator
        ): Tag {
            return Tag(
                EnumSet.of(TagFlag.NONE), "lastseg",
                trans.attributes.single {
                    it.info.name == locator.attributeName
                }.item.elementAt(locator.attributeIndex).match(
                    {
                        getLastSeg(trans.request.target) //if attribute content is internal, use parent request's URL.
                    },
                    {
                        getLastSeg(it.target) //or uses itself.
                    }
                )
            )
        }

        private fun getLastSeg(t: URI): String {
            val ext = Paths.get(t.path).toFile().name

            if (ext.isBlank()) {
                return FallBackName
            }

            return ext
        }
    }
    
    /**
     * Creates EXT tags with URL extension.
     */
    class ExtSpecialTagFactory : SpecialTagFactory {
        override fun <Document : Request> create(
            trans: SerializeTransaction<Document>,
            locator: AttributeLocator
        ): Tag {
            return Tag(
                EnumSet.of(TagFlag.NONE), "ext",
                trans.attributes.single {
                    it.info.name == locator.attributeName
                }.item.elementAt(locator.attributeIndex).match({ "" }, {
                    getLastExtension(it.target)
                })
            )
        }

        private fun getLastExtension(t: URI): String {
            val ext = Paths.get(t.path).toFile().extension

            return ext
        }
    }

    val factories: Iterable<SpecialTagFactory> =
        listOf(ExtSpecialTagFactory(), LastSegSpecialTagFactory(), IncSpecialTagFactory(), NameSpecialTagFactory())

    fun <Document : Request> build(trans: SerializeTransaction<Document>, locator: AttributeLocator): TagRepository {
        val ret = factories.map {
            it.create(trans, locator)
        }

        return TagRepositoryImpl(ret.toOption(), trans.tags.toOption())
    }
}
