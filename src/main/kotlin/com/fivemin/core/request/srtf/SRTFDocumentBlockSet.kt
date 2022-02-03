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

package com.fivemin.core.request.srtf

import arrow.core.Option
import com.fivemin.core.engine.RequestToken

class SRTFDocumentBlockSet {
    val blocks: MutableMap<RequestToken, SRTFDocumentBlock> = mutableMapOf()

    val count: Int
        get() {
            return _count
        }

    var _count = 0

    fun getBlockBy(token: RequestToken): SRTFDocumentBlock {
        return blocks.asIterable().single {
            it.value.token == token
        }.value
    }

    fun tryAddBlock(token: RequestToken, parent: Option<RequestToken>, name: SRTFPageBlock): Boolean {
        val wsHandle = parent.fold({ token }, {
            getBlockBy(it).bottomMost
        })

        var block = SRTFDocumentBlock(token, wsHandle, name)

        if (blocks.containsKey(token)) {
            return false
        }

        blocks[token] = block

        return true
    }

    fun removeIfExistByWorkingSetHandle(token: RequestToken) {
        if (!blocks.containsKey(token)) {
            return
        }

        var lst = blocks.filter {
            it.value.bottomMost == token
        }.toList()

        lst.forEach {
            blocks.remove(it.first)
        }
    }
}
