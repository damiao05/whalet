package controle.financeiro.controllers;

import controle.financeiro.dtos.FaturaDTO;
import controle.financeiro.dtos.PagamentoFaturaRequestDTO;
import controle.financeiro.dtos.TransacaoResponseDTO;
import controle.financeiro.services.FaturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/cartoes/{cartaoId}")
@RequiredArgsConstructor
public class FaturaController {

    private final FaturaService service;

    /** GET /api/cartoes/{id}/faturas?mes=2026-12  (mês de VENCIMENTO; sem o parâmetro, a fatura atual) */
    @GetMapping("/faturas")
    public FaturaDTO obter(@PathVariable UUID cartaoId,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return service.obter(cartaoId, mes);
    }

    @PostMapping("/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponseDTO pagar(@PathVariable UUID cartaoId,
                                      @Valid @RequestBody PagamentoFaturaRequestDTO dto) {
        return service.pagar(cartaoId, dto);
    }
}