package telas.coleta;

import dominio.administracao.*;
import dominio.candidatospartidos.Cargo;
import dominio.pesquisas.*;
import servicos.administracao.ParametroServico;
import servicos.coleta.ColetaServico;
import servicos.pesquisas.PesquisaService;

import java.time.LocalDate;
import java.util.List;

/**
 * Cria dados de exemplo só para testar as telas de Coleta: parâmetros (escolaridade, região,
 * faixa etária e faixa de renda), uma pesquisa "Em Coleta" com perguntas e plano amostral, e um
 * entrevistador. Só cria o que ainda não existe, então pode ser chamado a cada execução.
 *
 * Precisa ser chamado depois do login. Remova a chamada do Main antes da entrega final.
 */
public final class DadosExemplo {
    private DadosExemplo() {}

    public static void criar(ParametroServico parametros, PesquisaService pesquisas, ColetaServico coleta) {
        try {
            if (parametros.listarPorCategoria(CategoriaParametro.ESCOLARIDADE).isEmpty()) {
                parametros.cadastrar(CategoriaParametro.ESCOLARIDADE, "Ensino médio");
                parametros.cadastrar(CategoriaParametro.ESCOLARIDADE, "Ensino superior");
            }
            if (parametros.listarPorCategoria(CategoriaParametro.REGIAO).isEmpty()) {
                parametros.cadastrar(CategoriaParametro.REGIAO, "Norte");
                parametros.cadastrar(CategoriaParametro.REGIAO, "Sudeste");
            }
            if (parametros.listarPorCategoria(CategoriaParametro.FAIXA_ETARIA).isEmpty()) {
                parametros.cadastrarFaixaEtaria(18, 24);
                parametros.cadastrarFaixaEtaria(25, 34);
            }
            if (parametros.listarPorCategoria(CategoriaParametro.FAIXA_RENDA).isEmpty()) {
                parametros.cadastrarFaixaRenda(0, 2000);
                parametros.cadastrarFaixaRenda(2001, 5000);
            }

            if (pesquisas.listarPesquisas().isEmpty()) {
                // "Intenção de voto" exigiria candidatos associados; por isso o exemplo usa outro tipo
                TipoPesquisa tipo = (TipoPesquisa) parametros.listarPorCategoria(CategoriaParametro.TIPO_PESQUISA).stream()
                        .filter(t -> !t.getDescricao().toLowerCase().startsWith("inten"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Nenhum tipo de pesquisa diferente de Intenção de voto."));
                List<Parametro> regioes = parametros.listarPorCategoria(CategoriaParametro.REGIAO);

                Pesquisa p = new Pesquisa("P001", "Rejeição presidencial 2026", Cargo.PRESIDENTE, tipo,
                        LocalDate.now(), LocalDate.now().plusDays(30), 10);
                p.definirRegioes(List.of((Regiao) regioes.get(0), (Regiao) regioes.get(1)));
                pesquisas.cadastrarPesquisa(p);
                pesquisas.adicionarPergunta("P001", new Pergunta("Você rejeita o candidato A?", true,
                        List.of(new OpcaoResposta("Sim"), new OpcaoResposta("Não"))));
                pesquisas.adicionarPergunta("P001", new Pergunta("Qual é a sua principal preocupação?", false,
                        List.of(new OpcaoResposta("Economia"), new OpcaoResposta("Saúde"), new OpcaoResposta("Segurança"))));

                PlanoAmostral plano = new PlanoAmostral("Amostragem por cotas");
                plano.adicionarMeta("Norte", 3);
                plano.adicionarMeta("Sudeste", 2);
                plano.adicionarMeta("Ensino médio", 2);
                plano.adicionarMeta("Ensino superior", 1);
                pesquisas.definirPlanoAmostral("P001", plano);
                pesquisas.alterarSituacao("P001", SituPesquisa.EM_COLETA);
            }

            if (coleta.listarEntrevistadores().isEmpty()) {
                coleta.cadastrarEntrevistador("E1", "Maria Souza");
            }
        } catch (RuntimeException e) {
            System.out.println("Não foi possível criar os dados de exemplo: " + e.getMessage());
        }
    }
}