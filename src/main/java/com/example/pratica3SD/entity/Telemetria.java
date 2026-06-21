package com.example.pratica3SD.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Telemetria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Chave primária gerada automaticamente pelo banco

    // --- Campos Comuns (Todos os dispositivos têm) ---
    private Instant timestamp;
    private String tipo;
    private String nome;
    private Double temperatura;
    private Boolean ligado;
    private Double consumoEnergia;
    private Long tempoUso;
    private Double usoDados;

    // --- Campos Específicos: Computador ---
    private Double usoCpu;
    private Double usoRam;
    private String estadoComputador;
    private String aplicacao;
    private String eventoSeguranca;

    // --- Campos Específicos: Ar Condicionado ---
    private String estadoArCondicionado;

    // --- Campos Específicos: Projetor ---
    private String estadoProjetor;
    private String entradaVideo;
    private Double temperaturaInterna;
}