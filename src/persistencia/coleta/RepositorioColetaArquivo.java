package persistencia.coleta;

import dominio.coleta.Entrevista;
import dominio.coleta.Entrevistador;
import dominio.coleta.RepositorioColeta;
import excecoes.ExcecaoRegraNegocio;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RepositorioColetaArquivo implements RepositorioColeta {
    private final Path arquivo;
    private final Map<String, Entrevistador> entrevistadores = new LinkedHashMap<>();
    private final Map<String, Entrevista> entrevistas = new LinkedHashMap<>();

    public RepositorioColetaArquivo(String caminho) {
        this.arquivo = Paths.get(caminho);
        carregar();
    }

    private static String chave(String pesquisaCodigo, String id) {
        return pesquisaCodigo.trim().toUpperCase() + "|" + id.trim().toUpperCase();
    }

    @Override
    public synchronized Optional<Entrevistador> buscarEntrevistador(String codigo) {
        return Optional.ofNullable(codigo == null ? null : entrevistadores.get(codigo.trim().toUpperCase()));
    }

    @Override
    public synchronized List<Entrevistador> listarEntrevistadores() {
        return new ArrayList<>(entrevistadores.values());
    }

    @Override
    public synchronized void salvarEntrevistador(Entrevistador e) {
        entrevistadores.put(e.getCodigo(), e);
        gravar();
    }

    @Override
    public synchronized void removerEntrevistador(String codigo) {
        entrevistadores.remove(codigo.trim().toUpperCase());
        gravar();
    }

    @Override
    public synchronized Optional<Entrevista> buscarEntrevista(String pesquisaCodigo, String id) {
        return Optional.ofNullable(entrevistas.get(chave(pesquisaCodigo, id)));
    }

    @Override
    public synchronized List<Entrevista> listarEntrevistas(String pesquisaCodigo) {
        String alvo = pesquisaCodigo.trim().toUpperCase();
        List<Entrevista> saida = new ArrayList<>();
        for (Entrevista e : entrevistas.values()) {
            if (e.getPesquisaCodigo().equals(alvo)) saida.add(e);
        }
        return saida;
    }

    @Override
    public synchronized void salvarEntrevista(Entrevista e) {
        entrevistas.put(chave(e.getPesquisaCodigo(), e.getId()), e);
        gravar();
    }

    // ---------- arquivo ----------

    @SuppressWarnings("unchecked")
    private void carregar() {
        if (!Files.exists(arquivo)) return;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(arquivo))) {
            List<Entrevistador> es = (List<Entrevistador>) in.readObject();
            List<Entrevista> is = (List<Entrevista>) in.readObject();
            es.forEach(e -> entrevistadores.put(e.getCodigo(), e));
            is.forEach(i -> entrevistas.put(chave(i.getPesquisaCodigo(), i.getId()), i));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExcecaoRegraNegocio("Não foi possível ler o arquivo de coleta.", e);
        }
    }

    private void gravar() {
        Path temporario = Paths.get(arquivo + ".tmp");
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporario))) {
                out.writeObject(new ArrayList<>(entrevistadores.values()));
                out.writeObject(new ArrayList<>(entrevistas.values()));
            }
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ExcecaoRegraNegocio("Não foi possível gravar o arquivo de coleta.", e);
        }
    }
}
