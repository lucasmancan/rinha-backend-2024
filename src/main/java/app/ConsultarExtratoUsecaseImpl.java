package app;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

@Singleton
public class ConsultarExtratoUsecaseImpl implements ConsultarExtratoUsecase {
    private final DSLContext context;

    @Inject
    public ConsultarExtratoUsecaseImpl(DataSource dataSource) {
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    //

    select saldo, limite, t.valor, t.tipo, t.descricao, t.realizada_em from clientes c
            join transacoes t on t.cliente_d = c.cliente_id
            where c.cliente_id = $1

    //

    @Override
    public Extrato gerarExtratoPorIdCliente(int idCliente) {


        return null;
    }
}
