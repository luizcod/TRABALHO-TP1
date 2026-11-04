package dominio.analiseEstatistica;

/** Tipo da resposta registrada: opção escolhida (candidato) ou resposta especial. */
public enum TipoResposta {
    OPCAO("Opção"),
    NAO_SABE("Não sabe"),
    NAO_RESPONDEU("Não respondeu"),
    NENHUMA("Nenhuma das opções");

    private final String rotulo;

    TipoResposta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Indica se o rótulo corresponde a uma resposta especial (fora da base dos percentuais). */
    public static boolean rotuloEspecial(String rotulo) {
        for (TipoResposta t : values()) {
            if (t != OPCAO && t.rotulo.equals(rotulo)) {
                return true;
            }
        }
        return false;
    }
}
