package br.gov.pr.celepar.qualidadeimagem.resultado;

public record ResultadoAvaliacao(
        String filtro,
        StatusAvaliacao status,
        double valorMedido,
        double limiteMinimo,
        double limiteMaximo,
        String escala,
        String regiaoAnalisada,
        String mensagem) {

}
