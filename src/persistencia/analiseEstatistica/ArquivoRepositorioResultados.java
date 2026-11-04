package persistencia.analiseEstatistica;

import dominio.analiseEstatistica.Resultado;
import excecoes.ExcecaoRegraNegocio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Guarda os resultados processados em arquivo, por serialização de objetos. */
public class ArquivoRepositorioResultados implements RepositorioResultados {

    private final Path arquivo;
    private final Map<String, Resultado> dados = new LinkedHashMap<>();

    public ArquivoRepositorioResultados(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    private static String chave(String pesquisaCodigo, String perguntaId) {
        return pesquisaCodigo + "#" + perguntaId;
    }

    @Override
    public synchronized void salvar(Resultado r) {
        dados.put(chave(r.pesquisaCodigo(), r.perguntaId()), r);
        persistir();
    }

    @Override
    public synchronized Optional<Resultado> buscar(String pesquisaCodigo, String perguntaId) {
        return Optional.ofNullable(dados.get(chave(pesquisaCodigo, perguntaId)));
    }

    @Override
    public synchronized List<Resultado> listar() {
        return new ArrayList<>(dados.values());
    }

    @SuppressWarnings("unchecked")
    private void carregar() {
        if (!Files.exists(arquivo)) {
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(arquivo))) {
            dados.putAll((Map<String, Resultado>) in.readObject());
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new ExcecaoRegraNegocio("Não foi possível ler o arquivo de resultados: " + arquivo, e);
        }
    }

    private void persistir() {
        try {
            Path pai = arquivo.toAbsolutePath().getParent();
            if (pai != null) {
                Files.createDirectories(pai);
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(arquivo))) {
                out.writeObject(new LinkedHashMap<>(dados));
            }
        } catch (IOException e) {
            throw new ExcecaoRegraNegocio("Não foi possível gravar o arquivo de resultados: " + arquivo, e);
        }
    }
}
