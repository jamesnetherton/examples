package com.github.jamesnetherton.application;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RouteBuilder loggingRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer://foo?period=5000")
                .setHeader(Exchange.FILE_NAME, constant("test-log.log"))
                .setBody().constant("Hello World")
                .to("file:/tmp/deployment/logs/");
            }
        };
    }
}