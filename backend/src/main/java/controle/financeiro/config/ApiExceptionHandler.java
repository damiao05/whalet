package controle.financeiro.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * Em produção o Spring esconde as mensagens de erro. Aqui devolvemos sempre {"message": "..."} com um texto
 * seguro, que o Angular exibe ao usuário. Erros inesperados continuam caindo no tratamento padrão (500 genérico).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record Erro(String message) {}

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Erro> aoLancarStatus(ResponseStatusException e) {
        String mensagem = e.getReason() != null ? e.getReason() : "Não foi possível concluir a operação";
        return ResponseEntity.status(e.getStatusCode()).body(new Erro(mensagem));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> aoValidar(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Dados inválidos");
        return ResponseEntity.badRequest().body(new Erro(mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> aoLerCorpo(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new Erro("Requisição inválida. Confira os campos enviados."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Erro> aoNegarAcesso(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Erro("Sessão inválida. Entre novamente."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> aoViolarIntegridade(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Erro("Operação não permitida pelas regras de integridade dos dados."));
    }
}
