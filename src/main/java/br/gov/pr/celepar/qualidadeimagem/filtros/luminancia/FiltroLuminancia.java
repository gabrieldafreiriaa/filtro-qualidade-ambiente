package br.gov.pr.celepar.qualidadeimagem.filtros.luminancia;

import boofcv.struct.image.GrayF32;

public class FiltroLuminancia {

    public static final float LIMITE_MINIMO = 60.0f;
    public static final float LIMITE_MAXIMO = 200.0f;

    public float calcularMedia(GrayF32 imagemCinza) {
        if (imagemCinza == null || imagemCinza.width <= 0 || imagemCinza.height <= 0) {
            throw new IllegalArgumentException("Imagem cinza invalida para calculo de luminancia.");
        }

        float somaIntensidade = 0;
        int totalPixels = imagemCinza.width * imagemCinza.height;

        for (int y = 0; y < imagemCinza.height; y++) {
            for (int x = 0; x < imagemCinza.width; x++) {
                somaIntensidade += imagemCinza.get(x, y);
            }
        }

        return somaIntensidade / totalPixels;
    }

    public boolean estaAprovado(float media) {
        return media >= LIMITE_MINIMO && media <= LIMITE_MAXIMO;
    }
}
