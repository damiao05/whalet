CREATE TABLE recorrencias (
    id                UUID PRIMARY KEY,
    usuario_id        UUID NOT NULL REFERENCES usuarios(id),
    descricao         VARCHAR(255) NOT NULL,
    valor             NUMERIC(15,2) NOT NULL,
    tipo              VARCHAR(20) NOT NULL,
    conta_id          UUID REFERENCES contas(id),
    conta_destino_id  UUID REFERENCES contas(id),
    cartao_id         UUID REFERENCES cartoes_credito(id),
    categoria_id      UUID REFERENCES categorias(id),
    frequencia        VARCHAR(20) NOT NULL,
    intervalo         INTEGER NOT NULL DEFAULT 1,
    data_inicio       DATE NOT NULL,
    data_fim          DATE,
    gerado_ate        DATE,
    ativa             BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao      TIMESTAMP(6) NOT NULL DEFAULT now(),
    CONSTRAINT recorrencias_valor_chk     CHECK (valor > 0),
    CONSTRAINT recorrencias_tipo_chk      CHECK (tipo IN ('RECEITA', 'DESPESA', 'TRANSFERENCIA')),
    CONSTRAINT recorrencias_freq_chk      CHECK (frequencia IN ('SEMANAL', 'MENSAL', 'ANUAL')),
    CONSTRAINT recorrencias_intervalo_chk CHECK (intervalo BETWEEN 1 AND 12),
    CONSTRAINT recorrencias_fim_chk       CHECK (data_fim IS NULL OR data_fim >= data_inicio),
    CONSTRAINT recorrencias_origem_chk CHECK (
        (tipo IN ('RECEITA', 'DESPESA')
            AND num_nonnulls(conta_id, cartao_id) = 1 AND conta_destino_id IS NULL)
        OR
        (tipo = 'TRANSFERENCIA'
            AND conta_id IS NOT NULL AND conta_destino_id IS NOT NULL AND conta_id <> conta_destino_id
            AND cartao_id IS NULL AND categoria_id IS NULL)
    )
);
CREATE INDEX idx_recorrencias_usuario ON recorrencias(usuario_id) WHERE ativa;

ALTER TABLE transacoes ADD COLUMN recorrencia_id UUID REFERENCES recorrencias(id);
CREATE INDEX idx_transacoes_recorrencia ON transacoes(recorrencia_id) WHERE recorrencia_id IS NOT NULL;