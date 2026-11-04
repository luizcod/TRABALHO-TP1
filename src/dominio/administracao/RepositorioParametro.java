package dominio.administracao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface RepositorioParametro {
    Optional<Parametro> buscarPorId(String id);
    List<Parametro> listar();
    void salvar(Parametro parametro);   // insere ou atualiza (a chave é o id)
    void remover(String id);

    default List<Parametro> listarPorCategoria(CategoriaParametro categoria) {
        return listar().stream()
                .filter(p -> p.getCategoria() == categoria)
                .collect(Collectors.toList());
    }
}