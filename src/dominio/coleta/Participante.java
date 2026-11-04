package dominio.coleta;

import dominio.administracao.Escolaridade;
import dominio.administracao.FaixaEtaria;
import dominio.administracao.FaixaRenda;
import dominio.administracao.Regiao;
import excecoes.ExcecaoRegraNegocio;

import java.io.Serializable;

/** Participante fictício e suas características de amostra (parâmetros cadastrados em Administração). */
public class Participante implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final Genero genero;
    private final FaixaEtaria faixaEtaria;
    private final Escolaridade escolaridade;
    private final Regiao regiao;
    private final FaixaRenda faixaRenda;

    public Participante(String codigo, Genero genero, FaixaEtaria faixaEtaria,
                        Escolaridade escolaridade, Regiao regiao, FaixaRenda faixaRenda) {
        if (codigo == null || codigo.isBlank()) {
            throw new ExcecaoRegraNegocio("O código do participante é obrigatório.");
        }
        if (genero == null || faixaEtaria == null || escolaridade == null
                || regiao == null || faixaRenda == null) {
            throw new ExcecaoRegraNegocio("O participante precisa ter gênero, faixa etária, "
                    + "escolaridade, região e faixa de renda.");
        }
        this.codigo = codigo.trim().toUpperCase();
        this.genero = genero;
        this.faixaEtaria = faixaEtaria;
        this.escolaridade = escolaridade;
        this.regiao = regiao;
        this.faixaRenda = faixaRenda;
    }

    public String getCodigo()            { return codigo; }
    public Genero getGenero()            { return genero; }
    public FaixaEtaria getFaixaEtaria()  { return faixaEtaria; }
    public Escolaridade getEscolaridade(){ return escolaridade; }
    public Regiao getRegiao()            { return regiao; }
    public FaixaRenda getFaixaRenda()    { return faixaRenda; }

    @Override
    public String toString() {
        return codigo + " [" + genero + ", " + faixaEtaria + ", " + escolaridade + ", "
                + regiao + ", " + faixaRenda + "]";
    }
}
