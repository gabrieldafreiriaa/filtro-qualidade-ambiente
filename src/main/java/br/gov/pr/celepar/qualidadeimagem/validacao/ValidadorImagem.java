package br.gov.pr.celepar.qualidadeimagem.validacao;

import boofcv.struct.image.GrayU8;

public class ValidadorImagem {
    public static boolean valida(GrayU8 imagem) {
        return imagem != null && imagem.width > 0 && imagem.height > 0;
    }

    public static boolean tamanhoArquivoValido(byte[] arquivo, long limiteBytes) {
        return arquivo != null
                && arquivo.length > 0
                && arquivo.length <= limiteBytes;
    }
}
