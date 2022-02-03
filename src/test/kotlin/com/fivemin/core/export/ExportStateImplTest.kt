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

package com.fivemin.core.export

import arrow.core.none
import com.fivemin.core.ElemIterator
import com.fivemin.core.StringIterator
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.export.ExportHandleImpl
import io.mockk.every
import io.mockk.mockk
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class ExportStateImplTest {
    
    var strIt = ElemIterator(StringIterator())
    
    @BeforeMethod
    fun before() {
        strIt = ElemIterator(StringIterator())
    }
    
    fun mockExportData() : ExportData {
        return mockk<ExportData>()
    }
    
    fun mockFileIOToken(name : String) : FileIOToken {
        var token = mockk<FileIOToken>()
        
        every {
            token.fileName
        } returns(FileName(name))
        
        every {
            token.addSuffix(any())
        } answers {
            mockFileIOToken(name + " - (Dup)")
        }
        
        return token
    }
    
    @Test
    fun testCreate() {
        var directIO = mockk<DirectIO>()
        
        every {
            directIO.getToken(any())
        } returns(DirectoryIOToken("C:\\"))
        
        var state = ExportStateImpl(directIO, none())
        var exportHandle = ExportHandleImpl(ExportInfoImpl(mockFileIOToken(strIt.gen())), mockExportData())
        
        var result = state.create(exportHandle)
        
        
        assertEquals(result.info.token.fileName.name.name, strIt[0])
    }
    
    @Test
    fun testDuplicatedCreate() {
        var directIO = mockk<DirectIO>()
    
        every {
            directIO.getToken(any())
        } returns(DirectoryIOToken("C:\\"))
    
        var state = ExportStateImpl(directIO, none())
    
        var exportHandleFirst = ExportHandleImpl(ExportInfoImpl(mockFileIOToken(strIt.gen())), mockExportData())
        var exportHandleSecond = ExportHandleImpl(ExportInfoImpl(mockFileIOToken(strIt[0]!!)), mockExportData())
    
        var resultFirst = state.create(exportHandleFirst)
        var resultSecond = state.create(exportHandleSecond)
        
        
        assertEquals(resultFirst.info.token.fileName.name.name, strIt[0])
        assertEquals(resultSecond.info.token.fileName.name.name, strIt[0] + " - (Dup)")
    }
}