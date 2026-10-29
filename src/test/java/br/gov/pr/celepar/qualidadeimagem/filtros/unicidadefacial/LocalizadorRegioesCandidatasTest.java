package br.gov.pr.celepar.qualidadeimagem.filtros.unicidadefacial;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.gov.pr.celepar.qualidadeimagem.imagem.RegiaoImagem;

class LocalizadorRegioesCandidatasTest {

    private static final int COR_PELE = 0xFFC89678;
    private final LocalizadorRegioesCandidatas localizador = new LocalizadorRegioesCandidatas();

    @Test
    void deveRejeitarImagemNula() {
        assertThrows(IllegalArgumentException.class, () -> localizador.localizar(null));
    }

    @Test
    void deveRetornarListaVaziaParaImagemSemCorDePele() {
        var imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        assertTrue(localizador.localizar(imagem).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB})
    void deveLocalizarRegiaoOpacaEPreservarImagem(int tipo) {
        var imagem = new BufferedImage(100, 100, tipo);
        preencher(imagem, 10, 20, 30, 40, COR_PELE);
        int[] antes = imagem.getRGB(0, 0, 100, 100, null, 0, 100);

        assertEquals(List.of(new RegiaoImagem(10, 20, 30, 40)), localizador.localizar(imagem));
        assertArrayEquals(antes, imagem.getRGB(0, 0, 100, 100, null, 0, 100));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 128, 254})
    void deveIgnorarPixelsComTransparencia(int alfa) {
        var imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        preencher(imagem, 10, 20, 30, 40, (alfa << 24) | (COR_PELE & 0xFFFFFF));

        assertTrue(localizador.localizar(imagem).isEmpty());
    }

    @Test
    void pixelsTransparentesNaoDevemUnirRegioesOpacas() {
        var imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        preencher(imagem, 5, 10, 30, 40, COR_PELE);
        preencher(imagem, 60, 10, 30, 40, COR_PELE);
        preencher(imagem, 35, 25, 25, 1, COR_PELE & 0xFFFFFF);

        assertEquals(List.of(new RegiaoImagem(5, 10, 30, 40),
                new RegiaoImagem(60, 10, 30, 40)), localizador.localizar(imagem));
    }

    @Test
    void deveAceitarAreaMinimaExataERegiaoNaBorda() {
        var imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        preencher(imagem, 75, 60, 25, 40, COR_PELE);

        assertEquals(List.of(new RegiaoImagem(75, 60, 25, 40)), localizador.localizar(imagem));
    }

    @Test
    void deveRejeitarAreaLogoAbaixoDoMinimo() {
        var imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        preencher(imagem, 0, 0, 25, 40, COR_PELE);
        imagem.setRGB(10, 10, 0xFF000000);

        // Retângulo com 1000 pixels, mas componente com apenas 999.
        assertTrue(localizador.localizar(imagem).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"40, 44", "40, 64"})
    void deveAceitarLimitesInclusivosDeProporcao(int largura, int altura) {
        var imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        preencher(imagem, 0, 0, largura, altura, COR_PELE);

        assertEquals(List.of(new RegiaoImagem(0, 0, largura, altura)), localizador.localizar(imagem));
    }

    @ParameterizedTest
    @CsvSource({"40, 43", "40, 65"})
    void deveRejeitarProporcoesForaDosLimites(int largura, int altura) {
        var imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        preencher(imagem, 0, 0, largura, altura, COR_PELE);

        assertTrue(localizador.localizar(imagem).isEmpty());
    }

    private static void preencher(BufferedImage imagem, int x, int y,
            int largura, int altura, int cor) {
        for (int linha = y; linha < y + altura; linha++) {
            for (int coluna = x; coluna < x + largura; coluna++) {
                imagem.setRGB(coluna, linha, cor);
            }
        }
    }
}
