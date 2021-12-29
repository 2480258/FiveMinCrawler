package com.fivemin.core.engine.transaction.export

import com.fivemin.core.AttributeMockFactory
import com.fivemin.core.AttributeMockFactory.Companion.asMultiUpgrade
import com.fivemin.core.AttributeMockFactory.Companion.asSingleUpgrade
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import io.mockk.every
import io.mockk.mockk
import org.testng.annotations.Test

import org.testng.Assert.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class JsonExportAdapterTest {
    
    val uriIt = ElemIterator(UriIterator())
    
    fun mockDirectIO(): DirectIO {
        val directIO: DirectIO = mockk()
        
        every {
            directIO.getToken(any())
        } returns (DirectoryIOToken(System.getProperty("user.dir") + "\\TrashBin"))
        
        return directIO
    }
    
    
    @Test
    fun testParse() {
        val tagExp = TagExpression("111")
        
        val adapter = JsonExportAdapter(tagExp, ExportHandleFactoryImpl(mockDirectIO(), "JsonExportAdapterTest"))
        
        val single = AttributeMockFactory.getSingleStringAttr("test1", "value")
        val multi = AttributeMockFactory.getMultiSingleAttr("test2", listOf("value1", "value2"))
        
        var ext = adapter.parse(
            DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK),
            multi.asMultiUpgrade(TagRepositoryImpl()).plus(single.asSingleUpgrade(TagRepositoryImpl()))
        )
        
        ext.map {
            it.fold({
                fail()
            }) {
                it.data.save(it.request.token)
            }
        }
        
        val path = Paths.get(System.getProperty("user.dir"), "TrashBin", "111")
        assertEquals(Files.readString(path), "{\"test1\":\"value\",\"test2\":[\"value1\",\"value2\"]}")
        
        File(System.getProperty("user.dir") + "\\TrashBin\\111").delete()
    }
}