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
import java.net.URI

class AutomaticRedirectResponseBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader,
    override val afterRedirect: ResponseBody
) : HttpAutomaticRedirectResponseBody

class RedirectResponseBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader,
    override val redirectDest: URI
) : RedirectResponseBody

class RecoverableErrorBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader
) : RecoverableErrorBody

class CriticalErrorBodyImpl(override val requestBody: RequestBody, override val error: Option<Throwable>) :
    CriticalErrorBody

class SuccessBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader,
    override val body: MemoryData,
    override val contentType: MediaType,
    override val responseTime: ResponseTime
) : HttpSuccessBody

