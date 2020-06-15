package de.nikeshkin.postgresreactivelistener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PostgresReactiveListenerApplication

fun main(args: Array<String>) {
	runApplication<PostgresReactiveListenerApplication>(*args)
}
