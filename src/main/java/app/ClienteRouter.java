package app;

import io.jooby.Jooby;
import org.jooq.exception.IntegrityConstraintViolationException;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

public class ClienteRouter extends Jooby {
    {

        get("/health-check", (ctx) -> "OK");

        path("/clientes", () -> {
            get("/{id}/extrato", (ctx -> {

                return null;
            }));

            post("/{id}", (ctx -> {

               return null;
            }));
        });

    }
}
