package br.gov.pr.celepar.qualidadeimagem.api;

import br.gov.pr.celepar.qualidadeimagem.filtros.simetrialuz.FiltroSimetriaLuz;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/qualidade/simetria-luz")
public class SimetriaLuzApi {

    private final FiltroSimetriaLuz filtro = new FiltroSimetriaLuz();

    @POST
    @Consumes({"image/jpeg", "image/png"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response avaliarSimetria(byte[] fotoBytes) {
        try {
            ResultadoAvaliacao resultado = filtro.avaliar(fotoBytes);
            return Response.ok(resultado).build();
        } catch (ValidacaoImagemException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErroResposta(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErroResposta("Erro interno ao processar a imagem."))
                .build();
        }
    }

    public record ErroResposta(String mensagem) {}
}