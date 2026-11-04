package dominio.coleta;

import excecoes.ExcecaoRegraNegocio;

import java.io.Serializable;

public class Entrevistador implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigo;
    private String nome;

    public Entrevistador(String codigo, String nome) {
        if (codigo == null || codigo.isBlank()) {
            throw new ExcecaoRegraNegocio("O código do entrevistador é obrigatório.");
        }
        this.codigo = codigo.trim().toUpperCase();
        setNome(nome);
    }

    public String getCodigo() { return codigo; }
    public String getNome()   { return nome; }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ExcecaoRegraNegocio("O nome do entrevistador é obrigatório.");
        }
        this.nome = nome.trim();
    }

    @Override
    public String toString() { return codigo + " - " + nome; }
}
