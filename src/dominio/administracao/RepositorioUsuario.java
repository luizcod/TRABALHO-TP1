//interface do repositório de autenticaçao
package dominio.administracao;

import java.util.List;
import java.util.Optional;

public interface RepositorioUsuario {
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorId(String id);
    List<Usuario> listar();
    void salvar(Usuario usuario);   // insere ou atualiza (a chave é o id)
    void remover(String id);
}