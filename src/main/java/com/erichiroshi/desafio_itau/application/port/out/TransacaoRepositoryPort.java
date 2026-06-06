package com.erichiroshi.desafio_itau.application.port.out;

import com.erichiroshi.desafio_itau.domain.model.Transacao;

public interface TransacaoRepositoryPort {

    void save(Transacao transacao);

    void deleteAll();
}
