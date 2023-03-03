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
import com.fivemin.core.engine.transaction.AbstractPolicy
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
    class SuccessPolicy<InTrans : Transaction<Request>, OutTrans: StrictTransaction<InTrans, Request>>
        : AbstractPolicy<InTrans, OutTrans, Request>(mockk(), mockk()) {
        override suspend fun <Ret> progressAsync(
            trans: InTrans,
            state: SessionStartedState,
            next: suspend (Either<Throwable, OutTrans>) -> Either<Throwable, Ret>
        ): Either<Throwable, Ret> {
            val ret = when (trans) {
                is InitialTransaction<Request> -> {
                    trans.upgradeAsDocument("a")
                }
    
                is PrepareTransaction<Request> -> {
                    trans.upgrade()
                }
    
                is FinalizeRequestTransaction<Request> -> {
                    trans.upgrade()
                }
    
                is SerializeTransaction<Request> -> {
                    trans.upgrade()
                }
    
                else -> {
                    throw IllegalArgumentException()
                }
            }
            
            return next((ret as OutTrans).right())
        }
    }
    class ThrowingPolicy<InTrans : Transaction<Request>, OutTrans: Transaction<Request>> : TransactionPolicy<InTrans, OutTrans, Request, Request> {
        override suspend fun <Ret> progressAsync(
            trans: InTrans,
            state: SessionStartedState,
            next: suspend (Either<Throwable, OutTrans>) -> Either<Throwable, Ret>
        ): Either<Throwable, Ret> {
            throw NullPointerException()
        }
    }
    
    class FailingPolicy<InTrans : Transaction<Request>, OutTrans: Transaction<Request>> : TransactionPolicy<InTrans, OutTrans, Request, Request> {
        override suspend fun <Ret> progressAsync(
            trans: InTrans,
            state: SessionStartedState,
            next: suspend (Either<Throwable, OutTrans>) -> Either<Throwable, Ret>
        ): Either<Throwable, Ret> {
            return NullPointerException().left()
        }
    }
    
    companion object {
        
        inline fun <reified InTrans : Transaction<Request>, OutTrans : Transaction<Request>> createThrowingPolicy(): TransactionPolicy<InTrans, OutTrans, Request, Request> {
            return ThrowingPolicy()
        }
        
        inline fun <reified InTrans : Transaction<Request>, OutTrans : Transaction<Request>> createFallingPolicy(): TransactionPolicy<InTrans, OutTrans, Request, Request> {
            return FailingPolicy()
        }
        
        fun createSessionInitState(info: TaskInfo? = null): SessionInitState {
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
                    SessionContext(LocalUniqueKeyTokenRepo(), none()),
                    info ?: createTaskInfo()
                )
            )
            
            return sess
        }
        
        fun <T> createDetachableSessionStarted(info: TaskInfo? = null): SessionDetachableStartedState {
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
                    SessionContext(LocalUniqueKeyTokenRepo(), none()),
                    info ?: createTaskInfo()
                )
            )
            
            return sess
        }
        
        fun <T> createSessionStarted(info: TaskInfo? = null): SessionStartedState {
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
                    SessionContext(LocalUniqueKeyTokenRepo(), none()),
                    info ?: createTaskInfo()
                )
            )
            
            return sess
        }
    
        fun createPolicySet() : DocumentPolicyStorageFactoryCollector<Request>{
            val prepMock: AbstractPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request> = SuccessPolicy()

            val reqMock: AbstractPolicy<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request> = SuccessPolicy()
    
            val selMock: AbstractPolicy<FinalizeRequestTransaction<Request>, SerializeTransaction<Request>, Request> = SuccessPolicy()
    
            val expMock: AbstractPolicy<SerializeTransaction<Request>, ExportTransaction<Request>, Request> = SuccessPolicy()
            
            return DocumentPolicyStorageFactoryCollector(
                mapOf<DocumentTransaction, Any>(
                    DocumentTransaction.Prepare to (prepMock),
                    DocumentTransaction.Request to (reqMock),
                    DocumentTransaction.Serialize to (selMock),
                    DocumentTransaction.Export to (expMock)
                )
            )
        }
        
        
        fun createTaskInfo(): TaskInfo {
            val info: TaskInfo = mockk()
            val taskFac: CrawlerTaskFactory<Request> = mockk()
            
            every {
                taskFac.policySet
            } returns (createPolicySet())
            
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
                    task.start(any(), any())
                }
                
                task
            }
            
            return info
        }
    }
}