package com.fivemin.core.initialize

import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.session.ArchivedSession
import com.fivemin.core.engine.session.ArchivedSessionSet
import com.fivemin.core.engine.session.UriUniqueKey
import com.fivemin.core.engine.transaction.StringUniqueKey
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ResumeDataFactoryTest {
    var uriIt = ElemIterator(UriIterator())
    
    @BeforeMethod
    fun before() {
        uriIt = ElemIterator(UriIterator())
    }
    
    @Test
    fun testArchivedSessionGet() {
        var opt = ArchivedSession(
            listOf(
                StringUniqueKey(uriIt.gen().toString())
            )
        )
        var ret = ProtoBuf.encodeToByteArray(opt)
        var res = ProtoBuf.decodeFromByteArray<ArchivedSession>(ret)
    
        assert(res.isConflict(StringUniqueKey(uriIt[0]!!.toString())))
    }
    
    
    @Test
    fun testGet() {
        val opt = ResumeOption(
            ArchivedSessionSet(
                listOf(
                    ArchivedSession(
                        listOf(
                            StringUniqueKey(uriIt.gen().toString())
                        )
                    )
                )
            )
        )
        
        
        var df = ResumeDataFactory()
        
        var ret = df.save(opt)
        var res = df.get(ret)
        
        
        res.fold({
            fail()
                 },
            {
            assert(it.archivedSessionSet.isConflict(StringUniqueKey(uriIt[0].toString())))
        })
    }
}