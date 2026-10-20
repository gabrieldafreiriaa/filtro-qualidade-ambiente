package br.gov.pr.celepar.qualidadeimagem.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.awt.image.BufferedImage; //Representa a imagem decodificada na memória.
import java.io.ByteArrayInputStream; //Permite ler os bytes recebidos como uma fonte de dados.
import java.io.IOException; //Representa erros que podem ocorrer durante a leitura.
import javax.imageio.ImageIO; //Oferece métodos para ler e escrever imagens.
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.filtros.luminancia.FiltroLuminancia;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;

@Path("/api/qualidade/luminancia")

public class LuminanciaApi {

    private final FiltroLuminancia filtro = new FiltroLuminancia();

    @POST
    @Consumes({ "image/jpeg", "image/png" })
    @Produces({ MediaType.TEXT_PLAIN })
    public String avaliar(byte[] fotoBytes) {
        if (fotoBytes == null || fotoBytes.length == 0) {
            return "Nenhuma imagem recebida.";
        }

        try (ByteArrayInputStream entrada = new ByteArrayInputStream(fotoBytes)) {
            BufferedImage foto = ImageIO.read(entrada);

            if (foto == null) {
                return "O conteudo enviado não foi reconhecido como imagem.";
            }

            GrayU8 imagem = ConvertBufferedImage.convertFromSingle(
                    foto,
                    null,
                    GrayU8.class);

            ResultadoAvaliacao resultado = filtro.avaliar(imagem);

            return "Status: " + resultado.status()
                    + " | Média: " + resultado.valorMedido()
                    + " | Mensagem: " + resultado.mensagem();
        } catch (IOException e) {
            return "Não foi possivel ler a imagem enviada.";
        }
    }

}
