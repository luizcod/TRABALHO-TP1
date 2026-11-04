package integracao.analiseEstatistica;

import dominio.analiseEstatistica.PesquisaResumo;

import java.util.List;
import java.util.Optional;

/**
 * Porta de entrada para o módulo de Pesquisas (responsável: Sabrina).
 * Implemente esta interface chamando as classes/serviços reais de Pesquisa.
 */
public interface PesquisaGateway {

    Optional<PesquisaResumo> buscar(String codigo);

    List<PesquisaResumo> listar();

    /** Muda a situação da pesquisa para PROCESSADA no módulo de Pesquisas. */
    void marcarComoProcessada(String codigo);
}
