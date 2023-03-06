package fivemin.notifyServer.notifyServer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NotifyServerApplication

fun main(args: Array<String>) {
	runApplication<NotifyServerApplication>(*args)
}
