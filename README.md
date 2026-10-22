# Filtro de Qualidade do Ambiente

Projeto Java 21 com Quarkus e BoofCV para avaliar propriedades de imagens por operações clássicas, sem modelos treinados. O escopo implementado é a avaliação da luminância média da imagem inteira. A localização do rosto, o recorte facial, a nitidez e os demais filtros ainda não estão implementados. A aplicação não identifica pessoas nem comprova presença física.

## Requisitos e execução

É necessário JDK 21. O Maven Wrapper está incluído; a primeira execução precisa de acesso aos repositórios para baixar Maven e dependências.

```bash
./mvnw quarkus:dev
```

A API fica disponível em `http://localhost:8080`. Para compilar, testar e empacotar:

```bash
./mvnw test
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Os testes geram pequenas imagens em memória, sem precisar de fotografias pessoais. Cobrem o cálculo de luminância, limites de entrada, preparação e respostas HTTP. O endpoint de exemplo `GET /hello` do Quarkus também permanece no projeto.

## Endpoint de luminância

`POST /api/qualidade/luminancia`

Envie os bytes do arquivo diretamente no corpo, com `Content-Type: image/png` ou `image/jpeg`. A entrada não é multipart nem Base64. A resposta da aplicação usa `application/json`.

```bash
curl -i -X POST http://localhost:8080/api/qualidade/luminancia \
  -H 'Content-Type: image/png' \
  -H 'Accept: application/json' \
  --data-binary '@foto.png'
```

Para JPEG, use `Content-Type: image/jpeg` e o caminho do arquivo correspondente.

No Postman:

1. Selecione `POST` e informe a URL do endpoint.
2. Em **Body → binary**, selecione a imagem.
3. Em **Headers**, configure o `Content-Type` correspondente ao arquivo.
4. Envie a requisição e confira o código HTTP e o JSON.

## Limites e formatos

| Regra | Valor atual |
|---|---|
| Formatos reais permitidos | PNG e JPEG |
| Tamanho máximo do arquivo | 5 MiB, ou 5.242.880 bytes |
| Quantidade máxima de pixels | 12.000.000 (`largura × altura`) |
| Dimensões | Largura e altura maiores que zero |

Os limites são inclusivos e estão definidos em `LuminanciaApi`. São parâmetros operacionais iniciais ajustáveis, não critérios comprovados de qualidade facial. Não há peso mínimo nem resolução mínima de qualidade definidos.

O preparador verifica o formato reconhecido pelo `ImageIO` nos bytes do arquivo. Alterar a extensão ou declarar um GIF como `image/png` não o torna permitido. O cabeçalho HTTP e o formato real são verificados separadamente; não há exigência adicional de correspondência entre PNG e JPEG quando ambos são permitidos.

A validação de bytes ocorre antes da leitura; as dimensões são consultadas antes da decodificação da matriz de pixels. O servidor HTTP pode aplicar um limite próprio e rejeitar o corpo antes da execução do método. O projeto ainda não configura esse limite explicitamente em `application.properties`.

## Cálculo e resposta

A imagem é convertida para `GrayU8` usando `ConvertBufferedImage.convertFromSingle` do BoofCV. A média dos valores em cinza, na escala de 0 a 255, é calculada por `ImageStatistics.mean`:

```text
média = soma das intensidades / quantidade de pixels
```

A regra atual aprova `60 ≤ média ≤ 200`. Valores abaixo de 60 ou acima de 200 são reprovados. O cálculo e a decisão usam o valor original; apenas a média exposta no JSON é arredondada para duas casas decimais com `HALF_UP`.

Exemplo ilustrativo de avaliação concluída, HTTP `200`:

```json
{
  "filtro": "LUMINANCIA",
  "status": "APROVADO",
  "media": 128.00,
  "limiteMinimo": 60.0,
  "limiteMaximo": 200.0,
  "escala": "0-255",
  "regiaoAnalisada": "IMAGEM_TOTAL",
  "mensagem": "Luminância dentro dos parâmetros aceitáveis."
}
```

Uma imagem decodificada que falha na regra de luminância também retorna HTTP `200`, com `status: REPROVADO`. Isso indica que a avaliação foi concluída.

Entradas vazias, formatos reais não permitidos, falhas de leitura e limites excedidos tratados pelo preparador retornam HTTP `400`. Exemplo:

```json
{
  "mensagem": "Formato de imagem não permitido. Envie um arquivo PNG ou JPEG."
}
```

Um `Content-Type` não aceito pelo endpoint pode gerar HTTP `415`. Rejeições do servidor, anteriores ao método, podem ter outro formato de resposta. A ordem das propriedades JSON não é garantida.

O JSON de uma avaliação concluída contém oito campos: `filtro`, `status`, `media`, `limiteMinimo`, `limiteMaximo`, `escala`, `regiaoAnalisada` e `mensagem`. Os limites se aplicam à média na escala informada; `IMAGEM_TOTAL` indica que todos os pixels da imagem são analisados. As respostas de entrada inválida contêm apenas `mensagem`.

## Organização e reutilização

| Componente | Responsabilidade |
|---|---|
| `api/LuminanciaApi` | Receber HTTP, coordenar chamadas e montar a resposta |
| `validacao/ValidadorImagem` | Regras compartilhadas de validade, bytes e pixels |
| `validacao/ValidacaoImagemException` | Comunicar falhas de preparação |
| `imagem/PreparadorImagem` | Receber `byte[]`, validar, decodificar e converter |
| `imagem/ImagemPreparada` | Agrupar imagem colorida e versão cinza |
| `filtros/luminancia/FiltroLuminancia` | Calcular a média e aplicar os limites |
| `resultado` | Resultado Java e situações da avaliação |

Exemplo de reutilização em Java:

```java
PreparadorImagem preparador = new PreparadorImagem(5L * 1024 * 1024, 12_000_000L);
ImagemPreparada imagem = preparador.preparar(fotoBytes);
ResultadoAvaliacao resultado = new FiltroLuminancia().avaliar(imagem.cinza());
```

Prepare uma vez para compartilhar a imagem entre filtros. As imagens contidas no record são mutáveis: filtros devem apenas lê-las ou copiar antes de modificar. O filtro também valida a imagem recebida para proteger chamadas diretas.

## Limitações e próximos passos

A avaliação atual usa `IMAGEM_TOTAL`: fundo, cabelo e roupas influenciam a média. Média baixa isoladamente não comprova iluminação inadequada do rosto. Uma imagem decodificável também não comprova que o conteúdo seja uma fotografia ou contenha um rosto.

O filtro permite informar o nome da região analisada, mas esse parâmetro apenas descreve a entrada: não localiza nem recorta o rosto. A localização facial, análise regional e medidas complementares serão desenvolvidas separadamente. Os limiares atuais são referências iniciais que precisam ser avaliadas com imagens representativas.

O roteiro imediato está em `descritivo/Filtro_Qualidade_Ambiente_Guia_Inicial.pdf`; a proposta mais ampla também está em `descritivo/`.
