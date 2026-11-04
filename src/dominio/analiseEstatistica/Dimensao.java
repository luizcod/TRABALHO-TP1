package dominio.analiseEstatistica;

/** Características da amostra que podem ser usadas para cruzar e filtrar respostas. */
public enum Dimensao {
    REGIAO("Região"),
    FAIXA_ETARIA("Faixa etária"),
    ESCOLARIDADE("Escolaridade"),
    GENERO("Gênero"),
    FAIXA_RENDA("Faixa de renda");

    private final String rotulo;

    Dimensao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
