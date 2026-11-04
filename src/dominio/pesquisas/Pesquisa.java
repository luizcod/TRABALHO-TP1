package dominio.pesquisas;

import dominio.administracao.Regiao;
import dominio.administracao.TipoPesquisa;
import dominio.candidatospartidos.Candidato;
import dominio.candidatospartidos.Cargo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade de Domínio representando uma Pesquisa Eleitoral simulada.
 * codigo, nome, cargo [Enum Cargo], tipoPesquisa [parâmetro de Administração],
 * regioes [parâmetros de Administração], dataInicio, dataFim, qtdPrevista,
 * situacao [Enum SituPesquisa], lista de Pergunta, lista de Candidato associados, PlanoAmostral.
 */
public class Pesquisa implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Descrição do tipo de pesquisa que exige candidatos associados. */
    public static final String TIPO_INTENCAO_DE_VOTO = "Intenção de voto";

    private String codigo;
    private String nome;
    private Cargo cargo;
    private TipoPesquisa tipoPesquisa;
    private List<Regiao> regioes;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private int qtdPrevista;
    private SituPesquisa situacao;
    private List<Pergunta> perguntas;
    private List<Candidato> candidatosAssociados;
    private PlanoAmostral planoAmostral;

    public Pesquisa() {
        this.situacao = SituPesquisa.PLANEJADA;
        this.regioes = new ArrayList<>();
        this.perguntas = new ArrayList<>();
        this.candidatosAssociados = new ArrayList<>();
        this.planoAmostral = new PlanoAmostral();
    }

    public Pesquisa(String codigo, String nome, Cargo cargo, TipoPesquisa tipoPesquisa,
                    LocalDate dataInicio, LocalDate dataFim, int qtdPrevista) {
        this();
        this.codigo = codigo;
        this.nome = nome;
        this.cargo = cargo;
        this.tipoPesquisa = tipoPesquisa;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.qtdPrevista = qtdPrevista;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public TipoPesquisa getTipoPesquisa() {
        return tipoPesquisa;
    }

    public void setTipoPesquisa(TipoPesquisa tipoPesquisa) {
        this.tipoPesquisa = tipoPesquisa;
    }

    /** Regra: pesquisas de intenção de voto precisam de pelo menos um candidato. */
    public boolean isIntencaoDeVoto() {
        return tipoPesquisa != null
                && TIPO_INTENCAO_DE_VOTO.equalsIgnoreCase(tipoPesquisa.getDescricao());
    }

    // ---------- Abrangência (regiões) ----------

    public List<Regiao> getRegioes() {
        return Collections.unmodifiableList(regioes);
    }

    /** Substitui a abrangência da pesquisa, sem repetir regiões. */
    public void definirRegioes(List<Regiao> novasRegioes) {
        List<Regiao> nova = new ArrayList<>();
        if (novasRegioes != null) {
            for (Regiao r : novasRegioes) {
                if (r != null && !nova.contains(r)) {
                    nova.add(r);
                }
            }
        }
        this.regioes = nova;
    }

    public boolean abrangeRegiao(Regiao regiao) {
        return regiao != null && regioes.contains(regiao);
    }

    // ---------- Período e quantidade ----------

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public int getQtdPrevista() {
        return qtdPrevista;
    }

    public void setQtdPrevista(int qtdPrevista) {
        this.qtdPrevista = qtdPrevista;
    }

    public SituPesquisa getSituacao() {
        return situacao;
    }

    public void setSituacao(SituPesquisa situacao) {
        this.situacao = situacao;
    }

    // ---------- Perguntas ----------

    public List<Pergunta> getPerguntas() {
        return Collections.unmodifiableList(perguntas);
    }

    public void adicionarPergunta(Pergunta pergunta) {
        if (pergunta != null && !perguntas.contains(pergunta)) {
            perguntas.add(pergunta);
        }
    }

    public void removerPergunta(Pergunta pergunta) {
        perguntas.remove(pergunta);
    }

    // ---------- Candidatos ----------

    public List<Candidato> getCandidatosAssociados() {
        return Collections.unmodifiableList(candidatosAssociados);
    }

    public boolean contemCandidato(Candidato candidato) {
        return candidatosAssociados.contains(candidato);
    }

    public void associarCandidato(Candidato candidato) {
        if (candidato != null && !candidatosAssociados.contains(candidato)) {
            candidatosAssociados.add(candidato);
        }
    }

    public void desassociarCandidato(Candidato candidato) {
        candidatosAssociados.remove(candidato);
    }

    // ---------- Plano amostral ----------

    public PlanoAmostral getPlanoAmostral() {
        return planoAmostral;
    }

    public void setPlanoAmostral(PlanoAmostral planoAmostral) {
        this.planoAmostral = (planoAmostral != null) ? planoAmostral : new PlanoAmostral();
    }

    /**
     * Regra: Se a pesquisa estiver ENCERRADA ou PROCESSADA, bloquear edições/alterações.
     */
    public boolean isBloqueada() {
        return situacao != null && situacao.isBloqueadaParaEdicao();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pesquisa pesquisa = (Pesquisa) o;
        return Objects.equals(codigo != null ? codigo.trim().toUpperCase() : null,
                pesquisa.codigo != null ? pesquisa.codigo.trim().toUpperCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo != null ? codigo.trim().toUpperCase() : null);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Cargo: %s | Tipo: %s | Regiões: %s | Situação: %s | Prevista: %d | Candidatos: %d | Perguntas: %d",
                codigo, nome, cargo, tipoPesquisa, regioes, situacao, qtdPrevista,
                candidatosAssociados.size(), perguntas.size());
    }
}