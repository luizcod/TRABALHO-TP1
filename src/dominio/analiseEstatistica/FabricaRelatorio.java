package dominio.analiseEstatistica;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Monta objetos Relatorio a partir de resultados e comparações (padrão Factory). */
public final class FabricaRelatorio {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FabricaRelatorio() {
    }

    private static boolean mostrarCandidato(FiltroRelatorio f, String opcao) {
        return f.candidato() == null || f.candidato().equalsIgnoreCase(opcao);
    }

    /** Barra textual: cada 2,5 pontos percentuais = 1 caractere '#'. */
    private static String barra(double percentual) {
        return "#".repeat(Math.max(0, (int) Math.round(percentual / 2.5)));
    }

    public static String pct(double v) {
        return String.format(BR, "%.1f%%", v);
    }

    public static Relatorio deResultado(PesquisaResumo p, FiltroRelatorio filtro, Resultado r,
                                        boolean incluirCruzamentos) {
        Relatorio rel = new Relatorio("Relatório de resultados - " + p.nome());
        rel.adicionarCabecalho("Pesquisa: " + p.codigo() + " - " + p.nome());
        rel.adicionarCabecalho("Cargo: " + p.cargo() + " | Tipo: " + p.tipoPesquisa());
        rel.adicionarCabecalho("Filtros aplicados: " + filtro.descricao());
        rel.adicionarCabecalho("Base dos percentuais: " + r.totalValidas() + " respostas válidas de "
                + r.totalRespostas() + " respostas no total");
        rel.adicionarCabecalho("Gerado em: " + rel.geradoEm().format(DATA_HORA));

        List<String[]> validas = new ArrayList<>();
        List<String[]> barras = new ArrayList<>();
        // linhasValidas() já calcula os percentuais com TODOS os votos válidos da amostra;
        // o candidato escolhido só é destacado depois do cálculo.
        for (Resultado.LinhaResultado l : r.linhasValidas()) {
            if (mostrarCandidato(filtro, l.rotulo())) {
                validas.add(new String[]{l.rotulo(), String.valueOf(l.absoluto()), pct(l.percentual())});
                barras.add(new String[]{l.rotulo(), pct(l.percentual()), barra(l.percentual())});
            }
        }
        rel.adicionarSecao(new Relatorio.Secao("Resultado geral (percentual sobre respostas válidas)",
                List.of("Opção", "Quantidade", "Percentual"), validas));
        rel.adicionarSecao(new Relatorio.Secao("Gráfico de barras textual - distribuição dos votos válidos",
                List.of("Opção", "Percentual", "Barra proporcional"), barras));

        List<String[]> especiais = new ArrayList<>();
        for (Resultado.LinhaResultado l : r.linhasEspeciais()) {
            especiais.add(new String[]{l.rotulo(), String.valueOf(l.absoluto()), pct(l.percentual())});
        }
        rel.adicionarSecao(new Relatorio.Secao("Respostas especiais (fora da base; % sobre o total de respostas)",
                List.of("Tipo", "Quantidade", "Percentual"), especiais));

        for (Map.Entry<String, Map<String, Map<String, Integer>>> e : r.distribuicoes().entrySet()) {
            boolean cruzamento = e.getKey().contains(" x ");
            if (cruzamento && !incluirCruzamentos) {
                continue;
            }
            rel.adicionarSecao(secaoDistribuicao(e.getKey(), e.getValue(), cruzamento, filtro));
        }
        return rel;
    }

    private static Relatorio.Secao secaoDistribuicao(String nome, Map<String, Map<String, Integer>> grupos,
                                                     boolean cruzamento, FiltroRelatorio filtro) {
        List<String[]> linhas = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> g : grupos.entrySet()) {
            Map<String, Integer> validas = new LinkedHashMap<>();
            List<String[]> especiais = new ArrayList<>();
            g.getValue().forEach((rot, q) -> {
                if (TipoResposta.rotuloEspecial(rot)) {
                    especiais.add(new String[]{g.getKey(), rot, String.valueOf(q), "-"});
                } else {
                    validas.put(rot, q);
                }
            });
            Map<String, Double> pct = CalculadoraPercentual.distribuir(validas);
            // denominador = todos os candidatos do grupo; só a exibição é filtrada
            validas.forEach((rot, q) -> {
                if (mostrarCandidato(filtro, rot)) {
                    linhas.add(new String[]{g.getKey(), rot, String.valueOf(q), pct(pct.get(rot))});
                }
            });
            linhas.addAll(especiais);
        }
        String titulo = (cruzamento ? "Distribuição conjunta: " : "Distribuição por ") + nome;
        return new Relatorio.Secao(titulo, List.of("Grupo", "Opção", "Quantidade", "% no grupo"), linhas);
    }

    public static Relatorio deComparacao(ComparacaoPesquisa c) {
        PesquisaResumo a = c.pesquisaA();
        PesquisaResumo b = c.pesquisaB();
        Relatorio rel = new Relatorio("Comparação de pesquisas - " + a.codigo() + " x " + b.codigo());
        rel.adicionarCabecalho("A: " + a.codigo() + " - " + a.nome() + " (" + c.resultadoA().totalValidas()
                + " respostas válidas)");
        rel.adicionarCabecalho("B: " + b.codigo() + " - " + b.nome() + " (" + c.resultadoB().totalValidas()
                + " respostas válidas)");
        rel.adicionarCabecalho("Cargo: " + a.cargo() + " | Tipo: " + a.tipoPesquisa());
        rel.adicionarCabecalho("Variação = % de B menos % de A, em pontos percentuais");
        rel.adicionarCabecalho("Gerado em: " + rel.geradoEm().format(DATA_HORA));
        List<String[]> linhas = new ArrayList<>();
        for (ComparacaoPesquisa.LinhaComparacao l : c.linhas()) {
            linhas.add(new String[]{l.opcao(), l.qtdA() + " (" + pct(l.pctA()) + ")",
                    l.qtdB() + " (" + pct(l.pctB()) + ")",
                    String.format(BR, "%+.1f p.p.", l.variacao())});
        }
        rel.adicionarSecao(new Relatorio.Secao("Comparação por opção",
                List.of("Opção", "A: qtd (%)", "B: qtd (%)", "Variação"), linhas));
        return rel;
    }
}
