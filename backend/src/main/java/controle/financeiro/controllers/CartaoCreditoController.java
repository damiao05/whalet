package controle.financeiro.controllers;

import controle.financeiro.dtos.CartaoRequestDTO;
import controle.financeiro.dtos.CartaoResponseDTO;
import controle.financeiro.services.CartaoCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cartoes")
@RequiredArgsConstructor
public class CartaoCreditoController {

    private final  CartaoCreditoService service;

    @GetMapping
    public List<CartaoResponseDTO> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartaoResponseDTO criar(@Valid @RequestBody CartaoRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public CartaoResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody CartaoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void arquivar(@PathVariable UUID id) {
        service.arquivar(id);
    }

}
