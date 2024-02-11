package app;


record SaldoTransacao (long limit, long saldo){}

record Transacao(int idCliente, long valor, String descricao){}

public interface RegistrarTransacaoUsecase {
    SaldoTransacao registrar(Transacao transacao);
}
