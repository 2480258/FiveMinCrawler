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
import com.fivemin.core.engine.session.SessionRepositoryImpl
import com.fivemin.core.engine.session.UniqueKeyRepositoryImpl
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

            var sessRepo = SessionRepositoryImpl()
            val sess : SessionStartedState = SessionStartedStateImpl(SessionInfo(sessRepo, none()), SessionData(UniqueKeyRepositoryImpl(none()), sessRepo, 3))

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