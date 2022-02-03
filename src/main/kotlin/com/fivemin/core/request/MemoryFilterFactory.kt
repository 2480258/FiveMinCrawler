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

import arrow.core.Option
import com.fivemin.core.engine.*
import java.nio.charset.Charset

interface MemoryFilterFactory {
    fun createByteFilter(expectSize: Option<Long>, handle: RequestToken): MemoryFilter
    fun createStringFilter(expectSize: Option<Long>, handle: RequestToken, enc: Option<Charset>): MemoryFilter
    fun createHtmlFilter(expectSize: Option<Long>, handle: RequestToken, enc: Option<Charset>): MemoryFilter
}

class MemoryFilterFactoryImpl(
    io: DirectIO,
    val factory: HtmlDocumentFactory
) : MemoryFilterFactory {

    val token: DirectoryIOToken

    init {
        token = io.getToken(UsingPath.TEMP)
    }

    override fun createByteFilter(expectSize: Option<Long>, handle: RequestToken): MemoryFilter {
        return TranslatableFilter(expectSize, handle, token)
    }

    override fun createStringFilter(expectSize: Option<Long>, handle: RequestToken, enc: Option<Charset>): MemoryFilter {
        return StringFilterImpl(TranslatableFilter(expectSize, handle, token), enc)
    }

    override fun createHtmlFilter(expectSize: Option<Long>, handle: RequestToken, enc: Option<Charset>): MemoryFilter {
        return HtmlFilterImpl(StringFilterImpl(TranslatableFilter(expectSize, handle, token), enc), factory)
    }
}
