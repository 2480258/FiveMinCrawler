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

import arrow.core.Either
import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.ResponseBody
import com.fivemin.core.request.cookie.CookieResolveTarget
import com.fivemin.core.request.cookie.CookiedRequester
import kotlinx.coroutines.Deferred

class HttpRequestProcedure(
    info: PerformedRequesterInfo,
    config: RequesterConfig,
    private val adapter: RequesterAdapter
) :
    CookiedRequester {
    private val target: CookieResolveTarget
    
    init {
        target = config.targetFactory.create(info, adapter.cookieRepository)
    }
    
    suspend fun request(request: Request): Deferred<Either<Throwable, ResponseBody>> {
        return target.sync {
            adapter.requestAsync(request)
        }
    }
}
