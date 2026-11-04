package dominio.administracao;

import excecoes.ExcecaoRegraNegocio;

public final class CriaParametro {
    private CriaParametro() {}

    //categorias definidas
    public static Parametro criar(CategoriaParametro categoria, String descricao) {
        switch (categoria) {
            case ESCOLARIDADE:  
                return new Escolaridade(descricao);
            case REGIAO:        
                return new Regiao(descricao);
            case TIPO_PESQUISA: 
                return new TipoPesquisa(descricao);
            default:
                throw new ExcecaoRegraNegocio("A categoria " + categoria + " exige limites mínimo e máximo.");
        }
    }

    public static FaixaEtaria criarFaixaEtaria(int min, int max) {
        return new FaixaEtaria(min, max);
    }

    public static FaixaRenda criarFaixaRenda(double min, double max) {
        return new FaixaRenda(min, max);
    }
}