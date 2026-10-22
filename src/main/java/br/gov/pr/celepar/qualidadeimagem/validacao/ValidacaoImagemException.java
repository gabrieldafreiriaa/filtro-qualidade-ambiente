package br.gov.pr.celepar.qualidadeimagem.validacao;

public class ValidacaoImagemException extends RuntimeException {

    public ValidacaoImagemException(String mensagem) {
        super(mensagem);
    }

    public ValidacaoImagemException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}