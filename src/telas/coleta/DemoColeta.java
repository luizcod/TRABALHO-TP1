package telas.coleta;

import dominio.administracao.CategoriaParametro;
import dominio.administracao.Perfil;
import dominio.administracao.Usuario;
import dominio.candidatospartidos.Cargo;
import dominio.pesquisas.Pergunta;
import dominio.pesquisas.Pesquisa;
import dominio.pesquisas.SituPesquisa;
import persistencia.administracao.RepositorioParametroArquivo;
import persistencia.administracao.RepositorioUsuarioArquivo;
import persistencia.coleta.RepositorioColetaArquivo;
import persistencia.pesquisas.PesquisaRepositoryArquivo;
import servicos.administracao.Autenticacao;
import servicos.administracao.ParametroServico;
import servicos.coleta.ColetaServico;
import servicos.pesquisas.PesquisaService;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumSet;

/**
 * SÓ PARA TESTE VISUAL: abre a tela Registrar Entrevista numa janela, com dados de exemplo
 * gravados numa pasta temporária (não mexe na pasta "dados" do projeto).
 * Pode ser apagada quando a janela principal e o login estiverem prontos.
 */
public class DemoColeta {
    public static void main(String[] args) throws Exception {
        Path d = Files.createTempDirectory("demo-coleta");

        // usuário logado (Administrador passa em qualquer verificação de perfil)
        var repoUsuarios = new RepositorioUsuarioArquivo(d + "/u.dat");
        repoUsuarios.salvar(new Usuario("Admin", "admin@sistema.com", "admin1234",
                EnumSet.of(Perfil.ADMINISTRADOR)));
        Autenticacao.getInstancia().configurar(repoUsuarios);
        Autenticacao.getInstancia().login("admin@sistema.com", "admin1234");

        // parâmetros
        var parametros = new ParametroServico(new RepositorioParametroArquivo(d + "/p.dat"));
        parametros.cadastrarFaixaEtaria(16, 24);
        parametros.cadastrarFaixaEtaria(25, 34);
        parametros.cadastrar(CategoriaParametro.ESCOLARIDADE, "Ensino médio");
        parametros.cadastrar(CategoriaParametro.ESCOLARIDADE, "Ensino superior");
        parametros.cadastrar(CategoriaParametro.REGIAO, "Sudeste");
        parametros.cadastrar(CategoriaParametro.REGIAO, "Sul");
        parametros.cadastrarFaixaRenda(0, 3000);
        parametros.cadastrarFaixaRenda(3001, 8000);

        // pesquisa em coleta com 3 perguntas
        var repoPesquisas = new PesquisaRepositoryArquivo(d + "/pq.dat");
        var pesquisas = new PesquisaService(repoPesquisas);
        Pesquisa p = new Pesquisa("005", "Cenário eleitoral - São Paulo", Cargo.GOVERNADOR, null,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(20), 50);
        Pergunta q1 = new Pergunta("Se a eleição para governador fosse hoje, em quem você votaria?", true);
        q1.adicionarOpcaoResposta("Ana Souza");
        q1.adicionarOpcaoResposta("Carlos Lima");
        q1.adicionarOpcaoResposta("Marina Costa");
        Pergunta q2 = new Pergunta("Seu voto para governador já está decidido?", true);
        q2.adicionarOpcaoResposta("Sim");
        q2.adicionarOpcaoResposta("Não");
        q2.adicionarOpcaoResposta("Ainda posso mudar");
        Pergunta q3 = new Pergunta("Qual área deve ser prioridade para o próximo governo?", false);
        q3.adicionarOpcaoResposta("Saúde");
        q3.adicionarOpcaoResposta("Educação");
        q3.adicionarOpcaoResposta("Segurança pública");
        p.adicionarPergunta(q1);
        p.adicionarPergunta(q2);
        p.adicionarPergunta(q3);
        p.setSituacao(SituPesquisa.EM_COLETA);
        repoPesquisas.salvarPesquisa(p);

        // coleta + um entrevistador
        var coleta = new ColetaServico(new RepositorioColetaArquivo(d + "/c.dat"), pesquisas, parametros);
        coleta.cadastrarEntrevistador("E1", "Juliana Martins");
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Coleta e Amostra (teste)");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JTabbedPane abas = new JTabbedPane();
            TelaRegistrarEntrevista registrar =
                    new TelaRegistrarEntrevista(coleta, pesquisas, parametros, janela::dispose);
            TelaEntrevistadores entrevistadores = new TelaEntrevistadores(coleta);
            abas.addTab("Registrar Entrevista", registrar);
            abas.addTab("Entrevistadores", entrevistadores);
            // ao trocar de aba, recarrega as listas (ex.: novo entrevistador aparece no combo)
            abas.addChangeListener(e -> {
                registrar.recarregarListas();
                entrevistadores.recarregarListas();
            });
            janela.setContentPane(abas);
            janela.setSize(1000, 700);
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}