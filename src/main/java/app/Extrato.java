package app;

import java.util.List;

public record Extrato(Saldo saldo,
                      List<HistoricoTransacao> ultimasTransacoes) {
}
