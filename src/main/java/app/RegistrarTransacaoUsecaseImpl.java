package app;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Singleton
public class RegistrarTransacaoUsecaseImpl implements RegistrarTransacaoUsecase {
    private final DSLContext context;

    @Inject
    public RegistrarTransacaoUsecaseImpl(DataSource dataSource) {
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Override
    public SaldoTransacao registrar(Transacao transacao) {

        boolean atualizado = false;
        SaldoTransacao saldoTransacao = null;

        while (!atualizado) {
            Cliente cliente = buscarClientePorId(transacao.idCliente());

            long saldoClienteAtualizado = cliente.saldo - transacao.valor();

            if (TipoTransacao.DEBITO == transacao.tipo()) {
                if (cliente.limite < (saldoClienteAtualizado*-1)) {
                    throw new LimiteInsulficienteException();
                }
            }

            boolean clienteAtualizado = context.update(table("clientes"))
                    .set(field("saldo"), saldoClienteAtualizado)
                    .set(field("versao"), cliente.versao + 1)
                    .where(field("cliente_id").eq(transacao.idCliente()))
                    .and(field("versao").eq(cliente.versao()))
                    .execute() > 0;

            if (!clienteAtualizado)
                continue;

            context.insertInto(table("transacoes"), field("cliente_id"), field("realizada_em"), field("valor"), field("tipo"), field("descricao"))
                    .values(transacao.idCliente(), LocalDateTime.now(), transacao.valor(), transacao.tipo().codigo, transacao.descricao())
                    .execute();

            atualizado = true;

            saldoTransacao = new SaldoTransacao(cliente.limite(), saldoClienteAtualizado);
        }

        return saldoTransacao;
    }


    private Cliente buscarClientePorId(int idCliente) {
        return Optional.ofNullable(context.fetchOne(table("clientes"), field("cliente_id").eq(idCliente)))
                .map(this::recordToCliente).orElseThrow(ClienteNaoEncontradoException::new);
    }

    private Cliente recordToCliente(Record record1) {
        return new Cliente(record1.get("cliente_id", Integer.class),
                record1.get("saldo", Long.class),
                record1.get("limite", Long.class),
                record1.get("versao", Integer.class)
        );
    }

    static class LimiteInsulficienteException extends RuntimeException {
    }

    static class ClienteNaoEncontradoException extends RuntimeException {
    }

    record Cliente(int idCliente, long saldo, long limite, int versao) {
    }
}
