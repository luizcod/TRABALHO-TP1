package dominio.pesquisas;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Objeto DTO que encapsula os resultados do acompanhamento do andamento da pesquisa.
 * Atende ao requisito:
 * "Acompanhar o andamento das pesquisas, apresentando quantidade prevista, quantidade
 * realizada e percentual de conclusão. Identificar pesquisas que não atingiram a quantidade
 * mínima prevista para determinado grupo da amostra."
 */
public class RelatorioAcompanhamento implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigoPesquisa;
    private final String nomePesquisa;
    private final int quantidadePrevista;
    private final int quantidadeRealizada;
    private final double percentualConclusao;
    private final Map<String, Integer> entrevistasPorGrupo;
    private final List<String> gruposAbaixoDaMeta;
    private final boolean todasMetasAtingidas;

    public RelatorioAcompanhamento(String codigoPesquisa,
                                   String nomePesquisa,
                                   int quantidadePrevista,
                                   int quantidadeRealizada,
                                   double percentualConclusao,
                                   Map<String, Integer> entrevistasPorGrupo,
                                   List<String> gruposAbaixoDaMeta) {
        this.codigoPesquisa = codigoPesquisa;
        this.nomePesquisa = nomePesquisa;
        this.quantidadePrevista = quantidadePrevista;
        this.quantidadeRealizada = quantidadeRealizada;
        this.percentualConclusao = percentualConclusao;
        this.entrevistasPorGrupo = entrevistasPorGrupo != null ? Collections.unmodifiableMap(entrevistasPorGrupo) : Collections.emptyMap();
        this.gruposAbaixoDaMeta = gruposAbaixoDaMeta != null ? Collections.unmodifiableList(gruposAbaixoDaMeta) : Collections.emptyList();
        this.todasMetasAtingidas = this.gruposAbaixoDaMeta.isEmpty();
    }

    public String getCodigoPesquisa() {
        return codigoPesquisa;
    }

    public String getNomePesquisa() {
        return nomePesquisa;
    }

    public int getQuantidadePrevista() {
        return quantidadePrevista;
    }

    public int getQuantidadeRealizada() {
        return quantidadeRealizada;
    }

    public double getPercentualConclusao() {
        return percentualConclusao;
    }

    public Map<String, Integer> getEntrevistasPorGrupo() {
        return entrevistasPorGrupo;
    }

    public List<String> getGruposAbaixoDaMeta() {
        return gruposAbaixoDaMeta;
    }

    public boolean isTodasMetasAtingidas() {
        return todasMetasAtingidas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== ACOMPANHAMENTO DA PESQUISA [%s - %s] ===\n", codigoPesquisa, nomePesquisa));
        sb.append(String.format("• Entrevistas Previstas: %d | Realizadas: %d\n", quantidadePrevista, quantidadeRealizada));
        sb.append(String.format("• Percentual de Conclusão: %.2f%%\n", percentualConclusao));
        sb.append("• Distribuição por Grupos de Amostra:\n");
        for (Map.Entry<String, Integer> entry : entrevistasPorGrupo.entrySet()) {
            sb.append(String.format("   - Grupo '%s': %d entrevistas\n", entry.getKey(), entry.getValue()));
        }
        if (gruposAbaixoDaMeta.isEmpty()) {
            sb.append("• Status das Metas: TODAS AS METAS AMOSTRAIS FORAM ATINGIDAS!\n");
        } else {
            sb.append("• ALERTA - Grupos que NÃO atingiram o mínimo previsto no plano amostral:\n");
            for (String grupo : gruposAbaixoDaMeta) {
                sb.append(String.format("   [!] %s\n", grupo));
            }
        }
        return sb.toString();
    }
}
