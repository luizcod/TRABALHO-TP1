package persistencia.pesquisas;

import dominio.candidatospartidos.Cargo;
import dominio.pesquisas.Pesquisa;
import dominio.pesquisas.SituPesquisa;
import excecoes.PersistenciaException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação da persistência de Pesquisas Eleitorais em arquivo binário
 * através da serialização Java nativa.
 *
 * Padrão de Projeto aplicado: Singleton (opcional via getInstance()) e DAO/Repository.
 */
public class PesquisaRepositoryArquivo implements PesquisaRepository {

    private static final String DEFAULT_ARQUIVO_PESQUISAS = "dados/pesquisas.dat";

    private final File arquivoPesquisas;
    private final List<Pesquisa> pesquisasEmMemoria;

    private static PesquisaRepositoryArquivo instanciaSingleton;

    public static synchronized PesquisaRepositoryArquivo getInstance() throws PersistenciaException {
        if (instanciaSingleton == null) {
            instanciaSingleton = new PesquisaRepositoryArquivo();
        }
        return instanciaSingleton;
    }

    public PesquisaRepositoryArquivo() throws PersistenciaException {
        this(DEFAULT_ARQUIVO_PESQUISAS);
    }

    public PesquisaRepositoryArquivo(String caminhoArquivo) throws PersistenciaException {
        this.arquivoPesquisas = new File(caminhoArquivo);
        this.pesquisasEmMemoria = new ArrayList<>();
        garantirDiretorio();
        carregarDoArquivo();
    }

    private void garantirDiretorio() {
        if (arquivoPesquisas.getParentFile() != null && !arquivoPesquisas.getParentFile().exists()) {
            arquivoPesquisas.getParentFile().mkdirs();
        }
    }

    // =========================================================================
    // PERSISTÊNCIA EM DISCO (ObjectOutputStream / ObjectInputStream)
    // =========================================================================

    @SuppressWarnings("unchecked")
    private synchronized void carregarDoArquivo() throws PersistenciaException {
        if (!arquivoPesquisas.exists() || arquivoPesquisas.length() == 0) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivoPesquisas))) {
            List<Pesquisa> carregadas = (List<Pesquisa>) ois.readObject();
            pesquisasEmMemoria.clear();
            if (carregadas != null) {
                pesquisasEmMemoria.addAll(carregadas);
            }
        } catch (EOFException e) {
            // Arquivo vazio
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Falha ao ler o arquivo de pesquisas: " + arquivoPesquisas.getPath(), e);
        }
    }

    private synchronized void gravarNoArquivo() throws PersistenciaException {
        garantirDiretorio();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivoPesquisas))) {
            oos.writeObject(new ArrayList<>(pesquisasEmMemoria));
            oos.flush();
        } catch (IOException e) {
            throw new PersistenciaException("Falha ao gravar os dados de pesquisas no arquivo: " + arquivoPesquisas.getPath(), e);
        }
    }

    // =========================================================================
    // MÉTODOS DO REPOSITÓRIO
    // =========================================================================

    @Override
    public synchronized void salvarPesquisa(Pesquisa pesquisa) throws PersistenciaException {
        pesquisasEmMemoria.add(pesquisa);
        gravarNoArquivo();
    }

    @Override
    public synchronized Optional<Pesquisa> buscarPesquisaPorCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        return pesquisasEmMemoria.stream()
                .filter(p -> codigo.trim().equalsIgnoreCase(p.getCodigo()))
                .findFirst();
    }

    @Override
    public synchronized List<Pesquisa> listarPesquisas() {
        return Collections.unmodifiableList(new ArrayList<>(pesquisasEmMemoria));
    }

    @Override
    public synchronized List<Pesquisa> listarPesquisasPorCargo(Cargo cargo) {
        return pesquisasEmMemoria.stream()
                .filter(p -> p.getCargo() == cargo)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<Pesquisa> listarPesquisasPorSituacao(SituPesquisa situacao) {
        return pesquisasEmMemoria.stream()
                .filter(p -> p.getSituacao() == situacao)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void atualizarPesquisa(Pesquisa pesquisa) throws PersistenciaException {
        for (int i = 0; i < pesquisasEmMemoria.size(); i++) {
            if (pesquisasEmMemoria.get(i).getCodigo().equalsIgnoreCase(pesquisa.getCodigo())) {
                pesquisasEmMemoria.set(i, pesquisa);
                gravarNoArquivo();
                return;
            }
        }
    }

    @Override
    public synchronized void removerPesquisa(String codigo) throws PersistenciaException {
        if (codigo == null) return;
        boolean removido = pesquisasEmMemoria.removeIf(p -> codigo.trim().equalsIgnoreCase(p.getCodigo()));
        if (removido) {
            gravarNoArquivo();
        }
    }
}
