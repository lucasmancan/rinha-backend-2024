package app.consultarextrato;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

record HistoricoTransacao(String tipo,
                          long valor,
                          String descricao,
                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                          LocalDateTime realizadaEm) {
}
