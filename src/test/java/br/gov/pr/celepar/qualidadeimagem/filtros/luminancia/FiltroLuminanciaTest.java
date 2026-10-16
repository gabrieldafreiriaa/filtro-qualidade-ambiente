package br.gov.pr.celepar.qualidadeimagem.filtros.luminancia;

import boofcv.struct.image.GrayF32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FiltroLuminanciaTest {

    private FiltroLuminancia filtroLuminancia;

    @BeforeEach
    void setUp() {
        filtroLuminancia = new FiltroLuminancia();
    }

    @Test
    @DisplayName("Deve reprovar imagem uniforme escura com media abaixo de 60")
    void testImagemUniformeEscura() {
        GrayF32 imagem = new GrayF32(10, 10);
        preencherImagem(imagem, 20.0f);

        float media = filtroLuminancia.calcularMedia(imagem);

        assertEquals(20.0f, media, 0.001f);
        assertFalse(filtroLuminancia.estaAprovado(media));
    }

    @Test
    @DisplayName("Deve aprovar imagem uniforme com luminancia intermediaria")
    void testImagemUniformeIntermediaria() {
        GrayF32 imagem = new GrayF32(10, 10);
        preencherImagem(imagem, 128.0f);

        float media = filtroLuminancia.calcularMedia(imagem);

        assertEquals(128.0f, media, 0.001f);
        assertTrue(filtroLuminancia.estaAprovado(media));
    }

    @Test
    @DisplayName("Deve reprovar imagem uniforme clara com media acima de 200")
    void testImagemUniformeClara() {
        GrayF32 imagem = new GrayF32(10, 10);
        preencherImagem(imagem, 240.0f);

        float media = filtroLuminancia.calcularMedia(imagem);

        assertEquals(240.0f, media, 0.001f);
        assertFalse(filtroLuminancia.estaAprovado(media));
    }

    @Test
    @DisplayName("Deve validar exatamente os limites de 60 e 200")
    void testLimitesExatos() {
        assertTrue(filtroLuminancia.estaAprovado(60.0f), "Limite inferior 60 deve ser aprovado");
        assertTrue(filtroLuminancia.estaAprovado(200.0f), "Limite superior 200 deve ser aprovado");
        assertFalse(filtroLuminancia.estaAprovado(59.99f), "Abaixo de 60 deve ser reprovado");
        assertFalse(filtroLuminancia.estaAprovado(200.01f), "Acima de 200 deve ser reprovado");
    }

    @Test
    @DisplayName("Deve lancar excecao para imagem nula ou com dimensoes invalidas")
    void testImagemInvalida() {
        assertThrows(IllegalArgumentException.class, () -> filtroLuminancia.calcularMedia(null));
        assertThrows(IllegalArgumentException.class, () -> filtroLuminancia.calcularMedia(new GrayF32(0, 0)));
    }

    private void preencherImagem(GrayF32 imagem, float valor) {
        for (int y = 0; y < imagem.height; y++) {
            for (int x = 0; x < imagem.width; x++) {
                imagem.set(x, y, valor);
            }
        }
    }
}
