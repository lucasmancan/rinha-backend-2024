package app;

import io.jooby.Jooby;
import io.jooby.StatusCode;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ClienteRouter extends Jooby {

    {
        final var dataSource = require(DataSource.class);

        get("/health-check", (ctx) -> {

            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement("""
                                                       
                            update clientes set saldo = 0 where cliente_id > -1;
                            delete from transacoes where 1=1; 
                                                        
                            """,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                var result = stmt.executeUpdate();

                return "OK";
            }
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
                             limit 10
                                                        
                            """,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    stmt.setInt(1, idCliente);

                    var result = stmt.executeQuery();

                    if (!result.next()) {
                        return ctx.send(StatusCode.UNPROCESSABLE_ENTITY);
                    }

                    ArrayList<HistoricoTransacao> listaTransacoes = new ArrayList<>();
                    while (result.next()) {
                        listaTransacoes.add(new HistoricoTransacao(
                                result.getString("tipo"),
                                result.getLong("valor"),
                                result.getString("descricao"),
                                result.getDate("realizada_em")
                        ));
                    }

                    result.first();
                    return new Extrato(new Saldo(result.getLong("saldo"), LocalDateTime.now(), result.getLong("limite")), listaTransacoes);
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
                                and (( ? > 0 ) or (saldo + ?) >= (limite * -1))
                                returning saldo, limite
                                                        
                            """, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    stmt.setLong(1, request.valorToLong());
                    stmt.setInt(2, idCliente);
                    stmt.setLong(3, request.valorToLong());
                    stmt.setLong(4, request.valorToLong());

                    var result = stmt.executeQuery();

                    if (!result.first()) {
                        return ctx.send(StatusCode.UNPROCESSABLE_ENTITY);
                    }

                    PreparedStatement stmtInsert = connection.prepareStatement("""
                                                        
                            insert into transacoes (cliente_id, valor, descricao, tipo) values (?,?,?,?);
                            """);

                    stmtInsert.setInt(1, ctx.path("id").intValue());
                    stmtInsert.setLong(2, request.valorAbsoluto());
                    stmtInsert.setString(3, request.descricao);
                    stmtInsert.setString(4, request.tipo);
                    stmtInsert.executeUpdate();

                    return new SaldoTransacao(result.getLong("limite"), result.getLong("saldo"));
                }
            }));
        });

    }

    public record TransacaoRequest(BigDecimal valor, String tipo, String descricao) {

        public boolean valido() {
            if (valor.compareTo(BigDecimal.ZERO) == 0 || valor.scale() != 0)
                return false;

            if (!Objects.equals(tipo, "d") && !Objects.equals(tipo, "c")) {
                return false;
            }

            return descricao != null && (descricao.length() <= 10 && descricao.length() >= 1);
        }

        public long valorAbsoluto() {
            return this.valor.longValue();
        }

        public long valorToLong() {
            if (tipo.equals("d")) {
                return valor.longValue() * -1;
            }
            return valor.longValue();
        }
    }
}
