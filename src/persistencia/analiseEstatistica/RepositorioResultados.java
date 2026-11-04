package persistencia.analiseEstatistica;

import excecoes.ExcecaoRegraNegocio;
import dominio.analiseEstatistica.Resultado;
import java.util.List;
import java.util.Optional;

public interface RepositorioResultados {

    void salvar(Resultado resultado);

    Optional<Resultado> buscar(String pesquisaCodigo, String perguntaId);

    List<Resultado> listar();
}
