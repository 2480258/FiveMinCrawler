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

package com.fivemin.core.initialize

import com.fivemin.core.engine.DirectIO
import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.ResponseData
import com.fivemin.core.request.RequesterCore
import com.fivemin.core.request.cookie.CookieResolveTargetFactory

interface RequesterFactory {
    val key: String
    fun build(info: PerformedRequesterInfo, io: DirectIO, factory: CookieResolveTargetFactory): RequesterCore<ResponseData>
}
