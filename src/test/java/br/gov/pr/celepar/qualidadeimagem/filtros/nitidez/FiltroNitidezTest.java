package br.gov.pr.celepar.qualidadeimagem.filtros.nitidez;

import boofcv.struct.image.GrayU8;
import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

class FiltroNitidezTest {

    private final FiltroNitidez filtro = new FiltroNitidez();

    // 1. Utilitário interno para simular o recebimento de bytes (igual ao de Simetria)
    private byte[] converterParaByteArray(GrayU8 imagemBoof) throws Exception {
        BufferedImage bufferedImage = new BufferedImage(imagemBoof.width, imagemBoof.height, BufferedImage.TYPE_BYTE_GRAY);
        boofcv.io.image.ConvertBufferedImage.convertTo(imagemBoof, bufferedImage);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    // 2. Teste da matemática pura (Uniforme = Variância 0)
    @Test
    void imagemUniformeDeveReprovarPorFaltaDeNitidez() throws Exception {
        GrayU8 imagemPlana = new GrayU8(100, 100);
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 100; x++) {
                imagemPlana.set(x, y, 128); 
            }
        }
        
        byte[] fotoBytes = converterParaByteArray(imagemPlana);
        ResultadoAvaliacao resultado = filtro.avaliar(fotoBytes);

        // A variância será ~0.0, bem abaixo do limite de 100.0
        assertEquals(StatusAvaliacao.REPROVADO, resultado.status());
        assertEquals(0.0, resultado.valorMedido(), 0.001);
    }

    // 3. Teste do xadrez (Variância altíssima, muitas bordas)
    @Test
    void imagemComBordasAcentuadasDeveSerAprovada() throws Exception {
        GrayU8 imagemXadrez = new GrayU8(10, 10);
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                imagemXadrez.set(x, y, ((x + y) % 2 == 0) ? 0 : 255);
            }
        }

        byte[] fotoBytes = converterParaByteArray(imagemXadrez);
        ResultadoAvaliacao resultado = filtro.avaliar(fotoBytes);

        // A variância vai bater o limite e aprovar com folga
        assertEquals(StatusAvaliacao.APROVADO, resultado.status());
        assertTrue(resultado.valorMedido() > FiltroNitidez.LIMITE_MINIMO);
    }

    // 4. Teste de contrato obrigatório
    @Test
    void arquivoNuloDeveLancarExcecao() {
        ValidacaoImagemException excecao = assertThrows(
            ValidacaoImagemException.class, 
            () -> filtro.avaliar(null) // Passa null direto
        );

        // Garante que a exceção padrão do sistema foi disparada
        assertEquals("O array de bytes da imagem não pode ser nulo ou vazio.", excecao.getMessage());
    }
}