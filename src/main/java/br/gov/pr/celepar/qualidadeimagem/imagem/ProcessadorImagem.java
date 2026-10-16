package br.gov.pr.celepar.qualidadeimagem.imagem;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ProcessadorImagem {

    // Limite de seguranca para evitar estouro de memoria (20 MB)
    public static final int TAMANHO_MAXIMO_BYTES = 20 * 1024 * 1024;

    public GrayF32 decodificarParaEscalaDeCinza(byte[] dadosArquivo) throws IOException {
        if (dadosArquivo == null || dadosArquivo.length == 0) {
            throw new IllegalArgumentException("O arquivo de imagem enviado esta vazio ou nulo.");
        }

        if (dadosArquivo.length > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("O arquivo excede o tamanho maximo permitido de 20 MB.");
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(dadosArquivo);
        BufferedImage imagemOriginal = ImageIO.read(inputStream);

        if (imagemOriginal == null) {
            throw new IllegalArgumentException("O arquivo enviado nao e uma imagem valida ou formato nao suportado.");
        }

        GrayF32 imagemCinza = new GrayF32(imagemOriginal.getWidth(), imagemOriginal.getHeight());
        ConvertBufferedImage.convertFrom(imagemOriginal, imagemCinza);

        return imagemCinza;
    }
}
