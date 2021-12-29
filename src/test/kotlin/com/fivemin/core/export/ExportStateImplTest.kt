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