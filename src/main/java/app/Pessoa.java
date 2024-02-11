package app;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

record Pessoa(String id,
              String nome,
              String apelido,
              @JsonFormat(shape = JsonFormat.Shape.STRING)
              LocalDate nascimento,
              List<String> stack) { }
