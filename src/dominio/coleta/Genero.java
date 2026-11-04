package dominio.coleta;

public enum Genero {
    FEMININO("Feminino"), MASCULINO("Masculino"), OUTRO("Outro");

    private final String descricao;

    Genero(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }

    @Override
    public String toString() { return descricao; }
}
