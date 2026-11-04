package persistencia.analiseEstatistica;

import dominio.analiseEstatistica.Relatorio;

import java.io.IOException;
import java.nio.file.Path;

/** Estratégia de exportação de relatórios (padrão Strategy). */
public interface Exportador {

    /** Exporta o relatório para o destino. Exportadores de tela ignoram o destino. */
    void exportar(Relatorio r, Path destino) throws IOException;

    /** Extensão sugerida para o arquivo (vazia para tela). */
    String extensao();
}
