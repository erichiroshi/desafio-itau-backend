package com.erichiroshi.desafio_itau.shared.http.exception;

import com.erichiroshi.desafio_itau.domain.exception.TransacaoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransacaoException.class)
    public ResponseEntity<Void> handlerTransacao(TransacaoException ex) {

        log.warn("Transação com erros | msg:{}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> handlerHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        log.warn("Json Inválido | msg:{}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
