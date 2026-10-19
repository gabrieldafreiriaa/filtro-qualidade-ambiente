package br.gov.pr.celepar.qualidadeimagem.filtros.luminancia;

import boofcv.alg.misc.ImageMiscOps;
import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FiltroLuminanciaTest {

    private FiltroLuminancia filtro;

    // Roda antes de CADA teste para criar uma instância limpa do filtro
    @BeforeEach
    void setUp() {
        filtro = new FiltroLuminancia();
    }

    @Test
    void deveAprovarImagemComLuminanciaAdequada() {
        // Cria uma imagem 100x100 com tom cinza médio (valor 120)
        GrayU8 imagem = new GrayU8(100, 100);
        ImageMiscOps.fill(imagem, 120);

        ResultadoAvaliacao resultado = filtro.avaliar(imagem);

        // Verifica se aprovou e se a média deu exatamente 120
        Assertions.assertEquals(StatusAvaliacao.APROVADO, resultado.status());
        Assertions.assertEquals(120.0, resultado.valorMedido(), 0.001);
    }

    @Test
    void deveReprovarImagemMuitoEscura() {
        // Cria uma imagem escura (valor 20, menor que o limite de 60)
        GrayU8 imagem = new GrayU8(100, 100);
        ImageMiscOps.fill(imagem, 20);

        ResultadoAvaliacao resultado = filtro.avaliar(imagem);

        Assertions.assertEquals(StatusAvaliacao.REPROVADO, resultado.status());
        Assertions.assertEquals(20.0, resultado.valorMedido(), 0.001);
    }

    @Test
    void deveReprovarImagemMuitoClara() {
        // Cria uma imagem estourada (valor 240, maior que o limite de 200)
        GrayU8 imagem = new GrayU8(100, 100);
        ImageMiscOps.fill(imagem, 240);

        ResultadoAvaliacao resultado = filtro.avaliar(imagem);

        Assertions.assertEquals(StatusAvaliacao.REPROVADO, resultado.status());
        Assertions.assertEquals(240.0, resultado.valorMedido(), 0.001);
    }

    @Test
    void deveRetornarEntradaInvalidaParaImagemNula() {
        ResultadoAvaliacao resultado = filtro.avaliar(null);

        Assertions.assertEquals(StatusAvaliacao.ENTRADA_INVALIDA, resultado.status());
    }
}
