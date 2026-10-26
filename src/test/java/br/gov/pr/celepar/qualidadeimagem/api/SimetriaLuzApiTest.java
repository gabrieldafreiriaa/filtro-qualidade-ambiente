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
public class SimetriaLuzApiTest {

    @Test
    void deveRetornarAvaliacaoAprovadaParaIluminacaoSimetrica() throws Exception {
        BufferedImage imagem = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                imagem.setRGB(x, y, 0x808080);
            }
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", bytes);

        given()
                .contentType("image/png")
                .body(bytes.toByteArray())
                .when().post("/api/qualidade/simetria-luz")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("$", aMapWithSize(8))
                .body("status", equalTo("APROVADO"))
                .body("valorMedido", equalTo(0.0f))
                .body("mensagem", equalTo("Iluminação simétrica."));
    }

    @Test
    void deveRetornarAvaliacaoReprovadaParaSombraLateral() throws Exception {
        BufferedImage imagem = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                int cor = (x < 10) ? 0xC8C8C8 : 0x323232; // Esquerda clara (200), direita escura (50)
                imagem.setRGB(x, y, cor);
            }
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", bytes);

        given()
                .contentType("image/png")
                .body(bytes.toByteArray())
                .when().post("/api/qualidade/simetria-luz")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("status", equalTo("REPROVADO"))
                .body("mensagem", equalTo("Assimetria de luz detectada. Possível sombra lateral dura."));
    }

    @Test
    void deveRetornarErroParaConteudoInvalido() {
        given()
                .contentType("image/png")
                .body("conteudo invalido".getBytes(StandardCharsets.UTF_8))
                .when().post("/api/qualidade/simetria-luz")
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
                .when().post("/api/qualidade/simetria-luz")
                .then()
                .statusCode(400)
                .contentType("application/json")
                .body("mensagem", equalTo("O array de bytes da imagem não pode ser nulo ou vazio."));
    }
}
