package persistencia.analiseEstatistica;

import dominio.analiseEstatistica.Relatorio;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Exporta em CSV (separador ';', UTF-8 com BOM, abre direto no Excel em português). */
public class ExportadorCSV implements Exportador {

    @Override
    public void exportar(Relatorio r, Path destino) throws IOException {
        StringBuilder sb = new StringBuilder("\uFEFF");
        linha(sb, List.of(r.titulo()));
        for (String c : r.cabecalho()) {
            linha(sb, List.of(c));
        }
        for (Relatorio.Secao s : r.secoes()) {
            sb.append('\n');
            linha(sb, List.of(s.titulo()));
            linha(sb, s.colunas());
            for (String[] l : s.linhas()) {
                linha(sb, List.of(l));
            }
        }
        criarPasta(destino);
        Files.writeString(destino, sb.toString(), StandardCharsets.UTF_8);
    }

    private static void linha(StringBuilder sb, List<String> celulas) {
        for (int i = 0; i < celulas.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(escapar(celulas.get(i)));
        }
        sb.append('\n');
    }

    private static String escapar(String v) {
        if (v.contains(";") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    static void criarPasta(Path destino) throws IOException {
        Path pai = destino.toAbsolutePath().getParent();
        if (pai != null) {
            Files.createDirectories(pai);
        }
    }

    @Override
    public String extensao() {
        return ".csv";
    }
}
