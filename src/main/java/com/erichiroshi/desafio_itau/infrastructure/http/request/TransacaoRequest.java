package com.erichiroshi.desafio_itau.infrastructure.http.request;

import com.erichiroshi.desafio_itau.application.input.TransacaoInput;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransacaoRequest(
        BigDecimal valor,
        OffsetDateTime dataHora
) {
    public TransacaoInput toInput() {
        return new TransacaoInput(valor, dataHora);
    }
}
