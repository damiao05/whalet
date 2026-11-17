package controle.financeiro.controllers;

import controle.financeiro.dtos.RecorrenciaRequestDTO;
import controle.financeiro.dtos.RecorrenciaResponseDTO;
import controle.financeiro.services.RecorrenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recorrencias")
@RequiredArgsConstructor
public class RecorrenciaController {

    private final RecorrenciaService service;

    @GetMapping
    public List<RecorrenciaResponseDTO> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecorrenciaResponseDTO criar(@Valid @RequestBody RecorrenciaRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public RecorrenciaResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody RecorrenciaRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void encerrar(@PathVariable UUID id) {
        service.encerrar(id);
    }
}