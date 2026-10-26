package br.gov.pr.celepar.qualidadeimagem.api;

import br.gov.pr.celepar.qualidadeimagem.filtros.nitidez.FiltroNitidez;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/qualidade/nitidez")
public class NitidezApi {

    private final FiltroNitidez filtro = new FiltroNitidez();

    @POST
    @Consumes({"image/jpeg", "image/png"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response avaliar(byte[] fotoBytes) {
        try {
            // Toda a validação e processamento acontecem dentro do filtro
            ResultadoAvaliacao resultado = filtro.avaliar(fotoBytes);
            return Response.ok(resultado).build();
            
        } catch (ValidacaoImagemException e) {
            // Captura erros de arquivo nulo, corrompido ou fora de formato
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(new ErroResposta(e.getMessage()))
                           .build();
        } catch (Exception e) {
            // Captura falhas inesperadas do servidor
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new ErroResposta("Erro interno ao processar a imagem."))
                           .build();
        }
    }
    
    // Record auxiliar para formatar a mensagem de erro em JSON
    public record ErroResposta(String mensagem) {}
}