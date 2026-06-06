package com.erichiroshi.desafio_itau.infrastructure.http;

import com.erichiroshi.desafio_itau.application.port.in.TransacaoPort;
import com.erichiroshi.desafio_itau.infrastructure.http.request.TransacaoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transacao")
public class TransacaoController {

    private final TransacaoPort transacaoPort;

    @PostMapping
    public ResponseEntity<Void> saveTransacao(@RequestBody TransacaoRequest request) {

        transacaoPort.execute(request.toInput());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
