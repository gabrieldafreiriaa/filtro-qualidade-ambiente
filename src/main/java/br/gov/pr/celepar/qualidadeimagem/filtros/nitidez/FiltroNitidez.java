package br.gov.pr.celepar.qualidadeimagem.filtros.nitidez;

import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidadorImagem;

public class FiltroNitidez {

    public static final double LIMITE_MINIMO = 100.0;
    public static final String ESCALA = "VARIÂNCIA_LAPLACIANO";
    public static final String REGIAO = "IMAGEM_TOTAL";

    public ResultadoAvaliacao avaliar(GrayU8 imagem) {

        //Validação inicial compartilhada
        if (!ValidadorImagem.valida(imagem)) {

            return new ResultadoAvaliacao(

                "NITIDEZ",
                StatusAvaliacao.ENTRADA_INVALIDA,
                0.0,
                LIMITE_MINIMO,
                Double.POSITIVE_INFINITY,
                ESCALA,
                REGIAO,
                "A imagem fornecida é nula ou inválida."
            );
        }

        int width = imagem.width;
        int height = imagem.height;

        //Validação de dimensão mínima de 3x3
        if (width < 3 || height < 3) {

            return new ResultadoAvaliacao(
                "NITIDEZ",
                StatusAvaliacao.INCONCLUSIVO,
                0.0,
                LIMITE_MINIMO,
                Double.POSITIVE_INFINITY,
                ESCALA,
                REGIAO,
                "Dimensão de imagem insuficiente para cálculo do foco (mínimo 3x3 pixels)."
            );
        }

        //Cálculo matemático da variância
        double variancia = calcularVarianciaLaplaciano(imagem);

        //Aplicação do critério de corte
        StatusAvaliacao status;
        String mensagem;

        if (variancia < LIMITE_MINIMO) {

            status = StatusAvaliacao.REPROVADO;
            mensagem = "Imagem com foco insuficiente ou borrada.";
            
        } else {

            status = StatusAvaliacao.APROVADO;
            mensagem = "Nitidez dentro dos parâmetros aceitáveis.";

        }

        return new ResultadoAvaliacao(
            "NITIDEZ",
            status,
            variancia,
            LIMITE_MINIMO,
            Double.POSITIVE_INFINITY,
            ESCALA,
            REGIAO,
            mensagem
        );
    }

    private double calcularVarianciaLaplaciano(GrayU8 imagem) {
        
        int width = imagem.width;
        int height = imagem.height;
        int totalPixels = (width - 2) * (height - 2);

        double soma = 0.0;
        double somaQuadrados = 0.0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                
                int centro = imagem.get(x, y);
                int cima = imagem.get(x, y - 1);
                int baixo = imagem.get(x, y + 1);
                int esquerda = imagem.get(x - 1, y);
                int direita = imagem.get(x + 1, y);

                double laplaciano = cima + baixo + esquerda + direita - (4.0 * centro);

                soma += laplaciano;
                somaQuadrados += laplaciano * laplaciano;

            }
        }
        
        double media = soma / totalPixels;
        double variancia = (somaQuadrados / totalPixels) - (media * media);

        return Math.max(0.0, variancia);
    }
}