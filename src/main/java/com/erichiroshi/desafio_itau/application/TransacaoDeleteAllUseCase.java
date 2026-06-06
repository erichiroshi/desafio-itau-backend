package com.erichiroshi.desafio_itau.application;

import com.erichiroshi.desafio_itau.application.port.in.TransacaoDeletePort;
import com.erichiroshi.desafio_itau.application.port.out.TransacaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransacaoDeleteAllUseCase implements TransacaoDeletePort {

    private final TransacaoRepositoryPort transacaoRepository;

    @Override
    public void execute() {

        log.debug("Deletando todas as transações");

        transacaoRepository.deleteAll();

        log.debug("Todas as transações foram deletadas");

    }
}
