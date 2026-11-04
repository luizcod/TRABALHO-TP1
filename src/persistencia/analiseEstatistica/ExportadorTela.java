package persistencia.analiseEstatistica;

import dominio.analiseEstatistica.Relatorio;

import java.io.PrintStream;
import java.nio.file.Path;

/** Exibe o relatório no console (formato visual em tela). */
public class ExportadorTela implements Exportador {

    private final PrintStream saida;

    public ExportadorTela() {
        this(System.out);
    }

    public ExportadorTela(PrintStream saida) {
        this.saida = saida;
    }

    @Override
    public void exportar(Relatorio r, Path destino) {
        r.linhasTexto().forEach(saida::println);
    }

    @Override
    public String extensao() {
        return "";
    }
}
