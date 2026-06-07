package com.erichiroshi.desafio_itau.infrastructure.http.response;

import com.erichiroshi.desafio_itau.application.output.EstatisticaOutput;

public record EstatisticaResponse(
        long count,
        double sum,
        double avg,
        double min,
        double max
) {
    public static EstatisticaResponse toResponse(EstatisticaOutput estatisticaOutput) {
        return new EstatisticaResponse(
                estatisticaOutput.count(),
                estatisticaOutput.sum(),
                estatisticaOutput.avg(),
                estatisticaOutput.min(),
                estatisticaOutput.max());
    }
}
