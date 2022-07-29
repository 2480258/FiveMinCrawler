/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core.engine.crawlingTask

import arrow.core.toOption
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.ElemIterator
import com.fivemin.core.TaskMockFactory
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

        val state = TaskMockFactory.createSessionStarted<Request>()

        runBlocking {
            val proc = addtagPolicy.process(req, req.upgradeAsDocument("a"), TaskMockFactory.createTaskInfo(), state)

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
