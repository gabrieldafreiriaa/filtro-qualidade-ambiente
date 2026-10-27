package br.gov.pr.celepar.qualidadeimagem.filtros.simetrialuz;

import br.gov.pr.celepar.qualidadeimagem.resultado.ResultadoAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.resultado.StatusAvaliacao;
import br.gov.pr.celepar.qualidadeimagem.validacao.ValidacaoImagemException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

class FiltroSimetriaLuzTest {

    private final FiltroSimetriaLuz filtro = new FiltroSimetriaLuz();

    // Utilitário interno do teste para fingir que um byte[] veio da internet
    private byte[] converterMatrizParaByteArray(GrayU8 imagemBoof) throws Exception {
        BufferedImage bufferedImage = new BufferedImage(imagemBoof.width, imagemBoof.height, BufferedImage.TYPE_BYTE_GRAY);
        boofcv.io.image.ConvertBufferedImage.convertTo(imagemBoof, bufferedImage);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    @Test
    void imagemComIluminacaoPerfeitaDeveSerAprovada() throws Exception {
        // Cenário: Criamos uma imagem de 100x100 onde todos os pixels têm cor 128 (cinza médio)
        GrayU8 imagemPerfeita = new GrayU8(100, 100);
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 100; x++) {
                imagemPerfeita.set(x, y, 128);
            }
        }
        
        // Simula o byte[] chegando do endpoint
        byte[] fotoBytes = converterMatrizParaByteArray(imagemPerfeita);

        // Ação: Passamos para o filtro avaliar
        ResultadoAvaliacao resultado = filtro.avaliar(fotoBytes);

        // Verificação: A diferença deve ser 0 (pois esquerdo=128 e direito=128)
        assertEquals(StatusAvaliacao.APROVADO, resultado.status());
        assertEquals(0.0, resultado.valorMedido(), 0.001);
    }

    @Test
    void imagemComSombraLateralDuraDeveSerReprovada() throws Exception {
        // Cenário: Criamos uma imagem 100x100
        // O lado esquerdo será claro (cor 200) e o lado direito será escuro (cor 50)
        GrayU8 imagemComSombra = new GrayU8(100, 100);
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 100; x++) {
                if (x < 50) {
                    imagemComSombra.set(x, y, 200); // Esquerda clara
                } else {
                    imagemComSombra.set(x, y, 50);  // Direita escura
                }
            }
        }

        byte[] fotoBytes = converterMatrizParaByteArray(imagemComSombra);

        // Ação
        ResultadoAvaliacao resultado = filtro.avaliar(fotoBytes);

        // Verificação: A diferença média deve ser 150 (200 - 50 = 150), o que é bem maior que o limite (15.0)
        assertEquals(StatusAvaliacao.REPROVADO, resultado.status());
        assertEquals(150.0, resultado.valorMedido(), 0.001);
    }

    @Test
    void arquivoNuloDeveLancarExcecao() {
        // Cenário e Ação juntos: Tenta passar um array nulo para a assinatura exigida pelo Sênior
        ValidacaoImagemException excecao = assertThrows(
            ValidacaoImagemException.class, 
            () -> filtro.avaliar(null)
        );

        // Verificação: Garante que a exceção estourou e que a API receberia a mensagem correta
        assertEquals("O array de bytes da imagem não pode ser nulo ou vazio.", excecao.getMessage());
    }
}