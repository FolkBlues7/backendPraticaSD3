package com.example.pratica3SD.service;

import com.example.pratica3SD.config.RabbitMqConfig;
import com.example.pratica3SD.entity.Telemetria;
import com.example.pratica3SD.repository.TelemetriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqConsumer {

    private final TelemetriaRepository repository;
    private final ObjectMapper objectMapper;

    // Injeção de dependências via construtor
    public RabbitMqConsumer(TelemetriaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    // O Spring Boot monitoriza a fila de telemetria automaticamente
    @RabbitListener(queues = RabbitMqConfig.QUEUE_TELEMETRIA)
    public void receberTelemetria(String payload) {
        System.out.println("[Backend <- RabbitMQ] Nova telemetria recebida: " + payload);

        try {
            // 1. Converte o JSON (String) para a Entidade Telemetria
            Telemetria telemetria = objectMapper.readValue(payload, Telemetria.class);

            // 2. Guarda na base de dados PostgreSQL
            repository.save(telemetria);

            System.out.println("[Backend] Telemetria de " + telemetria.getNome() + " guardada no PostgreSQL com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao processar telemetria do RabbitMQ: " + e.getMessage());
        }
    }

    // Monitoriza a fila de alertas/anomalias
    @RabbitListener(queues = RabbitMqConfig.QUEUE_ALERTAS)
    public void receberAlerta(String payload) {
        System.out.println("[Backend <- RabbitMQ] ⚠️ ALERTA DE ANOMALIA RECEBIDO: " + payload);

        try {
            // Se o alerta também for um JSON de Telemetria, guardamos na base de dados:
            Telemetria alerta = objectMapper.readValue(payload, Telemetria.class);
            repository.save(alerta);
        } catch (Exception e) {
            System.err.println("Erro ao salvar alerta: " + e.getMessage());
        }
    }
}