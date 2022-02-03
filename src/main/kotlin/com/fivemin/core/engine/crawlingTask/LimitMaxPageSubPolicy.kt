package com.fivemin.core.engine.crawlingTask

import arrow.core.Either
import arrow.core.right
import arrow.core.left
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.atomic.AtomicInteger

class LimitMaxPageSubPolicy<Document : Request> (private val maxPageNum: Int) :
    TransactionSubPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document>{
    
    val pageCount : AtomicInteger = AtomicInteger(0)
    
    override suspend fun process(
        source: InitialTransaction<Document>,
        dest: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                val cnt = pageCount.getAndIncrement()
    
                if(cnt >= maxPageNum) {
                    ExceedsMaxPageException().left()
                } else {
                    dest.right()
                }
    
            }
        }
    
    }
}

class ExceedsMaxPageException : Exception()