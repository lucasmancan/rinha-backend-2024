package app;

import io.jooby.Jooby;
import io.jooby.StatusCode;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ClienteRouter extends Jooby {

    {
        final var dataSource = require(DataSource.class);

        get("/health-check", (ctx) -> {
            return "OK";
        });

        path("/clientes", () -> {

            get("/{id}/extrato", (ctx -> {

                var idCliente = ctx.path("id").intValue();

                if (idCliente < 0 || idCliente > 5) {
                    return ctx.send(StatusCode.NOT_FOUND);
                }

                try (Connection connection = dataSource.getConnection()) {

                    PreparedStatement stmt = connection.prepareStatement("""
                                                        
                            select cliente.saldo,
                             cliente.limite,
                             transacao.valor,
                             transacao.tipo,
                             transacao.descricao,
                             transacao.realizada_em
                             from clientes cliente
                             left join transacoes transacao on cliente.cliente_id = transacao.cliente_id
                             where cliente.cliente_id = ?
                             order by transacao.realizada_em desc
                             limit 10;
                                                  
                            """);

                    stmt.setInt(1, idCliente);

                    var result = stmt.executeQuery();

                    ArrayList<HistoricoTransacao> listaTransacoes = new ArrayList<>();

                    long saldoCliente = 0;
                    long limiteCliente = 0;

                    while (result.next()) {

                        if (result.isFirst()) {
                            saldoCliente = result.getLong("saldo");
                            limiteCliente = result.getLong("limite");
                        }

                        listaTransacoes.add(new HistoricoTransacao(
                                result.getString("tipo"),
                                result.getLong("valor"),
                                result.getString("descricao"),
                                result.getTimestamp("realizada_em")
                        ));
                    }

                    return new Extrato(new Saldo(saldoCliente,
                            LocalDateTime.now(),
                            limiteCliente),
                            listaTransacoes);
                }
            }));

            post("/{id}/transacoes", (ctx -> {

                var idCliente = ctx.path("id").intValue();

                if (idCliente < 0 || idCliente > 5) {
                    return ctx.send(StatusCode.NOT_FOUND);
                }

                var request = ctx.body(TransacaoRequest.class);

                if (!request.valido()) {
                    return ctx.send(StatusCode.UNPROCESSABLE_ENTITY);
                }

                try (Connection connection = dataSource.getConnection()) {
                    PreparedStatement stmt = connection.prepareStatement("""
                                                        
                            update clientes set saldo = saldo + ?
                                where cliente_id = ?
                                and (( ? > 0 ) or (saldo + ?) > (limite * -1))
                                returning saldo, limite
                                                        
                            """);

                    stmt.setLong(1, request.valorToLong());
                    stmt.setInt(2, idCliente);
                    stmt.setLong(3, request.valorToLong());
                    stmt.setLong(4, request.valorToLong());

                    var result = stmt.executeQuery();

                    if (!result.isBeforeFirst()) {
                        return ctx.send(StatusCode.UNPROCESSABLE_ENTITY);
                    }

                    result.next();

                    PreparedStatement stmtInsert = connection.prepareStatement("""             
                            insert into transacoes (cliente_id, valor, descricao, tipo, realizada_em) values (?,?,?,?,?);
                            """);

                    stmtInsert.setInt(1, ctx.path("id").intValue());
                    stmtInsert.setLong(2, request.valorAbsoluto());
                    stmtInsert.setString(3, request.descricao());
                    stmtInsert.setString(4, request.tipo());
                    stmtInsert.setTimestamp(5, Timestamp.from(Instant.now()));
                    stmtInsert.executeUpdate();
                    stmtInsert.close();

                    return new SaldoTransacao(result.getLong("limite"), result.getLong("saldo"));
                }
            }));
        });

    }


}
