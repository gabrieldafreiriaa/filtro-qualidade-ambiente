package br.gov.pr.celepar.qualidadeimagem.imagem;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

class PreparadorImagemTest {

    @ParameterizedTest
    @ValueSource(strings = {"gif", "bmp"})
    void deveRejeitarFormatosDiferentesDePngEJpeg(String formato) throws IOException {
        byte[] arquivo = criarImagem(formato);
        PreparadorImagem preparador = new PreparadorImagem(arquivo.length, 6);

        ValidacaoImagemException erro = assertThrows(ValidacaoImagemException.class,
                () -> preparador.preparar(arquivo));

        assertEquals("Formato de imagem não permitido. Envie um arquivo PNG ou JPEG.",
                erro.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"png", "jpeg"})
    void devePrepararImagemNosLimitesDeBytesEPixels(String formato) throws IOException {
        byte[] arquivo = criarImagem(formato);
        PreparadorImagem preparador = new PreparadorImagem(arquivo.length, 6);

        ImagemPreparada imagem = preparador.preparar(arquivo);

        assertEquals(3, imagem.original().getWidth());
        assertEquals(2, imagem.original().getHeight());
        assertEquals(3, imagem.cinza().width);
        assertEquals(2, imagem.cinza().height);
        assertEquals(128, imagem.cinza().get(0, 0));
        assertEquals(0x808080, imagem.original().getRGB(0, 0) & 0xFFFFFF);
    }

    @Test
    void deveRejeitarArquivoAcimaDoLimite() throws IOException {
        byte[] arquivo = criarImagem("png");
        PreparadorImagem preparador = new PreparadorImagem(arquivo.length - 1, 6);

        ValidacaoImagemException erro = assertThrows(ValidacaoImagemException.class,
                () -> preparador.preparar(arquivo));

        assertEquals("A imagem excede o tamanho máximo permitido.", erro.getMessage());
    }

    @Test
    void deveRejeitarImagemAcimaDoLimiteDePixels() throws IOException {
        byte[] arquivo = criarImagem("png");
        PreparadorImagem preparador = new PreparadorImagem(arquivo.length, 5);

        ValidacaoImagemException erro = assertThrows(ValidacaoImagemException.class,
                () -> preparador.preparar(arquivo));

        assertEquals("A imagem possui dimensões inválidas ou excede o limite de pixels.",
                erro.getMessage());
    }

    @Test
    void deveRejeitarArquivoNuloOuVazio() {
        PreparadorImagem preparador = new PreparadorImagem(1024, 6);

        for (byte[] arquivo : new byte[][] {null, new byte[0]}) {
            ValidacaoImagemException erro = assertThrows(ValidacaoImagemException.class,
                    () -> preparador.preparar(arquivo));
            assertEquals("Nenhuma imagem recebida.", erro.getMessage());
        }
    }

    @Test
    void deveRejeitarConteudoNaoReconhecido() {
        PreparadorImagem preparador = new PreparadorImagem(1024, 6);

        ValidacaoImagemException erro = assertThrows(ValidacaoImagemException.class,
                () -> preparador.preparar(new byte[] {1, 2, 3}));

        assertEquals("O conteúdo enviado não foi reconhecido como imagem.", erro.getMessage());
    }

    @Test
    void devePreservarCausaDeErroDeLeitura() throws IOException {
        // Preserva a assinatura PNG, mas remove os dados necessários à leitura.
        byte[] truncado = Arrays.copyOf(criarImagem("png"), 8);
        PreparadorImagem preparador = new PreparadorImagem(1024, 6);

        ValidacaoImagemException erro = assertThrows(ValidacaoImagemException.class,
                () -> preparador.preparar(truncado));

        assertEquals("Não foi possível ler a imagem enviada.", erro.getMessage());
        assertInstanceOf(IOException.class, erro.getCause());
    }

    @ParameterizedTest
    @CsvSource({"0, 6", "-1, 6", "1024, 0", "1024, -1"})
    void deveRejeitarLimitesInvalidos(long bytes, long pixels) {
        assertThrows(IllegalArgumentException.class,
                () -> new PreparadorImagem(bytes, pixels));
    }

    private byte[] criarImagem(String formato) throws IOException {
        BufferedImage imagem = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                imagem.setRGB(x, y, 0x808080);
            }
        }
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            assertTrue(ImageIO.write(imagem, formato, bytes));
            return bytes.toByteArray();
        }
    }
}
