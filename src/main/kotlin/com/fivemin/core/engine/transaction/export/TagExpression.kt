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

import com.fivemin.core.engine.TagRepository

/**
 * Express string with some arguments.
 */
class TagExpression(private val originalString: String) {
    private val regexTag: String = "&\\(([0-z]*)\\)" //$(name)
    private val regex: Regex = Regex(regexTag)

    fun build(tagRepo: TagRepository): String {
        return parse(originalString, tagRepo)
    }

    private fun parse(macroStr: String, tagRepo: TagRepository): String {
        val matches = regex.findAll(macroStr).toList()

        val macroMap = mutableMapOf<String, String>()

        var str: String = macroStr

        matches.forEach {
            if (it.value.length > 3) {
                val qry = it.value.subSequence(2, it.value.length - 1)

                if (tagRepo.contains(qry.toString()) && !qry.contains('\\')) { //if $[\path_to_somewhere]. ignore it.
                    macroMap[qry.toString()] = tagRepo[qry.toString()].value
                }
            }
        }

        macroMap.forEach {
            str = str.replace("&(" + it.key + ")", tagRepo[it.key].value)
        }

        return str
    }
}
