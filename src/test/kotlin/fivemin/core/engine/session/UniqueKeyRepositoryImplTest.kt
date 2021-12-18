package fivemin.core.engine.session

import arrow.core.none
import arrow.core.toOption
import fivemin.core.ElemIterator
import fivemin.core.IteratorElemFactory
import fivemin.core.StringIterator
import fivemin.core.UriIterator
import fivemin.core.engine.SessionToken
import fivemin.core.engine.UniqueKey
import fivemin.core.engine.transaction.StringUniqueKey
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class UniqueKeyIterator : IteratorElemFactory<UniqueKey> {
    val it = StringIterator()

    override fun getNext(): UniqueKey {
        return StringUniqueKey(it.getNext())
    }
}

class UniqueKeyRepositoryImplTest {

    lateinit var r: UniqueKeyRepositoryImpl
    var it = ElemIterator(UniqueKeyIterator())

    @BeforeMethod
    fun before() {
        it = ElemIterator(UniqueKeyIterator())
        r = UniqueKeyRepositoryImpl(none())
    }

    @Test
    fun testPreset() {
        var arch = ArchivedSessionSet(listOf(ArchivedSession(listOf(it.gen()))))
        r = UniqueKeyRepositoryImpl(arch.toOption())

        assertThrows {
            r.addAlias(SessionToken.create(), it[0]!!)
        }
    }

    @Test
    fun testAddAlias() {
        var fi = r.addAlias(SessionToken.create(), it.gen())
        assertThrows {
            var re = r.addAlias(SessionToken.create(), it[0]!!)
        }
    }


    @Test
    fun testMaxSelfAlias() {
        var src = SessionToken.create()

        r.addAlias(src, it.gen())
        r.addAlias(src, it[0]!!)
        r.addAlias(src, it[0]!!)

        assertThrows {
            r.addAlias(src, it[0]!!)
        }
    }


    @Test
    fun testTransferOwnership() {
        var src = SessionToken.create()
        var dest = SessionToken.create()

        r.addAlias(src, it.gen())
        r.transferOwnership(src, dest)
        r.addAlias(dest, it[0]!!)
    }
}