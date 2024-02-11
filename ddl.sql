SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

SET default_tablespace = '';

SET default_table_access_method = heap;

DROP TABLE IF EXISTS public.pessoas;

CREATE TABLE public.pessoas (
    id uuid not null,
    apelido varchar(32) unique not null,
    nascimento date not null,
    nome varchar(100) not null,
    stack varchar(255),
    primary key (id)
);




-- Nova rinha
DROP TABLE IF EXISTS public.clientes;

CREATE TABLE public.clientes (
    cliente_id int not null,
    nome varchar(32) not null,
    data_criacao timestamp not null,
    limite bigint not null,
    saldo bigint not null,
    primary key (id)
);

CREATE TABLE public.transacoes (
    id int not null,
    cliente_id int not null,
    realizada_em timestamp not null,
    valor bigint not null,
    tipo char not null,
    primary key (id)
);

DO $$
BEGIN
  INSERT INTO clientes (nome, limite)
  VALUES
    ('o barato sai caro', 1000 * 100),
    ('zan corp ltda', 800 * 100),
    ('les cruders', 10000 * 100),
    ('padaria joia de cocaia', 100000 * 100),
    ('kid mais', 5000 * 100);
END; $$