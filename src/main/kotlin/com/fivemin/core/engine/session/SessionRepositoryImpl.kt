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

package com.fivemin.core.engine.session

import arrow.core.Option
import arrow.core.toOption
import com.fivemin.core.engine.FinishObserver
import com.fivemin.core.engine.SessionInfo
import com.fivemin.core.engine.SessionRepository
import com.fivemin.core.engine.SessionToken
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Saves Set of Sessions.
 * TODO: Performance Optimization Required.
 */
class SessionRepositoryImpl : SessionRepository, FinishObserver {
    private val hashSet: HashSet<SessionToken> = HashSet<SessionToken>()
    private val finish: CountDownLatch = CountDownLatch(1)
    private val lock: ReentrantLock = ReentrantLock()

    private var currentRemain: Int = 0
    
    
    /**
     * Factory Method for SessionInfo.
     * @param parent SessionToken with requested this request. if 'root', set this none.
     */
    override fun create(parent: Option<SessionToken>): SessionInfo {
        return SessionInfo(this, SessionToken.create().toOption())
    }
    
    /**
     * Not Thread-Safe. Do not call with other methods.
     * Returns collection of URL which added with onExportableFinish()
     */
    override fun getDetachables(): Iterable<SessionToken> {
        return hashSet
    }
    
    /**
     * Synchronized.
     * Increase number of remain requests.
     */
    override fun onStart() {
        lock.withLock { currentRemain++ }
    }
    
    /**
     *  Synchronized.
     *  Decreases number of remain requests.
     */
    override fun onFinish(token: SessionToken) {
        lock.withLock {
            currentRemain--
            if (currentRemain == 0)
                finish.countDown()
        }
    }
    
    /**
     * Synchronized.
     * Decreases number of remain requests and add this token to the Resume File.
     */
    override fun onExportableFinish(token: SessionToken) {
        lock.withLock {
            currentRemain--
            hashSet.add(token)
            if (currentRemain == 0)
                finish.countDown()
        }
    }
    
    /**
     * Blocks until all requests finishes.
     */
    override fun waitFinish() {
        finish.await()
    }
}
