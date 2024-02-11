package app;

import io.jooby.Jooby;
import org.jooq.exception.IntegrityConstraintViolationException;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

public class PessoaRouter extends Jooby {
    {
        PessoaService service = new PessoaServiceJooqImpl(require(DataSource.class));

        get("/contagem-pessoas", (ctx) -> service.contarPessoas());

        path("/pessoas", () -> {
            get("/{id}", (ctx -> {


                var optionalPessoa = service.buscarPorId(UUID.fromString(ctx.path("id").value()));

                if (optionalPessoa.isEmpty()) {
                    ctx.setResponseCode(404);
                    return "";
                }

                return optionalPessoa.get();
            }));

            get("/", (ctx -> {

                var termo = ctx.query("t").value();

                if (termo == null || termo.isBlank()) {
                    ctx.setResponseCode(400);
                    return "";
                }

                return service.buscarPorTermo(termo);
            }));

            post("/", (ctx -> {
                Pessoa pessoaRequest = null;
                try{
                    pessoaRequest = ctx.body(Pessoa.class);

                    System.out.println();
                }catch (Exception e){
                    ctx.setResponseCode(400);
                    return "";
                }

                if (pessoaRequest.apelido() == null || pessoaRequest.apelido().isBlank() || pessoaRequest.apelido().length() > 32
                        || pessoaRequest.nome() == null || pessoaRequest.nome().isBlank() || pessoaRequest.nome().length() > 100 || invalidStack(pessoaRequest.stack())) {
                    ctx.setResponseCode(422);
                    return "";
                }

                try {
                    var pessoa = service.salvar(pessoaRequest);

                    ctx.setResponseCode(201);
                    ctx.setResponseHeader("Location", format("/pessoas/%s", pessoa.id()));
                    return pessoa;
                } catch (IntegrityConstraintViolationException e) {
                    ctx.setResponseCode(422);
                    return "";
                }

            }));
        });

    }

    private boolean invalidStack(List<String> stack) {
        if (stack == null) {
            return false;
        }

        for (String s : stack) {
            if (s.length() > 32) {
                return true;
            }
        }

        return false;
    }
}
