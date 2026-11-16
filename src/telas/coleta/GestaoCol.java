package telas.coleta;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import telas.Tela;


public class GestaoCol implements Tela{
    @Override
    public Parent getRoot() {
        BorderPane raiz = new BorderPane();
        return raiz;
    }
}
