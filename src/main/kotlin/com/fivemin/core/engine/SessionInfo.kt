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

package com.fivemin.core.engine

import arrow.core.Option
import com.fivemin.core.LoggerController

class SessionInfo
constructor(
    private val finish: FinishObserver,
    val parent: Option<SessionToken>
) {

    companion object {
        private val logger = LoggerController.getLogger("SessionInfo")
    }

    val token: SessionToken = SessionToken.create()
    val isDetachable: DetachableState
        get() {
            return detachable
        }
    private var detachable = DetachableState.NOTMODIFIED
    private var progress = ProgressState.STARTED

    private var reenterent = 0

    init {
        finish.onStart()
    }
    
    /**
     * Counts number of task.
     * Program won't finish until every registered task is done.
     */
    suspend fun <T> doRegisteredTask(func: suspend () -> T): T {
        try {
            reenterent++
            return func()
        } finally {
            reenterent--

            if (reenterent == 0) {
                setFinished()
            }
        }
    }

    private fun setFinished() {
        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }

        progress = ProgressState.FINISHED

        if (detachable == DetachableState.WANT) {
            finish.onExportableFinish(token)
        } else {
            finish.onFinish(token)
        }
    }

    fun setDetachable() {
        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }

        detachable = DetachableState.WANT
    }

    fun setNonDetachable() {

        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }

        detachable = DetachableState.HATE
    }
}
