package br.gov.pr.celepar.qualidadeimagem.filtros.nitidez;

import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.imagem.ImagemPreparada;
import br.gov.pr.celepar.qualidadeimagem.imagem.PreparadorImagem;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

public class FiltroNitidez {

    public static final double LIMITE_MINIMO = 100.0;
    public static final String ESCALA = "VARIÂNCIA_LAPLACIANO";
    public static final String REGIAO = "IMAGEM_TOTAL";
    
    private static final long LIMITE_ARQUIVO_BYTES = 5L * 1024 * 1024;
    private static final long LIMITE_PIXELS = 12_000_000L;

    private final PreparadorImagem preparador = new PreparadorImagem(LIMITE_ARQUIVO_BYTES, LIMITE_PIXELS);

    // 1. A assinatura agora exige o array de bytes
    public ResultadoAvaliacao avaliar(byte[] fotoBytes) {

        // 2. Validação obrigatória de nulidade
        if (fotoBytes == null || fotoBytes.length == 0) {
            throw new ValidacaoImagemException("O array de bytes da imagem não pode ser nulo ou vazio.");
        }

        // 3. O Preparador assume toda a decodificação I/O e validações genéricas
        ImagemPreparada imagemPreparada = preparador.preparar(fotoBytes);
        GrayU8 imagem = imagemPreparada.cinza();

        int width = imagem.width;
        int height = imagem.height;

        // 4. Regra de negócio específica deste filtro (mínimo 3x3)
        if (width < 3 || height < 3) {
            return new ResultadoAvaliacao(
                "NITIDEZ", StatusAvaliacao.INCONCLUSIVO, 0.0, LIMITE_MINIMO, Double.POSITIVE_INFINITY, 
                ESCALA, REGIAO, "Dimensão de imagem insuficiente para cálculo do foco (mínimo 3x3 pixels)."
            );
        }

        // 5. Cálculo matemático intocado
        double variancia = calcularVarianciaLaplaciano(imagem);

        StatusAvaliacao status = (variancia < LIMITE_MINIMO) ? StatusAvaliacao.REPROVADO : StatusAvaliacao.APROVADO;
        String mensagem = (variancia < LIMITE_MINIMO) ? "Imagem com foco insuficiente ou borrada." : "Nitidez dentro dos parâmetros aceitáveis.";

        // 6. Retorno em Java Record
        return new ResultadoAvaliacao(
            "NITIDEZ", status, variancia, LIMITE_MINIMO, Double.POSITIVE_INFINITY, ESCALA, REGIAO, mensagem
        );
    }

    // A matemática pura continua isolada aqui embaixo (pode manter como estava)
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