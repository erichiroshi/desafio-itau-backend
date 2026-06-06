package com.erichiroshi.desafio_itau.domain.model;

import com.erichiroshi.desafio_itau.domain.exception.TransacaoException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Transacao(
        BigDecimal valor,
        OffsetDateTime dataHora
) {

    public static Transacao newTransacao(BigDecimal valor, OffsetDateTime dataHora) {
        if (valor == null || dataHora == null) {
            throw new TransacaoException("Valor ou dataHora não podem ser nulos.");
        }

        if (dataHora.isAfter(OffsetDateTime.now())) {
            throw new TransacaoException("A transação NÃO DEVE acontecer no futuro.");
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransacaoException("A transação NÃO DEVE ter valor negativo");
        }

        return new Transacao(valor, dataHora);
    }

}
