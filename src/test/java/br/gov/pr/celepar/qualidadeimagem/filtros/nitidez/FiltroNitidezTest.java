package br.gov.pr.celepar.qualidadeimagem.filtros.nitidez;

import boofcv.alg.misc.ImageMiscOps;
import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FiltroNitidezTest {

    private FiltroNitidez filtro;

    @BeforeEach
    void setUp() {
        filtro = new FiltroNitidez();
    }

    @Test
    void deveReprovarImagemUniformeComVarianciaZero() {
        // Imagem uniforme (todos os pixels iguais) tem laplaciano zero e variância zero
        GrayU8 imagem = new GrayU8(100, 100);
        ImageMiscOps.fill(imagem, 128);

        ResultadoAvaliacao resultado = filtro.avaliar(imagem);

        Assertions.assertEquals(StatusAvaliacao.REPROVADO, resultado.status());
        Assertions.assertEquals(0.0, resultado.valorMedido(), 0.001);
        Assertions.assertEquals("Imagem com foco insuficiente ou borrada.", resultado.mensagem());
    }

    @Test
    void deveAprovarImagemComBordasNitidas() {
        // Imagem com padrão alternado gerando alto contraste e bordas nítidas
        GrayU8 imagem = new GrayU8(50, 50);
        for (int y = 0; y < imagem.height; y++) {
            for (int x = 0; x < imagem.width; x++) {
                int valor = ((x + y) % 2 == 0) ? 255 : 0;
                imagem.set(x, y, valor);
            }
        }

        ResultadoAvaliacao resultado = filtro.avaliar(imagem);

        Assertions.assertEquals(StatusAvaliacao.APROVADO, resultado.status());
        Assertions.assertTrue(resultado.valorMedido() >= FiltroNitidez.LIMITE_MINIMO);
        Assertions.assertEquals("Nitidez dentro dos parâmetros aceitáveis.", resultado.mensagem());
    }

    @Test
    void deveRetornarEntradaInvalidaParaImagemNula() {
        ResultadoAvaliacao resultado = filtro.avaliar(null);

        Assertions.assertEquals(StatusAvaliacao.ENTRADA_INVALIDA, resultado.status());
        Assertions.assertEquals("A imagem fornecida é nula ou inválida.", resultado.mensagem());
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "0, 10", "10, 0"})
    void deveRetornarEntradaInvalidaParaImagemVazia(int largura, int altura) {
        ResultadoAvaliacao resultado = filtro.avaliar(new GrayU8(largura, altura));

        Assertions.assertEquals(StatusAvaliacao.ENTRADA_INVALIDA, resultado.status());
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "2, 2", "2, 10", "10, 2"})
    void deveRetornarInconclusivoParaDimensaoMenorQue3x3(int largura, int altura) {
        ResultadoAvaliacao resultado = filtro.avaliar(new GrayU8(largura, altura));

        Assertions.assertEquals(StatusAvaliacao.INCONCLUSIVO, resultado.status());
        Assertions.assertEquals(0.0, resultado.valorMedido());
    }
}
