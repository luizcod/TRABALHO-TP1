package dominio.administracao;

import java.nio.file.Path;

public interface ExportadorRelatorioUsuarios {
    void exportar(RelatorioUsuarios relatorio, Path destino);
}