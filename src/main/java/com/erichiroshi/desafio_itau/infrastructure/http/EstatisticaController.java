package com.erichiroshi.desafio_itau.infrastructure.http;

import com.erichiroshi.desafio_itau.application.EstatisticaUseCase;
import com.erichiroshi.desafio_itau.application.port.output.EstatisticaOutput;
import com.erichiroshi.desafio_itau.infrastructure.http.response.EstatisticaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/estatistica")
@RestController
public class EstatisticaController {

    private final EstatisticaUseCase estatisticaUseCase;

    @GetMapping
    public ResponseEntity<EstatisticaResponse> getEstatisticaLast60Seconds() {

        EstatisticaOutput estatisticaOutput = estatisticaUseCase.execute();

        EstatisticaResponse response = EstatisticaResponse.toResponse(estatisticaOutput);

        return ResponseEntity.ok().body(response);
    }
}
