package controle.financeiro.controllers;

import controle.financeiro.dtos.ContaRequestDTO;
import controle.financeiro.dtos.ContaResponseDTO;
import controle.financeiro.services.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService service;

    @GetMapping
    public List<ContaResponseDTO> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContaResponseDTO criar(@Valid @RequestBody ContaRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public ContaResponseDTO atualizar(@PathVariable UUID id, @Valid  @RequestBody ContaRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void arquivar(@PathVariable UUID id) {
        service.arquivar(id);
    }

}
