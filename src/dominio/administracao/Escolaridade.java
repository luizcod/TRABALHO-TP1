package dominio.administracao;

public class Escolaridade extends Parametro {
    private static final long serialVersionUID = 1L;

    public Escolaridade(String descricao){ 
        super(descricao); 
    }

    @Override
    public CategoriaParametro getCategoria() { 
        return CategoriaParametro.ESCOLARIDADE;
    }
}