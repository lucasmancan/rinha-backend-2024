package app;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Date;
import java.time.LocalDateTime;

public record HistoricoTransacao(String tipo,
                                 long valor,
                                 String descricao,
                                 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                 Date realizadaEm) {
}
