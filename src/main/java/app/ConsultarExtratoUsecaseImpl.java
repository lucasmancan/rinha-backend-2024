package app;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
public class ConsultarExtratoUsecaseImpl implements ConsultarExtratoUsecase {
    private final DSLContext context;

    @Inject
    public ConsultarExtratoUsecaseImpl(DSLContext context) {
        this.context = context;
    }

    @Override
    public Extrato gerarExtratoPorIdCliente(int idCliente) {
        return null;
    }
}
