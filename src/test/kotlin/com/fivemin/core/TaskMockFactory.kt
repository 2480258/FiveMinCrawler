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

package com.fivemin.core

import arrow.core.Either
import arrow.core.none
import arrow.core.right
import arrow.core.valid
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.engine.*
import com.fivemin.core.engine.crawlingTask.DocumentPolicyStorageFactory
import com.fivemin.core.engine.crawlingTask.DocumentPolicyStorageFactoryCollector
import com.fivemin.core.engine.session.BloomFilterFactory
import com.fivemin.core.engine.session.BloomFilterUniqueKeyRepository
import com.fivemin.core.engine.session.bFilter.BloomFilterImpl
import com.fivemin.core.engine.transaction.AbstractPolicyOption
import com.fivemin.core.engine.transaction.StringUniqueKeyProvider
import com.fivemin.core.engine.transaction.UriUniqueKeyProvider
import com.fivemin.core.engine.transaction.export.ExportTransactionPolicy
import com.fivemin.core.engine.transaction.finalizeRequest.FinalizeRequestTransactionPolicy
import com.fivemin.core.engine.transaction.finalizeRequest.RequestWaiter
import com.fivemin.core.engine.transaction.prepareRequest.PrepareRequestTransactionPolicy
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionPolicy
import com.fivemin.core.initialize.DocumentUniqueKeyProviderImpl
import com.fivemin.core.initialize.MovementFactoryImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TaskMockFactory {
    companion object {
        fun <T> createSessionStarted() : SessionStartedState {
            val mock: BloomFilterFactory = mockk()
    
            every {
                mock.createEmpty()
            } returns (BloomFilterImpl(100, 0.00000001))
    
            var sessRepo = BloomFilterUniqueKeyRepository(mock, none())
    
    
            val sess : SessionStartedState = SessionStartedStateImpl(SessionInfo(sessRepo, sessRepo), SessionData(sessRepo, sessRepo), SessionContext(LocalUniqueKeyTokenRepo(), none()))

            return sess
        }


        fun createPolicySet (
            prepare: PrepareRequestTransactionPolicy<Request>? = null,
            request: FinalizeRequestTransactionPolicy<Request>? = null,
            serialize: SerializeTransactionPolicy<Request>? = null,
            export: ExportTransactionPolicy<Request>? = null
        ) : DocumentPolicyStorageFactoryCollector {
            var prepMock : PrepareRequestTransactionPolicy<Request> = mockk()
            coEvery {
                prepMock.progressAsync(any(), any(), any())
            } coAnswers {
                coroutineScope {
                    async {
                        firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").right()
                    }
                }
            }

            var reqMock : FinalizeRequestTransactionPolicy<Request> = mockk()
            coEvery {
                reqMock.progressAsync(any(), any(), any())
            } coAnswers {
                coroutineScope {
                    async {
                        firstArg<PrepareTransaction<Request>>().upgrade().right()
                    }
                }
            }

            var selMock : SerializeTransactionPolicy<Request> = mockk()
            coEvery {
                selMock.progressAsync(any(), any(), any())
            } coAnswers {
                coroutineScope {
                    async {
                        firstArg<FinalizeRequestTransaction<Request>>().upgrade().right()
                    }
                }
            }

            var expMock : ExportTransactionPolicy<Request> = mockk()
            coEvery {
                expMock.progressAsync(any(), any(), any())
            } coAnswers {
                coroutineScope {
                    async {
                        firstArg<SerializeTransaction<Request>>().upgrade().right()
                    }
                }
            }


            return DocumentPolicyStorageFactoryCollector(DocumentPolicyStorageFactory(
                prepare ?: prepMock,
                request ?: reqMock,
                serialize ?: selMock,
                export ?: expMock
            ))
        }


        fun createTaskInfo(
            prepare: PrepareRequestTransactionPolicy<Request>? = null,
            request: FinalizeRequestTransactionPolicy<Request>? = null,
            serialize: SerializeTransactionPolicy<Request>? = null,
            export: ExportTransactionPolicy<Request>? = null): TaskInfo {
            val info: TaskInfo = mockk()
            val taskFac: CrawlerTaskFactory<Request> = mockk()

            every {
                taskFac.policySet
            } returns(createPolicySet(prepare, request, serialize, export))

            every {
                info.createTask<Request>()
            } returns (taskFac)

            every {
                info.uniqueKeyProvider
            } returns(KeyProvider(UriUniqueKeyProvider(), StringUniqueKeyProvider()))

            every {
                taskFac["get2"](
                    any<DocumentType>()
                )
            } answers {
                val task: CrawlerTask2<
                        InitialTransaction<HttpRequest>,
                        PrepareTransaction<HttpRequest>,
                        FinalizeRequestTransaction<HttpRequest>, Request, Request, Request> =
                    mockk()

                coEvery {
                    task.start(any(), any(), any())
                }

                task
            }

            return info
        }
    }
}