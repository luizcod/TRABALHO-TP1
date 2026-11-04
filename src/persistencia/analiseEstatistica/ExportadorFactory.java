package persistencia.analiseEstatistica;

/** Cria o Exportador correto para cada formato (padrão Factory). Novos formatos = nova classe + um case. */
public final class ExportadorFactory {

    private ExportadorFactory() {
    }

    public static Exportador criar(FormatoExportacao formato) {
        return switch (formato) {
            case TELA -> new ExportadorTela();
            case PDF -> new ExportadorPDF();
            case CSV -> new ExportadorCSV();
        };
    }
}
