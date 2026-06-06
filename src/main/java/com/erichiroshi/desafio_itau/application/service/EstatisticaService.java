package com.erichiroshi.desafio_itau.application.service;

import com.erichiroshi.desafio_itau.domain.model.Estatistica;
import com.erichiroshi.desafio_itau.domain.model.Transacao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstatisticaService {

    public Estatistica getEstatistica(List<Transacao> list) {

        if (list.isEmpty()) {
            return new Estatistica(0, 0, 0, 0, 0);
        }

        List<BigDecimal> bigDecimals = list.stream().map(Transacao::valor).toList();

        DoubleSummaryStatistics stats = bigDecimals.stream().map(BigDecimal::doubleValue)
                .collect(Collectors.summarizingDouble(Double::doubleValue));

        long count = stats.getCount();
        double sum = stats.getSum();
        double min = stats.getMin();
        double max = stats.getMax();
        double avg = stats.getAverage();

        return new Estatistica(count, sum, avg, min, max);
    }
}
