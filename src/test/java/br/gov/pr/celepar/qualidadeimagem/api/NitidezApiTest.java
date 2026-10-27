package br.gov.pr.celepar.qualidadeimagem.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.aMapWithSize;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class NitidezApiTest {

    @Test
    void deveRetornarAvaliacaoEmJsonParaImagemUniforme() throws Exception {
        // Imagem 10x10 cinza uniforme -> Laplaciano e variância = 0 -> REPROVADO
        BufferedImage imagem = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                imagem.setRGB(x, y, 0x808080);
            }
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", bytes);

        given()
                .contentType("image/png")
                .body(bytes.toByteArray())
                .when().post("/api/qualidade/nitidez")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("$", aMapWithSize(8))
                .body("filtro", equalTo("NITIDEZ"))
                .body("status", equalTo("REPROVADO"))
                .body("valorMedido", equalTo(0.0f))
                .body("mensagem", equalTo("Imagem com foco insuficiente ou borrada."));
    }

    @Test
    void deveRetornarAvaliacaoAprovadaParaImagemNitida() throws Exception {
        // Imagem 20x20 com padrão xadrez preto/branco -> alta variância do Laplaciano -> APROVADO
        BufferedImage imagem = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                int cor = ((x + y) % 2 == 0) ? 0xFFFFFF : 0x000000;
                imagem.setRGB(x, y, cor);
            }
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", bytes);

        given()
                .contentType("image/png")
                .body(bytes.toByteArray())
                .when().post("/api/qualidade/nitidez")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("status", equalTo("APROVADO"))
                .body("mensagem", equalTo("Nitidez dentro dos parâmetros aceitáveis."));
    }

    @Test
    void deveRetornarErroEmJsonParaConteudoInvalido() {
        given()
                .contentType("image/png")
                .body("conteudo invalido".getBytes(StandardCharsets.UTF_8))
                .when().post("/api/qualidade/nitidez")
                .then()
                .statusCode(400)
                .contentType("application/json")
                .body("mensagem", equalTo("O conteúdo enviado não foi reconhecido como imagem."));
    }

    @Test
    void deveRetornarErroParaPayloadVazio() {
        given()
                .contentType("image/png")
                .body(new byte[0])
                .when().post("/api/qualidade/nitidez")
                .then()
                .statusCode(400)
                .contentType("application/json")
                .body("mensagem", equalTo("O array de bytes da imagem não pode ser nulo ou vazio."));
    }
}
