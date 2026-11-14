package controle.financeiro.services;

import controle.financeiro.dtos.ContaRequestDTO;
import controle.financeiro.dtos.ContaResponseDTO;
import controle.financeiro.models.Conta;
import controle.financeiro.repositories.ContaRepository;
import controle.financeiro.repositories.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioLogado usuarioLogado;

    @Transactional(readOnly = true)
    public List<ContaResponseDTO> listar() {
        UUID usuarioId = usuarioLogado.obter().getId();
        return repository.findByUsuarioIdAndAtivaTrueOrderByNomeAsc(usuarioId)
                .stream().map(c -> ContaResponseDTO.de(c, movimentacao(c.getId()))).toList();
    }

    @Transactional
    public ContaResponseDTO criar(ContaRequestDTO dto) {
        Conta conta = new Conta();
        conta.setUsuario(usuarioLogado.obter());
        aplicar(conta, dto);
        return ContaResponseDTO.de(repository.save(conta), BigDecimal.ZERO);
    }

    @Transactional
    public ContaResponseDTO atualizar(UUID id, ContaRequestDTO dto) {
        Conta conta = buscar(id);
        aplicar(conta, dto);
        return ContaResponseDTO.de(conta, movimentacao(conta.getId()));
    }

    @Transactional
    public void arquivar(UUID id) {
        buscar(id).setAtiva(false);
    }

    private BigDecimal movimentacao(UUID contaId) {
        return Objects.requireNonNullElse(
                transacaoRepository.somarMovimentacaoDaConta(contaId), BigDecimal.ZERO);
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