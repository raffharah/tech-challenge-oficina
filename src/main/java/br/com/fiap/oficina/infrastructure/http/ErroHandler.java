package br.com.fiap.oficina.infrastructure.http;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErroHandler {

    private static final Logger log = LoggerFactory.getLogger(ErroHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        List<ErroResponse.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErroResponse.CampoInvalido(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErroResponse("REQUISICAO_INVALIDA", "Requisicao invalida", campos, null));
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ResponseEntity<ErroResponse> naoEncontrado(NaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ErroResponse> conflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    ResponseEntity<ErroResponse> conflitoDePersistencia(Exception ex) {
        log.warn("Conflito de persistencia", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("CONFLITO", "Operacao em conflito com o estado atual dos dados", null, null));
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErroResponse> dominio(DomainException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro nao tratado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse("ERRO_INTERNO", "Erro interno do servidor", null, null));
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, DomainException ex) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(ex.getCodigo(), ex.getMessage(), null, ex.getDetalhes()));
    }
}
