package telas;

import dominio.administracao.Perfil;
import dominio.administracao.Usuario;
import dominio.administracao.RelatorioUsuarios;
import dominio.pesquisas.SituPesquisa;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import servicos.administracao.Autenticacao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Inicio implements Tela {
    private final Autenticacao auth = Autenticacao.getInstancia();

    @Override
    public Parent getRoot() {
        Usuario u = auth.getUsuarioLogado();

        Label saudacao = new Label("Olá, " + u.getNome());
        saudacao.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        FlowPane cartoes = new FlowPane(15, 15);
        if (auth.temPermissao(Perfil.ADMINISTRADOR)) { //se é adm
            RelatorioUsuarios r = Contexto.usuarios.gerarRelatorio();
            cartoes.getChildren().addAll(
                    caixa("Usuários", String.valueOf(r.getTotal())),
                    caixa("Ativos", String.valueOf(r.getAtivos())),
                    caixa("Inativos", String.valueOf(r.getInativos())));
        }
        if (auth.temPermissao(Perfil.PESQUISADOR)) { // se é pesquisador
            cartoes.getChildren().addAll(
                    caixa("Planejadas", contar(SituPesquisa.PLANEJADA)),
                    caixa("Em coleta", contar(SituPesquisa.EM_COLETA)));
        }
        if (auth.temPermissao(Perfil.ANALISTA)) { // se é analista
            cartoes.getChildren().addAll(
                    caixa("Aguardando processamento", contar(SituPesquisa.ENCERRADA)),
                    caixa("Processadas", contar(SituPesquisa.PROCESSADA)));
        }


        VBox raiz = new VBox(15, saudacao, cartoes);
        raiz.setPadding(new Insets(25));
        return raiz;
    }

    private String contar(SituPesquisa s) {
        return String.valueOf(Contexto.pesquisas.listarPesquisasPorSituacao(s).size());
    }

    private VBox caixa(String titulo, String valor) {
        Label v = new Label(valor);
        v.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        VBox c = new VBox(4, v, new Label(titulo));
        c.setPadding(new Insets(15));
        c.setPrefWidth(170);
        c.setStyle("-fx-background-color: #f2f5fa; -fx-border-color: #d5dbe6");
        return c;
    }
}