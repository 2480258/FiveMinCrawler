package fivemin.core.engine.transaction.export

import arrow.core.toOption
import fivemin.core.engine.Tag
import fivemin.core.engine.TagFlag
import fivemin.core.engine.TagRepositoryImpl
import org.testng.annotations.Test

import org.testng.Assert.*
import java.util.*

class TagExpressionTest {
    
    @Test
    fun testBuild() {
        val tagRepo = TagRepositoryImpl(listOf(Tag(EnumSet.of(TagFlag.CONVERT_TO_ATTRIBUTE), "name", "value")).toOption())
        
        val tagExp = TagExpression("111&(name)222")
        
        assertEquals(tagExp.build(tagRepo), "111value222")
    }
}