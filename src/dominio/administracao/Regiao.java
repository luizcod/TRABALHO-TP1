package dominio.administracao;

public class Regiao extends Parametro {
    private static final long serialVersionUID = 1L;

    public Regiao(String descricao) { 
        super(descricao); 
    }

    @Override
    public CategoriaParametro getCategoria() { 
        return CategoriaParametro.REGIAO; 
    }
}