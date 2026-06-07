package com.erichiroshi.desafio_itau.infrastructure.http;

import com.erichiroshi.desafio_itau.application.port.in.TransacaoDeletePort;
import com.erichiroshi.desafio_itau.application.port.in.TransacaoSavePort;
import com.erichiroshi.desafio_itau.infrastructure.http.docs.TransacaoControllerDocs;
import com.erichiroshi.desafio_itau.infrastructure.http.request.TransacaoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transacao")
public class TransacaoController implements TransacaoControllerDocs {

    private final TransacaoSavePort transacaoSavePort;
    private final TransacaoDeletePort transacaoDeletePort;

    @PostMapping
    public ResponseEntity<Void> saveTransacao(@RequestBody TransacaoRequest request) {

        transacaoSavePort.execute(request.toInput());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllTransacao() {

        transacaoDeletePort.execute();

        return ResponseEntity.ok().build();
    }
}
