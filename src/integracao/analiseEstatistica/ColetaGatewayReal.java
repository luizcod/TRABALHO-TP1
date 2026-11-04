package integracao.analiseEstatistica;

import dominio.analiseEstatistica.Dimensao;
import dominio.analiseEstatistica.RespostaAnalise;
import dominio.analiseEstatistica.TipoResposta;
import dominio.coleta.Entrevista;
import dominio.coleta.Participante;
import dominio.coleta.RepositorioColeta;
import dominio.coleta.Resposta;
import dominio.pesquisas.Pergunta;
import excecoes.ExcecaoRegraNegocio;
import servicos.pesquisas.PesquisaService;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Entrega à Análise as respostas das entrevistas concluídas, já unidas ao perfil do participante. */
public class ColetaGatewayReal implements ColetaGateway {
    private final RepositorioColeta repositorio;
    private final PesquisaService pesquisas;

    public ColetaGatewayReal(RepositorioColeta repositorio, PesquisaService pesquisas) {
        this.repositorio = repositorio;
        this.pesquisas = pesquisas;
    }

    /** perguntaId segue a ordem do questionário: "Q1" é a primeira pergunta. */
    @Override
    public List<RespostaAnalise> respostas(String pesquisaCodigo, String perguntaId) {
        List<Pergunta> perguntas = pesquisas.buscarPesquisaPorCodigo(pesquisaCodigo).getPerguntas();
        int indice;
        try {
            indice = Integer.parseInt(perguntaId.substring(1)) - 1;
        } catch (RuntimeException e) {
            throw new ExcecaoRegraNegocio("Identificador de pergunta inválido: " + perguntaId + ".");
        }
        if (indice < 0 || indice >= perguntas.size()) {
            throw new ExcecaoRegraNegocio("A pesquisa " + pesquisaCodigo + " não possui a pergunta "
                    + perguntaId + ".");
        }
        Pergunta alvo = perguntas.get(indice);

        List<RespostaAnalise> saida = new ArrayList<>();
        for (Entrevista e : repositorio.listarEntrevistas(pesquisaCodigo)) {
            if (!e.isConcluida()) continue;
            for (Resposta r : e.getRespostas()) {
                if (!r.getPergunta().equals(alvo)) continue;
                TipoResposta tipo = r.isEspecial() ? converter(r) : TipoResposta.OPCAO;
                String opcao = r.isEspecial() ? null : r.getOpcao().getTexto();
                saida.add(new RespostaAnalise(pesquisaCodigo, perguntaId, e.getId(),
                        e.getData(), tipo, opcao, perfil(e.getParticipante())));
            }
        }
        return saida;
    }

    private static TipoResposta converter(Resposta r) {
        switch (r.getEspecial()) {
            case NAO_SABE:      return TipoResposta.NAO_SABE;
            case NAO_RESPONDEU: return TipoResposta.NAO_RESPONDEU;
            default:            return TipoResposta.NENHUMA;
        }
    }

    private static Map<Dimensao, String> perfil(Participante p) {
        Map<Dimensao, String> m = new EnumMap<>(Dimensao.class);
        m.put(Dimensao.REGIAO, p.getRegiao().getDescricao());
        m.put(Dimensao.FAIXA_ETARIA, p.getFaixaEtaria().getDescricao());
        m.put(Dimensao.ESCOLARIDADE, p.getEscolaridade().getDescricao());
        m.put(Dimensao.GENERO, p.getGenero().getDescricao());
        m.put(Dimensao.FAIXA_RENDA, p.getFaixaRenda().getDescricao());
        return m;
    }
}
