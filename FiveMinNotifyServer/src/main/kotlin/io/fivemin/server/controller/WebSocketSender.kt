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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fivemin.core.logger.QueueLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.collect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentSkipListSet

@Suppress("unused")
@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    @Autowired
    private val sender = WebSocketSender()
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(sender, "ws").setAllowedOrigins("*").withSockJS()
    }
}

@Component
class WebSocketSender : TextWebSocketHandler() {
    @Autowired
    lateinit var notifyLogger: NotifyLogger
    val sessions = ConcurrentSkipListSet<WebSocketSession>()
    
    init {
        QueueLogger.setWebSocketLoggerEndpoint(notifyLogger)
        val objectMapper = ObjectMapper()
        
        GlobalScope.launch {
            notifyLogger.logEvent.collect { item ->
                sessions.parallelStream().forEach { session ->
                    session.sendMessage(TextMessage(objectMapper.writeValueAsString(item)))
                }
            }
        }
    }
    
    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        super.afterConnectionEstablished(session)
    }
}