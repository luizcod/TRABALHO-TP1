package dominio.analiseEstatistica;

import excecoes.ExcecaoRegraNegocio;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Filtros de relatório (padrão Builder).
 * Período e características definem a AMOSTRA. O candidato é apenas destaque de apresentação:
 * os percentuais são calculados com todos os votos válidos da amostra e depois só a linha dele é mostrada.
 */
public final class FiltroRelatorio {

    private final LocalDate inicio;
    private final LocalDate fim;
    private final String candidato;
    private final Map<Dimensao, String> caracteristicas;

    private FiltroRelatorio(Builder b) {
        inicio = b.inicio;
        fim = b.fim;
        candidato = b.candidato;
        caracteristicas = new EnumMap<>(Dimensao.class);
        caracteristicas.putAll(b.caracteristicas);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FiltroRelatorio semFiltros() {
        return new Builder().construir();
    }

    public boolean vazio() {
        return inicio == null && fim == null && candidato == null && caracteristicas.isEmpty();
    }

    /** Só período e características definem a amostra; não exclui outros candidatos antes do cálculo. */
    public boolean aceita(RespostaAnalise r) {
        if (inicio != null || fim != null) {
            if (r.data() == null) {
                return false;
            }
            if (inicio != null && r.data().isBefore(inicio)) {
                return false;
            }
            if (fim != null && r.data().isAfter(fim)) {
                return false;
            }
        }
        for (Map.Entry<Dimensao, String> e : caracteristicas.entrySet()) {
            if (!r.valorPerfil(e.getKey()).equalsIgnoreCase(e.getValue())) {
                return false;
            }
        }
        return true;
    }

    public String candidato() {
        return candidato;
    }

    public String descricao() {
        if (vazio()) {
            return "nenhum";
        }
        List<String> partes = new ArrayList<>();
        if (inicio != null || fim != null) {
            partes.add("período " + (inicio == null ? "..." : inicio) + " a " + (fim == null ? "..." : fim));
        }
        if (candidato != null) {
            partes.add("candidato em destaque = " + candidato);
        }
        caracteristicas.forEach((d, v) -> partes.add(d.rotulo() + " = " + v));
        return String.join("; ", partes);
    }

    public static final class Builder {
        private LocalDate inicio;
        private LocalDate fim;
        private String candidato;
        private final Map<Dimensao, String> caracteristicas = new EnumMap<>(Dimensao.class);

        public Builder periodo(LocalDate inicio, LocalDate fim) {
            if (inicio != null && fim != null && fim.isBefore(inicio)) {
                throw new ExcecaoRegraNegocio("A data final não pode ser anterior à data inicial.");
            }
            this.inicio = inicio;
            this.fim = fim;
            return this;
        }

        public Builder candidato(String candidato) {
            this.candidato = (candidato == null || candidato.isBlank()) ? null : candidato.trim();
            return this;
        }

        public Builder caracteristica(Dimensao dimensao, String valor) {
            if (dimensao != null && valor != null && !valor.isBlank()) {
                caracteristicas.put(dimensao, valor.trim());
            }
            return this;
        }

        public FiltroRelatorio construir() {
            return new FiltroRelatorio(this);
        }
    }
}
