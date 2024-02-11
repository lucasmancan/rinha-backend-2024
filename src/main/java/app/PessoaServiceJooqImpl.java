package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@Singleton
public class PessoaServiceJooqImpl implements PessoaService {
    private final DSLContext context;

    @Inject
    public PessoaServiceJooqImpl(DataSource dataSource) {
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Override
    public Pessoa salvar(Pessoa pessoa) {

        var id = UUID.randomUUID();

        String joinStack = null;
        if(pessoa.stack() != null){
            joinStack = String.join(",", pessoa.stack());
        }

        context.insertInto(table("pessoas"), field("id"), field("nome"), field("apelido"), field("nascimento"), field("stack"))
                .values(id, pessoa.nome(), pessoa.apelido(), pessoa.nascimento(), joinStack)
                .execute();


        var pessoaSalva = new Pessoa(id.toString(), pessoa.nome(), pessoa.apelido(), pessoa.nascimento(), pessoa.stack());

        return pessoaSalva;
    }
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public Optional<Pessoa> buscarPorId(UUID id) throws JsonProcessingException {

        Record record = context.fetchOne(table("pessoas"), field("id").eq(id));

        return Optional.ofNullable(record).map(this::recordToPessoa);
    }


    @Override
    public List<Pessoa> buscarPorTermo(String termo) {

        var expressaoTermoAproximado = "%" + termo + "%";

        return context.fetch(table("pessoas"),
                        field("nome").like(expressaoTermoAproximado).or(field("apelido").like(expressaoTermoAproximado)).or(field("stack").like(expressaoTermoAproximado)))
                .stream()
                .map(this::recordToPessoa)
                .collect(Collectors.toList());

    }


    @Override
    public Integer contarPessoas() {
        return context.select(count()).from("pessoas").fetch().get(0).value1();
    }

    private Pessoa recordToPessoa(Record record1) {
        List<String> stack = null;

        if(record1.get("stack") != null){
            stack = Arrays.asList(record1.get("stack", String.class).split(","));

        }
        return new Pessoa(record1.get("id", String.class),
                record1.get("nome", String.class),
                record1.get("apelido", String.class),
                record1.get("nascimento", LocalDate.class),stack
        );
    }
}
