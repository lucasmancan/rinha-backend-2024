SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

SET default_tablespace = '';

SET default_table_access_method = heap;

-- Nova rinha
DROP TABLE IF EXISTS public.clientes;

CREATE TABLE public.clientes (
    cliente_id serial not null,
    nome varchar(32) not null,
    data_criacao timestamp not null default current_timestamp,
    limite bigint not null,
    saldo bigint not null default 0,
    versao bigint not null default 0,
    primary key (cliente_id)
);

CREATE TABLE public.transacoes (
    id serial not null,
    cliente_id int not null,
    realizada_em timestamp not null,
    valor bigint not null,
    tipo char not null,
    descricao varchar(10) null,
    primary key (id)
);

  INSERT INTO public.clientes (nome, limite)
  VALUES
    ('o barato sai caro', 1000 * 100),
    ('zan corp ltda', 800 * 100),
    ('les cruders', 10000 * 100),
    ('padaria joia de cocaia', 100000 * 100),
    ('kid mais', 5000 * 100);
