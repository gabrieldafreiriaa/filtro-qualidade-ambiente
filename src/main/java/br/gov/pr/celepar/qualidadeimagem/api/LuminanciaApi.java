package br.gov.pr.celepar.qualidadeimagem.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;
import br.gov.pr.celepar.qualidadeimagem.filtros.luminancia.FiltroLuminancia;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.imagem.ImagemPreparada;
import br.gov.pr.celepar.qualidadeimagem.imagem.PreparadorImagem;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

@Path("/api/qualidade/luminancia")

public class LuminanciaApi {

    private static final long LIMITE_ARQUIVO_BYTES = 5L * 1024 * 1024;
    private static final long LIMITE_PIXELS = 12_000_000L;

    private final PreparadorImagem preparador = new PreparadorImagem(LIMITE_ARQUIVO_BYTES, LIMITE_PIXELS);

    private final FiltroLuminancia filtro = new FiltroLuminancia();

    @POST
    @Consumes({ "image/jpeg", "image/png" })
    @Produces(MediaType.APPLICATION_JSON)
    public Response avaliar(byte[] fotoBytes) {
        ImagemPreparada imagem;

        try {
            imagem = preparador.preparar(fotoBytes);
        } catch (ValidacaoImagemException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("mensagem", e.getMessage()))
                    .build();
        }

        ResultadoAvaliacao resultado = filtro.avaliar(imagem.cinza());

        return Response.ok(Map.of(
                "filtro", resultado.filtro(),
                "status", resultado.status(),
                "media", BigDecimal.valueOf(resultado.valorMedido())
                        .setScale(2, RoundingMode.HALF_UP),
                "limiteMinimo", resultado.limiteMinimo(),
                "limiteMaximo", resultado.limiteMaximo(),
                "escala", resultado.escala(),
                "regiaoAnalisada", resultado.regiaoAnalisada(),
                "mensagem", resultado.mensagem()))
                .build();
    }
}
