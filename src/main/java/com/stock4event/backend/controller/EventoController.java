package com.stock4event.backend.controller;

import com.stock4event.backend.entity.Evento;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.EmpresaRepository;
import com.stock4event.backend.repository.EventoRepository;
import com.stock4event.backend.security.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Eventos da empresa do usuário logado. A empresa dona é sempre a do token. */
@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventoController {

    @Autowired private EventoRepository repository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<Evento> listar(@RequestParam(required = false) Long empresaId,
                                @RequestParam(required = false) String status) {
        Long propria = acesso.resolverEmpresa(empresaId);
        return status != null
                ? repository.findByEmpresaIdAndStatus(propria, status)
                : repository.findByEmpresaId(propria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evento> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ev -> {
                    acesso.exigirEmpresa(ev.getEmpresa().getId());
                    return ResponseEntity.ok(ev);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Evento> criar(@RequestBody Evento dados) {
        if (dados.getEmpresa() != null && dados.getEmpresa().getId() != null) {
            acesso.resolverEmpresa(dados.getEmpresa().getId());
        }
        Evento evento = new Evento();
        evento.setEmpresa(empresaRepository.findById(acesso.empresaId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Empresa não encontrada.")));
        aplicarDados(evento, dados);
        return ResponseEntity.status(201).body(repository.save(evento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evento> atualizar(@PathVariable Long id,
                                             @RequestBody Evento dados) {
        return repository.findById(id).map(ev -> {
            acesso.exigirEmpresa(ev.getEmpresa().getId());
            aplicarDados(ev, dados);
            return ResponseEntity.ok(repository.save(ev));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        Evento evento = repository.findById(id).orElse(null);
        if (evento == null) return ResponseEntity.notFound().build();
        acesso.exigirEmpresa(evento.getEmpresa().getId());
        repository.delete(evento);
        return ResponseEntity.noContent().build();
    }

    /** Mesmas regras das anotações da entidade Evento (nome até 150, datas e status obrigatórios). */
    private void aplicarDados(Evento destino, Evento dados) {
        if (dados.getNome() == null || dados.getNome().isBlank() || dados.getNome().length() > 150) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Nome do evento é obrigatório (até 150 caracteres).");
        }
        if (dados.getDataInicio() == null || dados.getDataFim() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Datas de início e fim são obrigatórias.");
        }
        String status = dados.getStatus() == null ? "planejado" : dados.getStatus().trim();
        if (status.isBlank() || status.length() > 20) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Status do evento é obrigatório (até 20 caracteres).");
        }
        destino.setNome(dados.getNome().trim());
        destino.setDataInicio(dados.getDataInicio());
        destino.setDataFim(dados.getDataFim());
        destino.setStatus(status);
    }
}
