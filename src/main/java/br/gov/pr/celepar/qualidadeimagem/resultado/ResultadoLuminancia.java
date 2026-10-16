package br.gov.pr.celepar.qualidadeimagem.resultado;

public class ResultadoLuminancia {

    private SituacaoAvaliacao situacao;
    private Float medida;
    private String escala;
    private String regiao;
    private String limiteAplicado;
    private String motivo;

    public ResultadoLuminancia() {
    }

    public ResultadoLuminancia(SituacaoAvaliacao situacao, Float medida, String escala, String regiao, String limiteAplicado, String motivo) {
        this.situacao = situacao;
        this.medida = medida;
        this.escala = escala;
        this.regiao = regiao;
        this.limiteAplicado = limiteAplicado;
        this.motivo = motivo;
    }

    public static ResultadoLuminancia inconclusivo(String motivo) {
        return new ResultadoLuminancia(
                SituacaoAvaliacao.INCONCLUSIVO,
                null,
                "0 a 255 (intensidade de cinza)",
                "Imagem inteira (100%)",
                "60 <= media <= 200",
                motivo
        );
    }

    public static ResultadoLuminancia avaliado(float media, boolean aprovado) {
        return new ResultadoLuminancia(
                aprovado ? SituacaoAvaliacao.APROVADO : SituacaoAvaliacao.REPROVADO,
                media,
                "0 a 255 (intensidade de cinza)",
                "Imagem inteira (100%)",
                "60 <= media <= 200",
                aprovado ? "Luminancia dentro dos limites aceitaveis." : "Luminancia fora dos limites aceitaveis."
        );
    }

    public SituacaoAvaliacao getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoAvaliacao situacao) {
        this.situacao = situacao;
    }

    public Float getMedida() {
        return medida;
    }

    public void setMedida(Float medida) {
        this.medida = medida;
    }

    public String getEscala() {
        return escala;
    }

    public void setEscala(String escala) {
        this.escala = escala;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public String getLimiteAplicado() {
        return limiteAplicado;
    }

    public void setLimiteAplicado(String limiteAplicado) {
        this.limiteAplicado = limiteAplicado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
