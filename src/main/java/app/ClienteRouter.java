package app;

import app.consultarextrato.ConsultarExtratoUsecaseImpl;
import app.registrartransacao.RegistrarTransacaoOptimisticLockingUsecaseImpl;
import app.registrartransacao.RegistrarTransacaoUsecaseImpl;
import app.registrartransacao.Transacao;
import io.jooby.Jooby;
import io.jooby.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Objects;

public class ClienteRouter extends Jooby {

    private final Logger logger = LoggerFactory.getLogger(ClienteRouter.class);

    {

        final var registrarTransacaoUsecase = new RegistrarTransacaoUsecaseImpl(require(DataSource.class));
        final var consultarExtratoUsecase = new ConsultarExtratoUsecaseImpl(require(DataSource.class));


        get("/health-check", (ctx) -> "OK");

        path("/clientes", () -> {
            get("/{id}/extrato", (ctx -> {

                try {
                    return consultarExtratoUsecase.gerarExtratoPorIdCliente(ctx.path("id").intValue());
                } catch (ClienteNaoEncontradoException e) {
                    return ctx.send(StatusCode.NOT_FOUND);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    return ctx.send(StatusCode.SERVER_ERROR);
                }
            }));

            post("/{id}/transacoes", (ctx -> {

                try {
                    var request = ctx.body(TransacaoRequest.class);

                    if (!request.valido()) {
                        return ctx.send(StatusCode.UNPROCESSABLE_ENTITY);
                    }

                    return registrarTransacaoUsecase.registrar(new Transacao(
                            ctx.path("id").intValue(),
                            request.valor().longValue(),
                            request.tipo(),
                            request.descricao()
                    ));
                } catch (ClienteNaoEncontradoException e) {
                    return ctx.send(StatusCode.NOT_FOUND);
                } catch (LimiteInsuficienteException limiteInsulficienteException) {
                    return ctx.send(StatusCode.UNPROCESSABLE_ENTITY);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    return ctx.send(StatusCode.SERVER_ERROR);
                }
            }));
        });

    }

    public record TransacaoRequest(BigDecimal valor, String tipo, String descricao) {

        public boolean valido() {
            if (valor.compareTo(BigDecimal.ZERO) ==0 || valor.scale() != 0)
                return false;

            if (!Objects.equals(tipo, "d") && !Objects.equals(tipo, "c")) {
                return false;
            }

            return descricao != null && (descricao.length() <= 10 && descricao.length() >= 1);
        }
    }
}
