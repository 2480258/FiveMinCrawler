package fivemin.core.engine

import fivemin.core.Logger
import arrow.core.*
import fivemin.core.LoggerController
import kotlinx.coroutines.*


suspend fun <T> SessionInitState.ifDetachable(func: suspend (SessionDetachableInitState) -> T): Option<T> {
    return if (this is SessionDetachableInitState) {
        Some(func(this))
    } else {
        none()
    }
}

suspend fun <T> SessionStartedState.ifDetachable(func: suspend (SessionDetachableStartedState) -> T): Option<T> {
    return if (this is SessionDetachableStartedState) {
        Some(func(this))
    } else {
        none()
    }
}

data class SessionData constructor(
    val KeyRepo: UniqueKeyRepository,
    val SessionRepo: SessionRepository,
    val RetryCount: Int
) {

}

enum class ProgressState {
    STARTED, FINISHED, ERROR
}

enum class DetachableState {
    NOTMODIFIED, WANT, HATE
}


interface SessionState {
    val info: SessionInfo
    val Data: SessionData
}

interface SessionAddableAlias : SessionState {

    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }

    fun addAlias(key: UniqueKey) {
        logger.debug(info.token.tokenNumber.toString() + " < Adding alias ["+ key.toString() + "]")
        Data.KeyRepo.addAlias(info.token, key)
    }
}

interface SessionMarkDetachable : SessionState {
    fun setDetachable() {
        info.setDetachable()
    }

    fun setNonDetachable() {
        info.setNonDetachable()
    }

    val isDetachable: DetachableState
        get() {
            return info.isDetachable
        }
}

interface SessionRetryable : SessionState {
    companion object {
        private val logger = LoggerController.getLogger("SessionRetryable")


        val MaxRetryCount: Int = 3
    }


    suspend fun <T> retryAsync(func: suspend (SessionInitState) -> Deferred<Validated<Throwable, T>>): Deferred<Validated<Throwable, T>> {
        if (Data.RetryCount >= MaxRetryCount) {
            return coroutineScope {
                async {
                    TaskCanceledException().invalid()
                }
            }
        }

        logger.info(this.info.token.tokenNumber.toString() + " < Retrying")
        var st = this as? SessionDetachable
        var state = st.rightIfNotNull { }
            .fold({ SessionDetachableInitStateImpl(info, Data) }, { SessionInitStateImpl(info, Data) })

        var r = func(state)

        return r
    }
}

interface SessionDetachable : SessionState {

    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }

    suspend fun detach(func: suspend (SessionInitState) -> Option<Throwable>): Deferred<Option<Throwable>> {
        var detached = Data.SessionRepo.create(info.token.toOption())
        Data.KeyRepo.transferOwnership(info.token, detached.token)

        coroutineScope {
            launch {
                logger.info(info.token.tokenNumber.toString() + " < Detached")
                func(SessionInitStateImpl(detached, Data))
            }
        }

        return coroutineScope {
            async {
                TaskCanceledException().toOption()
            }
        }
    }
}

interface SessionStartable : SessionState {

    companion object {
        private val logger = LoggerController.getLogger("SessionStartable")
    }

    suspend fun <T> start(
        key: UniqueKey,
        func: suspend (SessionStartedState) -> Deferred<Validated<Throwable, T>>
    ): Deferred<Validated<Throwable, T>> {
        logger.debug(key.toString() + " < Creating SessionStartable")

        Data.KeyRepo.addAlias(info.token, key)

        var st = this is SessionDetachable

        var state = if (this is SessionDetachable) {
            SessionDetachableStartedStateImpl(info, Data)
        } else {
            SessionStartedStateImpl(info, Data)
        }

        var r = func(state)
        var ret = r.await()

        ret.fold({
            info.finish()
        }, {
            info.error()
        }) //TODO This is different from source

        return r
    }


}

interface SessionDetachRetryable : SessionState {

}

interface SessionChildGeneratable : SessionState {


    companion object {
        private val logger = LoggerController.getLogger("SessionChildGeneratable")
    }
    suspend fun <T> getChildSession(func: suspend (SessionInitState) -> Deferred<Validated<Throwable, T>>): Deferred<Validated<Throwable, T>> {

        logger.info(info.token.tokenNumber.toString() + " < Creating child session")

        var detached = Data.SessionRepo.create(info.parent)
        var ret = func(SessionDetachableInitStateImpl(detached, Data))
        return ret
    }
}

interface SessionInitState : SessionStartable {

}

interface SessionDetachableInitState : SessionDetachable, SessionInitState {

}

interface SessionStartedState : SessionRetryable, SessionChildGeneratable, SessionAddableAlias, SessionMarkDetachable {

}

interface SessionDetachableStartedState : SessionStartedState, SessionDetachable, SessionDetachRetryable {

}

data class SessionInitStateImpl constructor(override val info: SessionInfo, override val Data: SessionData) :
    SessionInitState {

}

data class SessionStartedStateImpl constructor(override val info: SessionInfo, override val Data: SessionData) :
    SessionStartedState {

}

data class SessionDetachableInitStateImpl constructor(override val info: SessionInfo, override val Data: SessionData) :
    SessionDetachableInitState {

}

data class SessionDetachableStartedStateImpl constructor(
    override val info: SessionInfo,
    override val Data: SessionData
) : SessionDetachableStartedState {

}
