
package br.gov.pr.celepar.qualidadeimagem.filtros.luminancia;

import boofcv.alg.misc.ImageStatistics;
import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;

public class FiltroLuminancia {

    public static final double LIMITE_MINIMO = 60.0;
    public static final double LIMITE_MAXIMO = 200.0;
    public static final String ESCALA = "0-255";
    public static final String REGIAO = "IMAGEM_TOTAL";

    public ResultadoAvaliacao avaliar(GrayU8 imagem) {
        // 1. Validação de segurança: a imagem veio nula?
        if (imagem == null) {
            return new ResultadoAvaliacao(
                    "LUMINANCIA",
                    StatusAvaliacao.ENTRADA_INVALIDA,
                    0.0,
                    LIMITE_MINIMO,
                    LIMITE_MAXIMO,
                    ESCALA,
                    REGIAO,
                    "A imagem fornecida é nula ou inválida.");
        }

        // 2. Cálculo matemático via BoofCV
        double media = ImageStatistics.mean(imagem);

        // 3. Aplicação das regras de corte
        StatusAvaliacao status;
        String mensagem;

        if (media < LIMITE_MINIMO) {
            status = StatusAvaliacao.REPROVADO;
            mensagem = "Imagem subexposta (muito escura).";
        } else if (media > LIMITE_MAXIMO) {
            status = StatusAvaliacao.REPROVADO;
            mensagem = "Imagem superexposta (muito clara).";
        } else {
            status = StatusAvaliacao.APROVADO;
            mensagem = "Luminância dentro dos parâmetros aceitáveis.";
        }

        // 4. Monta e devolve o nosso record de resultado
        return new ResultadoAvaliacao(
                "LUMINANCIA",
                status,
                media,
                LIMITE_MINIMO,
                LIMITE_MAXIMO,
                ESCALA,
                REGIAO,
                mensagem);
    }
}
