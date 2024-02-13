package app;


record SaldoTransacao (long limit, long saldo){}

enum TipoTransacao {
    DEBITO("d"), CREDITO("c");
    public String codigo;
    TipoTransacao(String codigo){
        this.codigo = codigo;
    }

    public static TipoTransacao toEnum(String stringValue){
        switch (stringValue) {
            case "d" -> {
                return DEBITO;
            }
            case "c" -> {
                return CREDITO;
            }
            default -> {
                throw new IllegalArgumentException();
            }
        }
    }
}
record Transacao(int idCliente, long valor, TipoTransacao tipo,  String descricao){}

public interface RegistrarTransacaoUsecase {
    SaldoTransacao registrar(Transacao transacao);
}
