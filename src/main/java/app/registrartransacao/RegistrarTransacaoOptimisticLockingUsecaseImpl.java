package app.registrartransacao;

import app.DadosSaldoCliente;
import app.ClienteNaoEncontradoException;
import app.LimiteInsuficienteException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.time.LocalDateTime;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Singleton
@Deprecated
public class RegistrarTransacaoOptimisticLockingUsecaseImpl implements RegistrarTransacaoUsecase {
    private final DSLContext context;

    private final String DEBITO = "d";

    @Inject
    public RegistrarTransacaoOptimisticLockingUsecaseImpl(DataSource dataSource) {
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Override
    public SaldoTransacao registrar(Transacao transacao) {

        boolean atualizado = false;
        SaldoTransacao saldoTransacao = null;

        while (!atualizado) {
            DadosSaldoCliente cliente = buscarSaldoClientePorId(transacao.idCliente());

            long saldoClientePosTransacao;

            if (DEBITO.equals(transacao.tipo())) {
                saldoClientePosTransacao = cliente.saldo() - transacao.valor();

                if (cliente.limite() < (saldoClientePosTransacao * -1)) {
                    throw new LimiteInsuficienteException();
                }
            } else {
                saldoClientePosTransacao = cliente.saldo() + transacao.valor();
            }

            boolean clienteAtualizado = context.update(table("clientes"))
                    .set(field("saldo"), saldoClientePosTransacao)
                    .set(field("versao"), cliente.versao() + 1)
                    .where(field("cliente_id").eq(transacao.idCliente()))
                    .and(field("versao").eq(cliente.versao()))
                    .execute() > 0;

            if (!clienteAtualizado)
                continue;

            context.insertInto(table("transacoes"), field("cliente_id"),
                            field("realizada_em"), field("valor"), field("tipo"), field("descricao"))
                    .values(transacao.idCliente(), LocalDateTime.now(), transacao.valor(), transacao.tipo(), transacao.descricao())
                    .execute();

            atualizado = true;

            saldoTransacao = new SaldoTransacao(cliente.limite(), saldoClientePosTransacao);
        }

        return saldoTransacao;
    }


    private DadosSaldoCliente buscarSaldoClientePorId(int idCliente) {

        var result = context.selectFrom("clientes", field("saldo"), field("limite"), field("versao"))
                .where(field("cliente_id").eq(idCliente)).forUpdate()
                .fetch();

        if (result.isEmpty()) {
            throw new ClienteNaoEncontradoException();
        }

        return result.get(0).map(this::recordToCliente);
    }

    private DadosSaldoCliente recordToCliente(Record record1) {
        return new DadosSaldoCliente(record1.get("saldo", Long.class),
                record1.get("limite", Long.class),
                record1.get("versao", Integer.class)
        );
    }


}
