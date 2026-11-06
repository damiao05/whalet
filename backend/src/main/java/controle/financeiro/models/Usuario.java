package controle.financeiro.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String primeiro_nome;

    @Column(nullable = false)
    private String sobrenome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String senha;

    @Column(name = "auth_provider", nullable = false)
    private String auth_provider;

    @Column(name = "provider_id", nullable = true)
    private String provider_id;

    @Column(name = "data_criacao")
    private LocalDateTime data_criacao = LocalDateTime.now();
}
