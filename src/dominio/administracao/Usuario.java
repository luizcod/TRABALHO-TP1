package dominio.administracao;

import excecoes.ExcecaoRegraNegocio;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;


public class Usuario implements Serializable{
    private static final long serialVersionUID = 1L;
    private String nome;
    private String email;
    private String senhaHash;
    private String salt;
    private final String id;
    private boolean ativo;
    private final Set<Perfil> perfis = EnumSet.noneOf(Perfil.class);

    public Usuario(String nome, String email, String senha, Set<Perfil> perfis) {
        this.id = UUID.randomUUID().toString();
        setNome(nome);
        setEmail(email);
        setSenha(senha);
        setPerfis(perfis);
        this.ativo = true;
    }

    //REgra de acesso
    public boolean autenticar(String senhaInformada) {
        boolean senhaCorreta = senhaHash.equals(gerarHash(senhaInformada, salt));
        if (!senhaCorreta) {
            return false;
        }
        if (!ativo) {
            throw new ExcecaoRegraNegocio("Usuário inativo não pode acessar o sistema.");
        }
        return true;
    }

    public boolean temPerfil(Perfil perfil) {
        return perfis.contains(perfil);
    }

                //Admin tem acesso total
    public boolean podeAcessar(Perfil perfilExigido) {
        return perfis.contains(Perfil.ADMINISTRADOR) || perfis.contains(perfilExigido);
    }

    public void ativar(){ 
        this.ativo = true; 
    }
    public void desativar(){ 
        this.ativo = false; 
    }

    //Getters
    public String getId(){
        return id;
    }

    public String getNome(){
        return nome;
    }

    public String getEmail(){
        return email;
    }

    public boolean getAtivo(){
        return ativo;
    }

    public Set<Perfil> getPerfis(){ //ninguém altera por fora
        return Collections.unmodifiableSet(perfis);
    }

    //Setters
    public void setNome(String nome){
        if(nome == null || nome.isBlank()){
            throw new ExcecaoRegraNegocio("O nome do usuário é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public void setEmail(String email) {
        ValidadorUsuario.validarEmail(email);
        this.email = email.trim().toLowerCase();
    }

    public void setSenha(String senha) {
        ValidadorUsuario.validarSenha(senha);
        this.salt = gerarSalt();
        this.senhaHash = gerarHash(senha, salt);
    }

    public void setPerfis(Set<Perfil> novosPerfis) {
        if (novosPerfis == null || novosPerfis.isEmpty()) {
            throw new ExcecaoRegraNegocio("O usuário deve ter pelo menos um perfil.");
        }
        perfis.clear();
        perfis.addAll(novosPerfis);
    }

    //Hash senha
    private static String gerarSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String gerarHash(String senha, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((salt + senha).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    @Override
    public String toString() {
        return nome + " <" + email + "> " + perfis + (ativo ? "" : " [inativo]");
    }

}

