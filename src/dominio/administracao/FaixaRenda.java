package dominio.administracao;

import excecoes.ExcecaoRegraNegocio;

public class FaixaRenda extends Parametro {
    private static final long serialVersionUID = 1L;
    public static final double SEM_LIMITE = Double.POSITIVE_INFINITY;

    private double rendaMinima;   // em salários mínimos
    private double rendaMaxima;

    /** Valores em salários mínimos. Ex.: new FaixaRenda(0, 2) ou new FaixaRenda(5, SEM_LIMITE). */
    public FaixaRenda(double rendaMinima, double rendaMaxima) {
        super(montarDescricao(rendaMinima, rendaMaxima));
        validarLimites(rendaMinima, rendaMaxima);
        this.rendaMinima = rendaMinima;
        this.rendaMaxima = rendaMaxima;
    }

    @Override
    public CategoriaParametro getCategoria() { return CategoriaParametro.FAIXA_RENDA; }

    public double getRendaMinima() { return rendaMinima; }
    public double getRendaMaxima() { return rendaMaxima; }

    public void setLimites(double minima, double maxima) {
        validarLimites(minima, maxima);
        this.rendaMinima = minima;
        this.rendaMaxima = maxima;
        setDescricao(montarDescricao(minima, maxima));
    }

    public boolean contem(double renda) {
        return renda >= rendaMinima && renda <= rendaMaxima;
    }

    public boolean sobrepoe(FaixaRenda o) {
        return rendaMinima <= o.rendaMaxima && o.rendaMinima <= rendaMaxima;
    }

    private static void validarLimites(double min, double max) {
        if (min < 0) {
            throw new ExcecaoRegraNegocio("A renda mínima não pode ser negativa.");
        }
        if (max < min) {
            throw new ExcecaoRegraNegocio("A renda máxima não pode ser menor que a mínima.");
        }
    }

    private static String montarDescricao(double min, double max) {
        return max == SEM_LIMITE
            ? "Acima de " + min + " salários mínimos"
            : "De " + min + " a " + max + " salários mínimos";
    }

    @Override
    public boolean conflitaCom(Parametro outro) {
        if (outro instanceof FaixaRenda) {
            FaixaRenda f = (FaixaRenda) outro;
            return sobrepoe(f);
        }
        return false;
    }
}