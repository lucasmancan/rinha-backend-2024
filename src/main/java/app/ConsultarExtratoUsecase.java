package app;


import java.time.LocalDateTime;
import java.util.List;

record HistoricoTransacao(int idCliente, long valor, String descricao, LocalDateTime realizadaEm) { }

record Extrato(long total, LocalDateTime dataExtrato, long limite, List<HistoricoTransacao> ultimasTransacoes) { }

public interface ConsultarExtratoUsecase {
    Extrato gerarExtratoPorIdCliente(int idCliente);
}
