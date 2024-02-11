package app;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Singleton
public class RegistrarTransacaoUsecaseImpl implements RegistrarTransacaoUsecase {
    private final DSLContext context;

    @Inject
    public RegistrarTransacaoUsecaseImpl(DSLContext context) {
        this.context = context;
    }

    @Override
    public SaldoTransacao registrar(Transacao transacao) {
        Cliente cliente = buscarClientePorId(transacao.idCliente());






        return null;
    }

    private Cliente buscarClientePorId(int idCliente) {
        return Optional.ofNullable(context.fetchOne(table("clientes"), field("cliente_id").eq(idCliente)))
                .map(this::recordToCliente).orElseThrow(ClienteNaoEncontradoException::new);
    }

    private Cliente recordToCliente(Record record1) {
        return new Cliente(record1.get("cliente_id", Integer.class),
                record1.get("saldo", Long.class),
                record1.get("limite", Long.class),
                record1.get("versao", Integer.class)
        );
    }

    class ClienteNaoEncontradoException extends RuntimeException {}
    record Cliente(int idCliente, long saldo, long limite, int versao){}
}
