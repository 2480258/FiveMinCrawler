package fivemin.core.engine

import arrow.core.toOption
import fivemin.core.ElemIterator
import fivemin.core.StringIterator
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import java.util.*

class TagRepositoryImplTest {

    var it = ElemIterator(StringIterator())

    private fun getConnected(name: String, newval: String): TagRepositoryImpl {
        val oldval = it.gen()
        val tag = Tag(EnumSet.noneOf(TagFlag::class.java), name, oldval)
        val newtag = Tag(EnumSet.noneOf(TagFlag::class.java), name, newval)

        val oldrepo = TagRepositoryImpl(listOf(tag).toOption())
        return TagRepositoryImpl(listOf(newtag).toOption(), oldrepo.toOption())
    }

    @BeforeMethod
    fun before() {
        it = ElemIterator(StringIterator())
    }

    @Test
    fun testKeyNotFound() {
        var repo = TagRepositoryImpl()

        assertThrows {
            repo.get(it.gen())
        }
    }

    @Test
    fun testConnectedGetEnumerableTest() {
        val name = it.gen()
        val newval = it.gen()

        val newrepo = getConnected(name, newval)

        assertEquals(newrepo.count {
            it.name == name
        }, 1)
    }

    @Test
    fun testConnectedGetEnumerableTest_NoOverlap() {
        val name = it.gen()
        val oldval = it.gen()
        val newval = it.gen()

        val tag = Tag(EnumSet.noneOf(TagFlag::class.java), name, oldval)
        val newtag = Tag(EnumSet.noneOf(TagFlag::class.java), it.gen(), newval)

        val oldrepo = TagRepositoryImpl(listOf(tag).toOption())
        val newrepo = TagRepositoryImpl(listOf(newtag).toOption(), oldrepo.toOption())

        assertEquals(1, newrepo.count {
            it.name == name
        })
    }

    @Test
    fun testConnectedContainsKey() {
        val name = it.gen()
        val newval = it.gen()

        val newrepo = getConnected(name, newval)

        assert(newrepo.contains(name))
    }

    @Test
    fun testConnectedContainsKey_NoOverlap() {
        val oldtag = Tag(EnumSet.noneOf(TagFlag::class.java), it.gen(), it.gen())
        val newtag = Tag(EnumSet.noneOf(TagFlag::class.java), it.gen(), it.gen())

        val oldrepo = TagRepositoryImpl(listOf(oldtag).toOption())
        val newrepo = TagRepositoryImpl(listOf(newtag).toOption(), oldrepo.toOption())

        assert(newrepo.contains(oldtag.name))
    }

    @Test
    fun testConectedindexerTest_NoOverlap() {
        val oldtag = Tag(EnumSet.noneOf(TagFlag::class.java), it.gen(), it.gen())
        val newtag = Tag(EnumSet.noneOf(TagFlag::class.java), it.gen(), it.gen())

        val oldrepo = TagRepositoryImpl(listOf(oldtag).toOption())
        val newrepo = TagRepositoryImpl(listOf(newtag).toOption(), oldrepo.toOption())

        assertEquals(oldtag.value, newrepo.get(oldtag.name).value)
    }

    @Test
    fun testConnectedIndexerTest_Overlap() {
        val name = it.gen()
        val newval = it.gen()

        val newrepo = getConnected(name, newval)

        assertEquals(newval, newrepo.get(name).value)
    }

    @Test
    fun indexerTest() {
        val tag = Tag(EnumSet.noneOf(TagFlag::class.java), it.gen(), it.gen())
        val repo = TagRepositoryImpl(listOf(tag).toOption())

        assertEquals(repo.get(it.get(0)!!).value, it[1])
    }

    @Test
    fun connectTest() {
        val oldtag = Tag(EnumSet.of(TagFlag.UNIQUE), it.gen(), it.gen())
        val newtag = Tag(EnumSet.of(TagFlag.UNIQUE), it.gen(), it.gen())

        var originalTag = TagRepositoryImpl(listOf(oldtag).toOption())
        assertThrows {
            TagRepositoryImpl(listOf(newtag).toOption(), originalTag.toOption())
        }
    }

    @Test
    fun uniqueDuplicateTest() {
        assertThrows {
            TagRepositoryImpl(listOf(Tag(EnumSet.of(TagFlag.UNIQUE), it.gen(), it.gen()), Tag(EnumSet.of(TagFlag.UNIQUE), it.gen(), it.gen())).toOption())
        }
    }

    @Test
    fun duplicateTest() {
        var str = it.gen()
        assertThrows {
            TagRepositoryImpl(listOf(Tag(EnumSet.of(TagFlag.UNIQUE), str, str), Tag(EnumSet.of(TagFlag.UNIQUE), str, str)).toOption())
        }
    }
}