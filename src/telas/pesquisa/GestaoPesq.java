package telas.pesquisa;

import javafx.scene.Parent;
import telas.Contexto;
import telas.Tela;

/** Seção "Pesquisas" do menu principal. */
public class GestaoPesq implements Tela {

    @Override
    public Parent getRoot() {
        return new TelaConsultaPesquisasView(Contexto.pesquisas);
    }
}