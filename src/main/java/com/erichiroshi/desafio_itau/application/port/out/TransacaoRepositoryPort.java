package com.erichiroshi.desafio_itau.application.port.out;

import com.erichiroshi.desafio_itau.domain.model.Transacao;

import java.util.List;

public interface TransacaoRepositoryPort {

    void save(Transacao transacao);

    void deleteAll();

    List<Transacao> findAll60Seconds();
}
