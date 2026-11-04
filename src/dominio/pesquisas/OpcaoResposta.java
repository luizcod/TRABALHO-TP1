package dominio.pesquisas;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa uma opção de resposta possível para uma Pergunta de pesquisa.
 * Modelagem Etapa 2: texto.
 */
public class OpcaoResposta implements Serializable {
    private static final long serialVersionUID = 1L;

    private String texto;

    public OpcaoResposta() {
    }

    public OpcaoResposta(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpcaoResposta that = (OpcaoResposta) o;
        return Objects.equals(texto != null ? texto.trim().toLowerCase() : null,
                              that.texto != null ? that.texto.trim().toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto != null ? texto.trim().toLowerCase() : null);
    }

    @Override
    public String toString() {
        return texto;
    }
}
