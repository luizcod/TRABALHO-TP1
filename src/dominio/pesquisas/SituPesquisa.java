package dominio.pesquisas;

import java.io.Serializable;

/**
 * Enumeração dos estados do ciclo de vida de uma Pesquisa Eleitoral.
 */
public enum SituPesquisa implements Serializable {
    PLANEJADA("Planejada"),
    EM_COLETA("Em Coleta"),
    ENCERRADA("Encerrada"),
    PROCESSADA("Processada");

    private final String descricao;

    SituPesquisa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Regra: Se a pesquisa estiver ENCERRADA ou PROCESSADA, não pode sofrer edições/alterações.
     */
    public boolean isBloqueadaParaEdicao() {
        return this == ENCERRADA || this == PROCESSADA;
    }

    @Override
    public String toString() {
        return descricao;
    }

    public boolean podeIrPara(SituPesquisa destino) {
        return destino.ordinal() == this.ordinal() + 1;
    }
}
