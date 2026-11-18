package telas;

import javafx.application.Application;
import javafx.stage.Stage;
import servicos.administracao.ParametroServico;
import servicos.coleta.ColetaServico;
import servicos.pesquisas.PesquisaService;
import telas.coleta.TelaAcompanharPesquisas;
import telas.coleta.TelaRegistrarEntrevista;

import java.util.function.Consumer;

/**
 * Aplicação JavaFX que abre as telas do módulo Coleta. No JavaFX quem inicia o programa é uma
 * classe que estende Application; por isso o Main chama {@link #iniciar} e não abre a janela
 * diretamente.
 *
 * Uso no Main (depois do login):
 *   telas.AppColeta.iniciar(coleta, pesquisas, parametros, "entrevista");   // ou "acompanhar"
 */
public class AppColeta extends Application {
    private static ColetaServico coleta;
    private static PesquisaService pesquisas;
    private static ParametroServico parametros;
    private static String telaInicial = "entrevista";

    /** Abre a janela e só volta quando ela for fechada. Só pode ser chamado uma vez por execução. */
    public static void iniciar(ColetaServico c, PesquisaService p, ParametroServico pa, String tela) {
        coleta = c;
        pesquisas = p;
        parametros = pa;
        telaInicial = tela;
        Application.launch(AppColeta.class);
    }

    @Override
    public void start(Stage stage) {
        Consumer<String> navegar = item -> System.out.println("Clicou em: " + item);
        if ("acompanhar".equalsIgnoreCase(telaInicial)) {
            new TelaAcompanharPesquisas(coleta, pesquisas, navegar).abrir(stage);
        } else {
            new TelaRegistrarEntrevista(coleta, pesquisas, parametros, navegar).abrir(stage);
        }
    }
}