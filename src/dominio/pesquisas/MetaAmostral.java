package dominio.pesquisas;

import java.io.Serializable;
import java.util.Objects;

/**
 * Define uma cota ou meta mínima de entrevistas para determinado grupo da amostra
 * (ex: por região "Norte", faixa etária "16-24", etc.).
 * Modelagem Etapa 2: grupo, minimo.
 */
public class MetaAmostral implements Serializable {
    private static final long serialVersionUID = 1L;

    private String grupo;
    private int minimo;

    public MetaAmostral() {
    }

    public MetaAmostral(String grupo, int minimo) {
        this.grupo = grupo;
        this.minimo = minimo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public int getMinimo() {
        return minimo;
    }

    public void setMinimo(int minimo) {
        this.minimo = minimo;
    }

    /**
     * Verifica se uma quantidade realizada atingiu a meta mínima estipulada.
     */
    public boolean atingiuMeta(int quantidadeRealizada) {
        return quantidadeRealizada >= this.minimo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaAmostral that = (MetaAmostral) o;
        return Objects.equals(grupo != null ? grupo.trim().toUpperCase() : null,
                              that.grupo != null ? that.grupo.trim().toUpperCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grupo != null ? grupo.trim().toUpperCase() : null);
    }

    @Override
    public String toString() {
        return String.format("Meta Grupo '%s': mínimo de %d entrevistas", grupo, minimo);
    }
}
