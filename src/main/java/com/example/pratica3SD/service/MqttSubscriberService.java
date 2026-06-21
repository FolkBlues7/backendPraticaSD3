package com.example.pratica3SD.service;

import com.example.pratica3SD.entity.Telemetria;
import com.example.pratica3SD.repository.TelemetriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MqttSubscriberService {

    private static final Logger LOGGER = Logger.getLogger(MqttSubscriberService.class.getName());

    // Injeção de dependências do Spring
    private final TelemetriaRepository repository;
    private final ObjectMapper objectMapper;
    private MqttClient mqttClient;

    // Lemos as configurações (se não existirem, usamos os valores padrão após os dois pontos)
    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client.id:backend-spring-boot-subscriber}")
    private String clientId;

    @Value("${mqtt.topic:laboratorios/telemetria/#}")
    private String topic;

    // O Spring Boot injeta o Repositório e o conversor JSON (ObjectMapper) automaticamente
    public MqttSubscriberService(TelemetriaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    // O @PostConstruct faz com que este método corra logo após o Spring arrancar
    @PostConstruct
    public void init() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            // Configuramos o que fazer quando uma mensagem chegar
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    LOGGER.warning("A conexão MQTT foi perdida. A tentar reconectar...");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    LOGGER.info("Nova mensagem no tópico [" + topic + "]: " + payload);

                    try {
                        // 1. Converte o JSON em texto para o Objeto Java (Entidade)
                        Telemetria telemetria = objectMapper.readValue(payload, Telemetria.class);

                        // 2. Guarda na base de dados (H2)
                        repository.save(telemetria);

                        LOGGER.info("Dados guardados com sucesso na base de dados!");
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erro ao processar a mensagem do MQTT", e);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Como este é apenas um Subscriber (recetor), não precisamos disto.
                }
            });

            // Conectar e Subscrever
            mqttClient.connect(options);
            mqttClient.subscribe(topic);
            LOGGER.info("Serviço MQTT iniciado! Ligado a " + brokerUrl + " e a ouvir o tópico: " + topic);

        } catch (MqttException e) {
            LOGGER.log(Level.SEVERE, "Falha ao iniciar o cliente MQTT", e);
        }
    }

    // O @PreDestroy garante que desligamos o cliente de forma limpa quando a aplicação parar
    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                LOGGER.info("Cliente MQTT desligado com sucesso.");
            }
        } catch (MqttException e) {
            LOGGER.warning("Erro ao tentar desligar o cliente MQTT.");
        }
    }
}