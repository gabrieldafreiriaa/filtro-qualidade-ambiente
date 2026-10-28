package br.gov.pr.celepar.qualidadeimagem.resultado;

import java.util.List;

public record ResultadoPipeline(

 StatusAvaliacao statusGeral,
 String mensagemGeral,
 List<ResultadoAvaliacao> filtros

) {}