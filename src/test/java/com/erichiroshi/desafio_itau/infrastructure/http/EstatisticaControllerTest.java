package com.erichiroshi.desafio_itau.infrastructure.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.estatistica.janela-segundos=60")
@DisplayName("EstatisticaController - Testes funcionais (HTTP)")
class EstatisticaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void limparEstado() throws Exception {
        mockMvc.perform(delete("/transacao"));
    }

    private void salvarTransacao(String valor, OffsetDateTime dataHora) throws Exception {
        String body = """
                {
                    "valor": %s,
                    "dataHora": "%s"
                }
                """.formatted(valor, dataHora.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        mockMvc.perform(post("/transacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Nested
    @DisplayName("GET /estatistica - sem transações")
    class SemTransacoes {

        @Test
        @DisplayName("deve retornar 200 com todos os valores zerados")
        void deveRetornarZerosQuandoSemTransacoes() throws Exception {
            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(0))
                    .andExpect(jsonPath("$.sum").value(0))
                    .andExpect(jsonPath("$.avg").value(0))
                    .andExpect(jsonPath("$.min").value(0))
                    .andExpect(jsonPath("$.max").value(0));
        }

        @Test
        @DisplayName("deve retornar JSON com os campos obrigatórios mesmo sem transações")
        void deveRetornarCamposObrigatorios() throws Exception {
            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").exists())
                    .andExpect(jsonPath("$.sum").exists())
                    .andExpect(jsonPath("$.avg").exists())
                    .andExpect(jsonPath("$.min").exists())
                    .andExpect(jsonPath("$.max").exists());
        }
    }

    @Nested
    @DisplayName("GET /estatistica - com transações recentes")
    class ComTransacoesRecentes {

        @Test
        @DisplayName("deve calcular estatísticas de uma única transação")
        void deveCalcularUmaTransacao() throws Exception {
            salvarTransacao("100.00", OffsetDateTime.now().minusSeconds(5));

            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1))
                    .andExpect(jsonPath("$.sum").value(100.0))
                    .andExpect(jsonPath("$.avg").value(100.0))
                    .andExpect(jsonPath("$.min").value(100.0))
                    .andExpect(jsonPath("$.max").value(100.0));
        }

        @Test
        @DisplayName("deve calcular min e max corretamente com múltiplas transações")
        void deveCalcularMinMaxComMultiplasTransacoes() throws Exception {
            salvarTransacao("10.00", OffsetDateTime.now().minusSeconds(5));
            salvarTransacao("50.00", OffsetDateTime.now().minusSeconds(10));
            salvarTransacao("30.00", OffsetDateTime.now().minusSeconds(15));

            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(3))
                    .andExpect(jsonPath("$.sum").value(90.0))
                    .andExpect(jsonPath("$.avg").value(30.0))
                    .andExpect(jsonPath("$.min").value(10.0))
                    .andExpect(jsonPath("$.max").value(50.0));
        }

        @Test
        @DisplayName("deve considerar apenas transações dos últimos 60 segundos")
        void deveConsiderarApenasTransacoesRecentes() throws Exception {
            salvarTransacao("200.00", OffsetDateTime.now().minusSeconds(5));   // dentro
            salvarTransacao("999.00", OffsetDateTime.now().minusSeconds(61));  // fora
            salvarTransacao("999.00", OffsetDateTime.now().minusYears(1));     // fora

            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1))
                    .andExpect(jsonPath("$.sum").value(200.0));
        }

        @Test
        @DisplayName("deve retornar content-type application/json")
        void deveRetornarContentTypeJson() throws Exception {
            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("GET /estatistica - cenários de borda")
    class CenariosDeBorda {

        @Test
        @DisplayName("deve retornar zeros após deletar transações")
        void deveRetornarZerosAposDelete() throws Exception {
            salvarTransacao("500.00", OffsetDateTime.now().minusSeconds(5));

            mockMvc.perform(delete("/transacao"));

            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(0))
                    .andExpect(jsonPath("$.sum").value(0));
        }

        @Test
        @DisplayName("deve calcular corretamente com transação de valor zero")
        void deveCalcularComValorZero() throws Exception {
            salvarTransacao("0", OffsetDateTime.now().minusSeconds(5));

            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1))
                    .andExpect(jsonPath("$.sum").value(0))
                    .andExpect(jsonPath("$.min").value(0))
                    .andExpect(jsonPath("$.max").value(0));
        }

        @Test
        @DisplayName("deve retornar count correto com muitas transações")
        void deveContarMuitasTransacoes() throws Exception {
            for (int i = 1; i <= 10; i++) {
                salvarTransacao(String.valueOf(i * 10), OffsetDateTime.now().minusSeconds(i));
            }

            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(10))
                    .andExpect(jsonPath("$.min").value(10.0))
                    .andExpect(jsonPath("$.max").value(100.0));
        }
    }
}
