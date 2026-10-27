package br.gov.pr.celepar.qualidadeimagem.api;

import br.gov.pr.celepar.qualidadeimagem.pipeline.PipelineQualidade;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoPipeline;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/qualidade/avaliar")
public class QualidadeApi {

    private final PipelineQualidade pipeline = new PipelineQualidade();

    @param

    @POST
    @Consumes({"image/jpeg", "imagem/png"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response avaliar(byte[] fotoBytes) {

        try {

            ResultadoPipeline resultado = pipeline.executar(fotoBytes);
            return Response.ok(resultado).build();

        } catch (ValidacaoImagemException e) {

            return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErroResposta(e.getMessage()))
            .build();
        }
    }

    public record ErroResposta(String mensagem) {}
}