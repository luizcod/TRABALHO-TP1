package dominio.analiseEstatistica;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Comparação entre duas pesquisas compatíveis (percentuais sobre respostas válidas). */
public final class ComparacaoPesquisa {

    /** variacao = pctB - pctA, em pontos percentuais. */
    public record LinhaComparacao(String opcao, int qtdA, double pctA, int qtdB, double pctB, double variacao) { }

    private final PesquisaResumo a;
    private final PesquisaResumo b;
    private final Resultado ra;
    private final Resultado rb;

    /** Valida a compatibilidade (inclusive a pergunta) mesmo se instanciada fora do AnaliseService. */
    public ComparacaoPesquisa(PesquisaResumo a, Resultado ra, PesquisaResumo b, Resultado rb) {
        VerificadorCompatibilidade.verificar(a, b, ra, rb);
        this.a = a;
        this.b = b;
        this.ra = ra;
        this.rb = rb;
    }

    public PesquisaResumo pesquisaA() {
        return a;
    }

    public PesquisaResumo pesquisaB() {
        return b;
    }

    public Resultado resultadoA() {
        return ra;
    }

    public Resultado resultadoB() {
        return rb;
    }

    public List<LinhaComparacao> linhas() {
        Set<String> opcoes = new LinkedHashSet<>(ra.votosPorOpcao().keySet());
        opcoes.addAll(rb.votosPorOpcao().keySet());
        Map<String, Double> pa = CalculadoraPercentual.distribuir(ra.votosPorOpcao());
        Map<String, Double> pb = CalculadoraPercentual.distribuir(rb.votosPorOpcao());
        List<LinhaComparacao> linhas = new ArrayList<>();
        for (String op : opcoes) {
            int qa = ra.votosPorOpcao().getOrDefault(op, 0);
            int qb = rb.votosPorOpcao().getOrDefault(op, 0);
            double xa = pa.getOrDefault(op, 0.0);
            double xb = pb.getOrDefault(op, 0.0);
            linhas.add(new LinhaComparacao(op, qa, xa, qb, xb, Math.round((xb - xa) * 10) / 10.0));
        }
        return linhas;
    }
}
