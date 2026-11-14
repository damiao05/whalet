package controle.financeiro.controllers;

import controle.financeiro.dtos.TransacaoRequestDTO;
import controle.financeiro.dtos.TransacaoResponseDTO;
import controle.financeiro.services.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService service;

    /** GET /api/transacoes?mes=2026-11 (sem o parâmetro, usa o mês atual) */
    @GetMapping
    public List<TransacaoResponseDTO> listar(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return service.listar(mes != null ? mes : YearMonth.now());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponseDTO criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public TransacaoResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody TransacaoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}