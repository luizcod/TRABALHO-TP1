package dominio.analiseEstatistica;

import excecoes.ExcecaoRegraNegocio;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * Resposta de uma entrevista já "achatada" com o perfil do participante.
 * É o que o módulo de Coleta e Amostra entrega para a Análise (via ColetaGateway).
 */
public record RespostaAnalise(String pesquisaCodigo, String perguntaId, String entrevistaId,
                              LocalDate data, TipoResposta tipo, String opcao,
                              Map<Dimensao, String> perfil) {

    public static final String NAO_INFORMADO = "Não informado";

    public RespostaAnalise {
        Objects.requireNonNull(pesquisaCodigo, "pesquisaCodigo");
        Objects.requireNonNull(perguntaId, "perguntaId");
        Objects.requireNonNull(entrevistaId, "entrevistaId");
        Objects.requireNonNull(tipo, "tipo");
        if (tipo == TipoResposta.OPCAO && (opcao == null || opcao.isBlank())) {
            throw new ExcecaoRegraNegocio("Resposta do tipo OPÇÃO exige a opção escolhida (entrevista "
                    + entrevistaId + ").");
        }
        perfil = perfil == null ? Map.of() : Map.copyOf(perfil);
    }

    /** Texto usado para agrupar: o nome da opção ou o rótulo da resposta especial. */
    public String rotulo() {
        return tipo == TipoResposta.OPCAO ? opcao : tipo.rotulo();
    }

    public String valorPerfil(Dimensao d) {
        return perfil.getOrDefault(d, NAO_INFORMADO);
    }
}
