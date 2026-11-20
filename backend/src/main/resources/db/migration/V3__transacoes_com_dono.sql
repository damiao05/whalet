DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM transacoes) THEN
        RAISE EXCEPTION 'Existem transacoes sem dono. Apague-as (dados de teste) antes de aplicar a V3.';
    END IF;
END $$;

ALTER TABLE transacoes
    ADD COLUMN usuario_id      UUID NOT NULL REFERENCES usuarios(id),
    ADD COLUMN conta_id        UUID REFERENCES contas(id),
    ADD COLUMN cartao_id       UUID REFERENCES cartoes_credito(id),
    ADD COLUMN data_transacao  DATE NOT NULL;

ALTER TABLE transacoes
    ADD CONSTRAINT transacoes_origem_chk CHECK (num_nonnulls(conta_id, cartao_id) = 1),
    ADD CONSTRAINT transacoes_tipo_chk   CHECK (tipo IN ('RECEITA', 'DESPESA')),
    ADD CONSTRAINT transacoes_valor_chk  CHECK (valor > 0);

CREATE INDEX idx_transacoes_usuario_data ON transacoes(usuario_id, data_transacao DESC);
CREATE INDEX idx_transacoes_conta        ON transacoes(conta_id);
CREATE INDEX idx_transacoes_cartao       ON transacoes(cartao_id);