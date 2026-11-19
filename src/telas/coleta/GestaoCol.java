package telas.coleta;

import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import telas.Tela;

import java.util.function.Supplier;

/**
 * Seção "Coleta" do menu principal. Reúne as três telas do módulo em abas:
 * Registrar Entrevista, Acompanhar Pesquisas e Entrevistadores.
 * A tela de cada aba é criada de novo sempre que a aba é aberta, para mostrar dados atualizados
 * (por exemplo, um entrevistador recém-cadastrado já aparece em Registrar Entrevista).
 */
public class GestaoCol implements Tela {
    private final TabPane abas = new TabPane();

    @Override
    public Parent getRoot() {
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        adicionar("Registrar Entrevista", TelaRegistrarEntrevista::new);
        adicionar("Acompanhar Pesquisas", TelaAcompanharPesquisas::new);
        adicionar("Entrevistadores", TelaEntrevistadores::new);

        abas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> carregar(nova));
        carregar(abas.getSelectionModel().getSelectedItem());
        return abas;
    }

    private void adicionar(String titulo, Supplier<Tela> fabrica) {
        Tab aba = new Tab(titulo);
        aba.setUserData(fabrica);
        abas.getTabs().add(aba);
    }

    @SuppressWarnings("unchecked")
    private void carregar(Tab aba) {
        if (aba == null) return;
        Supplier<Tela> fabrica = (Supplier<Tela>) aba.getUserData();
        aba.setContent(fabrica.get().getRoot());
    }
}