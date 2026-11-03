package coleta;

public class Participante {
    private String codigo;
    private String genero;

    public Participante(String codigo, String genero) {
        this.codigo = codigo;
        this.genero = genero;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}