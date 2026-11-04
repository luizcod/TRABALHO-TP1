package persistencia.administracao;

import dominio.administracao.ExportadorRelatorioUsuarios;
import dominio.administracao.RelatorioUsuarios;
import excecoes.ExcecaoRegraNegocio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class ExportadorCsvUsuarios implements ExportadorRelatorioUsuarios {
    private static final String BOM = "\uFEFF";
    private static final String QUEBRA = "\r\n";

    @Override
    public void exportar(RelatorioUsuarios r, Path destino) {
        StringBuilder csv = new StringBuilder(BOM);
        csv.append("Indicador;Quantidade").append(QUEBRA);
        csv.append("Total de usuários;").append(r.getTotal()).append(QUEBRA);
        csv.append("Usuários ativos;").append(r.getAtivos()).append(QUEBRA);
        csv.append("Usuários inativos;").append(r.getInativos()).append(QUEBRA);
        r.getPorPerfil().forEach((perfil, qtd) ->
                csv.append("Perfil ").append(perfil).append(';').append(qtd).append(QUEBRA));
        csv.append(QUEBRA);
        csv.append("Gerado em;")
                .append(r.getGeradoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append(QUEBRA);

        try {
            if (destino.getParent() != null) {
                Files.createDirectories(destino.getParent());
            }
            Files.writeString(destino, csv, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExcecaoRegraNegocio("Não foi possível exportar o relatório de usuários.", e);
        }
    }
}