package dominio.pesquisas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa o plano amostral e a metodologia estatística de uma pesquisa.
 * Modelagem Etapa 2: metodologia, lista de MetaAmostral.
 */
public class PlanoAmostral implements Serializable {
    private static final long serialVersionUID = 1L;

    private String metodologia;
    private List<MetaAmostral> metas;

    public PlanoAmostral() {
        this.metas = new ArrayList<>();
    }

    public PlanoAmostral(String metodologia) {
        this.metodologia = metodologia;
        this.metas = new ArrayList<>();
    }

    public PlanoAmostral(String metodologia, List<MetaAmostral> metas) {
        this.metodologia = metodologia;
        this.metas = (metas != null) ? new ArrayList<>(metas) : new ArrayList<>();
    }

    public String getMetodologia() {
        return metodologia;
    }

    public void setMetodologia(String metodologia) {
        this.metodologia = metodologia;
    }

    public List<MetaAmostral> getMetas() {
        return Collections.unmodifiableList(metas);
    }

    public void adicionarMeta(MetaAmostral meta) {
        if (meta != null && !metas.contains(meta)) {
            metas.add(meta);
        }
    }

    public void adicionarMeta(String grupo, int minimo) {
        adicionarMeta(new MetaAmostral(grupo, minimo));
    }

    public void removerMeta(MetaAmostral meta) {
        metas.remove(meta);
    }

    @Override
    public String toString() {
        return String.format("Plano Amostral [Metodologia: %s, %d metas definidas]",
                metodologia, metas.size());
    }
}
