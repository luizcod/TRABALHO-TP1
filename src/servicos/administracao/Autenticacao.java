package servicos.administracao;

import dominio.administracao.Perfil;
import dominio.administracao.RepositorioUsuario;
import dominio.administracao.Usuario;
import excecoes.ExcecaoRegraNegocio;

import java.util.Optional;

public class Autenticacao {

    // única instância da classe Autenticacao
    private static class Holder {
        private static final Autenticacao INSTANCIA = new Autenticacao();
    }

    public static Autenticacao getInstancia() {
        return Holder.INSTANCIA;
    }

    private RepositorioUsuario repositorio;
    private Usuario usuarioLogado;

    private Autenticacao() {}

    // Deve ser chamado uma vez na inicialização do sistema
    public synchronized void configurar(RepositorioUsuario repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("Repositório de usuários é obrigatório.");
        }
        this.repositorio = repositorio;
    }

    //login/logout

    public synchronized Usuario login(String email, String senha) {
        if (repositorio == null) {
            throw new IllegalStateException("Autenticacao não foi configurada.");
        }
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new ExcecaoRegraNegocio("Informe e-mail e senha.");
        }

        Optional<Usuario> encontrado = repositorio.buscarPorEmail(email.trim().toLowerCase());
        if (encontrado.isEmpty()) {
            throw new ExcecaoRegraNegocio("E-mail ou senha incorretos.");
        }

        Usuario usuario = encontrado.get();
        if (!usuario.getAtivo()) {
            throw new ExcecaoRegraNegocio("Usuário inativo não pode acessar o sistema.");
        }
        if (!usuario.autenticar(senha)) {
            throw new ExcecaoRegraNegocio("E-mail ou senha incorretos.");
        }

        this.usuarioLogado = usuario;
        return usuario;
    }

    public synchronized void logout() {
        this.usuarioLogado = null;
    }

    //Consulta

    public synchronized boolean estaAutenticado() {
        return usuarioLogado != null;
    }

    public synchronized Usuario getUsuarioLogado() {
        exigirAutenticacao();
        return usuarioLogado;
    }

    //Controle de acesso

    //garante que há alguém logado e que continua ativo
    public synchronized void exigirAutenticacao() {
        if (usuarioLogado == null) {
            throw new ExcecaoRegraNegocio("É necessário fazer login para continuar.");
        }
        if (!usuarioLogado.getAtivo()) {
            usuarioLogado = null; // foi desativado durante a sessão
            throw new ExcecaoRegraNegocio("Usuário inativo não pode acessar o sistema.");
        }
    }

    //Exige um perfil e Administrador passa em qualquer verificação
    public synchronized void exigirPerfil(Perfil perfil) {
        exigirAutenticacao();
        if (!usuarioLogado.podeAcessar(perfil)) {
            throw new ExcecaoRegraNegocio(
                "Acesso negado: esta funcionalidade exige o perfil " + perfil + ".");
        }
    }

    // versão booleana do exigir perfil
    public synchronized boolean temPermissao(Perfil perfil) {
        return usuarioLogado != null
            && usuarioLogado.getAtivo()
            && usuarioLogado.podeAcessar(perfil);
    }
}