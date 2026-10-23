package br.gov.pr.celepar.qualidadeimagem.api;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.filtros.nitidez.FiltroNitidez;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidadorImagem;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Path("/api/qualidade/nitidez")
public class NitidezApi {

    private static final long LIMITE_ARQUIVO_BYTES = 5L * 1024 * 1024;
    private final FiltroNitidez filtro = new FiltroNitidez();

    @POST
    @Consumes({"image/jpeg", "image/png"})
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> avaliar(byte[] fotoBytes) {
        if (fotoBytes == null || fotoBytes.length == 0) {
            return Map.of("mensagem", "Nenhuma imagem recebida.");
        }
        if (!ValidadorImagem.tamanhoArquivoValido(fotoBytes, LIMITE_ARQUIVO_BYTES)) {
            return Map.of("mensagem", "A imagem excede o tamanho máximo permitido de 5 MiB.");
        }

        try (ByteArrayInputStream entrada = new ByteArrayInputStream(fotoBytes)) {
            BufferedImage foto = ImageIO.read(entrada);

            if (foto == null) {
                return Map.of("mensagem", "O conteudo enviado não foi reconhecido como imagem.");
            }

            GrayU8 imagem = ConvertBufferedImage.convertFromSingle(foto, null, GrayU8.class);
            ResultadoAvaliacao resultado = filtro.avaliar(imagem);

            return Map.of(
                    "status", resultado.status(),
                    "variancia", BigDecimal.valueOf(resultado.valorMedido()).setScale(2, RoundingMode.HALF_UP),
                    "mensagem", resultado.mensagem()
            );
        } catch (IOException e) {
            return Map.of("mensagem", "Não foi possivel ler a imagem enviada.");
        }
    }
}
