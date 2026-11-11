package controle.financeiro.services;

import controle.financeiro.dtos.ContaRequestDTO;
import controle.financeiro.dtos.ContaResponseDTO;
import controle.financeiro.models.Conta;
import controle.financeiro.repositories.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository repository;
    private final UsuarioLogado usuarioLogado;

    @Transactional(readOnly = true)
    public List<ContaResponseDTO> listar() {
        UUID usuarioId = usuarioLogado.obter().getId();
        return repository.findByUsuarioIdAndAtivaTrueOrderByNomeAsc(usuarioId)
                .stream().map(ContaResponseDTO::de).toList();
    }

    @Transactional
    public ContaResponseDTO criar(ContaRequestDTO dto) {
        Conta conta = new Conta();
        conta.setUsuario(usuarioLogado.obter());
        aplicar(conta, dto);
        return ContaResponseDTO.de(repository.save(conta));
    }

    @Transactional
    public ContaResponseDTO atualizar(UUID id, ContaRequestDTO dto) {
        Conta conta = buscar(id);
        aplicar(conta, dto);
        return ContaResponseDTO.de(conta);
    }

    @Transactional
    public void arquivar(UUID id) {
        buscar(id).setAtiva(false);
    }

    private Conta buscar(UUID id) {
        return repository.findByIdAndUsuarioId(id, usuarioLogado.obter().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }

    private void aplicar(Conta conta, ContaRequestDTO dto) {
        conta.setNome(dto.nome().trim());
        conta.setTipo(dto.tipo());
        conta.setInstituicao(dto.instituicao());
        conta.setSaldoInicial(dto.saldoInicial());
    }

}
