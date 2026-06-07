package com.erichiroshi.desafio_itau.application;

import com.erichiroshi.desafio_itau.application.input.TransacaoInput;
import com.erichiroshi.desafio_itau.application.output.EstatisticaOutput;
import com.erichiroshi.desafio_itau.application.port.out.TransacaoRepositoryPort;
import com.erichiroshi.desafio_itau.domain.model.Transacao;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Use Cases - Orquestração")
@ExtendWith(MockitoExtension.class)
class UseCasesTest {

    // ---------------------------------------------------------------
    // TransacaoSaveUseCase
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("TransacaoSaveUseCase")
    class TransacaoSaveUseCaseTest {

        @Mock
        private TransacaoRepositoryPort repository;

        @InjectMocks
        private TransacaoSaveUseCase useCase;

        @Test
        @DisplayName("deve delegar o save ao repositório com o domínio correto")
        void deveDelegarSaveAoRepositorio() {
            var input = new TransacaoInput(new BigDecimal("100.00"), OffsetDateTime.now().minusSeconds(1));

            useCase.execute(input);

            verify(repository, times(1)).save(any(Transacao.class));
        }

        @Test
        @DisplayName("deve chamar repositório exatamente uma vez")
        void deveChamarRepositorioUmaVez() {
            var input = new TransacaoInput(BigDecimal.TEN, OffsetDateTime.now().minusSeconds(1));

            useCase.execute(input);

            verify(repository, times(1)).save(any(Transacao.class));
            verifyNoMoreInteractions(repository);
        }
    }

    // ---------------------------------------------------------------
    // TransacaoDeleteAllUseCase
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("TransacaoDeleteAllUseCase")
    class TransacaoDeleteAllUseCaseTest {

        @Mock
        private TransacaoRepositoryPort repository;

        @InjectMocks
        private TransacaoDeleteAllUseCase useCase;

        @Test
        @DisplayName("deve delegar o deleteAll ao repositório")
        void deveDelegarDeleteAllAoRepositorio() {
            useCase.execute();

            verify(repository, times(1)).deleteAll();
        }

        @Test
        @DisplayName("deve chamar apenas deleteAll e nada mais")
        void deveChamarApenasDeleteAll() {
            useCase.execute();

            verify(repository, times(1)).deleteAll();

            verifyNoMoreInteractions(repository);
        }
    }

    // ---------------------------------------------------------------
    // EstatisticaUseCase
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("EstatisticaUseCase")
    class EstatisticaUseCaseTest {

        @Mock
        private TransacaoRepositoryPort repository;

        // SimpleMeterRegistry é um MeterRegistry real em memória — sem servidor externo.
        // Usamos @Spy em vez de @Mock para que o Timer.builder().register() funcione de
        // verdade, já que o Micrometer usa o registry internamente de forma não-trivial.
        @Spy
        private MeterRegistry meterRegistry = new SimpleMeterRegistry();

        @InjectMocks
        private EstatisticaUseCase useCase;

        @Test
        @DisplayName("deve retornar output corretamente com transações")
        void deveRetornarOutputComTransacoes() {
            var transacoes = List.of(
                    Transacao.newTransacao(new BigDecimal("50.00"), OffsetDateTime.now().minusSeconds(5)),
                    Transacao.newTransacao(new BigDecimal("150.00"), OffsetDateTime.now().minusSeconds(10))
            );

            when(repository.findAllAfter(any())).thenReturn(transacoes);

            EstatisticaOutput output = useCase.execute();

            assertThat(output.count()).isEqualTo(2L);
            assertThat(output.sum()).isEqualTo(200.0);
            assertThat(output.avg()).isEqualTo(100.0);
            assertThat(output.min()).isEqualTo(50.0);
            assertThat(output.max()).isEqualTo(150.0);
        }

        @Test
        @DisplayName("deve retornar zeros quando repositório está vazio")
        void deveRetornarZerosComRepositorioVazio() {

            when(repository.findAllAfter(any())).thenReturn(Collections.emptyList());

            EstatisticaOutput output = useCase.execute();

            assertThat(output.count()).isZero();
            assertThat(output.sum()).isZero();
        }

        @Test
        @DisplayName("deve chamar findAll60Seconds exatamente uma vez")
        void deveChamarFindAll60SecondsUmaVez() {
            when(repository.findAllAfter(any())).thenReturn(Collections.emptyList());

            useCase.execute();

            verify(repository, times(1)).findAllAfter(any());
        }
    }
}
