package integracao.analiseEstatistica;

import dominio.analiseEstatistica.RespostaAnalise;

import java.util.List;

/**
 * Porta de entrada para o módulo de Coleta e Amostra (responsável: Luiz Eduardo).
 * Deve devolver as respostas de uma pergunta já unidas ao perfil do participante.
 */
public interface ColetaGateway {

    List<RespostaAnalise> respostas(String pesquisaCodigo, String perguntaId);
}
