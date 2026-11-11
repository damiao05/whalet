CREATE TABLE contas (
    id             UUID PRIMARY KEY,
    usuario_id     UUID NOT NULL REFERENCES usuarios(id),
    nome           VARCHAR(100) NOT NULL,
    tipo           VARCHAR(20)  NOT NULL,
    instituicao    VARCHAR(100),
    saldo_inicial  NUMERIC(15,2) NOT NULL DEFAULT 0,
    ativa          BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao   TIMESTAMP(6) NOT NULL DEFAULT now(),
    CONSTRAINT contas_tipo_chk
        CHECK (tipo IN ('CORRENTE', 'POUPANCA', 'DINHEIRO', 'INVESTIMENTO'))
);
CREATE INDEX idx_contas_usuario ON contas(usuario_id);

CREATE TABLE cartoes_credito (
    id                  UUID PRIMARY KEY,
    usuario_id          UUID NOT NULL REFERENCES usuarios(id),
    nome                VARCHAR(100) NOT NULL,
    bandeira            VARCHAR(30),
    limite              NUMERIC(15,2) NOT NULL,
    dia_fechamento      INTEGER NOT NULL,
    dia_vencimento      INTEGER NOT NULL,
    conta_pagamento_id  UUID REFERENCES contas(id),
    ativo               BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao        TIMESTAMP(6) NOT NULL DEFAULT now(),
    CONSTRAINT cartoes_limite_chk     CHECK (limite >= 0),
    CONSTRAINT cartoes_fechamento_chk CHECK (dia_fechamento BETWEEN 1 AND 31),
    CONSTRAINT cartoes_vencimento_chk CHECK (dia_vencimento BETWEEN 1 AND 31)
);
CREATE INDEX idx_cartoes_usuario ON cartoes_credito(usuario_id);