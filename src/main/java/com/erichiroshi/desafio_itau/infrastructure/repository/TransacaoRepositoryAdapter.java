package com.erichiroshi.desafio_itau.infrastructure.repository;

import com.erichiroshi.desafio_itau.application.port.out.TransacaoRepositoryPort;
import com.erichiroshi.desafio_itau.domain.model.Transacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Repository
public class TransacaoRepositoryAdapter implements TransacaoRepositoryPort {

    private final CopyOnWriteArrayList<Transacao> transacaoList = new CopyOnWriteArrayList<>();

    @Override
    public void save(Transacao transacao) {

        log.debug("Salvando transação (Local) | {}", transacao);

        transacaoList.add(transacao);

        log.debug("Transação salva (Local) | {}", transacaoList.getLast());
    }

    @Override
    public void deleteAll() {

        log.debug("Deletando todas as transações (Local)");

        transacaoList.clear();

        log.debug("Todas as transações foram deletadas (Local)");
    }

    @Override
    public List<Transacao> findAllAfter(OffsetDateTime from) {

        log.debug("Estatística dos últimos 60 segundos (Local)");

        List<Transacao> list = transacaoList.stream().filter(t ->
                        t.dataHora().isAfter(from))
                .toList();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        ZoneId fusoLocal = ZoneId.systemDefault();

        if (log.isDebugEnabled()) {
            log.debug("timestamps now: {} | list: {}",
                    OffsetDateTime.now().format(fmt),
                    list.stream()
                            .map(t -> t.dataHora().atZoneSameInstant(fusoLocal).format(fmt))
                            .toList()
            );
        }
        return list;
    }

}
