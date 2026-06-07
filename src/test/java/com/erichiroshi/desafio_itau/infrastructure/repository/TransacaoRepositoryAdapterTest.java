package com.erichiroshi.desafio_itau.infrastructure.repository;

import com.erichiroshi.desafio_itau.domain.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransacaoRepositoryAdapter - Armazenamento em memória")
class TransacaoRepositoryAdapterTest {

    private TransacaoRepositoryAdapter repository;

    @BeforeEach
    void setUp() {
        repository = new TransacaoRepositoryAdapter();
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve salvar transação e ela aparecer no findAll60Seconds")
        void deveSalvarTransacao() {
            var transacao = Transacao.newTransacao(BigDecimal.TEN, OffsetDateTime.now().minusSeconds(5));
            repository.save(transacao);

            List<Transacao> result = repository.findAllAfter(OffsetDateTime.now().minusSeconds(60));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(transacao);
        }

        @Test
        @DisplayName("deve acumular múltiplas transações")
        void deveAcumularTransacoes() {
            repository.save(Transacao.newTransacao(BigDecimal.ONE, OffsetDateTime.now().minusSeconds(5)));
            repository.save(Transacao.newTransacao(BigDecimal.TEN, OffsetDateTime.now().minusSeconds(10)));
            repository.save(Transacao.newTransacao(new BigDecimal("50"), OffsetDateTime.now().minusSeconds(15)));

            assertThat(repository.findAllAfter(OffsetDateTime.now().minusSeconds(60))).hasSize(3);
        }
    }

    @Nested
    @DisplayName("deleteAll()")
    class DeleteAll {

        @Test
        @DisplayName("deve limpar todas as transações")
        void deveLimparTodasAsTransacoes() {
            repository.save(Transacao.newTransacao(BigDecimal.ONE, OffsetDateTime.now().minusSeconds(5)));
            repository.save(Transacao.newTransacao(BigDecimal.TEN, OffsetDateTime.now().minusSeconds(10)));

            repository.deleteAll();

            assertThat(repository.findAllAfter(OffsetDateTime.now().minusSeconds(60))).isEmpty();
        }

        @Test
        @DisplayName("não deve falhar ao deletar repositório já vazio")
        void naoDeveFalharQuandoVazio() {
            repository.deleteAll(); // não deve lançar exceção
            assertThat(repository.findAllAfter(OffsetDateTime.now().minusSeconds(60))).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll60Seconds()")
    class FindAll60Seconds {

        @Test
        @DisplayName("deve retornar lista vazia quando não há transações")
        void deveRetornarVazioSemTransacoes() {
            assertThat(repository.findAllAfter(OffsetDateTime.now().minusSeconds(60))).isEmpty();
        }

        @Test
        @DisplayName("deve excluir transações com mais de 60 segundos")
        void deveExcluirTransacoesAntigas() {
            var antiga = Transacao.newTransacao(BigDecimal.ONE, OffsetDateTime.now().minusSeconds(61));
            var recente = Transacao.newTransacao(BigDecimal.TEN, OffsetDateTime.now().minusSeconds(5));

            repository.save(antiga);
            repository.save(recente);

            List<Transacao> result = repository.findAllAfter(OffsetDateTime.now().minusSeconds(60));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(recente);
        }

        @Test
        @DisplayName("deve excluir transações com exatamente 60 segundos (fronteira)")
        void deveExcluirTransacaoComExatamente60Segundos() {
            OffsetDateTime referencia = OffsetDateTime.now().minusSeconds(60);
            // Salva com exatamente o mesmo instante que será usado como filtro
            var noBorde = Transacao.newTransacao(BigDecimal.ONE, referencia);
            repository.save(noBorde);

            List<Transacao> result = repository.findAllAfter(referencia);
            assertThat(result).isEmpty(); // isAfter é estrito — igual não passa
        }

        @Test
        @DisplayName("deve incluir transações com 59 segundos")
        void deveIncluirTransacaoComMenosDe60Segundos() {
            var quaseNoLimite = Transacao.newTransacao(BigDecimal.ONE, OffsetDateTime.now().minusSeconds(59));
            repository.save(quaseNoLimite);

            assertThat(repository.findAllAfter(OffsetDateTime.now().minusSeconds(60))).hasSize(1);
        }

        @Test
        @DisplayName("deve retornar apenas transações dentro da janela de 60s")
        void deveRetornarApenasTransacoesDentroJanela() {
            repository.save(Transacao.newTransacao(new BigDecimal("1"), OffsetDateTime.now().minusSeconds(10)));
            repository.save(Transacao.newTransacao(new BigDecimal("2"), OffsetDateTime.now().minusSeconds(30)));
            repository.save(Transacao.newTransacao(new BigDecimal("3"), OffsetDateTime.now().minusSeconds(59)));
            repository.save(Transacao.newTransacao(new BigDecimal("4"), OffsetDateTime.now().minusSeconds(61)));
            repository.save(Transacao.newTransacao(new BigDecimal("5"), OffsetDateTime.now().minusYears(1)));

            List<Transacao> result = repository.findAllAfter(OffsetDateTime.now().minusSeconds(60));
            assertThat(result).hasSize(3);
        }
    }
}
