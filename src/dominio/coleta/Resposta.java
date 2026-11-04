package dominio.coleta;

import dominio.pesquisas.OpcaoResposta;
import dominio.pesquisas.Pergunta;
import excecoes.ExcecaoRegraNegocio;

import java.io.Serializable;

/** Resposta a uma pergunta: ou uma opção do questionário, ou uma resposta especial. */
public final class Resposta implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Pergunta pergunta;
    private final OpcaoResposta opcao;      // null quando for resposta especial
    private final TipoEspecial especial;    // null quando escolheu uma opção

    private Resposta(Pergunta pergunta, OpcaoResposta opcao, TipoEspecial especial) {
        this.pergunta = pergunta;
        this.opcao = opcao;
        this.especial = especial;
    }

    public static Resposta daOpcao(Pergunta pergunta, OpcaoResposta opcao) {
        if (pergunta == null || opcao == null) {
            throw new ExcecaoRegraNegocio("Pergunta e opção escolhida são obrigatórias.");
        }
        if (!pergunta.getOpcoesResposta().contains(opcao)) {
            throw new ExcecaoRegraNegocio("A opção \"" + opcao + "\" não pertence à pergunta \""
                    + pergunta.getTexto() + "\".");
        }
        return new Resposta(pergunta, opcao, null);
    }

    public static Resposta especial(Pergunta pergunta, TipoEspecial tipo) {
        if (pergunta == null || tipo == null) {
            throw new ExcecaoRegraNegocio("Pergunta e tipo de resposta são obrigatórios.");
        }
        return new Resposta(pergunta, null, tipo);
    }

    public Pergunta getPergunta()      { return pergunta; }
    public OpcaoResposta getOpcao()    { return opcao; }
    public TipoEspecial getEspecial()  { return especial; }
    public boolean isEspecial()        { return especial != null; }

    @Override
    public String toString() {
        return pergunta.getTexto() + " -> " + (isEspecial() ? especial : opcao);
    }
}
