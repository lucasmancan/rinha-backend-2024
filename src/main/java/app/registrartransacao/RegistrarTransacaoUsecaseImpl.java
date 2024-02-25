package app.registrartransacao;

import app.ClienteNaoEncontradoException;
import app.LimiteInsuficienteException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jooq.impl.DSL.*;

@Singleton
public class RegistrarTransacaoUsecaseImpl implements RegistrarTransacaoUsecase {
    private final DSLContext context;

    public static final Field<Long> campoSaldoCliente = field("saldo", Long.class);
    public static final Field<Long> campoLimiteCliente = field("limite", Long.class);
    public static final Field<Long> campoValorTransacao = field("valor", Long.class);
    public static final Field<String> campoTipoTransacao = field("tipo", String.class);
    public static final Field<String> campoDescricaoTransacao = field("descricao", String.class);
    public static final Field<LocalDateTime> campoDataTransacao = field("realizada_em", LocalDateTime.class);
    public static final Field<Integer> campoIdCliente = field("cliente_id", Integer.class);

   // ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Inject
    public RegistrarTransacaoUsecaseImpl(DataSource dataSource) {
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Override
    public SaldoTransacao registrar(Transacao transacao) {

        var clienteExiste = context.fetchExists(table("clientes")
                .where(campoIdCliente.eq(transacao.idCliente())));

        if (!clienteExiste) {
            throw new ClienteNaoEncontradoException();
        }

        var saldoClienteAtualizado = context.update(table("clientes"))
                .set(campoSaldoCliente, campoSaldoCliente.plus(transacao.valor()))
                .where(campoIdCliente.eq(transacao.idCliente()))
                .and(condition(transacao.valor() > 0).or(field("saldo").plus(transacao.valor()).gt(field("limite").multiply(-1))))
                .returningResult(campoSaldoCliente, campoLimiteCliente)
                .fetch();

        if ( saldoClienteAtualizado.isEmpty()) {
            throw new LimiteInsuficienteException();
        }

        context.insertInto(table("transacoes"),
                        campoIdCliente,
                        campoDataTransacao,
                        campoValorTransacao,
                        campoTipoTransacao,
                        campoDescricaoTransacao)
                .values(transacao.idCliente(), LocalDateTime.now(), transacao.valorAbsoluto(), transacao.tipo(), transacao.descricao())
                .execute();

        return new SaldoTransacao(saldoClienteAtualizado.get(0).get(campoLimiteCliente),
                saldoClienteAtualizado.get(0).get(campoSaldoCliente));
    }
}
