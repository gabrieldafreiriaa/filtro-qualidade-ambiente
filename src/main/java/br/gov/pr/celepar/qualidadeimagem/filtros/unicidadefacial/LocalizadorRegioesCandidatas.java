package br.gov.pr.celepar.qualidadeimagem.filtros.unicidadefacial;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import br.gov.pr.celepar.qualidadeimagem.imagem.RegiaoImagem;

public class LocalizadorRegioesCandidatas {

    private static final double FRACAO_AREA_MINIMA = 0.10;
    private static final double PROPORCAO_MINIMA = 1.1;
    private static final double PROPORCAO_MAXIMA = 1.6;

    /** Retorna regiões candidatas por cor e geometria, sem confirmar rostos. */
    public List<RegiaoImagem> localizar(BufferedImage imagem) {
        if (imagem == null) {
            throw new IllegalArgumentException(
                    "A imagem não pode ser nula.");
        }

        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        boolean[][] mascaraPele = new boolean[altura][largura];

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int rgb = imagem.getRGB(x, y);

                mascaraPele[y][x] = ehPixelDePele(rgb);
            }
        }

        boolean[][] visitados = new boolean[altura][largura];
        List<RegiaoImagem> regioes = new ArrayList<>();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (mascaraPele[y][x] && !visitados[y][x]) {
                    ArrayDeque<int[]> fila = new ArrayDeque<>();

                    fila.addLast(new int[] { x, y });
                    visitados[y][x] = true;

                    int minX = x;
                    int maxX = x;
                    int minY = y;
                    int maxY = y;
                    int quantidadePixels = 0;

                    while (!fila.isEmpty()) {
                        int[] pixel = fila.removeFirst();
                        quantidadePixels++;

                        int atualX = pixel[0];
                        int atualY = pixel[1];

                        minX = Math.min(minX, atualX);
                        maxX = Math.max(maxX, atualX);
                        minY = Math.min(minY, atualY);
                        maxY = Math.max(maxY, atualY);

                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dx = -1; dx <= 1; dx++) {
                                int vizinhoX = atualX + dx;
                                int vizinhoY = atualY + dy;

                                if (vizinhoX < 0 || vizinhoX >= largura
                                        || vizinhoY < 0 || vizinhoY >= altura) {
                                    continue;
                                }

                                if (mascaraPele[vizinhoY][vizinhoX]
                                        && !visitados[vizinhoY][vizinhoX]) {
                                    visitados[vizinhoY][vizinhoX] = true;
                                    fila.addLast(new int[] {
                                            vizinhoX, vizinhoY
                                    });
                                }
                            }
                        }
                    }

                    int larguraRegiao = maxX - minX + 1;
                    int alturaRegiao = maxY - minY + 1;

                    // A área usa os pixels do componente, não os do retângulo.
                    double areaImagem = (double) largura * altura;
                    double fracaoArea = quantidadePixels / areaImagem;
                    double proporcao = (double) alturaRegiao / larguraRegiao;

                    if (fracaoArea >= FRACAO_AREA_MINIMA
                            && proporcao >= PROPORCAO_MINIMA
                            && proporcao <= PROPORCAO_MAXIMA) {
                        regioes.add(new RegiaoImagem(
                                minX,
                                minY,
                                larguraRegiao,
                                alturaRegiao));
                    }
                }
            }
        }

        return regioes;
    }

    private boolean ehPixelDePele(int rgb) {
        int vermelho = (rgb >> 16) & 255;
        int verde = (rgb >> 8) & 255;
        int azul = rgb & 255;

        double cb = 128
                - 0.168736 * vermelho
                - 0.331264 * verde
                + 0.5 * azul;

        double cr = 128
                + 0.5 * vermelho
                - 0.418688 * verde
                - 0.081312 * azul;

        return cb >= 80 && cb <= 120
                && cr >= 133 && cr <= 173;
    }
}
