import dominio.administracao.*;
import dominio.coleta.RepositorioColeta;
import integracao.analiseEstatistica.ColetaGatewayReal;
import integracao.analiseEstatistica.PesquisaGatewayReal;
import persistencia.administracao.*;
import persistencia.analiseEstatistica.ArquivoRepositorioResultados;
import persistencia.coleta.RepositorioColetaArquivo;
import servicos.administracao.*;
import servicos.analiseEstatistica.AnaliseService;
import servicos.coleta.ColetaServico;
import servicos.pesquisas.PesquisaService;

import java.nio.file.Path;
import java.util.EnumSet;

public class Main {
    public static void main(String[] args) {
        RepositorioUsuario repoUsuarios = new RepositorioUsuarioArquivo("dados/usuarios.dat");
        RepositorioParametro repoParametros = new RepositorioParametroArquivo("dados/parametros.dat");
        RepositorioColeta repoColeta = new RepositorioColetaArquivo("dados/coleta.dat");

        if (repoUsuarios.listar().isEmpty()) {
            repoUsuarios.salvar(new Usuario("Administrador", "admin@sistema.com",
                    "admin1234", EnumSet.of(Perfil.ADMINISTRADOR)));
        }
        if (repoParametros.listar().isEmpty()) {
            for (String t : new String[]{"Intenção de voto", "Rejeição", "Conhecimento", "Segundo turno simulado"})
                repoParametros.salvar(CriaParametro.criar(CategoriaParametro.TIPO_PESQUISA, t));
        }

        Autenticacao.getInstancia().configurar(repoUsuarios);
        UsuarioServico usuarios = new UsuarioServico(repoUsuarios, new ExportadorCsvUsuarios());
        ParametroServico parametros = new ParametroServico(repoParametros);
        PesquisaService pesquisas = PesquisaService.getInstance();
        ColetaServico coleta = new ColetaServico(repoColeta, pesquisas, parametros);
        AnaliseService analise = new AnaliseService(
                new PesquisaGatewayReal(pesquisas),
                new ColetaGatewayReal(repoColeta, pesquisas),
                new ArquivoRepositorioResultados(Path.of("dados/resultados.dat")));

        // Impede excluir parâmetro usado por participantes
        parametros.registrarVerificadorDeUso(coleta::parametroEmUso);

        // as telas entram aqui depois
    }
}