package persistencia.administracao;

import dominio.administracao.RepositorioUsuario;
import dominio.administracao.Usuario;
import excecoes.ExcecaoRegraNegocio;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RepositorioUsuarioArquivo implements RepositorioUsuario {
    private final Path arquivo;
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();

    public RepositorioUsuarioArquivo(String caminho) {
        this.arquivo = Paths.get(caminho);
        carregar();
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        String procurado = email.trim().toLowerCase();
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equals(procurado))
                .findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public List<Usuario> listar() {
        return new ArrayList<>(usuarios.values());
    }

    @Override
    public void salvar(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
        gravar();
    }

    @Override
    public void remover(String id) {
        usuarios.remove(id);
        gravar();
    }

    // Arquivo

    @SuppressWarnings("unchecked")
    private void carregar() {
        if (!Files.exists(arquivo)) return;   // primeira execução começa vazio
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(arquivo))) {
            List<Usuario> lidos = (List<Usuario>) in.readObject();
            for (Usuario u : lidos) usuarios.put(u.getId(), u);
        } catch (IOException | ClassNotFoundException e) {
            throw new ExcecaoRegraNegocio("Não foi possível ler o arquivo de usuários.", e);
        }
    }

    private void gravar() {
        Path temporario = Paths.get(arquivo + ".tmp");
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporario))) {
                out.writeObject(new ArrayList<>(usuarios.values()));
            }
            // grava em arquivo temporário e troca no final para evitar corromper o original
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ExcecaoRegraNegocio("Não foi possível gravar o arquivo de usuários.", e);
        }
    }
}