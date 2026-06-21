package com.example.pratica3SD.controller;

import com.example.pratica3SD.entity.Telemetria;
import com.example.pratica3SD.repository.TelemetriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telemetria")
public class TelemetriaController {

    private final TelemetriaRepository repository;

    // Injeção de dependência via construtor (boa prática recomendada pelo Spring)
    public TelemetriaController(TelemetriaRepository repository) {
        this.repository = repository;
    }

    // --- ENDPOINTS GENÉRICOS (Que você já tinha) ---

    // 1. Rota para buscar TODOS os registos de telemetria
    @GetMapping
    public List<Telemetria> listarTodas() {
        return repository.findAll();
    }

    // 2. Rota para buscar uma telemetria específica pelo ID do banco
    @GetMapping("/{id}")
    public ResponseEntity<Telemetria> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(telemetria -> ResponseEntity.ok().body(telemetria))
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Rota auxiliar para contar a quantidade de registos guardados
    @GetMapping("/count")
    public ResponseEntity<Long> contarRegistos() {
        long total = repository.count();
        return ResponseEntity.ok(total);
    }

    // --- ENDPOINTS ARQUITETURA IOT (Novos) ---

    // 4. Retorna o estado atual de todos os dispositivos (Última leitura de cada)
    @GetMapping("/dispositivos")
    public ResponseEntity<List<Telemetria>> listarDispositivos() {
        List<Telemetria> dispositivos = repository.findLatestTelemetriaPerDevice();
        return ResponseEntity.ok(dispositivos);
    }

    // 5. Retorna o estado atual de um dispositivo específico (Digital Twin)
    @GetMapping("/dispositivos/{nome}/digital-twin")
    public ResponseEntity<Telemetria> obterDigitalTwin(@PathVariable String nome) {
        Telemetria twin = repository.findFirstByNomeOrderByTimestampDesc(nome);
        if (twin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(twin);
    }

    // 6. Retorna todo o histórico de um dispositivo específico
    @GetMapping("/dispositivos/{nome}/historico")
    public ResponseEntity<List<Telemetria>> obterHistoricoTelemetria(@PathVariable String nome) {
        List<Telemetria> historico = repository.findByNomeOrderByTimestampDesc(nome);
        return ResponseEntity.ok(historico);
    }

    // 7. Retorna apenas os eventos de anomalia/segurança do dispositivo
    @GetMapping("/dispositivos/{nome}/historico/eventos")
    public ResponseEntity<List<Telemetria>> obterHistoricoEventos(@PathVariable String nome) {
        List<Telemetria> eventos = repository.findByNomeAndEventoSegurancaIsNotNullOrderByTimestampDesc(nome);
        return ResponseEntity.ok(eventos);
    }

    // 8. Retorna o estado atual de todos os dispositivos de uma sala específica filtrando pelo NOME
    @GetMapping("/laboratorios/{idLab}/dispositivos")
    public ResponseEntity<List<Telemetria>> listarDispositivosPorSala(@PathVariable String idLab) {

        // Padroniza a entrada.
        // Se o usuário mandar "3", vira "LAB-3-". Se mandar "LAB-3", vira "LAB-3-" também.
        String prefixoBusca = idLab.toUpperCase();
        if (!prefixoBusca.startsWith("LAB-")) {
            prefixoBusca = "LAB-" + prefixoBusca;
        }
        if (!prefixoBusca.endsWith("-")) {
            prefixoBusca = prefixoBusca + "-";
        }

        // Executa a busca passando o prefixo (ex: "LAB-3-")
        List<Telemetria> dispositivos = repository.findLatestByLaboratorioNome(prefixoBusca);

        if (dispositivos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dispositivos);
    }

    // 9. Retorna todos os alertas de um laboratório específico
    @GetMapping("/alertas/laboratorios/{idLab}")
    public ResponseEntity<List<Telemetria>> listarAlertasPorLaboratorio(@PathVariable String idLab) {

        // Padroniza a entrada do laboratório (ex: "3" vira "LAB-3-")
        String prefixoBusca = idLab.toUpperCase();
        if (!prefixoBusca.startsWith("LAB-")) {
            prefixoBusca = "LAB-" + prefixoBusca;
        }
        if (!prefixoBusca.endsWith("-")) {
            prefixoBusca = prefixoBusca + "-";
        }

        List<Telemetria> alertas = repository.findAlertsByLaboratorio(prefixoBusca);
        return ResponseEntity.ok(alertas);
    }

    // 10. Retorna todos os alertas de um dispositivo específico
    @GetMapping("/alertas/dispositivos/{nome}")
    public ResponseEntity<List<Telemetria>> listarAlertasPorDispositivo(@PathVariable String nome) {
        List<Telemetria> alertas = repository.findAlertsByDispositivo(nome);
        return ResponseEntity.ok(alertas);
    }

    // 11. Retorna o ÚLTIMO alerta geral do sistema (O mais recente)
    @GetMapping("/alertas/ultimo")
    public ResponseEntity<Telemetria> obterUltimoAlerta() {
        Telemetria ultimoAlerta = repository.findLatestAlert();

        if (ultimoAlerta == null) {
            // Se não houver nenhum alerta no banco, retorna 404 Not Found ou 204 No Content
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ultimoAlerta);
    }
}