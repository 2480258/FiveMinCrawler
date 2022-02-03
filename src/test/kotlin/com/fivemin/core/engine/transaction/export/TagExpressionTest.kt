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
import com.fivemin.core.engine.Tag
import com.fivemin.core.engine.TagFlag
import com.fivemin.core.engine.TagRepositoryImpl
import org.testng.annotations.Test

import org.testng.Assert.*
import java.util.*

class TagExpressionTest {
    
    @Test
    fun testBuild() {
        val tagRepo = TagRepositoryImpl(listOf(Tag(EnumSet.of(TagFlag.NONE), "name", "value"), Tag(EnumSet.of(TagFlag.NONE), "select", "sss")).toOption())
        
        val tagExp = TagExpression("111&(name)2&(select)22")
        
        assertEquals(tagExp.build(tagRepo), "111value2sss22")
    }
}