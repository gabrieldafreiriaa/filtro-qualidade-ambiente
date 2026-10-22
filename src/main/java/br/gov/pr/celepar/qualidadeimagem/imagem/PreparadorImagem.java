package br.gov.pr.celepar.qualidadeimagem.imagem;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidadorImagem;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

public class PreparadorImagem {

    private final long limiteBytes;
    private final long limitePixels;

    public PreparadorImagem(long limiteBytes, long limitePixels) {
        if (limiteBytes <= 0 || limitePixels <= 0) {
            throw new IllegalArgumentException(
                    "O limite de bytes e pixels deve ser maior que zero.");
        }

        this.limiteBytes = limiteBytes;
        this.limitePixels = limitePixels;
    }

    public ImagemPreparada preparar(byte[] fotoBytes) {
        if (fotoBytes == null || fotoBytes.length == 0) {
            throw new ValidacaoImagemException(
                    "Nenhuma imagem recebida.");
        }

        if (!ValidadorImagem.tamanhoArquivoValido(fotoBytes, limiteBytes)) {
            throw new ValidacaoImagemException(
                    "A imagem excede o tamanho máximo permitido.");
        }

        BufferedImage original;

        try (ByteArrayInputStream entrada = new ByteArrayInputStream(fotoBytes);
                MemoryCacheImageInputStream entradaImagem =
                        new MemoryCacheImageInputStream(entrada)) {

            Iterator<ImageReader> leitores = ImageIO.getImageReaders(entradaImagem);

            if (!leitores.hasNext()) {
                throw new ValidacaoImagemException(
                        "O conteúdo enviado não foi reconhecido como imagem.");
            }

            ImageReader leitor = leitores.next();

            try {
                leitor.setInput(entradaImagem);

                int largura = leitor.getWidth(0);
                int altura = leitor.getHeight(0);

                if (!ValidadorImagem.quantidadePixelsValida(
                        largura, altura, limitePixels)) {
                    throw new ValidacaoImagemException(
                            "A imagem possui dimensões inválidas ou excede o limite de pixels.");
                }

                original = leitor.read(0);
            } finally {
                leitor.dispose();
            }
        } catch (IOException e) {
            throw new ValidacaoImagemException(
                    "Não foi possível ler a imagem enviada.", e);
        }

        if (original == null) {
            throw new ValidacaoImagemException(
                    "O conteúdo enviado não foi reconhecido como imagem.");
        }

        GrayU8 cinza = ConvertBufferedImage.convertFromSingle(
                original, null, GrayU8.class);

        if (!ValidadorImagem.valida(cinza)) {
            throw new ValidacaoImagemException(
                    "A imagem fornecida é nula ou inválida.");
        }

        return new ImagemPreparada(original, cinza);
    }
}
