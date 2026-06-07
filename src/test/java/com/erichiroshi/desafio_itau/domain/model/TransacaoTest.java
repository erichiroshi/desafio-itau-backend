package com.erichiroshi.desafio_itau.domain.model;

import com.erichiroshi.desafio_itau.domain.exception.TransacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Transacao - Regras de domínio")
class TransacaoTest {

    @Nested
    @DisplayName("Criação válida")
    class CriacaoValida {

        @Test
        @DisplayName("deve criar transação com valor zero")
        void deveAceitarValorZero() {
            var transacao = Transacao.newTransacao(BigDecimal.ZERO, OffsetDateTime.now().minusSeconds(1));
            assertThat(transacao.valor()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("deve criar transação com valor positivo")
        void deveAceitarValorPositivo() {
            var transacao = Transacao.newTransacao(new BigDecimal("123.45"), OffsetDateTime.now().minusSeconds(1));
            assertThat(transacao.valor()).isEqualByComparingTo("123.45");
        }

        @Test
        @DisplayName("deve criar transação com dataHora no passado imediato")
        void deveAceitarDataHoraPassado() {
            var dataHora = OffsetDateTime.now().minusSeconds(1);
            var transacao = Transacao.newTransacao(BigDecimal.ONE, dataHora);
            assertThat(transacao.dataHora()).isEqualTo(dataHora);
        }

        @Test
        @DisplayName("deve criar transação com dataHora muito no passado")
        void deveAceitarDataHoraMuitoNoPassado() {
            var dataHora = OffsetDateTime.now().minusYears(5);
            assertThatNoException().isThrownBy(() ->
                    Transacao.newTransacao(BigDecimal.ONE, dataHora));
        }
    }

    @Nested
    @DisplayName("Campos nulos")
    class CamposNulos {

        @Test
        @DisplayName("deve lançar exceção quando valor é nulo")
        void deveLancarExcecaoQuandoValorNulo() {
            OffsetDateTime offsetDateTime = OffsetDateTime.now().minusSeconds(1);
            assertThatThrownBy(() -> Transacao.newTransacao(null, offsetDateTime))
                    .isInstanceOf(TransacaoException.class)
                    .hasMessageContaining("nulos");
        }

        @Test
        @DisplayName("deve lançar exceção quando dataHora é nula")
        void deveLancarExcecaoQuandoDataHoraNula() {
            assertThatThrownBy(() -> Transacao.newTransacao(BigDecimal.ONE, null))
                    .isInstanceOf(TransacaoException.class)
                    .hasMessageContaining("nulos");
        }

        @Test
        @DisplayName("deve lançar exceção quando ambos os campos são nulos")
        void deveLancarExcecaoQuandoAmbosNulos() {
            assertThatThrownBy(() -> Transacao.newTransacao(null, null))
                    .isInstanceOf(TransacaoException.class);
        }
    }

    @Nested
    @DisplayName("Restrições de valor")
    class RestricoesDeValor {

        @Test
        @DisplayName("deve rejeitar valor negativo")
        void deveRejeitarValorNegativo() {
            OffsetDateTime offsetDateTime = OffsetDateTime.now().minusSeconds(1);
            BigDecimal valorNegativo = new BigDecimal("-0.01");
            assertThatThrownBy(() ->
                    Transacao.newTransacao(valorNegativo, offsetDateTime))
                    .isInstanceOf(TransacaoException.class)
                    .hasMessageContaining("negativo");
        }

        @Test
        @DisplayName("deve rejeitar valor muito negativo")
        void deveRejeitarValorMuitoNegativo() {
            OffsetDateTime offsetDateTime = OffsetDateTime.now().minusSeconds(1);
            BigDecimal valorNegativo = new BigDecimal("-9999.99");
            assertThatThrownBy(() ->
                    Transacao.newTransacao(valorNegativo, offsetDateTime))
                    .isInstanceOf(TransacaoException.class);
        }
    }

    @Nested
    @DisplayName("Restrições de dataHora")
    class RestricoesDeDataHora {

        @Test
        @DisplayName("deve rejeitar transação no futuro")
        void deveRejeitarTransacaoFutura() {
            OffsetDateTime dataFutura = OffsetDateTime.now().plusSeconds(1);

            assertThatThrownBy(() ->
                    Transacao.newTransacao(BigDecimal.ONE, dataFutura))
                    .isInstanceOf(TransacaoException.class)
                    .hasMessageContaining("futuro");
        }

        @Test
        @DisplayName("deve rejeitar transação muito no futuro")
        void deveRejeitarTransacaoMuitoNoFuturo() {
            OffsetDateTime dataMuitoFutura = OffsetDateTime.now().plusYears(10);
            assertThatThrownBy(() ->
                    Transacao.newTransacao(BigDecimal.ONE, dataMuitoFutura))
                    .isInstanceOf(TransacaoException.class);
        }
    }
}
