CREATE TABLE usuarios (
    id             UUID PRIMARY KEY,
    primeiro_nome  VARCHAR(255) NOT NULL,
    sobrenome      VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    senha          VARCHAR(255),
    auth_provider  VARCHAR(255) NOT NULL,
    provider_id    VARCHAR(255),
    data_criacao   TIMESTAMP(6)
);

CREATE TABLE transacoes (
    id            UUID PRIMARY KEY,
    descricao     VARCHAR(255) NOT NULL,
    valor         NUMERIC(15,2) NOT NULL,
    tipo          VARCHAR(255) NOT NULL,
    metadados     JSONB,
    data_criacao  TIMESTAMP(6)
);