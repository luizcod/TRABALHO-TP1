package coleta;

public class MetaAmostral {
    private String grupo;
    private int minimo;

    public MetaAmostral(String grupo, int minimo) {
        this.grupo = grupo;
        this.minimo = minimo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public int getMinimo() {
        return minimo;
    }

    public void setMinimo(int minimo) {
        this.minimo = minimo;
    }
}