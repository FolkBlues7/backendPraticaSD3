package com.example.pratica3SD.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String QUEUE_TELEMETRIA = "telemetria_queue";
    public static final String QUEUE_ALERTAS = "alertas_queue";

    @Bean
    public Queue telemetriaQueue() {
        // O segundo parâmetro "true" define a fila como durável (não se perde se o RabbitMQ reiniciar)
        return new Queue(QUEUE_TELEMETRIA, true);
    }

    @Bean
    public Queue alertasQueue() {
        return new Queue(QUEUE_ALERTAS, true);
    }
}