package controle.financeiro.services;

import controle.financeiro.dtos.CategoriaRequestDTO;
import controle.financeiro.dtos.CategoriaResponseDTO;
import controle.financeiro.models.Categoria;
import controle.financeiro.models.TipoTransacao;
import controle.financeiro.models.Usuario;
import controle.financeiro.repositories.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static controle.financeiro.models.TipoTransacao.DESPESA;
import static controle.financeiro.models.TipoTransacao.RECEITA;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private record Padrao(String nome, TipoTransacao tipo, String cor) {}

    private static final List<Padrao> PADROES = List.of(
            new Padrao("Alimentação", DESPESA, "#F97316"),
            new Padrao("Moradia", DESPESA, "#6366F1"),
            new Padrao("Transporte", DESPESA, "#0EA5E9"),
            new Padrao("Saúde", DESPESA, "#EF4444"),
            new Padrao("Educação", DESPESA, "#8B5CF6"),
            new Padrao("Lazer", DESPESA, "#EC4899"),
            new Padrao("Compras", DESPESA, "#F59E0B"),
            new Padrao("Contas e assinaturas", DESPESA, "#14B8A6"),
            new Padrao("Outras despesas", DESPESA, "#64748B"),
            new Padrao("Salário", RECEITA, "#16A34A"),
            new Padrao("Rendimentos", RECEITA, "#22C55E"),
            new Padrao("Outras receitas", RECEITA, "#84CC16")
    );

    private final CategoriaRepository repository;
    private final UsuarioLogado usuarioLogado;

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar(TipoTransacao tipo, boolean incluirArquivadas) {
        UUID usuarioId = usuarioLogado.obter().getId();
        return repository.findByUsuarioIdOrderByTipoAscNomeAsc(usuarioId).stream()
                .filter(c -> incluirArquivadas || c.isAtiva())
                .filter(c -> tipo == null || c.getTipo() == tipo)
                .map(CategoriaResponseDTO::de)
                .toList();
    }

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        Usuario usuario = usuarioLogado.obter();
        validarNomeUnico(usuario.getId(), dto.tipo(), dto.nome().trim());

        Categoria categoria = new Categoria();
        categoria.setUsuario(usuario);
        categoria.setTipo(dto.tipo());
        categoria.setNome(dto.nome().trim());
        categoria.setCor(dto.cor());
        return CategoriaResponseDTO.de(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponseDTO atualizar(UUID id, CategoriaRequestDTO dto) {
        Categoria categoria = buscar(id);
        String novoNome = dto.nome().trim();

        if (categoria.getTipo() != dto.tipo()) {
            throw erro("Não é possível mudar o tipo de uma categoria");
        }
        if (!categoria.getNome().equalsIgnoreCase(novoNome)) {
            validarNomeUnico(categoria.getUsuario().getId(), categoria.getTipo(), novoNome);
        }
        categoria.setNome(novoNome);
        categoria.setCor(dto.cor());
        return CategoriaResponseDTO.de(categoria);
    }

    @Transactional
    public void arquivar(UUID id) {
        buscar(id).setAtiva(false);
    }

    @Transactional
    public void criarPadroes(Usuario usuario) {
        List<Categoria> categorias = PADROES.stream().map(p -> {
            Categoria c = new Categoria();
            c.setUsuario(usuario);
            c.setNome(p.nome());
            c.setTipo(p.tipo());
            c.setCor(p.cor());
            return c;
        }).toList();
        repository.saveAll(categorias);
    }

    private Categoria buscar(UUID id) {
        return repository.findByIdAndUsuarioId(id, usuarioLogado.obter().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    private void validarNomeUnico(UUID usuarioId, TipoTransacao tipo, String nome) {
        if (repository.existsByUsuarioIdAndTipoAndNomeIgnoreCaseAndAtivaTrue(usuarioId, tipo, nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome");
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}