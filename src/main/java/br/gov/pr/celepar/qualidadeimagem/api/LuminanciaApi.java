package br.gov.pr.celepar.qualidadeimagem.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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
    public Map<String, Object> avaliar(byte[] fotoBytes) {
        ImagemPreparada imagem;

        try {
            imagem = preparador.preparar(fotoBytes);
        } catch (ValidacaoImagemException e) {
            return Map.of("mensagem", e.getMessage());
        }

        ResultadoAvaliacao resultado = filtro.avaliar(imagem.cinza());

        return Map.of(
                "status", resultado.status(),
                "media", BigDecimal.valueOf(resultado.valorMedido())
                        .setScale(2, RoundingMode.HALF_UP),
                "mensagem", resultado.mensagem());
    }
}
