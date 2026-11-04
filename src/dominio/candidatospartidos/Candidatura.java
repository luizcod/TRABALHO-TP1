package dominio.candidatospartidos;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa a situação da candidatura de um candidato.
 * Situações comuns: "VALIDADA", "INATIVA", "PRÉ-CANDIDATO", "CANDIDATO".
 */
public class Candidatura implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SITUACAO_VALIDADA = "VALIDADA";
    public static final String SITUACAO_INATIVA = "INATIVA";
    public static final String SITUACAO_PRE_CANDIDATO = "PRÉ-CANDIDATO";
    public static final String SITUACAO_CANDIDATO = "CANDIDATO";

    private String situacao;

    public Candidatura() {
        this.situacao = SITUACAO_PRE_CANDIDATO;
    }

    public Candidatura(String situacao) {
        setSituacao(situacao);
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        if (situacao == null || situacao.trim().isEmpty()) {
            this.situacao = SITUACAO_PRE_CANDIDATO;
        } else {
            this.situacao = situacao.trim().toUpperCase();
        }
    }

    /**
     * Verifica se a candidatura está apta a participar de pesquisas eleitorais.
     * Regra: somente candidaturas validadas/ativas são permitidas em pesquisas.
     */
    public boolean isValidaParaPesquisa() {
        return SITUACAO_VALIDADA.equalsIgnoreCase(situacao) || SITUACAO_CANDIDATO.equalsIgnoreCase(situacao);
    }

    /**
     * Verifica se a candidatura é considerada ativa para fins de indicadores cadastrais.
     */
    public boolean isAtiva() {
        return !SITUACAO_INATIVA.equalsIgnoreCase(situacao);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candidatura that = (Candidatura) o;
        return Objects.equals(situacao, that.situacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(situacao);
    }

    @Override
    public String toString() {
        return "Candidatura{" +
                "situacao='" + situacao + '\'' +
                '}';
    }
}
