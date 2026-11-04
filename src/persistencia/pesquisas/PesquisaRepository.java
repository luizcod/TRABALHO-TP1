package persistencia.pesquisas;

import dominio.candidatospartidos.Cargo;
import dominio.pesquisas.Pesquisa;
import dominio.pesquisas.SituPesquisa;
import excecoes.PersistenciaException;

import java.util.List;
import java.util.Optional;

/**
 * Interface de Repositório para o Módulo de Pesquisas.
 */
public interface PesquisaRepository {

    void salvarPesquisa(Pesquisa pesquisa) throws PersistenciaException;
    Optional<Pesquisa> buscarPesquisaPorCodigo(String codigo) throws PersistenciaException;
    List<Pesquisa> listarPesquisas() throws PersistenciaException;
    List<Pesquisa> listarPesquisasPorCargo(Cargo cargo) throws PersistenciaException;
    List<Pesquisa> listarPesquisasPorSituacao(SituPesquisa situacao) throws PersistenciaException;
    void atualizarPesquisa(Pesquisa pesquisa) throws PersistenciaException;
    void removerPesquisa(String codigo) throws PersistenciaException;
}
