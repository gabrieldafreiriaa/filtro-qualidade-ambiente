package br.gov.pr.celepar.qualidadeimagem.filtros.luminancia;

import boofcv.alg.misc.ImageStatistics;

import boofcv.struct.image.GrayU8;

import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;

import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;

public class FiltroLuminancia {
    // Constantes que nunca mudam usamos public static final
    public static final double MIN_LUMINANCIA = 60.0;
    public static final double MAX_LUMINANCIA = 200.0;
    public static final String ESCALA = "0-255";
    public static final String REGIAO = "IMAGEM_TOTAL";
}
