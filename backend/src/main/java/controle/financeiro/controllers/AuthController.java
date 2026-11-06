package controle.financeiro.controllers;

import controle.financeiro.dtos.AuthResponseDTO;
import controle.financeiro.dtos.GoogleLoginRequestDTO;
import controle.financeiro.dtos.LoginRequestDTO;
import controle.financeiro.dtos.RegistroRequestDTO;
import controle.financeiro.models.Usuario;
import controle.financeiro.repositories.UsuarioRepository;
import controle.financeiro.services.GoogleTokenVerifier;
import controle.financeiro.services.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.email().trim().toLowerCase());

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(dto.senha(), usuarioOpt.get().getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }

        Usuario usuario = usuarioOpt.get();
        String token = jwtService.gerarToken(usuario.getEmail());

        return ResponseEntity.ok(new AuthResponseDTO(
                token,
                usuario.getPrimeiro_nome(),
                usuario.getEmail()
        ));
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@Valid @RequestBody GoogleLoginRequestDTO dto) {
        try {
            var payload = googleTokenVerifier.verificar(dto.idToken());

            if (payload == null || !Boolean.TRUE.equals(payload.getEmailVerified())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token do Google inválido");
            }

            String email = payload.getEmail();
            String nome = (String) payload.get("given_name");
            String sobrenome = (String) payload.get("family_name");
            String googleId = payload.getSubject();

            Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
                Usuario novo = new Usuario();
                novo.setEmail(email);
                novo.setPrimeiro_nome(nome != null ? nome : email);
                novo.setSobrenome(sobrenome != null ? sobrenome : "");
                novo.setAuth_provider("GOOGLE");
                novo.setProvider_id(googleId);
                return usuarioRepository.save(novo);
            });

            String token = jwtService.gerarToken(usuario.getEmail());
            return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getPrimeiro_nome(), usuario.getEmail()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falha ao validar token do Google");
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        String email = dto.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado");

        Usuario usuario = new Usuario();
        usuario.setPrimeiro_nome(dto.primeiroNome().trim());
        usuario.setSobrenome(dto.sobrenome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setAuth_provider("LOCAL");

        try {
            usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado");
        }

        String token = jwtService.gerarToken(usuario.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(
                token,
                usuario.getPrimeiro_nome(),
                usuario.getEmail())
        );
    }

}
