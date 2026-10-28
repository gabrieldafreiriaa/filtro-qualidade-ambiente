package br.gov.pr.celepar.qualidadeimagem.filtros.simetrialuz;

import boofcv.alg.misc.ImageStatistics;
import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.imagem.ImagemPreparada;
import br.gov.pr.celepar.qualidadeimagem.imagem.PreparadorImagem;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

public class FiltroSimetriaLuz {

    public static final double LIMITE_MAXIMO_DIFERENCA = 15.0;
    private static final long LIMITE_ARQUIVO_BYTES = 5L * 1024 * 1024;
    private static final long LIMITE_PIXELS = 12_000_000L;

    private final PreparadorImagem preparador = new PreparadorImagem(LIMITE_ARQUIVO_BYTES, LIMITE_PIXELS);

    public ResultadoAvaliacao avaliar(byte[] fotoBytes) {

        if (fotoBytes == null || fotoBytes.length == 0) {
            throw new ValidacaoImagemException(
                "O array de bytes da imagem não pode ser nulo ou vazio."
            );
        }

        ImagemPreparada imagemPreparada = preparador.preparar(fotoBytes);
        GrayU8 imagem = imagemPreparada.cinza();

        int width = imagem.width;
        int height = imagem.height;

        if (width < 2) {
            return new ResultadoAvaliacao(
                "SIMETRIA DE ILUMINAÇÃO",
                StatusAvaliacao.INCONCLUSIVO,
                0.0,
                0.0,
                LIMITE_MAXIMO_DIFERENCA,
                "Δ Absoluto (0 a 255)",
                "Metade Esquerda vs Direita",
                "Largura de imagem insuficiente para divisão lateral (mínimo 2 pixels)."
            );
        }

        int metade = width / 2;

        GrayU8 matrizEsquerda = imagem.subimage(0, 0, metade, height, null);
        GrayU8 matrizDireita = imagem.subimage(metade, 0, width, height, null);

        double mediaEsquerda = ImageStatistics.mean(matrizEsquerda);
        double mediaDireita = ImageStatistics.mean(matrizDireita);
        double diferencaAbsoluta = Math.abs(mediaEsquerda - mediaDireita);

        boolean aprovado = diferencaAbsoluta < LIMITE_MAXIMO_DIFERENCA;
        StatusAvaliacao status = aprovado ? StatusAvaliacao.APROVADO : StatusAvaliacao.REPROVADO;
        String mensagem = aprovado ? "Iluminação simétrica." : "Assimetria de luz detectada. Possível sombra lateral dura.";

        return new ResultadoAvaliacao(
            "SIMETRIA DE ILUMINAÇÃO", status, diferencaAbsoluta, 0.0, LIMITE_MAXIMO_DIFERENCA, "Δ Absoluto (0 a 255)", "Metade Esquerda vs Direita", mensagem
        );
    }
}