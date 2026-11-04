package dominio.pesquisas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa uma pergunta que compõe o questionário da pesquisa.
 * Modelagem Etapa 2: texto, obrigatoria, lista de OpcaoResposta.
 */
public class Pergunta implements Serializable {
    private static final long serialVersionUID = 1L;

    private String texto;
    private boolean obrigatoria;
    private List<OpcaoResposta> opcoesResposta;

    public Pergunta() {
        this.opcoesResposta = new ArrayList<>();
        this.obrigatoria = true;
    }

    public Pergunta(String texto, boolean obrigatoria) {
        this.texto = texto;
        this.obrigatoria = obrigatoria;
        this.opcoesResposta = new ArrayList<>();
    }

    public Pergunta(String texto, boolean obrigatoria, List<OpcaoResposta> opcoesResposta) {
        this.texto = texto;
        this.obrigatoria = obrigatoria;
        this.opcoesResposta = (opcoesResposta != null) ? new ArrayList<>(opcoesResposta) : new ArrayList<>();
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isObrigatoria() {
        return obrigatoria;
    }

    public void setObrigatoria(boolean obrigatoria) {
        this.obrigatoria = obrigatoria;
    }

    public List<OpcaoResposta> getOpcoesResposta() {
        return Collections.unmodifiableList(opcoesResposta);
    }

    public void adicionarOpcaoResposta(OpcaoResposta opcao) {
        if (opcao != null && !opcoesResposta.contains(opcao)) {
            opcoesResposta.add(opcao);
        }
    }

    public void adicionarOpcaoResposta(String textoOpcao) {
        adicionarOpcaoResposta(new OpcaoResposta(textoOpcao));
    }

    public void removerOpcaoResposta(OpcaoResposta opcao) {
        opcoesResposta.remove(opcao);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pergunta pergunta = (Pergunta) o;
        return Objects.equals(texto != null ? texto.trim().toLowerCase() : null,
                              pergunta.texto != null ? pergunta.texto.trim().toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto != null ? texto.trim().toLowerCase() : null);
    }

    @Override
    public String toString() {
        return String.format("Pergunta: \"%s\" [%s, %d opções]",
                texto, obrigatoria ? "Obrigatória" : "Opcional", opcoesResposta.size());
    }
}
