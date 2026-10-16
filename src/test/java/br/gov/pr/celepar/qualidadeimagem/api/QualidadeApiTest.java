package br.gov.pr.celepar.qualidadeimagem.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;

@QuarkusTest
class QualidadeApiTest {

    @Test
    @DisplayName("Deve avaliar imagem JPEG valida e aprovar luminancia")
    void testLuminanciaAprovada() throws IOException {
        byte[] imagemBytes = criarImagemBytes(128, 128, 128, "jpeg");

        given()
                .contentType("image/jpeg")
                .body(imagemBytes)
                .when()
                .post("/api/qualidade/luminancia")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("situacao", is("APROVADO"))
                .body("regiao", is("Imagem inteira (100%)"))
                .body("escala", is("0 a 255 (intensidade de cinza)"))
                .body("medida", org.hamcrest.Matchers.equalTo(128.0f));
    }

    @Test
    @DisplayName("Deve retornar inconclusivo para arquivo corrompido ou invalido")
    void testArquivoInvalido() {
        byte[] bytesInvalidos = "isto nao e uma imagem valida".getBytes();

        given()
                .contentType("image/jpeg")
                .body(bytesInvalidos)
                .when()
                .post("/api/qualidade/luminancia")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("situacao", is("INCONCLUSIVO"));
    }

    @Test
    @DisplayName("Deve retornar status 400 Bad Request para corpo vazio")
    void testCorpoVazio() {
        given()
                .contentType("image/jpeg")
                .body(new byte[0])
                .when()
                .post("/api/qualidade/luminancia")
                .then()
                .statusCode(400)
                .body("situacao", is("INCONCLUSIVO"));
    }

    private byte[] criarImagemBytes(int r, int g, int b, String formato) throws IOException {
        BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(r, g, b));
        g2d.fillRect(0, 0, 50, 50);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formato, baos);
        return baos.toByteArray();
    }
}
