package com.feng.langchain4jstarter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing


@SpringBootApplication
@EnableJpaAuditing
class Langchain4jStarterApplication

fun main(args: Array<String>) {
    runApplication<Langchain4jStarterApplication>(*args)

    // val serverInfo = McpImplementation().apply {
    //     name = "langchain4j-mcp-server"
    //     version = "1.0.0"
    // }
    //
    // val server = McpServer(listOf(WeatherTool()), serverInfo)
    // StdioMcpServerTransport(System.`in`, System.out, server)
    //
    // // Keep the process alive while stdio is open
    // Thread.currentThread().join()
}
