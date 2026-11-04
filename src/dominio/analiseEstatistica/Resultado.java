package dominio.analiseEstatistica;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Resultado estatístico de uma pergunta de uma pesquisa.
 * Percentuais das opções usam como base apenas as respostas válidas;
 * "não sabe", "não respondeu" e "nenhuma" ficam à parte.
 */
public final class Resultado implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Linha de apresentação: rótulo, quantidade absoluta e percentual. */
    public record LinhaResultado(String rotulo, int absoluto, double percentual) implements Serializable { }

    private final String pesquisaCodigo;
    private final String perguntaId;
    private final Map<String, Integer> votosPorOpcao;
    private final Map<TipoResposta, Integer> especiais;
    /** nome da combinação (ex.: "Região" ou "Região x Faixa etária") -> grupo -> rótulo -> quantidade */
    private final Map<String, Map<String, Map<String, Integer>>> distribuicoes;
    private final LocalDateTime processadoEm;

    public Resultado(String pesquisaCodigo, String perguntaId, Map<String, Integer> votosPorOpcao,
                     Map<TipoResposta, Integer> especiais,
                     Map<String, Map<String, Map<String, Integer>>> distribuicoes) {
        this.pesquisaCodigo = pesquisaCodigo;
        this.perguntaId = perguntaId;
        this.votosPorOpcao = new TreeMap<>(votosPorOpcao);
        this.especiais = new EnumMap<>(TipoResposta.class);
        this.especiais.putAll(especiais);
        this.distribuicoes = new LinkedHashMap<>();
        distribuicoes.forEach((nome, grupos) -> {
            Map<String, Map<String, Integer>> copiaGrupos = new TreeMap<>();
            grupos.forEach((g, cont) -> copiaGrupos.put(g, new TreeMap<>(cont)));
            this.distribuicoes.put(nome, copiaGrupos);
        });
        this.processadoEm = LocalDateTime.now();
    }

    public String pesquisaCodigo() {
        return pesquisaCodigo;
    }

    public String perguntaId() {
        return perguntaId;
    }

    public LocalDateTime processadoEm() {
        return processadoEm;
    }

    public Map<String, Integer> votosPorOpcao() {
        return Collections.unmodifiableMap(votosPorOpcao);
    }

    public int quantidade(TipoResposta tipo) {
        return especiais.getOrDefault(tipo, 0);
    }

    public int totalValidas() {
        return votosPorOpcao.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int totalRespostas() {
        int soma = totalValidas();
        for (TipoResposta t : TipoResposta.values()) {
            if (t != TipoResposta.OPCAO) {
                soma += quantidade(t);
            }
        }
        return soma;
    }

    /** Opções válidas com quantidade e percentual sobre o total de respostas válidas (soma 100,0%). */
    public List<LinhaResultado> linhasValidas() {
        Map<String, Double> pct = CalculadoraPercentual.distribuir(votosPorOpcao);
        List<LinhaResultado> linhas = new ArrayList<>();
        votosPorOpcao.forEach((op, q) -> linhas.add(new LinhaResultado(op, q, pct.get(op))));
        linhas.sort((a, b) -> Integer.compare(b.absoluto(), a.absoluto()));
        return linhas;
    }

    /** Respostas especiais, com percentual sobre o total de respostas coletadas. */
    public List<LinhaResultado> linhasEspeciais() {
        List<LinhaResultado> linhas = new ArrayList<>();
        int total = totalRespostas();
        for (TipoResposta t : TipoResposta.values()) {
            if (t != TipoResposta.OPCAO) {
                linhas.add(new LinhaResultado(t.rotulo(), quantidade(t),
                        CalculadoraPercentual.percentual(quantidade(t), total)));
            }
        }
        return linhas;
    }

    public Map<String, Map<String, Map<String, Integer>>> distribuicoes() {
        return Collections.unmodifiableMap(distribuicoes);
    }
}
