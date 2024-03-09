package app;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record Saldo(long total,
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    LocalDateTime dataExtrato,
                    long limite) {
}
