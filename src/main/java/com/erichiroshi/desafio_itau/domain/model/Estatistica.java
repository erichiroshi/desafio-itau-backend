package com.erichiroshi.desafio_itau.domain.model;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

public record Estatistica(

        long count,
        double sum,
        double avg,
        double min,
        double max
) {

    public static Estatistica getEstatistica(List<Transacao> list) {

        if (list.isEmpty()) {
            return new Estatistica(0, 0, 0, 0, 0);
        }

        DoubleSummaryStatistics stats = list.stream()
                .collect(Collectors.summarizingDouble(t -> t.valor().doubleValue()));

        long count = stats.getCount();
        double sum = stats.getSum();
        double min = stats.getMin();
        double max = stats.getMax();
        double avg = stats.getAverage();

        return new Estatistica(count, sum, avg, min, max);
    }
}
