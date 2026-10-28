package br.gov.pr.celepar.qualidadeimagem.pipeline;

import br.gov.pr.celepar.qualidadeimagem.filtros.luminancia.FiltroLuminancia;
import br.gov.pr.celepar.qualidadeimagem.filtros.nitidez.FiltroNitidez;
import br.gov.pr.celepar.qualidadeimagem.filtros.simetrialuz.FiltroSimetriaLuz;
import br.gov.pr.celepar.qualidadeimagem.imagem.ImagemPreparada;
import br.gov.pr.celepar.qualidadeimagem.imagem.PreparadorImagem;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoPipeline;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;

import java.util.ArrayList;
import java.util.List;

public class PipelineQualidade {

    private final PreparadorImagem preparador = new PreparadorImagem(5L *1024 * 1024, 12_000_000L);

    private final FiltroLuminancia filtroLuminancia = new FiltroLuminancia();
    private final FiltroNitidez filtroNitidez = new FiltroNitidez();
    private final FiltroSimetriaLuz filtroSimetriaLuz = new FiltroSimetriaLuz();

    public ResultadoPipeline executar(byte[] fotoBytes) {

        ImagemPreparada imagem = preparador.preparar(fotoBytes);

        List<ResultadoAvaliacao> avaliacoes = new ArrayList<>();
        
        ResultadoAvaliacao resLuminancia = filtroLuminancia.avaliar(imagem.cinza());
        avaliacoes.add(resLuminancia);
        ResultadoAvaliacao resNitidez = filtroNitidez.avaliar(fotoBytes);
        avaliacoes.add(resNitidez);
        ResultadoAvaliacao resSimetria = filtroSimetriaLuz.avaliar(fotoBytes);
        avaliacoes.add(resSimetria);

        boolean todosAprovados = avaliacoes.stream()
        .allMatch(r -> r.status() == StatusAvaliacao.APROVADO);

        StatusAvaliacao statusGeral = todosAprovados ? StatusAvaliacao.APROVADO : StatusAvaliacao.REPROVADO;
        String mensagemGeral;
        if (todosAprovados) 
        {

            mensagemGeral = "Imagem aprovada em todos os critérios de qualidade";
        
        } else {
          
            List<String>filtrosReprovados = avaliacoes.stream()
                .filter(r -> r.status() == StatusAvaliacao.REPROVADO)
                .map(ResultadoAvaliacao::filtro)
                .toList();
            
            String falhasFormatadas = String.join(", ", filtrosReprovados);

            if (filtrosReprovados.size() == 1) {

                mensagemGeral = "Imagem reprovada. O seguinte critério falhou: [" + falhasFormatadas + "]";
            
            } else {
                mensagemGeral = "Imagem reprovada. Os seguintes critérios falharam: [" + falhasFormatadas + "]";
            }
        }
        
        return new ResultadoPipeline(statusGeral, mensagemGeral, avaliacoes);

    }
}