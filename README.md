# Filtro de Qualidade do Ambiente

Microsservico desenvolvido em Java e Quarkus para avaliacao automatica da qualidade de imagens de ambiente, utilizando visao computacional classica com a biblioteca BoofCV.

Este componente integra a arquitetura de servicos da Celepar, tendo como primeira entrega o filtro de luminancia media.

---

## 1. Tecnologias Utilizadas

- Java: OpenJDK 21
- Framework: Quarkus 3.40.1
- Visao Computacional: BoofCV Core 1.1.2
- Gerenciador de Build: Apache Maven (via Maven Wrapper `./mvnw`)
- Testes: JUnit 5 e REST-assured

---

## 2. Estrutura de Pacotes do Projeto

O codigo-fonte esta organizado sob o pacote base `br.gov.pr.celepar.qualidadeimagem`:

- `api`: Camada de exposicao HTTP/REST, responsavel por receber a requisicao, orquestrar as chamadas e responder em JSON.
- `imagem`: Servicos de decodificacao de arquivos (JPEG, PNG), validacao de integridade e conversao de imagem para estruturas de dados em escala de cinza (`GrayF32`).
- `resultado`: DTOs (Data Transfer Objects) e enums que padronizam a resposta estruturada (`ResultadoLuminancia`, `SituacaoAvaliacao`).
- `filtros`: Regras matematicas e operacoes de visao computacional puras, desacopladas do protocolo HTTP.
  - `luminancia`: Implementacao do calculo de luminancia media (`FiltroLuminancia`).

---

## 3. Pre-requisitos

Para executar o projeto em sua estacao de trabalho, sao necessarios:

1. Java Development Kit (JDK) versao 21 instalada e configurada na variavel `JAVA_HOME`.
2. Acesso a internet na primeira execucao para download dos pacotes Maven.
3. Postman ou utilitario `curl` para envio de requisicoes HTTP.

Para checar a versao do Java instalada no terminal:
```bash
java -version
```

---

## 4. Como Executar o Projeto

Abra o terminal na pasta raiz do projeto (`filtro-qualidade-ambiente`):

### 4.1. Modo de Desenvolvimento (Live Coding)

No modo de desenvolvimento, qualquer alteracao no codigo-fonte e recarregada automaticamente sem reiniciar a aplicacao manualmente:

```bash
./mvnw quarkus:dev
```

A aplicacao ficara disponivel em: `http://localhost:8080`
A interface de desenvolvimento do Quarkus (Dev UI) fica em: `http://localhost:8080/q/dev/`

Para parar a aplicacao, pressione `Ctrl + C` no terminal.

### 4.2. Executar os Testes Automatizados

O projeto contem testes unitarios matematicos e testes de integracao da API. Para roda-los:

```bash
./mvnw test
```

### 4.3. Gerar o Pacote para Producao

Para compilar e gerar o pacote executavel jar:

```bash
./mvnw package
```

O arquivo gerado ficara em `target/quarkus-app/quarkus-run.jar`. Para executa-lo:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

---

## 5. Especificacao da API - Filtro de Luminancia

### Endpoint

- Metodo: `POST`
- Caminho: `/api/qualidade/luminancia`
- Headers suportados:
  - `Content-Type: image/jpeg`
  - `Content-Type: image/png`
  - `Content-Type: application/octet-stream`
- Corpo da requisicao: Bytes binarios do arquivo de imagem.

### Regra de Avaliacao

- Formula: Soma de todas as intensidades de cinza dividida pela quantidade total de pixels da imagem.
- Escala: 0 (preto absoluto) a 255 (branco absoluto).
- Criterio de aprovacao: 60.00 <= media <= 200.00.
- Regiao avaliada: Imagem inteira (100%).

### Exemplos de Resposta (JSON)

#### Imagem Aprovada:
```json
{
  "situacao": "APROVADO",
  "medida": 128.54,
  "escala": "0 a 255 (intensidade de cinza)",
  "regiao": "Imagem inteira (100%)",
  "limiteAplicado": "60 <= media <= 200",
  "motivo": "Luminancia dentro dos limites aceitaveis."
}
```

#### Imagem Reprovada (Subexposta ou Superexposta):
```json
{
  "situacao": "REPROVADO",
  "medida": 34.20,
  "escala": "0 a 255 (intensidade de cinza)",
  "regiao": "Imagem inteira (100%)",
  "limiteAplicado": "60 <= media <= 200",
  "motivo": "Luminancia fora dos limites aceitaveis."
}
```

#### Arquivo Invalido ou Corrompido:
```json
{
  "situacao": "INCONCLUSIVO",
  "medida": null,
  "escala": "0 a 255 (intensidade de cinza)",
  "regiao": "Imagem inteira (100%)",
  "limiteAplicado": "60 <= media <= 200",
  "motivo": "O arquivo enviado nao e uma imagem valida ou formato nao suportado."
}
```

---

## 6. Guia Pratico de Teste (Iniciantes)

### 6.1. Testando via Terminal com cURL

Com a aplicacao em execucao (`./mvnw quarkus:dev`), execute o comando abaixo substituindo `/caminho/para/sua_foto.jpg` pelo arquivo real:

```bash
curl -X POST http://localhost:8080/api/qualidade/luminancia \
  -H "Content-Type: image/jpeg" \
  --data-binary "@/caminho/para/sua_foto.jpg"
```

### 6.2. Testando via Postman

1. Abra o Postman e clique em `New` -> `HTTP Request`.
2. Mude o metodo de `GET` para `POST`.
3. Na barra de URL, insira: `http://localhost:8080/api/qualidade/luminancia`
4. Va para a aba `Headers` e adicione:
   - Key: `Content-Type`
   - Value: `image/jpeg` (ou `image/png`)
5. Va para a aba `Body`:
   - Selecione a opcao `binary`.
   - Clique em `Select File` e escolha a imagem no seu computador.
6. Clique no botao azul `Send`.
7. O resultado em formato JSON aparecera na area inferior com os campos `situacao`, `medida` e `motivo`.

---

## 7. Limitacoes Conhecidas e Cuidados

- Tamanho Maximo do Arquivo: A aplicacao impoe um limite de seguranca de 20 MB por arquivo para evitar saturacao de memoria.
- Escopo Atual: A analise de luminancia cobre 100% da area da imagem. Futuras fases integrarao a deteccao e recorte da regiao facial antes do calculo.
