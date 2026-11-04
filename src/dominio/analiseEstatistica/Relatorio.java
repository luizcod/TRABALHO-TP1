package dominio.analiseEstatistica;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Relatório genérico: título, linhas de cabeçalho e seções em forma de tabela. */
public final class Relatorio {

    /** Uma tabela do relatório. */
    public record Secao(String titulo, List<String> colunas, List<String[]> linhas) { }

    private final String titulo;
    private final List<String> cabecalho = new ArrayList<>();
    private final List<Secao> secoes = new ArrayList<>();
    private final LocalDateTime geradoEm = LocalDateTime.now();

    public Relatorio(String titulo) {
        this.titulo = titulo;
    }

    public Relatorio adicionarCabecalho(String linha) {
        cabecalho.add(linha);
        return this;
    }

    public Relatorio adicionarSecao(Secao secao) {
        secoes.add(secao);
        return this;
    }

    public String titulo() {
        return titulo;
    }

    public LocalDateTime geradoEm() {
        return geradoEm;
    }

    public List<String> cabecalho() {
        return Collections.unmodifiableList(cabecalho);
    }

    public List<Secao> secoes() {
        return Collections.unmodifiableList(secoes);
    }

    /** Texto com colunas alinhadas (usado na tela e no PDF). */
    public List<String> linhasTexto() {
        List<String> out = new ArrayList<>();
        out.add(titulo);
        out.add("=".repeat(Math.min(titulo.length(), 90)));
        out.addAll(cabecalho);
        out.add("");
        for (Secao s : secoes) {
            out.add(s.titulo());
            int n = s.colunas().size();
            int[] larg = new int[n];
            for (int i = 0; i < n; i++) {
                larg[i] = s.colunas().get(i).length();
            }
            for (String[] l : s.linhas()) {
                for (int i = 0; i < n; i++) {
                    larg[i] = Math.max(larg[i], l[i].length());
                }
            }
            out.add(formatar(s.colunas().toArray(new String[0]), larg));
            out.add("-".repeat(Math.min(90, java.util.Arrays.stream(larg).sum() + 2 * (n - 1))));
            for (String[] l : s.linhas()) {
                out.add(formatar(l, larg));
            }
            out.add("");
        }
        return out;
    }

    private static String formatar(String[] celulas, int[] larg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < celulas.length; i++) {
            if (i > 0) {
                sb.append("  ");
            }
            sb.append(String.format("%-" + larg[i] + "s", celulas[i]));
        }
        return sb.toString().stripTrailing();
    }
}
