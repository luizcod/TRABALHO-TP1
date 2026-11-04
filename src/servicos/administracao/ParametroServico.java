package servicos.administracao;

import dominio.administracao.*;
import excecoes.ExcecaoRegraNegocio;
import servicos.administracao.Autenticacao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ParametroServico {
    private final RepositorioParametro repositorio;
    private final Autenticacao autenticacao = Autenticacao.getInstancia();
    private final List<Predicate<Parametro>> verificadoresDeUso = new ArrayList<>();

    public ParametroServico(RepositorioParametro repositorio) {
        this.repositorio = repositorio;
    }

    //Se o parametro está sendo utilizado não pode ser excluido
    public void registrarVerificadorDeUso(Predicate<Parametro> emUso) {
        verificadoresDeUso.add(emUso);
    }

    // create
    public Parametro cadastrar(CategoriaParametro categoria, String descricao) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        return registrar(CriaParametro.criar(categoria, descricao));
    }

    public FaixaEtaria cadastrarFaixaEtaria(int minima, int maxima) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        return (FaixaEtaria) registrar(CriaParametro.criarFaixaEtaria(minima, maxima));
    }

    public FaixaRenda cadastrarFaixaRenda(double minima, double maxima) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        return (FaixaRenda) registrar(CriaParametro.criarFaixaRenda(minima, maxima));
    }

    // read (qualquer usuário autenticado)
    public List<Parametro> listar() {
        autenticacao.exigirAutenticacao();
        return repositorio.listar();
    }

    public List<Parametro> listarPorCategoria(CategoriaParametro categoria) {
        autenticacao.exigirAutenticacao();
        return repositorio.listarPorCategoria(categoria);
    }

    public Parametro buscarPorId(String id) {
        autenticacao.exigirAutenticacao();
        return buscarOuFalhar(id);
    }

    // update
    /** Para Escolaridade, Região e Tipo de Pesquisa. Faixas são editadas pelos limites. */
    public void editarDescricao(String id, String novaDescricao) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        Parametro atual = buscarOuFalhar(id);
        if (atual instanceof FaixaEtaria || atual instanceof FaixaRenda) {
            throw new ExcecaoRegraNegocio("Faixas são editadas pelos seus limites, não pela descrição.");
        }
        // valida a descrição e checa duplicidade antes de alterar
        garantirSemConflito(CriaParametro.criar(atual.getCategoria(), novaDescricao), id);
        atual.setDescricao(novaDescricao);
        repositorio.salvar(atual);
    }

    public void editarFaixaEtaria(String id, int minima, int maxima) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        FaixaEtaria faixa = buscarComo(id, FaixaEtaria.class);
        garantirSemConflito(CriaParametro.criarFaixaEtaria(minima, maxima), id);
        faixa.setLimites(minima, maxima);
        repositorio.salvar(faixa);
    }

    public void editarFaixaRenda(String id, double minima, double maxima) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        FaixaRenda faixa = buscarComo(id, FaixaRenda.class);
        garantirSemConflito(CriaParametro.criarFaixaRenda(minima, maxima), id);
        faixa.setLimites(minima, maxima);
        repositorio.salvar(faixa);
    }

    // delete
    public void excluir(String id) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        Parametro p = buscarOuFalhar(id);
        for (Predicate<Parametro> emUso : verificadoresDeUso) {
            if (emUso.test(p)) {
                throw new ExcecaoRegraNegocio(
                        "O parâmetro \"" + p + "\" está em uso e não pode ser excluído.");
            }
        }
        repositorio.remover(id);
    }

    // regras auxiliares
    private Parametro registrar(Parametro novo) {
        garantirSemConflito(novo, null);
        repositorio.salvar(novo);
        return novo;
    }

    // Falha se o candidato conflita com algum parâmetro da mesma categoria (exceto o próprio)
    private void garantirSemConflito(Parametro candidato, String idIgnorado) {
        for (Parametro existente : repositorio.listarPorCategoria(candidato.getCategoria())) {
            if (existente.getId().equals(idIgnorado)) continue;
            if (candidato.conflitaCom(existente)) {
                throw new ExcecaoRegraNegocio(
                        "\"" + candidato + "\" conflita com o parâmetro já cadastrado \"" + existente + "\".");
            }
        }
    }

    private Parametro buscarOuFalhar(String id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ExcecaoRegraNegocio("Parâmetro não encontrado."));
    }

    private <T extends Parametro> T buscarComo(String id, Class<T> tipo) {
        Parametro p = buscarOuFalhar(id);
        if (!tipo.isInstance(p)) {
            throw new ExcecaoRegraNegocio("O parâmetro informado não é do tipo esperado.");
        }
        return tipo.cast(p);
    }
}