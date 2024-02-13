package app;

import io.jooby.Jooby;
import io.jooby.StatusCode;

import javax.sql.DataSource;
import java.util.Objects;

record TransacaoRequest(long valor, String tipo, String descricao) {

    public boolean valido() {
        if (valor <= 0)
            return false;

        if (!Objects.equals(tipo, "d") && !Objects.equals(tipo, "c")) {
            return false;
        }

        if (descricao == null || (descricao.length() > 10 || descricao.length() < 1)) {
            return false;
        }

        return true;
    }
}

public class ClienteRouter extends Jooby {
    {

        RegistrarTransacaoUsecase registrarTransacaoUsecase = new RegistrarTransacaoUsecaseImpl(require(DataSource.class));


        get("/health-check", (ctx) -> "OK");

        path("/clientes", () -> {
            get("/{id}/extrato", (ctx -> {

                return ctx.send(StatusCode.OK);
            }));

            post("/{id}", (ctx -> {

                try {
                    var request = ctx.body(TransacaoRequest.class);

                    if (!request.valido()) {
                        return ctx.send(StatusCode.BAD_REQUEST);
                    }

                    return registrarTransacaoUsecase.registrar(new Transacao(
                            ctx.path("id").intValue(),
                            request.valor(),
                            TipoTransacao.toEnum(request.tipo()),
                            request.descricao()
                    ));
                } catch (RegistrarTransacaoUsecaseImpl.ClienteNaoEncontradoException e) {
                    return ctx.send(StatusCode.NOT_FOUND);
                } catch (RegistrarTransacaoUsecaseImpl.LimiteInsulficienteException limiteInsulficienteException) {
                    return ctx.send(StatusCode.UNPROCESSABLE_ENTITY);
                }  catch (Exception ex) {
                    ex.printStackTrace();
                    return ctx.send(StatusCode.SERVER_ERROR);
                }
            }));
        });

    }
}
