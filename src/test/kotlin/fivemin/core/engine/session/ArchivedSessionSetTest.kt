package fivemin.core.engine.session

import fivemin.core.ElemIterator
import fivemin.core.UriIterator
import fivemin.core.engine.transaction.StringUniqueKey
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