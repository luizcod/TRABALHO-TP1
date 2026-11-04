package dominio.analiseEstatistica;

import excecoes.ExcecaoRegraNegocio;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Núcleo do cálculo estatístico: contagens por opção, por característica e cruzamentos
 * (distribuição conjunta). Para grandes volumes, processa em partes de forma concorrente.
 */
public class Analise {

    static final int LIMITE_PARALELO = 50_000;

    /** Nome da combinação -> dimensões usadas. Primeiro as individuais, depois os pares. */
    private static final Map<String, List<Dimensao>> COMBINACOES = montarCombinacoes();

    private static Map<String, List<Dimensao>> montarCombinacoes() {
        Map<String, List<Dimensao>> mapa = new LinkedHashMap<>();
        Dimensao[] dims = Dimensao.values();
        for (Dimensao d : dims) {
            mapa.put(d.rotulo(), List.of(d));
        }
        for (int i = 0; i < dims.length; i++) {
            for (int j = i + 1; j < dims.length; j++) {
                mapa.put(dims[i].rotulo() + " x " + dims[j].rotulo(), List.of(dims[i], dims[j]));
            }
        }
        return mapa;
    }

    public Resultado processar(String pesquisaCodigo, String perguntaId, List<RespostaAnalise> todas) {
        List<RespostaAnalise> rs = todas.stream()
                .filter(r -> r.pesquisaCodigo().equals(pesquisaCodigo) && r.perguntaId().equals(perguntaId))
                .toList();
        if (rs.isEmpty()) {
            throw new ExcecaoRegraNegocio("Não há respostas registradas para a pesquisa " + pesquisaCodigo
                    + " (pergunta " + perguntaId + ").");
        }
        validarUnicidade(rs);

        Parcial total;
        if (rs.size() >= LIMITE_PARALELO) {
            int partes = Math.max(2, Runtime.getRuntime().availableProcessors());
            int tamanho = (int) Math.ceil(rs.size() / (double) partes);
            List<List<RespostaAnalise>> blocos = new ArrayList<>();
            for (int i = 0; i < rs.size(); i += tamanho) {
                blocos.add(rs.subList(i, Math.min(rs.size(), i + tamanho)));
            }
            total = blocos.parallelStream().map(Analise::acumular).reduce(Parcial::mesclar).orElseThrow();
        } else {
            total = acumular(rs);
        }
        return new Resultado(pesquisaCodigo, perguntaId, total.votos, total.especiais, total.dist);
    }

    /** Regra: uma única resposta por pergunta em cada entrevista. */
    private static void validarUnicidade(List<RespostaAnalise> rs) {
        Set<String> vistas = new HashSet<>();
        for (RespostaAnalise r : rs) {
            if (!vistas.add(r.entrevistaId())) {
                throw new ExcecaoRegraNegocio("A entrevista " + r.entrevistaId()
                        + " possui mais de uma resposta para a mesma pergunta.");
            }
        }
    }

    private static Parcial acumular(List<RespostaAnalise> parte) {
        Parcial p = new Parcial();
        for (RespostaAnalise r : parte) {
            p.contar(r);
        }
        return p;
    }

    private static String grupo(RespostaAnalise r, List<Dimensao> dims) {
        StringBuilder sb = new StringBuilder();
        for (Dimensao d : dims) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(r.valorPerfil(d));
        }
        return sb.toString();
    }

    /** Acumulador parcial (um por bloco), mesclado ao final. */
    private static final class Parcial {
        final Map<String, Integer> votos = new TreeMap<>();
        final Map<TipoResposta, Integer> especiais = new EnumMap<>(TipoResposta.class);
        final Map<String, Map<String, Map<String, Integer>>> dist = new LinkedHashMap<>();

        Parcial() {
            COMBINACOES.keySet().forEach(nome -> dist.put(nome, new TreeMap<>()));
        }

        void contar(RespostaAnalise r) {
            if (r.tipo() == TipoResposta.OPCAO) {
                votos.merge(r.rotulo(), 1, Integer::sum);
            } else {
                especiais.merge(r.tipo(), 1, Integer::sum);
            }
            COMBINACOES.forEach((nome, dims) -> dist.get(nome)
                    .computeIfAbsent(grupo(r, dims), k -> new TreeMap<>())
                    .merge(r.rotulo(), 1, Integer::sum));
        }

        void somar(Parcial o) {
            o.votos.forEach((k, v) -> votos.merge(k, v, Integer::sum));
            o.especiais.forEach((k, v) -> especiais.merge(k, v, Integer::sum));
            o.dist.forEach((nome, grupos) -> grupos.forEach((g, cont) -> {
                Map<String, Integer> alvo = dist.get(nome).computeIfAbsent(g, k -> new TreeMap<>());
                cont.forEach((rot, q) -> alvo.merge(rot, q, Integer::sum));
            }));
        }

        static Parcial mesclar(Parcial a, Parcial b) {
            Parcial novo = new Parcial();
            novo.somar(a);
            novo.somar(b);
            return novo;
        }
    }
}
