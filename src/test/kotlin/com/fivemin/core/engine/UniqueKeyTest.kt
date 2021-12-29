package com.fivemin.core.engine

import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.session.UriUniqueKey
import com.fivemin.core.engine.transaction.StringUniqueKey
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class UniqueKeyTest {
    
    var uriIt = ElemIterator(UriIterator())
    
    @BeforeMethod
    fun before() {
        uriIt = ElemIterator(UriIterator())
    }
    
    @Test
    fun testUriStringKey() {
        val uri = StringUniqueKey(uriIt.gen().toString())
        val uri2 = StringUniqueKey(uriIt[0].toString())
        
        assert(uri == uri2)
    }
    
    
    @Test
    fun testUriUniqueKey() {
        val uri = UriUniqueKey(uriIt.gen())
        val uri2 = UriUniqueKey(uriIt[0]!!)
        
        assert(uri == uri2)
    }
}