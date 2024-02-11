package app;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PessoaService {

    Pessoa salvar(Pessoa pessoa) throws JsonProcessingException;

    Optional<Pessoa> buscarPorId(UUID id) throws JsonProcessingException;

    List<Pessoa> buscarPorTermo(String termo);

    Integer contarPessoas();

}
