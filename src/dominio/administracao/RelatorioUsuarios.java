package dominio.administracao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class RelatorioUsuarios {
    private final int total;
    private final int ativos;
    private final int inativos;
    private final Map<Perfil, Integer> porPerfil;
    private final LocalDateTime geradoEm;

    private RelatorioUsuarios(int total, int ativos, Map<Perfil, Integer> porPerfil) {
        this.total = total;
        this.ativos = ativos;
        this.inativos = total - ativos;
        this.porPerfil = Collections.unmodifiableMap(porPerfil);
        this.geradoEm = LocalDateTime.now();
    }

    public static RelatorioUsuarios de(Collection<Usuario> usuarios) {
        int ativos = 0;
        Map<Perfil, Integer> porPerfil = new EnumMap<>(Perfil.class);
        for (Perfil p : Perfil.values()) {
            porPerfil.put(p, 0);                 // perfis sem usuários aparecem com 0
        }
        for (Usuario u : usuarios) {
            if (u.getAtivo()) ativos++;
            for (Perfil p : u.getPerfis()) {
                porPerfil.merge(p, 1, Integer::sum);
            }
        }
        return new RelatorioUsuarios(usuarios.size(), ativos, porPerfil);
    }

    public int getTotal(){
        return total;
    }
    public int getAtivos(){
        return ativos;
    }
    public int getInativos(){
        return inativos;
    }
    public Map<Perfil, Integer> getPorPerfil(){
        return porPerfil;
    }
    public int getQuantidade(Perfil perfil) {
        return porPerfil.get(perfil);
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total de usuários: ").append(total).append('\n');
        sb.append("Ativos: ").append(ativos).append('\n');
        sb.append("Inativos: ").append(inativos).append('\n');
        sb.append("Por perfil (um usuário pode ter mais de um):\n");
        porPerfil.forEach((p, q) -> sb.append("  ").append(p).append(": ").append(q).append('\n'));
        return sb.toString();
    }
}