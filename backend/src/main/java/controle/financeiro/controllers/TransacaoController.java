package controle.financeiro.controllers;

import controle.financeiro.models.Transacao;
import controle.financeiro.dtos.TransacaoRequestDTO;
import controle.financeiro.services.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService service;

    @PostMapping
    public ResponseEntity<Transacao> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        Transacao novaTransacao = service.criarTransacao(dto);
        return ResponseEntity.ok(novaTransacao);
    }

}
