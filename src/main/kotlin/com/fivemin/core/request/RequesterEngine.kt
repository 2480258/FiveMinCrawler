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

package com.fivemin.core.request

import com.fivemin.core.engine.RequesterSlotInfo
import com.fivemin.core.engine.ResponseData

interface RequesterEngine<out Resp : ResponseData> {
    val info: RequesterEngineConfig
    val count: Int

    fun get(info: RequesterSlotInfo): RequesterCore<Resp>
}

class RequesterEngineImpl<out Resp : ResponseData>(
    override val info: RequesterEngineConfig,
    private val requesterDic: Map<RequesterSlotInfo, RequesterCore<Resp>>
) :
    RequesterEngine<Resp> {

    override val count: Int
        get() {
            return requesterDic.size
        }

    override fun get(info: RequesterSlotInfo): RequesterCore<Resp> {
        return requesterDic[info]!!
    }
}

class RequesterEngineConfig
