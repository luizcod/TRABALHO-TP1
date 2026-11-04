package dominio.candidatospartidos;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidade de Domínio representando um Candidato associável a Pesquisas Eleitorais.
 * Modelagem Etapa 2: nome, numero, cargo [Enum Cargo], partido [0..1], candidatura.
 */
public class Candidato implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private int numero;
    private Cargo cargo;
    private String partido; // 0..1 (sigla ou nome do partido)
    private Candidatura candidatura;

    public Candidato() {
        this.candidatura = new Candidatura();
    }

    public Candidato(String nome, int numero, Cargo cargo) {
        this.nome = nome;
        this.numero = numero;
        this.cargo = cargo;
        this.partido = null;
        this.candidatura = new Candidatura(Candidatura.SITUACAO_VALIDADA);
    }

    public Candidato(String nome, int numero, Cargo cargo, String partido) {
        this(nome, numero, cargo);
        this.partido = partido;
    }

    public Candidato(String nome, int numero, Cargo cargo, String partido, Candidatura candidatura) {
        this.nome = nome;
        this.numero = numero;
        this.cargo = cargo;
        this.partido = partido;
        this.candidatura = (candidatura != null) ? candidatura : new Candidatura();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public String getPartido() {
        return partido;
    }

    public void setPartido(String partido) {
        this.partido = partido;
    }

    public Candidatura getCandidatura() {
        return candidatura;
    }

    public void setCandidatura(Candidatura candidatura) {
        this.candidatura = (candidatura != null) ? candidatura : new Candidatura();
    }

    /**
     * Retorna se a candidatura está validada/ativa para participação em pesquisa.
     * Regra: somente candidaturas em situação permitida podem participar de pesquisas.
     */
    public boolean isAptoParaPesquisa() {
        return candidatura != null && candidatura.isValidaParaPesquisa();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candidato candidato = (Candidato) o;
        return numero == candidato.numero && cargo == candidato.cargo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, cargo);
    }

    @Override
    public String toString() {
        String part = (partido != null && !partido.isEmpty()) ? partido : "SEM PARTIDO";
        String sit = (candidatura != null) ? candidatura.getSituacao() : "INDEFINIDA";
        return String.format("%s [Nº %d, Cargo: %s, Partido: %s, Situação: %s]",
                nome, numero, cargo, part, sit);
    }
}
