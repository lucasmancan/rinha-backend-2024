package app.consultarextrato;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

record Saldo (long total,
              @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
              LocalDateTime dataExtrato,
              long limite){}

public record Extrato(Saldo saldo,
                      List<HistoricoTransacao> ultimasTransacoes) {
}
