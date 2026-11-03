package coleta;

public class Entrevista {
    private String id;
    private boolean concluida;
    private Participante participante;
    private Entrevistador entrevistador;

    public Entrevista(String id, Participante participante, Entrevistador entrevistador) {
        this.id = id;
        this.participante = participante;
        this.entrevistador = entrevistador;
        this.concluida = false; 
    }

    public void concluirEntrevista() {
        this.concluida = true;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    public Participante getParticipante() {
        return participante;
    }

    public void setParticipante(Participante participante) {
        this.participante = participante;
    }

    public Entrevistador getEntrevistador() {
        return entrevistador;
    }

    public void setEntrevistador(Entrevistador entrevistador) {
        this.entrevistador = entrevistador;
    }
}