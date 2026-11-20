package controle.financeiro.controllers;

import controle.financeiro.dtos.StatusRequestDTO;
import controle.financeiro.dtos.TransacaoRequestDTO;
import controle.financeiro.dtos.TransacaoResponseDTO;
import controle.financeiro.services.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
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
        return service.listar(mes != null ? mes : YearMonth.now(ZoneId.of("America/Sao_Paulo")));
    }

    /** Retorna uma lista: 1 item, ou N se foi parcelado. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<TransacaoResponseDTO> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public TransacaoResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody TransacaoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    /** Marcar como pago/recebido: PATCH /api/transacoes/{id}/status  { "status": "EFETIVADA", "data": "2026-11-20" } */
    @PatchMapping("/{id}/status")
    public TransacaoResponseDTO alterarStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequestDTO dto) {
        return service.alterarStatus(id, dto);
    }

    /** DELETE /api/transacoes/{id}?todasParcelas=true apaga o grupo inteiro de parcelas. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean todasParcelas) {
        service.excluir(id, todasParcelas);
    }

    @GetMapping("/pendentes")
    public List<TransacaoResponseDTO> pendentes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return service.pendentes(ate);
    }

}