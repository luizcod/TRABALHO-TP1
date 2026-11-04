package dominio.administracao;

import excecoes.ExcecaoRegraNegocio;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class Parametro implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String descricao;

    protected Parametro(String descricao) {
        this.id = UUID.randomUUID().toString();
        setDescricao(descricao);
    }

    //cada filha diz em qual categoria está
    public abstract CategoriaParametro getCategoria();

    public String getId() { 
        return id; 
    }

    public String getDescricao(){ 
        return descricao; 
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new ExcecaoRegraNegocio(
                "A descrição do parâmetro (" + nomeCategoriaSeguro() + ") é obrigatória.");
        }
        this.descricao = descricao.trim();
    }

    private String nomeCategoriaSeguro(){ 
        return getClass().getSimpleName(); 
    }

    // 2parametros são iguais se forem da mesma categoria e tiverem a mesma descrição
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parametro)) return false;
        Parametro p = (Parametro) o;
        return getClass() == p.getClass()
            && descricao.equalsIgnoreCase(p.descricao);
    }

    // 2 parametros não podem coexistir no cadastro
    public boolean conflitaCom(Parametro outro) {
        return equals(outro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), descricao.toLowerCase());
    }

    @Override
    public String toString() { return descricao; }
}