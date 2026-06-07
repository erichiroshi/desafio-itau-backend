package com.erichiroshi.desafio_itau.application;

import com.erichiroshi.desafio_itau.application.input.TransacaoInput;
import com.erichiroshi.desafio_itau.application.output.EstatisticaOutput;
import com.erichiroshi.desafio_itau.application.port.out.TransacaoRepositoryPort;
import com.erichiroshi.desafio_itau.domain.model.Transacao;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
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

        @BeforeEach
        void configurarJanela() {
            // injeta o valor do @Value sem subir contexto Spring
            ReflectionTestUtils.setField(useCase, "janelaSegundos", 60L);
        }

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
            assertThat(output.avg()).isZero();
            assertThat(output.min()).isZero();
            assertThat(output.max()).isZero();
        }

        @Test
        @DisplayName("deve chamar findAllAfter exatamente uma vez por execução")
        void deveChamarFindAllAfterUmaVez() {
            when(repository.findAllAfter(any())).thenReturn(Collections.emptyList());

            useCase.execute();

            verify(repository, times(1)).findAllAfter(any());
        }

        @Test
        @DisplayName("deve passar o OffsetDateTime correto baseado na janela configurada")
        void devePassarOffsetDateTimeCorreto() {
            when(repository.findAllAfter(any())).thenReturn(Collections.emptyList());

            useCase.execute();

            ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
            verify(repository).findAllAfter(captor.capture());

            OffsetDateTime from = captor.getValue();
            OffsetDateTime esperado = OffsetDateTime.now().minusSeconds(60);

            // Aceita até 2s de diferença para evitar flakiness em ambientes lentos
            assertThat(from).isCloseTo(esperado, within(2, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("deve registrar a métrica no MeterRegistry a cada execução")
        void deveRegistrarMetrica() {
            when(repository.findAllAfter(any())).thenReturn(Collections.emptyList());

            useCase.execute();
            useCase.execute();
            useCase.execute();

            var timer = meterRegistry.find("estatistica.calculo").timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("deve respeitar janela configurada com valor diferente de 60")
        void deveRespeitarJanelaConfigurada() {
            ReflectionTestUtils.setField(useCase, "janelaSegundos", 120L);
            when(repository.findAllAfter(any())).thenReturn(Collections.emptyList());

            useCase.execute();

            ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
            verify(repository).findAllAfter(captor.capture());

            OffsetDateTime esperado = OffsetDateTime.now().minusSeconds(120);
            assertThat(captor.getValue()).isCloseTo(esperado, within(2, ChronoUnit.SECONDS));
        }
    }
}
