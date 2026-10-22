package br.gov.pr.celepar.qualidadeimagem.validacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ValidadorImagemTest {

    @ParameterizedTest
    @CsvSource({
            "4000, 3000, 12000000, true",
            "4001, 3000, 12000000, false",
            "0, 3000, 12000000, false",
            "3000, 0, 12000000, false",
            "-1, 3000, 12000000, false",
            "3000, -1, 12000000, false",
            "1, 1, 0, false",
            "1, 1, -1, false",
            "65536, 65536, 12000000, false"
    })
    void deveValidarQuantidadePixels(int largura, int altura, long limite, boolean esperado) {
        assertEquals(esperado,
                ValidadorImagem.quantidadePixelsValida(largura, altura, limite));
    }

    @ParameterizedTest
    @CsvSource({ "4, 5, true", "5, 5, true", "6, 5, false", "0, 5, false",
            "1, 0, false", "1, -1, false" })
    void deveValidarTamanhoArquivo(int tamanho, long limite, boolean esperado) {
        assertEquals(esperado,
                ValidadorImagem.tamanhoArquivoValido(new byte[tamanho], limite));
    }

    @Test
    void deveRejeitarArquivoNulo() {
        assertFalse(ValidadorImagem.tamanhoArquivoValido(null, 5));
    }
}
