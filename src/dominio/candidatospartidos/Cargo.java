package dominio.candidatospartidos;

import java.io.Serializable;

/**
 * Enumeração representando os cargos eleitorais previstos na modelagem do sistema.
 */
public enum Cargo implements Serializable {
    PRESIDENTE("Presidente"),
    GOVERNADOR("Governador"),
    SENADOR("Senador"),
    DEPUTADO("Deputado");

    private final String descricao;

    Cargo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
