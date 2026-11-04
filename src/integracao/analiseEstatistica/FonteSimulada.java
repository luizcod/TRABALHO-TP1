package integracao.analiseEstatistica;

import dominio.analiseEstatistica.Dimensao;
import dominio.analiseEstatistica.PesquisaResumo;
import dominio.analiseEstatistica.PesquisaResumo.Situacao;
import dominio.analiseEstatistica.RespostaAnalise;
import dominio.analiseEstatistica.TipoResposta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Dados 100% fictícios para testar o módulo de Análise sozinho,
 * enquanto os módulos de Pesquisas e Coleta ainda não estão integrados.
 */
public class FonteSimulada implements PesquisaGateway, ColetaGateway {

    public static final String PERGUNTA = "Q1";

    private static final String[] REGIOES = {"Norte", "Sul", "Centro"};
    private static final String[] FAIXAS = {"16-24", "25-39", "40-59", "60+"};
    private static final String[] ESCOLARIDADES = {"Fundamental", "Médio", "Superior"};
    private static final String[] GENEROS = {"Feminino", "Masculino"};
    private static final String[] RENDAS = {"Até 2 SM", "2 a 5 SM", "Acima de 5 SM"};

    private final Map<String, PesquisaResumo> pesquisas = new LinkedHashMap<>();
    private final Map<String, List<RespostaAnalise>> respostas = new HashMap<>();

    public FonteSimulada() {
        adicionar(new PesquisaResumo("P001", "Intenção de voto - 1ª rodada", "PRESIDENTE",
                "INTENCAO_VOTO", Situacao.ENCERRADA, 600), 600, 1, LocalDate.of(2026, 8, 1),
                new String[]{"Candidato Alfa", "Candidato Beta", "Candidato Gama"});
        adicionar(new PesquisaResumo("P002", "Intenção de voto - 2ª rodada", "PRESIDENTE",
                "INTENCAO_VOTO", Situacao.ENCERRADA, 800), 800, 2, LocalDate.of(2026, 9, 1),
                new String[]{"Candidato Alfa", "Candidato Beta", "Candidato Gama"});
        adicionar(new PesquisaResumo("P003", "Rejeição - 1ª rodada", "PRESIDENTE",
                "REJEICAO", Situacao.ENCERRADA, 300), 300, 3, LocalDate.of(2026, 9, 10),
                new String[]{"Candidato Alfa", "Candidato Beta", "Candidato Gama"});
        adicionar(new PesquisaResumo("P004", "Intenção de voto - Governador", "GOVERNADOR",
                "INTENCAO_VOTO", Situacao.EM_COLETA, 200), 120, 4, LocalDate.of(2026, 10, 5),
                new String[]{"Candidata Delta", "Candidato Épsilon"});
    }

    private void adicionar(PesquisaResumo p, int n, long semente, LocalDate base, String[] candidatos) {
        pesquisas.put(p.codigo(), p);
        Random rnd = new Random(semente);
        List<RespostaAnalise> lista = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            Map<Dimensao, String> perfil = new EnumMap<>(Dimensao.class);
            perfil.put(Dimensao.REGIAO, REGIOES[rnd.nextInt(REGIOES.length)]);
            perfil.put(Dimensao.FAIXA_ETARIA, FAIXAS[rnd.nextInt(FAIXAS.length)]);
            perfil.put(Dimensao.ESCOLARIDADE, ESCOLARIDADES[rnd.nextInt(ESCOLARIDADES.length)]);
            perfil.put(Dimensao.GENERO, GENEROS[rnd.nextInt(GENEROS.length)]);
            perfil.put(Dimensao.FAIXA_RENDA, RENDAS[rnd.nextInt(RENDAS.length)]);

            int sorteio = rnd.nextInt(100);
            TipoResposta tipo;
            String opcao = null;
            if (sorteio < 8) {
                tipo = TipoResposta.NAO_SABE;
            } else if (sorteio < 13) {
                tipo = TipoResposta.NAO_RESPONDEU;
            } else if (sorteio < 16) {
                tipo = TipoResposta.NENHUMA;
            } else {
                tipo = TipoResposta.OPCAO;
                int x = rnd.nextInt(100);
                opcao = candidatos[x < 45 ? 0 : (x < 80 || candidatos.length < 3 ? 1 : 2)];
            }
            lista.add(new RespostaAnalise(p.codigo(), PERGUNTA, p.codigo() + "-E" + i,
                    base.plusDays(rnd.nextInt(25)), tipo, opcao, perfil));
        }
        respostas.put(p.codigo(), lista);
    }

    @Override
    public Optional<PesquisaResumo> buscar(String codigo) {
        return Optional.ofNullable(pesquisas.get(codigo == null ? null : codigo.trim().toUpperCase()));
    }

    @Override
    public List<PesquisaResumo> listar() {
        return new ArrayList<>(pesquisas.values());
    }

    @Override
    public void marcarComoProcessada(String codigo) {
        pesquisas.computeIfPresent(codigo, (c, p) -> p.comSituacao(Situacao.PROCESSADA));
    }

    @Override
    public List<RespostaAnalise> respostas(String pesquisaCodigo, String perguntaId) {
        return respostas.getOrDefault(pesquisaCodigo, List.of());
    }
}
