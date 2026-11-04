package dominio.administracao;

import excecoes.ExcecaoRegraNegocio;

public class FaixaEtaria extends Parametro {
    private static final long serialVersionUID = 1L;
    public static final int SEM_LIMITE = Integer.MAX_VALUE;

    private int idadeMinima;
    private int idadeMaxima;

    public FaixaEtaria(int idadeMinima, int idadeMaxima) {
        super(montarDescricao(idadeMinima, idadeMaxima));
        validarLimites(idadeMinima, idadeMaxima);
        this.idadeMinima = idadeMinima;
        this.idadeMaxima = idadeMaxima;
    }

    @Override
    public CategoriaParametro getCategoria() { return CategoriaParametro.FAIXA_ETARIA; }

    public int getIdadeMinima(){ 
        return idadeMinima; 
    }
    public int getIdadeMaxima(){ 
        return idadeMaxima; 
    }

    public void setLimites(int minima, int maxima) {
        validarLimites(minima, maxima);
        this.idadeMinima = minima;
        this.idadeMaxima = maxima;
        setDescricao(montarDescricao(minima, maxima));
    }

    public boolean contem(int idade) {
        return idade >= idadeMinima && idade <= idadeMaxima;
    }

    //impede faixas sobrepostas
    public boolean sobrepoe(FaixaEtaria o) {
        return idadeMinima <= o.idadeMaxima && o.idadeMinima <= idadeMaxima;
    }

    private static void validarLimites(int min, int max) {
        if (min < 16) {
            throw new ExcecaoRegraNegocio("A idade mínima para votar é 16 anos.");
        }
        if (max < min) {
            throw new ExcecaoRegraNegocio("A idade máxima não pode ser menor que a mínima.");
        }
    }

    private static String montarDescricao(int min, int max) {
        return max == SEM_LIMITE ? min + " anos ou mais" : min + " a " + max + " anos";
    }

    @Override
    public boolean conflitaCom(Parametro outro) {
        if (outro instanceof FaixaEtaria) {
            FaixaEtaria f = (FaixaEtaria) outro;
            return sobrepoe(f);
        }
        return false;
    }
}