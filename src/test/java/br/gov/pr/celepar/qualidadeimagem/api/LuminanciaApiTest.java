package br.gov.pr.celepar.qualidadeimagem.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.aMapWithSize;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class LuminanciaApiTest {

    @Test
    void deveRetornarAvaliacaoEmJson() throws Exception {
        BufferedImage imagem = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        imagem.setRGB(0, 0, 0x808080);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", bytes);

        given()
                .contentType("image/png")
                .body(bytes.toByteArray())
                .when().post("/api/qualidade/luminancia")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("$", aMapWithSize(3))
                .body("status", equalTo("APROVADO"))
                .body("media", equalTo(128.0f))
                .body("mensagem", equalTo("Luminância dentro dos parâmetros aceitáveis."));
    }

    @Test
    void deveRetornarErroEmJsonParaConteudoInvalido() {
        given()
                .contentType("image/png")
                .body("conteudo invalido")
                .when().post("/api/qualidade/luminancia")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("mensagem", equalTo("O conteudo enviado não foi reconhecido como imagem."));
    }
}
