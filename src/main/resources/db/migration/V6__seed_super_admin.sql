-- Super admin de bootstrap: é ele quem cadastra os produtores, e sem ele não
-- há como entrar no sistema recém-instalado.
--
-- Antes ficava só no volume Docker local, inserido à mão, e sumia a cada
-- recriação do banco. Agora nasce com a migration.
--
-- Hash bcrypt de 'admin123' gerado com io.quarkus.elytron.security.common.BcryptUtil
-- (formato $2a$ — o parser do Elytron não aceita o $2b$ de outras ferramentas).
-- TROCAR ESTA SENHA antes de qualquer ambiente que não seja o local.
insert into usuario (id, nome, email, senha, ativo, admin)
values (
    '01890000-0000-7000-8000-000000000002',
    'Super Admin',
    'admin@agro.com',
    '$2a$10$Nsp3tbdPdcGY9AunKdMcI.f8sXZ6hLGX12.DEOMCzxcRo5aVbRANG',
    true,
    true
);
