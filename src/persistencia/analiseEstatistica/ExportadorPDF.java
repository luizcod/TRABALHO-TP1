package persistencia.analiseEstatistica;

import dominio.analiseEstatistica.Relatorio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporta em PDF sem bibliotecas externas: gera um PDF de texto simples (fonte Courier),
 * suficiente para relatórios tabulares. Para gráficos, troque por PDFBox/iText mantendo a interface.
 */
public class ExportadorPDF implements Exportador {

    private static final Charset WIN_1252 = Charset.forName("windows-1252");
    private static final int LARGURA_MAX = 95;
    private static final int LINHAS_POR_PAGINA = 60;

    @Override
    public void exportar(Relatorio r, Path destino) throws IOException {
        List<List<String>> paginas = paginar(quebrar(r.linhasTexto()));
        int n = paginas.size();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        escrever(out, "%PDF-1.4\n");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < n; i++) {
            kids.append(4 + 2 * i).append(" 0 R ");
        }
        objeto(out, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        objeto(out, offsets, 2, "<< /Type /Pages /Kids [" + kids + "] /Count " + n + " >>");
        objeto(out, offsets, 3,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>");
        for (int i = 0; i < n; i++) {
            int idPagina = 4 + 2 * i;
            int idConteudo = idPagina + 1;
            objeto(out, offsets, idPagina, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + idConteudo + " 0 R >>");
            byte[] conteudo = conteudo(paginas.get(i)).getBytes(WIN_1252);
            offsets.add(out.size());
            escrever(out, idConteudo + " 0 obj\n<< /Length " + conteudo.length + " >>\nstream\n");
            out.write(conteudo);
            escrever(out, "\nendstream\nendobj\n");
        }

        int inicioXref = out.size();
        int total = offsets.size() + 1;
        escrever(out, "xref\n0 " + total + "\n0000000000 65535 f \n");
        for (int off : offsets) {
            escrever(out, String.format("%010d 00000 n \n", off));
        }
        escrever(out, "trailer\n<< /Size " + total + " /Root 1 0 R >>\nstartxref\n" + inicioXref + "\n%%EOF\n");

        ExportadorCSV.criarPasta(destino);
        Files.write(destino, out.toByteArray());
    }

    private static void objeto(ByteArrayOutputStream out, List<Integer> offsets, int id, String corpo) {
        offsets.add(out.size());
        escrever(out, id + " 0 obj\n" + corpo + "\nendobj\n");
    }

    private static void escrever(ByteArrayOutputStream out, String s) {
        byte[] b = s.getBytes(WIN_1252);
        out.write(b, 0, b.length);
    }

    private static String conteudo(List<String> linhas) {
        StringBuilder sb = new StringBuilder("BT\n/F1 9 Tf\n12 TL\n40 802 Td\n");
        for (String l : linhas) {
            sb.append('(').append(escapar(l)).append(") Tj T*\n");
        }
        return sb.append("ET").toString();
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static List<String> quebrar(List<String> linhas) {
        List<String> out = new ArrayList<>();
        for (String l : linhas) {
            if (l.length() <= LARGURA_MAX) {
                out.add(l);
                continue;
            }
            for (int i = 0; i < l.length(); i += LARGURA_MAX) {
                out.add(l.substring(i, Math.min(l.length(), i + LARGURA_MAX)));
            }
        }
        return out;
    }

    private static List<List<String>> paginar(List<String> linhas) {
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < linhas.size(); i += LINHAS_POR_PAGINA) {
            paginas.add(linhas.subList(i, Math.min(linhas.size(), i + LINHAS_POR_PAGINA)));
        }
        if (paginas.isEmpty()) {
            paginas.add(List.of(""));
        }
        return paginas;
    }

    @Override
    public String extensao() {
        return ".pdf";
    }
}
