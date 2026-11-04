package persistencia.administracao;

import dominio.administracao.Parametro;
import dominio.administracao.RepositorioParametro;
import excecoes.ExcecaoRegraNegocio;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RepositorioParametroArquivo implements RepositorioParametro {
    private final Path arquivo;
    private final Map<String, Parametro> parametros = new LinkedHashMap<>();

    public RepositorioParametroArquivo(String caminho) {
        this.arquivo = Paths.get(caminho);
        carregar();
    }

    @Override
    public synchronized Optional<Parametro> buscarPorId(String id) {
        return Optional.ofNullable(parametros.get(id));
    }

    @Override
    public synchronized List<Parametro> listar() {
        return new ArrayList<>(parametros.values());
    }

    @Override
    public synchronized void salvar(Parametro parametro) {
        parametros.put(parametro.getId(), parametro);
        gravar();
    }

    @Override
    public synchronized void remover(String id) {
        parametros.remove(id);
        gravar();
    }

    // Arquivo

    @SuppressWarnings("unchecked")
    private void carregar() {
        if (!Files.exists(arquivo)) return;   // primeira execução começa vazio
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(arquivo))) {
            List<Parametro> lidos = (List<Parametro>) in.readObject();
            for (Parametro p : lidos) parametros.put(p.getId(), p);
        } catch (IOException | ClassNotFoundException e) {
            throw new ExcecaoRegraNegocio("Não foi possível ler o arquivo de parâmetros.", e);
        }
    }

    private void gravar() {
        Path temporario = Paths.get(arquivo + ".tmp");
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporario))) {
                out.writeObject(new ArrayList<>(parametros.values()));
            }
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ExcecaoRegraNegocio("Não foi possível gravar o arquivo de parâmetros.", e);
        }
    }
}