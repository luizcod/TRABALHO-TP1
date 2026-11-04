package dominio.coleta;

import java.util.List;
import java.util.Optional;

public interface RepositorioColeta {
    // entrevistadores
    Optional<Entrevistador> buscarEntrevistador(String codigo);
    List<Entrevistador> listarEntrevistadores();
    void salvarEntrevistador(Entrevistador entrevistador);
    void removerEntrevistador(String codigo);

    // entrevistas (a chave é pesquisa + id)
    Optional<Entrevista> buscarEntrevista(String pesquisaCodigo, String id);
    List<Entrevista> listarEntrevistas(String pesquisaCodigo);
    void salvarEntrevista(Entrevista entrevista);
}
