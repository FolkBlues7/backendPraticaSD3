package com.example.pratica3SD.repository;

import com.example.pratica3SD.entity.Telemetria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {

    // Retorna todo o histórico de um dispositivo específico, do mais recente pro mais antigo
    List<Telemetria> findByNomeOrderByTimestampDesc(String nome);

    // Retorna apenas a leitura MAIS RECENTE de um dispositivo (O Digital Twin)
    Telemetria findFirstByNomeOrderByTimestampDesc(String nome);

    // Retorna o histórico de eventos de segurança/anomalias de um dispositivo
    List<Telemetria> findByNomeAndEventoSegurancaIsNotNullOrderByTimestampDesc(String nome);

    // Query customizada para pegar apenas a última leitura de CADA dispositivo distinto
    @Query(value = "SELECT DISTINCT ON (nome) * FROM telemetria ORDER BY nome, timestamp DESC", nativeQuery = true)
    List<Telemetria> findLatestTelemetriaPerDevice();


    @Query(value = "SELECT DISTINCT ON (nome) * FROM telemetria WHERE nome LIKE CONCAT(:prefixo, '%') ORDER BY nome, timestamp DESC", nativeQuery = true)
    List<Telemetria> findLatestByLaboratorioNome(@Param("prefixo") String prefixo);
}