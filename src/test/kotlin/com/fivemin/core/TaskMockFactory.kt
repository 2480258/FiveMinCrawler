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
import arrow.core.left
import arrow.core.none
import arrow.core.right
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.engine.*
import com.fivemin.core.engine.crawlingTask.DocumentPolicyStorageFactoryCollector
import com.fivemin.core.engine.session.*
import com.fivemin.core.engine.session.bFilter.BloomFilterImpl
import com.fivemin.core.engine.session.database.DatabaseAdapterFactoryImpl
import com.fivemin.core.engine.transaction.StringUniqueKeyProvider
import com.fivemin.core.engine.transaction.UriUniqueKeyProvider
import com.fivemin.core.engine.transaction.export.ExportTransactionPolicy
import com.fivemin.core.engine.transaction.finalizeRequest.FinalizeRequestTransactionPolicy
import com.fivemin.core.engine.transaction.prepareRequest.PrepareRequestTransactionPolicy
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionPolicy
import com.fivemin.core.initialize.DocumentTransaction
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk

class TaskMockFactory {
    companion object {
        
        inline fun <reified InTrans : Transaction<Request>, OutTrans : Transaction<Request>> createThrowingPolicy(): TransactionPolicy<InTrans, OutTrans, Request, Request> {
            val policyMock1 = mockk<TransactionPolicy<InTrans, OutTrans, Request, Request>>()
            
            coEvery {
                policyMock1.progressAsync<Any>(any(), any(), any(), any())
            } answers {
                throw NullPointerException()
            }
            
            return policyMock1
        }
        
        inline fun <reified InTrans : Transaction<Request>, OutTrans : Transaction<Request>> createFallingPolicy(): TransactionPolicy<InTrans, OutTrans, Request, Request> {
            val policyMock1 = mockk<TransactionPolicy<InTrans, OutTrans, Request, Request>>()
            
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") policyMock1.progressAsync<Any>(
                    any(), any(), any(), any<suspend (Either<Throwable, OutTrans>) -> Either<Throwable, Any>>()
                )
            } coAnswers {
                NullPointerException().left()
            }
            
            return policyMock1
        }
        
        fun createSessionInitState(): SessionInitState {
            val mock: BloomFilterFactory = mockk()
            
            every {
                mock.createEmpty()
            } returns (BloomFilterImpl(100, 0.00000001))
            
            val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
            val persister = UniqueKeyPersisterImpl(persistFactory.get())
            
            var keyRepo = CompositeUniqueKeyRepository(
                persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
            )
            val fin = FinishObserverImpl()
            val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
            
            val sess: SessionInitStateImpl = spyk(
                SessionInitStateImpl(
                    SessionInfo(fin, keyRepo),
                    SessionData(keyRepo, sessRepo),
                    SessionContext(LocalUniqueKeyTokenRepo(), none())
                )
            )
            
            return sess
        }
        
        fun <T> createDetachableSessionStarted(): SessionDetachableStartedState {
            val mock: BloomFilterFactory = mockk()
            
            every {
                mock.createEmpty()
            } returns (BloomFilterImpl(100, 0.00000001))
            
            val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
            val persister = UniqueKeyPersisterImpl(persistFactory.get())
            
            var keyRepo = CompositeUniqueKeyRepository(
                persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
            )
            val fin = FinishObserverImpl()
            val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
            
            val sess: SessionDetachableStartedStateImpl = spyk(
                SessionDetachableStartedStateImpl(
                    SessionInfo(fin, keyRepo),
                    SessionData(keyRepo, sessRepo),
                    SessionContext(LocalUniqueKeyTokenRepo(), none())
                )
            )
            
            return sess
        }
        
        fun <T> createSessionStarted(): SessionStartedState {
            val mock: BloomFilterFactory = mockk()
            
            every {
                mock.createEmpty()
            } returns (BloomFilterImpl(100, 0.00000001))
            
            val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
            val persister = UniqueKeyPersisterImpl(persistFactory.get())
            
            var keyRepo = spyk(CompositeUniqueKeyRepository(
                persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
            ))
            val fin = FinishObserverImpl()
            val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
            
            val sess: SessionStartedState = spyk(
                SessionStartedStateImpl(
                    SessionInfo(fin, keyRepo),
                    SessionData(keyRepo, sessRepo),
                    SessionContext(LocalUniqueKeyTokenRepo(), none())
                )
            )
            
            return sess
        }
    
        fun createPolicySet4() : DocumentPolicyStorageFactoryCollector<Request>{
            var prepMock: PrepareRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") prepMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, PrepareTransaction<Request>>) -> Either<Throwable, PrepareTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().upgrade().upgrade().right()
            }
            var reqMock: FinalizeRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") reqMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, FinalizeRequestTransaction<Request>>) -> Either<Throwable, FinalizeRequestTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().upgrade().upgrade().right()
        
            }
    
            var selMock: SerializeTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") selMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, SerializeTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().upgrade().upgrade().right()
            }
    
            var expMock: ExportTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") expMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, ExportTransaction<Request>>) -> Either<Throwable, ExportTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().upgrade().upgrade().right()
            }
    
            return DocumentPolicyStorageFactoryCollector<Request>(
                mapOf<DocumentTransaction, Any>(
                    DocumentTransaction.Prepare to (prepMock),
                    DocumentTransaction.Request to (reqMock),
                    DocumentTransaction.Serialize to (selMock),
                    DocumentTransaction.Export to (expMock)
                )
            )
        }
    
    
        fun createPolicySet3() : DocumentPolicyStorageFactoryCollector<Request>{
            var prepMock: PrepareRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") prepMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, PrepareTransaction<Request>>) -> Either<Throwable, PrepareTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().upgrade().right()
            }
    
            var reqMock: FinalizeRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") reqMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, FinalizeRequestTransaction<Request>>) -> Either<Throwable, FinalizeRequestTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().upgrade().right()
        
            }
    
            var selMock: SerializeTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") selMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, SerializeTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().upgrade().right()
            }
    
            var expMock: ExportTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") expMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, ExportTransaction<Request>>) -> Either<Throwable, ExportTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().upgrade().right()
            }
    
            return DocumentPolicyStorageFactoryCollector<Request>(
                mapOf<DocumentTransaction, Any>(
                    DocumentTransaction.Prepare to (prepMock),
                    DocumentTransaction.Request to (reqMock),
                    DocumentTransaction.Serialize to (selMock),
                    DocumentTransaction.Export to (expMock)
                )
            )
        }
    
        fun createPolicySet2() : DocumentPolicyStorageFactoryCollector<Request>{
            var prepMock: PrepareRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") prepMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, PrepareTransaction<Request>>) -> Either<Throwable, PrepareTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().right()
            }
    
            var reqMock: FinalizeRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") reqMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, FinalizeRequestTransaction<Request>>) -> Either<Throwable, FinalizeRequestTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().right()
        
            }
    
            var selMock: SerializeTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") selMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, SerializeTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().right()
            }
    
            var expMock: ExportTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") expMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, ExportTransaction<Request>>) -> Either<Throwable, ExportTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").upgrade().right()
            }
    
            return DocumentPolicyStorageFactoryCollector<Request>(
                mapOf<DocumentTransaction, Any>(
                    DocumentTransaction.Prepare to (prepMock),
                    DocumentTransaction.Request to (reqMock),
                    DocumentTransaction.Serialize to (selMock),
                    DocumentTransaction.Export to (expMock)
                )
            )
        }
        
        fun createPolicySet1() : DocumentPolicyStorageFactoryCollector<Request>{
            var prepMock: PrepareRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") prepMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, PrepareTransaction<Request>>) -> Either<Throwable, PrepareTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").right()
            }
    
            var reqMock: FinalizeRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") reqMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, FinalizeRequestTransaction<Request>>) -> Either<Throwable, FinalizeRequestTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").right()
        
            }
    
            var selMock: SerializeTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") selMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, SerializeTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").right()
            }
    
            var expMock: ExportTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") expMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, ExportTransaction<Request>>) -> Either<Throwable, ExportTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").right()
            }
            
            return DocumentPolicyStorageFactoryCollector<Request>(
                mapOf<DocumentTransaction, Any>(
                    DocumentTransaction.Prepare to (prepMock),
                    DocumentTransaction.Request to (reqMock),
                    DocumentTransaction.Serialize to (selMock),
                    DocumentTransaction.Export to (expMock)
                )
            )
        }
        
        fun createPolicySet(
            prepare: PrepareRequestTransactionPolicy<Request>? = null,
            request: FinalizeRequestTransactionPolicy<Request>? = null,
            serialize: SerializeTransactionPolicy<Request>? = null,
            export: ExportTransactionPolicy<Request>? = null
        ): DocumentPolicyStorageFactoryCollector<Request> {
            var prepMock: PrepareRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") prepMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, PrepareTransaction<Request>>) -> Either<Throwable, PrepareTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<InitialTransaction<Request>>().upgradeAsDocument("a").right()
            }
            
            var reqMock: FinalizeRequestTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") reqMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, FinalizeRequestTransaction<Request>>) -> Either<Throwable, FinalizeRequestTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<PrepareTransaction<Request>>().upgrade().right()
                
            }
            
            var selMock: SerializeTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") selMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, SerializeTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<FinalizeRequestTransaction<Request>>().upgrade().right()
                
            }
            
            var expMock: ExportTransactionPolicy<Request> = mockk()
            coEvery {
                // DO NOT REMOVE GENERIC ARGUMENTS
                @Suppress("RemoveExplicitTypeArguments") expMock.progressAsync<Any>(
                    any(),
                    any(),
                    any(),
                    any<suspend (Either<Throwable, ExportTransaction<Request>>) -> Either<Throwable, ExportTransaction<Request>>>()
                )
            } coAnswers {
                firstArg<SerializeTransaction<Request>>().upgrade().right()
            }
            
            return DocumentPolicyStorageFactoryCollector<Request>(
                mapOf<DocumentTransaction, Any>(
                    DocumentTransaction.Prepare to (prepare ?: prepMock),
                    DocumentTransaction.Request to (request ?: reqMock),
                    DocumentTransaction.Serialize to (serialize ?: selMock),
                    DocumentTransaction.Export to (export ?: expMock)
                )
            
            )
        }
        
        
        fun createTaskInfo(
            prepare: PrepareRequestTransactionPolicy<Request>? = null,
            request: FinalizeRequestTransactionPolicy<Request>? = null,
            serialize: SerializeTransactionPolicy<Request>? = null,
            export: ExportTransactionPolicy<Request>? = null,
            policySet: DocumentPolicyStorageFactoryCollector<Request>? = null
        ): TaskInfo {
            val info: TaskInfo = mockk()
            val taskFac: CrawlerTaskFactory<Request> = mockk()
            
            every {
                taskFac.policySet
            } returns (policySet ?: createPolicySet(prepare, request, serialize, export))
            
            every {
                info.createTask<Request>()
            } returns (taskFac)
            
            every {
                info.uniqueKeyProvider
            } returns (KeyProvider(UriUniqueKeyProvider(), StringUniqueKeyProvider()))
            
            every {
                taskFac["get2"](
                    any<DocumentType>()
                )
            } answers {
                val task: CrawlerTask2<InitialTransaction<HttpRequest>, PrepareTransaction<HttpRequest>, FinalizeRequestTransaction<HttpRequest>, Request, Request, Request> =
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