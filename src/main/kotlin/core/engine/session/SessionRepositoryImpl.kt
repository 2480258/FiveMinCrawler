package core.engine.session

import arrow.core.Either
import arrow.core.Option
import arrow.core.toOption
import core.engine.FinishObserver
import core.engine.SessionInfo
import core.engine.SessionRepository
import core.engine.SessionToken
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SessionRepositoryImpl constructor() : SessionRepository, FinishObserver {
    private val hashSet : HashSet<SessionToken> = HashSet<SessionToken>()
    private val finish : CountDownLatch = CountDownLatch(1)
    private val lock : ReentrantLock = ReentrantLock()

    private var currentRemain : Int = 0


    override fun create(parent: Option<SessionToken>): SessionInfo {
        return SessionInfo(this, SessionToken.create().toOption())
    }

    override fun getDetachables(): Iterable<SessionToken> {
        return hashSet
    }

    override fun onStart() {
        lock.withLock{currentRemain++}
    }

    override fun onFinish(token: SessionToken) {
        lock.withLock{currentRemain--
            if(currentRemain == 0)
                finish.countDown()}
    }

    override fun onExportableFinish(token: SessionToken) {
        lock.withLock {
            currentRemain--
            hashSet.add(token)
            if(currentRemain == 0)
                finish.countDown()}
    }

    override fun waitFinish() {
        finish.await()
    }
}