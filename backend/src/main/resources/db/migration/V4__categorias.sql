CREATE TABLE categorias (
    id            UUID PRIMARY KEY,
    usuario_id    UUID NOT NULL REFERENCES usuarios(id),
    nome          VARCHAR(60) NOT NULL,
    tipo          VARCHAR(20) NOT NULL,
    cor           VARCHAR(7),
    ativa         BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao  TIMESTAMP(6) NOT NULL DEFAULT now(),
    CONSTRAINT categorias_tipo_chk CHECK (tipo IN ('RECEITA', 'DESPESA')),
    CONSTRAINT categorias_cor_chk  CHECK (cor IS NULL OR cor ~ '^#[0-9A-Fa-f]{6}$')
);

CREATE UNIQUE INDEX uq_categorias_usuario_tipo_nome
    ON categorias (usuario_id, tipo, lower(nome)) WHERE ativa;
CREATE INDEX idx_categorias_usuario ON categorias(usuario_id);

ALTER TABLE transacoes ADD COLUMN categoria_id UUID REFERENCES categorias(id);
CREATE INDEX idx_transacoes_categoria ON transacoes(categoria_id);

INSERT INTO categorias (id, usuario_id, nome, tipo, cor)
SELECT gen_random_uuid(), u.id, c.nome, c.tipo, c.cor
FROM usuarios u
CROSS JOIN (VALUES
    ('Alimentação',            'DESPESA', '#F97316'),
    ('Moradia',                'DESPESA', '#6366F1'),
    ('Transporte',             'DESPESA', '#0EA5E9'),
    ('Saúde',                  'DESPESA', '#EF4444'),
    ('Educação',               'DESPESA', '#8B5CF6'),
    ('Lazer',                  'DESPESA', '#EC4899'),
    ('Compras',                'DESPESA', '#F59E0B'),
    ('Contas e assinaturas',   'DESPESA', '#14B8A6'),
    ('Outras despesas',        'DESPESA', '#64748B'),
    ('Salário',                'RECEITA', '#16A34A'),
    ('Rendimentos',            'RECEITA', '#22C55E'),
    ('Outras receitas',        'RECEITA', '#84CC16')
) AS c(nome, tipo, cor);