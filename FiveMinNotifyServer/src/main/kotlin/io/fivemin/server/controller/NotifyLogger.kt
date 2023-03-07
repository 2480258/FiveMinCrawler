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

package io.fivemin.server.controller

import com.fivemin.core.logger.WebSocketLogger
import io.fivemin.server.model.SerializedLogItem
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks


@Component
class NotifyLogger : WebSocketLogger {
    
    private val logEventSink = Sinks.many().multicast().onBackpressureBuffer<SerializedLogItem>()
    
    val logEvent = logEventSink.asFlux()
    
    override fun logViaNetworkEnqueue(item: WebSocketLogger.EnqueueLogItem) {
        logEventSink.tryEmitNext(SerializedLogItem(item))
    }
    
    override fun logViaNetworkDequeue(item: WebSocketLogger.DequeueLogItem) {
        logEventSink.tryEmitNext(SerializedLogItem(item))
    }
}