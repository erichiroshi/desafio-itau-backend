package com.erichiroshi.desafio_itau.application;

import com.erichiroshi.desafio_itau.application.input.TransacaoInput;
import com.erichiroshi.desafio_itau.application.port.in.TransacaoSavePort;
import com.erichiroshi.desafio_itau.application.port.out.TransacaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransacaoSaveUseCase implements TransacaoSavePort {

    private final TransacaoRepositoryPort transacaoRepository;

    @Override
    public void execute(TransacaoInput input) {

        log.debug("Salvando transação: {}", input);

        transacaoRepository.save(input.toDomain());

        log.debug("Transação salva");
    }
}
