package controle.financeiro.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteção básica contra tentativas em massa de login/cadastro: no máximo 15 por minuto por IP.
 * Fica na memória (serve para uma instância só). Contra ataques sérios, o ideal é um filtro na frente
 * (por exemplo, as regras da Cloudflare).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TENTATIVAS = 15;
    private static final long JANELA_MS = 60_000;
    private static final Set<String> CAMINHOS = Set.of("/api/auth/login", "/api/auth/registro", "/api/auth/google");

    private record Janela(long inicio, int tentativas) {}

    private final Map<String, Janela> porIp = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !CAMINHOS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        long agora = System.currentTimeMillis();
        if (porIp.size() > 10_000) {
            porIp.clear(); // evita crescer sem limite
        }

        Janela janela = porIp.merge(ipDe(request), new Janela(agora, 1), (atual, nova) ->
                agora - atual.inicio() > JANELA_MS ? nova : new Janela(atual.inicio(), atual.tentativas() + 1));

        if (janela.tentativas() > MAX_TENTATIVAS) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Muitas tentativas. Aguarde um minuto e tente de novo.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    /** Atrás de proxy (Render/Cloudflare) o IP real vem no cabeçalho X-Forwarded-For. */
    private String ipDe(HttpServletRequest request) {
        String encaminhado = request.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            return encaminhado.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
