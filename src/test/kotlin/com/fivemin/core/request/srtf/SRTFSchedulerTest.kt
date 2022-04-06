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

import com.fivemin.core.DocumentMockFactory.Companion.getRequest
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsAttribute
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import com.fivemin.core.ElemIterator
import com.fivemin.core.StringIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
