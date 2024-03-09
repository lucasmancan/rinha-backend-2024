package app;

import java.math.BigDecimal;
import java.util.Objects;

public record TransacaoRequest(BigDecimal valor, String tipo, String descricao) {

        public boolean valido() {
            if (valor.compareTo(BigDecimal.ZERO) == 0 || valor.scale() != 0)
                return false;

            if (!Objects.equals(tipo, "d") && !Objects.equals(tipo, "c")) {
                return false;
            }

            return descricao != null && (descricao.length() <= 10 && descricao.length() >= 1);
        }

        public long valorAbsoluto() {
            return this.valor.longValue();
        }

        public long valorToLong() {
            if (tipo.equals("d")) {
                return valor.longValue() * -1;
            }
            return valor.longValue();
        }
    }