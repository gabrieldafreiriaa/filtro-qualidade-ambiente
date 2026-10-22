package br.gov.pr.celepar.qualidadeimagem.imagem;

import java.awt.image.BufferedImage;
import boofcv.struct.image.GrayU8;

public record ImagemPreparada(
        BufferedImage original,
        GrayU8 cinza) {

}
