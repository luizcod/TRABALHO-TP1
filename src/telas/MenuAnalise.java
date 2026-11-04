package telas;

import servicos.analiseEstatistica.AnaliseService;
import dominio.analiseEstatistica.Dimensao;
import dominio.analiseEstatistica.FiltroRelatorio;
import dominio.analiseEstatistica.PesquisaResumo;
import dominio.analiseEstatistica.Relatorio;
import dominio.analiseEstatistica.Resultado;
import persistencia.analiseEstatistica.FormatoExportacao;
import excecoes.ExcecaoRegraNegocio;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Menu em console do módulo de Análise (perfil Analista / Administrador). */
public class MenuAnalise {

    private static final String PERGUNTA_PADRAO = "Q1";

    private final AnaliseService servico;
    private final Scanner in;
    private Relatorio ultimoRelatorio;

    public MenuAnalise(AnaliseService servico, Scanner in) {
        this.servico = servico;
        this.in = in;
    }

    public void executar() {
        while (true) {
            System.out.println("\n=== Análise Estatística e Relatórios ===");
            System.out.println("1) Listar pesquisas");
            System.out.println("2) Processar pesquisa");
            System.out.println("3) Gerar relatório / resultados");
            System.out.println("4) Comparar pesquisas");
            System.out.println("5) Exportar último relatório (PDF ou CSV)");
            System.out.println("0) Voltar");
            String op = ler("Opção");
            if (op == null || op.equals("0")) {
                return;
            }
            try {
                switch (op) {
                    case "1" -> listar();
                    case "2" -> processar();
                    case "3" -> relatorio();
                    case "4" -> comparar();
                    case "5" -> exportar();
                    default -> System.out.println("Opção inválida.");
                }
            } catch (ExcecaoRegraNegocio e) {
                System.out.println("[Aviso] " + e.getMessage());
            }
        }
    }

    private void listar() {
        System.out.printf("%-6s %-34s %-12s %-14s %s%n", "Cód.", "Nome", "Cargo", "Tipo", "Situação");
        for (PesquisaResumo p : servico.listarPesquisas()) {
            System.out.printf("%-6s %-34s %-12s %-14s %s%n", p.codigo(), p.nome(), p.cargo(),
                    p.tipoPesquisa(), p.situacao());
        }
    }

    private void processar() {
        String codigo = ler("Código da pesquisa");
        String pergunta = lerComPadrao("Id da pergunta", PERGUNTA_PADRAO);
        Resultado r = servico.processarPesquisa(codigo, pergunta);
        System.out.println("Pesquisa processada: " + r.totalRespostas() + " respostas ("
                + r.totalValidas() + " válidas).");
    }

    private void relatorio() {
        String codigo = ler("Código da pesquisa");
        String pergunta = lerComPadrao("Id da pergunta", PERGUNTA_PADRAO);
        System.out.println("Filtros (Enter para ignorar):");
        FiltroRelatorio.Builder b = FiltroRelatorio.builder();
        b.candidato(ler("  Candidato"));
        for (Dimensao d : Dimensao.values()) {
            b.caracteristica(d, ler("  " + d.rotulo()));
        }
        LocalDate ini = lerData("  Data inicial (aaaa-mm-dd)");
        LocalDate fim = lerData("  Data final (aaaa-mm-dd)");
        b.periodo(ini, fim);
        String cruz = lerComPadrao("Incluir cruzamentos entre características? (s/n)", "n");
        ultimoRelatorio = servico.gerarRelatorio(codigo, pergunta, b.construir(), cruz.equalsIgnoreCase("s"));
        servico.exportar(ultimoRelatorio, FormatoExportacao.TELA, null);
    }

    private void comparar() {
        String a = ler("Código da pesquisa A");
        String b = ler("Código da pesquisa B");
        ultimoRelatorio = servico.compararPesquisas(a, PERGUNTA_PADRAO, b, PERGUNTA_PADRAO);
        servico.exportar(ultimoRelatorio, FormatoExportacao.TELA, null);
    }

    private void exportar() {
        if (ultimoRelatorio == null) {
            throw new ExcecaoRegraNegocio("Gere um relatório antes de exportar.");
        }
        String f = lerComPadrao("Formato (1=PDF, 2=CSV)", "1");
        FormatoExportacao formato = f.equals("2") ? FormatoExportacao.CSV : FormatoExportacao.PDF;
        String ext = formato == FormatoExportacao.CSV ? ".csv" : ".pdf";
        String nome = lerComPadrao("Arquivo de saída", "relatorio" + ext);
        servico.exportar(ultimoRelatorio, formato, Path.of(nome));
        System.out.println("Relatório exportado para " + Path.of(nome).toAbsolutePath());
    }

    private String ler(String rotulo) {
        System.out.print(rotulo + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private String lerComPadrao(String rotulo, String padrao) {
        String v = ler(rotulo + " [" + padrao + "]");
        return (v == null || v.isEmpty()) ? padrao : v;
    }

    private LocalDate lerData(String rotulo) {
        String v = ler(rotulo);
        if (v == null || v.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new ExcecaoRegraNegocio("Data inválida: " + v + " (use aaaa-mm-dd).");
        }
    }
}
