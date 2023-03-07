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

package io.fivemin.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fivemin.core.logger.WebSocketLogger


data class SerializedLogItem(@JsonProperty("message") val message: String, @JsonProperty("uri")  val uri: String, @JsonProperty("score")  val score: Double?) {
    companion object {
        private fun getMessageFromItem(item: WebSocketLogger.LogItem) : Message {
            return when(item) {
                is WebSocketLogger.EnqueueLogItem -> Message.ENQUEUE
                is WebSocketLogger.DequeueLogItem -> Message.DEQUEUE
                else -> throw IllegalArgumentException()
            }
        }
    
        private fun getScoreFromItem(item: WebSocketLogger.LogItem) : Double? {
            if(item is WebSocketLogger.EnqueueLogItem) {
                return item.score
            }
        
            return null
        }
    }
    
    constructor(item: WebSocketLogger.LogItem) : this(getMessageFromItem(item).toString(), item.request.target.toString(), getScoreFromItem(item))
    enum class Message {
        ENQUEUE,
        DEQUEUE
    }
}