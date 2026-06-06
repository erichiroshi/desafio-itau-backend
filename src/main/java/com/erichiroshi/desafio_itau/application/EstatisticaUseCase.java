package com.erichiroshi.desafio_itau.application;

import com.erichiroshi.desafio_itau.application.port.in.EstatisticaPort;
import com.erichiroshi.desafio_itau.application.port.out.TransacaoRepositoryPort;
import com.erichiroshi.desafio_itau.application.port.output.EstatisticaOutput;
import com.erichiroshi.desafio_itau.application.service.EstatisticaService;
import com.erichiroshi.desafio_itau.domain.model.Estatistica;
import com.erichiroshi.desafio_itau.domain.model.Transacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EstatisticaUseCase implements EstatisticaPort {

    private final TransacaoRepositoryPort transacaoRepository;
    private final EstatisticaService estatisticaService;

    @Override
    public EstatisticaOutput execute() {

        List<Transacao> list = transacaoRepository.findAll60Seconds();

        log.debug("Buscando estatísticas");

        Estatistica estatistica = estatisticaService.getEstatistica(list);

        log.debug("Estatísticas encontradas: {}", estatistica);

        return EstatisticaOutput.toOutput(estatistica);

    }
}
