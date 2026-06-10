package com.feng.langchain4jstarter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class Langchain4jStarterApplication

fun main(args: Array<String>) {
    runApplication<Langchain4jStarterApplication>(*args)
}
