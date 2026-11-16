package telas;

import servicos.administracao.ParametroServico;
import servicos.administracao.UsuarioServico;
import servicos.analiseEstatistica.AnaliseService;
import servicos.coleta.ColetaServico;
import servicos.pesquisas.PesquisaService;

/** Ponto único de acesso aos serviços para todas as telas. Preenchido pela Main. */
public final class Contexto {
    public static UsuarioServico usuarios;
    public static ParametroServico parametros;
    public static PesquisaService pesquisas;
    public static ColetaServico coleta;
    public static AnaliseService analise;

    private Contexto() {}
}