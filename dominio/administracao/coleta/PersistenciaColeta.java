package coleta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PersistenciaColeta {

    public static void salvarEntrevistaCSV(Entrevista entrevista, String nomeArquivo) {
        if (!entrevista.isConcluida()) {
            throw new ExcecaoRegraNegocio("Erro: Não é permitido salvar uma entrevista que ainda não foi concluída!");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo, true))) {
            writer.println(entrevista.getId() + ";" + 
                           entrevista.getParticipante().getCodigo() + ";" + 
                           entrevista.getEntrevistador().getCodigo() + ";" + 
                           entrevista.isConcluida());
            System.out.println("Entrevista salva com sucesso no ficheiro!");
        } catch (IOException e) {
            System.err.println("Erro ao gravar o ficheiro de persistência: " + e.getMessage());
        }
    }

    // Método para ler e exibir as entrevistas gravadas no ficheiro CSV
    public static void carregarEntrevistasCSV(String nomeArquivo) {
        System.out.println("\n--- A carregar entrevistas do ficheiro ---");
        try (BufferedReader reader = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                System.out.println("ID: " + dados[0] + " | Participante: " + dados[1] + 
                                   " | Entrevistador: " + dados[2] + " | Concluída: " + dados[3]);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o ficheiro: " + e.getMessage());
        }
    }
}