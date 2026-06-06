package com.erichiroshi.desafio_itau.application.input;

import com.erichiroshi.desafio_itau.domain.model.Transacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransacaoInput(
        BigDecimal valor,
        OffsetDateTime dataHora
) {

    public Transacao toDomain() {
        return Transacao.newTransacao(this.valor(), this.dataHora());
    }
}