package fivemin.core.engine

import fivemin.core.ElemIterator
import fivemin.core.UriIterator
import fivemin.core.engine.session.UriUniqueKey
import fivemin.core.engine.transaction.StringUniqueKey
import org.testng.annotations.Test

import org.testng.Assert.*

class UniqueKeyTest {
    
    val uriIt = ElemIterator(UriIterator())
    
    @Test
    fun testUriStringKey() {
        val uri = StringUniqueKey(uriIt.gen().toString())
        val uri2 = StringUniqueKey(uriIt[0].toString()!!)
        
        assert(uri == uri2)
    }
    
    
    @Test
    fun testUriUniqueKey() {
        val uri = UriUniqueKey(uriIt.gen())
        val uri2 = UriUniqueKey(uriIt[0]!!)
        
        assert(uri == uri2)
    }
}