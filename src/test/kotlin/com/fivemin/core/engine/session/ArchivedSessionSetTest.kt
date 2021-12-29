package com.fivemin.core.engine.session

import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.transaction.StringUniqueKey
import org.testng.annotations.Test

import org.testng.Assert.*

class ArchivedSessionSetTest {
    
    val uriIt = ElemIterator(UriIterator())
    
    @Test
    fun testIsConflict() {
        var opt = ArchivedSessionSet(
            listOf(
                ArchivedSession(
                    listOf(
                        StringUniqueKey(uriIt.gen().toString())
                    )
                )
            )
        )
        
        assert(opt.isConflict(StringUniqueKey(uriIt[0].toString())))
    }
}