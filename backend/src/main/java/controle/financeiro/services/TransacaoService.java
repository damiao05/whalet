package controle.financeiro.services;

import controle.financeiro.models.Transacao;
import controle.financeiro.dtos.TransacaoRequestDTO;
import controle.financeiro.repositories.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository repository;

    @Transactional
    public Transacao criarTransacao(TransacaoRequestDTO dto) {
        Transacao transacao = new Transacao();
        transacao.setDescricao(dto.descricao());
        transacao.setValor(dto.valor());
        transacao.setTipo(dto.tipo().toUpperCase());
        transacao.setMetadados(dto.metadados());

        return repository.save(transacao);
    }

}
