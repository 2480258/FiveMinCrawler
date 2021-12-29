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