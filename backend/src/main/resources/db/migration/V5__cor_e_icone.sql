ALTER TABLE contas
    ADD COLUMN cor  VARCHAR(7),
    ADD COLUMN icon VARCHAR(30),
    ADD CONSTRAINT contas_cor_chk CHECK (cor IS NULL OR cor ~ '^#[0-9A-Fa-f]{6}$');

ALTER TABLE cartoes_credito
    ADD COLUMN cor  VARCHAR(7),
    ADD COLUMN icon VARCHAR(30),
    ADD CONSTRAINT cartoes_cor_chk CHECK (cor IS NULL OR cor ~ '^#[0-9A-Fa-f]{6}$');