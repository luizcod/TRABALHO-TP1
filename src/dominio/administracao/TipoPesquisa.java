package dominio.administracao;

public class TipoPesquisa extends Parametro {
    private static final long serialVersionUID = 1L;

    public TipoPesquisa(String descricao) { 
        super(descricao); 
    }

    @Override
    public CategoriaParametro getCategoria() { 
        return CategoriaParametro.TIPO_PESQUISA; 
    }
}