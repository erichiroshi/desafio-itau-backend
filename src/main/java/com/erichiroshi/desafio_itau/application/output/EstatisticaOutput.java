package com.erichiroshi.desafio_itau.application.output;

import com.erichiroshi.desafio_itau.domain.model.Estatistica;

public record EstatisticaOutput(

        long count,
        double sum,
        double avg,
        double min,
        double max
) {
    public static EstatisticaOutput toOutput(Estatistica estatistica) {
        return new EstatisticaOutput(estatistica.count(),
                estatistica.sum(),
                estatistica.avg(),
                estatistica.min(),
                estatistica.max());
    }
}
