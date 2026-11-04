package servicos.administracao;

import dominio.administracao.*;
import excecoes.ExcecaoRegraNegocio;

import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Path;

public class UsuarioServico {
    private final RepositorioUsuario repositorio;
    private final Autenticacao autenticacao = Autenticacao.getInstancia();
    private final ExportadorRelatorioUsuarios exportador;

    public UsuarioServico(RepositorioUsuario repositorio, ExportadorRelatorioUsuarios exportador) {
        this.repositorio = repositorio;
        this.exportador = exportador;
    }
    // create
    public Usuario cadastrar(String nome, String email, String senha, Set<Perfil> perfis) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);   // só admin cria contas
        garantirEmailDisponivel(email, null);
        Usuario novo = new Usuario(nome, email, senha, perfis);  // o construtor valida tudo
        repositorio.salvar(novo);
        return novo;
    }

    // read
    public List<Usuario> listar() {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        return repositorio.listar();
    }

    public Usuario buscarPorId(String id) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ExcecaoRegraNegocio("Usuário não encontrado."));
    }

    public List<Usuario> pesquisar(String nome, Perfil perfil, Boolean ativo) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        return repositorio.listar().stream()
                .filter(u -> nome == null || nome.isBlank()
                        || u.getNome().toLowerCase().contains(nome.trim().toLowerCase()))
                .filter(u -> perfil == null || u.temPerfil(perfil))
                .filter(u -> ativo == null || u.getAtivo() == ativo)
                .collect(Collectors.toList());
    }

    // relatórios
    public RelatorioUsuarios gerarRelatorio() {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        return RelatorioUsuarios.de(repositorio.listar());
    }

    public void exportarRelatorio(Path destino) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        exportador.exportar(RelatorioUsuarios.de(repositorio.listar()), destino);
    }

    // update
    public void editar(String id, String nome, String email, Set<Perfil> perfis) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        Usuario u = buscarPorId(id);

        // valida tudo ANTES de alterar, para não deixar o objeto pela metade
        ValidadorUsuario.validarEmail(email);
        garantirEmailDisponivel(email, u);
        if (perfis == null || perfis.isEmpty())
            throw new ExcecaoRegraNegocio("O usuário deve ter pelo menos um perfil.");
        if (u.temPerfil(Perfil.ADMINISTRADOR) && !perfis.contains(Perfil.ADMINISTRADOR))
            garantirOutroAdminAtivo(u);

        u.setNome(nome);
        u.setEmail(email);
        u.setPerfis(perfis);
        repositorio.salvar(u);
    }

    public void alterarSenha(String id, String novaSenha) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        Usuario u = buscarPorId(id);
        u.setSenha(novaSenha);
        repositorio.salvar(u);
    }

    public void ativar(String id) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        Usuario u = buscarPorId(id);
        u.ativar();
        repositorio.salvar(u);
    }

    public void desativar(String id) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        Usuario u = buscarPorId(id);
        impedirAutoAlteracao(u, "desativar a própria conta");
        if (u.temPerfil(Perfil.ADMINISTRADOR)) garantirOutroAdminAtivo(u);
        u.desativar();
        repositorio.salvar(u);
    }

    // delete
    public void excluir(String id) {
        autenticacao.exigirPerfil(Perfil.ADMINISTRADOR);
        Usuario u = buscarPorId(id);
        impedirAutoAlteracao(u, "excluir a própria conta");
        if (u.temPerfil(Perfil.ADMINISTRADOR)) garantirOutroAdminAtivo(u);
        repositorio.remover(id);
    }

    //regras aux
    private void garantirEmailDisponivel(String email, Usuario atual) {
        repositorio.buscarPorEmail(email).ifPresent(existente -> {
            if (atual == null || !existente.getId().equals(atual.getId()))
                throw new ExcecaoRegraNegocio("Já existe um usuário com este e-mail.");
        });
    }

    private void impedirAutoAlteracao(Usuario alvo, String acao) {
        if (alvo.getId().equals(autenticacao.getUsuarioLogado().getId()))
            throw new ExcecaoRegraNegocio("Você não pode " + acao + ".");
    }

    // O sistema nunca pode ficar sem administrador ativo
    private void garantirOutroAdminAtivo(Usuario excecao) {
        boolean existe = repositorio.listar().stream().anyMatch(x ->
                !x.getId().equals(excecao.getId())
                        && x.getAtivo() && x.temPerfil(Perfil.ADMINISTRADOR));
        if (!existe)
            throw new ExcecaoRegraNegocio("O sistema precisa ter pelo menos um administrador ativo.");
    }


}