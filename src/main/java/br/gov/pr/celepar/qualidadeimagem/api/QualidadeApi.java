package br.gov.pr.celepar.qualidadeimagem.api;

import br.gov.pr.celepar.qualidadeimagem.filtros.luminancia.FiltroLuminancia;
import br.gov.pr.celepar.qualidadeimagem.imagem.ProcessadorImagem;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoLuminancia;
import boofcv.struct.image.GrayF32;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/qualidade")
public class QualidadeApi {

    private final FiltroLuminancia filtroLuminancia = new FiltroLuminancia();
    private final ProcessadorImagem processadorImagem = new ProcessadorImagem();

    @POST
    @Path("/luminancia")
    @Consumes({"image/jpeg", "image/png", MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.APPLICATION_JSON)
    public Response testarLuminancia(byte[] fotoBytes) {
        if (fotoBytes == null || fotoBytes.length == 0) {
            ResultadoLuminancia resultado = ResultadoLuminancia.inconclusivo("Nenhum dado ou arquivo recebido na requisicao.");
            return Response.status(Response.Status.BAD_REQUEST).entity(resultado).build();
        }

        try {
            GrayF32 imagemCinza = processadorImagem.decodificarParaEscalaDeCinza(fotoBytes);
            float media = filtroLuminancia.calcularMedia(imagemCinza);
            boolean aprovado = filtroLuminancia.estaAprovado(media);

            ResultadoLuminancia resultado = ResultadoLuminancia.avaliado(media, aprovado);
            return Response.ok(resultado).build();

        } catch (IllegalArgumentException e) {
            ResultadoLuminancia resultado = ResultadoLuminancia.inconclusivo(e.getMessage());
            return Response.ok(resultado).build();
        } catch (Exception e) {
            ResultadoLuminancia resultado = ResultadoLuminancia.inconclusivo("Erro no processamento da imagem: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resultado).build();
        }
    }
}
