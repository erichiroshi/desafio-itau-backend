package com.erichiroshi.desafio_itau.domain.model;

public record Estatistica(

        long count,
        double sum,
        double avg,
        double min,
        double max
) {
}
