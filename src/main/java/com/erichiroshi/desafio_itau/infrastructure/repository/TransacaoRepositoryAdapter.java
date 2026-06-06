package com.erichiroshi.desafio_itau.infrastructure.repository;

import com.erichiroshi.desafio_itau.application.port.out.TransacaoRepositoryPort;
import com.erichiroshi.desafio_itau.domain.model.Transacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class TransacaoRepositoryAdapter implements TransacaoRepositoryPort {

    private final List<Transacao> transacaoList = new ArrayList<>();

    @Override
    public void save(Transacao transacao) {

        log.debug("Salvando transação (Local) | {}", transacao);

        transacaoList.add(transacao);

        log.debug("Transação salva (Local) | {}", transacaoList.getLast());
    }

}
