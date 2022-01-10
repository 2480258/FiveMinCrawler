package com.fivemin.core.engine.crawlingTask

import arrow.core.toOption
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.ElemIterator
import com.fivemin.core.StubMockFactory.Companion.mockInfo
import com.fivemin.core.StubMockFactory.Companion.mockState
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.Test
import java.util.*

class AddTagAliasSubPolicyTest {

    var addtagPolicy = AddTagAliasSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request>()
    var uriIt = ElemIterator(UriIterator())

    private fun listTag(): Iterable<Tag> {
        val aliasTag = Tag(EnumSet.of(TagFlag.ALIAS), "a", "b")
        val notaliasTag = Tag(EnumSet.of(TagFlag.CONVERT_TO_ATTRIBUTE), "c", "d")

        return listOf(aliasTag, notaliasTag)
    }

    @Test
    fun testProcess() {
        val req =
            DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK, null, TagRepositoryImpl(listTag().toOption()))
                .upgrade()

        val state = mockState()

        runBlocking {
            val proc = addtagPolicy.process(req, req.upgradeAsDocument("a"), mockInfo(), state)

            coVerify(exactly = 1) {
                state.addAlias(any())
            }

            proc.await().swap().map {
                it.printStackTrace()
                fail()
            }
        }
    }
}
