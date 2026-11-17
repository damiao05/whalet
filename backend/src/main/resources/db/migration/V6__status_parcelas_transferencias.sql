ALTER TABLE transacoes
    ADD COLUMN status             VARCHAR(20) NOT NULL DEFAULT 'EFETIVADA',
    ADD COLUMN conta_destino_id   UUID REFERENCES contas(id),
    ADD COLUMN fatura_mes         DATE,
    ADD COLUMN grupo_parcelas_id  UUID,
    ADD COLUMN parcela_numero     INTEGER,
    ADD COLUMN parcela_total      INTEGER;

ALTER TABLE transacoes DROP CONSTRAINT transacoes_tipo_chk;
ALTER TABLE transacoes DROP CONSTRAINT transacoes_origem_chk;

ALTER TABLE transacoes
    ADD CONSTRAINT transacoes_tipo_chk
        CHECK (tipo IN ('RECEITA', 'DESPESA', 'TRANSFERENCIA', 'PAGAMENTO_FATURA')),
    ADD CONSTRAINT transacoes_status_chk
        CHECK (status IN ('PENDENTE', 'EFETIVADA')),

    ADD CONSTRAINT transacoes_origem_chk CHECK (
        (tipo IN ('RECEITA', 'DESPESA')
            AND num_nonnulls(conta_id, cartao_id) = 1
            AND conta_destino_id IS NULL AND fatura_mes IS NULL)
        OR
        (tipo = 'TRANSFERENCIA'
            AND conta_id IS NOT NULL AND conta_destino_id IS NOT NULL
            AND conta_id <> conta_destino_id
            AND cartao_id IS NULL AND fatura_mes IS NULL)
        OR
        (tipo = 'PAGAMENTO_FATURA'
            AND conta_id IS NOT NULL AND cartao_id IS NOT NULL
            AND fatura_mes IS NOT NULL AND conta_destino_id IS NULL)
    ),

    ADD CONSTRAINT transacoes_parcela_chk CHECK (
        (parcela_numero IS NULL AND parcela_total IS NULL AND grupo_parcelas_id IS NULL)
        OR
        (grupo_parcelas_id IS NOT NULL AND parcela_total BETWEEN 2 AND 360
            AND parcela_numero BETWEEN 1 AND parcela_total)
    );

CREATE INDEX idx_transacoes_grupo   ON transacoes(grupo_parcelas_id) WHERE grupo_parcelas_id IS NOT NULL;
CREATE INDEX idx_transacoes_destino ON transacoes(conta_destino_id)  WHERE conta_destino_id IS NOT NULL;
CREATE INDEX idx_transacoes_fatura  ON transacoes(cartao_id, fatura_mes) WHERE tipo = 'PAGAMENTO_FATURA';