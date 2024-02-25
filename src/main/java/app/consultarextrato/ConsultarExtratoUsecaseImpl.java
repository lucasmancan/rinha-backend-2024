package app.consultarextrato;

import app.ClienteNaoEncontradoException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.jooq.impl.DSL.field;

@Singleton
public class ConsultarExtratoUsecaseImpl implements ConsultarExtratoUsecase {
    private final DSLContext context;

    public static final Field<Long> campoSaldoCliente = field("clientes.saldo", Long.class);
    public static final Field<Long> campoLimiteCliente = field("clientes.limite", Long.class);
    public static final Field<Long> campoValorTransacao = field("transacoes.valor", Long.class);
    public static final Field<String> campoTipoTransacao = field("transacoes.tipo", String.class);
    public static final Field<String> campoDescricaoTransacao = field("transacoes.descricao", String.class);
    public static final Field<LocalDateTime> campoDataTransacao = field("transacoes.realizada_em", LocalDateTime.class);
    public static final Field<Integer> campoIdCliente = field("clientes.cliente_id", Integer.class);

    @Inject
    public ConsultarExtratoUsecaseImpl(DataSource dataSource) {
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Override
    public Extrato gerarExtratoPorIdCliente(int idCliente) {
        var result = context.select(campoSaldoCliente,
                        campoLimiteCliente,
                        campoValorTransacao,
                        campoTipoTransacao,
                        campoDescricaoTransacao,
                        campoDataTransacao)
                .from("clientes")
                .leftJoin("transacoes")
                .on(campoIdCliente.eq(field("transacoes.cliente_id", Integer.class)))
                .where(campoIdCliente.eq(idCliente))
                .orderBy(campoDataTransacao.desc())
                .limit(10)
                .fetch();

        if (result.isEmpty()) {
            throw new ClienteNaoEncontradoException();
        }

        ArrayList<HistoricoTransacao> listaTransacoes = new ArrayList<>(result.size());

        if (result.size() > 1) {
            result.forEach((record) -> {
                listaTransacoes.add(new HistoricoTransacao(record.get(campoTipoTransacao),
                        record.get(campoValorTransacao),
                        record.get(campoDescricaoTransacao),
                        record.get(campoDataTransacao)));
            });
        }

        return new Extrato(new Saldo(result.get(0).get(campoSaldoCliente),
                LocalDateTime.now(), result.get(0).get(campoLimiteCliente)),
                listaTransacoes);
    }
}
