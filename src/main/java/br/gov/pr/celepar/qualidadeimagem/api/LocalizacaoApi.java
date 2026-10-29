package br.gov.pr.celepar.qualidadeimagem.api;

import java.util.List;
import java.util.Map;

import br.gov.pr.celepar.qualidadeimagem.filtros.unicidadefacial.LocalizadorRegioesCandidatas;
import br.gov.pr.celepar.qualidadeimagem.imagem.ImagemPreparada;
import br.gov.pr.celepar.qualidadeimagem.imagem.PreparadorImagem;
import br.gov.pr.celepar.qualidadeimagem.imagem.RegiaoImagem;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/qualidade/localizacao")
public class LocalizacaoApi {

    private static final long LIMITE_ARQUIVO_BYTES = 5L * 1024 * 1024;
    private static final long LIMITE_PIXELS = 12_000_000L;

    private final PreparadorImagem preparador = new PreparadorImagem(LIMITE_ARQUIVO_BYTES, LIMITE_PIXELS);

    private final LocalizadorRegioesCandidatas localizador = new LocalizadorRegioesCandidatas();

    @POST
    @Consumes({ "image/jpeg", "image/png" })
    @Produces(MediaType.APPLICATION_JSON)
    public Response localizar(byte[] fotoBytes) {
        ImagemPreparada imagem;

        try {
            imagem = preparador.preparar(fotoBytes);
        } catch (ValidacaoImagemException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("mensagem", e.getMessage()))
                    .build();
        }

        List<RegiaoImagem> regioes = localizador.localizar(imagem.original());

        return Response.ok(Map.of(
                "quantidadeCandidatas", regioes.size(),
                "regioes", regioes))
                .build();
    }
}