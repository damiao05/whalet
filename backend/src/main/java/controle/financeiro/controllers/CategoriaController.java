package controle.financeiro.controllers;

import controle.financeiro.dtos.CategoriaRequestDTO;
import controle.financeiro.dtos.CategoriaResponseDTO;
import controle.financeiro.models.TipoTransacao;
import controle.financeiro.services.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;

    /** GET /api/categorias?tipo=DESPESA&incluirArquivadas=false */
    @GetMapping
    public List<CategoriaResponseDTO> listar(
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(defaultValue = "false") boolean incluirArquivadas) {
        return service.listar(tipo, incluirArquivadas);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponseDTO criar(@Valid @RequestBody CategoriaRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public CategoriaResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody CategoriaRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void arquivar(@PathVariable UUID id) {
        service.arquivar(id);
    }
}