package dominio.coleta;

import dominio.pesquisas.Pergunta;
import excecoes.ExcecaoRegraNegocio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Entrevista implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;                 // único dentro da pesquisa
    private final String pesquisaCodigo;
    private final Participante participante;
    private final Entrevistador entrevistador;
    private final LocalDate data;
    private final List<Resposta> respostas = new ArrayList<>();
    private boolean concluida;

    public Entrevista(String id, String pesquisaCodigo, Participante participante,
                      Entrevistador entrevistador, LocalDate data) {
        if (id == null || id.isBlank()) {
            throw new ExcecaoRegraNegocio("O identificador da entrevista é obrigatório.");
        }
        if (pesquisaCodigo == null || pesquisaCodigo.isBlank()) {
            throw new ExcecaoRegraNegocio("Toda entrevista deve estar vinculada a uma pesquisa.");
        }
        if (participante == null || entrevistador == null) {
            throw new ExcecaoRegraNegocio("A entrevista precisa de participante e entrevistador.");
        }
        this.id = id.trim().toUpperCase();
        this.pesquisaCodigo = pesquisaCodigo.trim().toUpperCase();
        this.participante = participante;
        this.entrevistador = entrevistador;
        this.data = (data != null) ? data : LocalDate.now();
    }

    /** Uma resposta por pergunta; entrevista concluída não pode ser alterada. */
    public void adicionarResposta(Resposta resposta) {
        exigirAberta();
        if (resposta == null) {
            throw new ExcecaoRegraNegocio("A resposta não pode ser nula.");
        }
        if (temRespostaPara(resposta.getPergunta())) {
            throw new ExcecaoRegraNegocio("A pergunta \"" + resposta.getPergunta().getTexto()
                    + "\" já foi respondida nesta entrevista.");
        }
        respostas.add(resposta);
    }

    /** Conclui se todas as perguntas obrigatórias da pesquisa têm resposta. */
    public void concluir(Collection<Pergunta> perguntasDaPesquisa) {
        exigirAberta();
        for (Pergunta p : perguntasDaPesquisa) {
            if (p.isObrigatoria() && !temRespostaPara(p)) {
                throw new ExcecaoRegraNegocio("Pergunta obrigatória sem resposta: " + p.getTexto());
            }
        }
        concluida = true;
    }

    public boolean temRespostaPara(Pergunta pergunta) {
        return respostas.stream().anyMatch(r -> r.getPergunta().equals(pergunta));
    }

    private void exigirAberta() {
        if (concluida) {
            throw new ExcecaoRegraNegocio("Entrevista concluída não pode ser alterada.");
        }
    }

    public String getId()                  { return id; }
    public String getPesquisaCodigo()      { return pesquisaCodigo; }
    public Participante getParticipante()  { return participante; }
    public Entrevistador getEntrevistador(){ return entrevistador; }
    public LocalDate getData()             { return data; }
    public boolean isConcluida()           { return concluida; }
    public List<Resposta> getRespostas()   { return Collections.unmodifiableList(respostas); }

    @Override
    public String toString() {
        return id + " (" + pesquisaCodigo + ") " + (concluida ? "concluída" : "em andamento");
    }
}
