package com.supportticket.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Support Ticket MCP Server application.
 *
 * <p>Bootstraps the Spring Boot application, which registers MCP prompts,
 * resources, and tools with the Model Context Protocol server runtime.</p>
 */
//@SpringBootApplication(exclude = org.springframework.cloud.client.CommonsClientAutoConfiguration.class)
@SpringBootApplication
@EnableFeignClients
public class SupportTicketMCPServerApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(SupportTicketMCPServerApplication.class, args);
    }

}