package com.erichiroshi.desafio_itau.infrastructure.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TransacaoController - Testes funcionais (HTTP)")
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void limparEstado() throws Exception {
        mockMvc.perform(delete("/transacao"));
    }

    // ---------------------------------------------------------------
    // POST /transacao
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("POST /transacao - caminho feliz")
    class PostCaminhoFeliz {

        @Test
        @DisplayName("deve retornar 201 com valor positivo e dataHora no passado")
        void deveRetornar201() throws Exception {
            String body = """
                    {
                        "valor": 123.45,
                        "dataHora": "%s"
                    }
                    """.formatted(OffsetDateTime.now().minusSeconds(5)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("deve aceitar valor zero (mínimo permitido)")
        void deveAceitarValorZero() throws Exception {
            String body = """
                    {
                        "valor": 0,
                        "dataHora": "%s"
                    }
                    """.formatted(OffsetDateTime.now().minusSeconds(1)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("deve aceitar transação muito antiga")
        void deveAceitarTransacaoAntiga() throws Exception {
            String body = """
                    {
                        "valor": 50.00,
                        "dataHora": "%s"
                    }
                    """.formatted(OffsetDateTime.now().minusYears(1)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /transacao - 422 Unprocessable Entity")
    class PostUnprocessable {

        @Test
        @DisplayName("deve retornar 422 com dataHora no futuro")
        void deveRetornar422ParaDataFutura() throws Exception {
            String body = """
                    {
                        "valor": 100.00,
                        "dataHora": "%s"
                    }
                    """.formatted(OffsetDateTime.now().plusSeconds(10)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("deve retornar 422 com valor negativo")
        void deveRetornar422ParaValorNegativo() throws Exception {
            String body = """
                    {
                        "valor": -0.01,
                        "dataHora": "%s"
                    }
                    """.formatted(OffsetDateTime.now().minusSeconds(1)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("deve retornar 422 com valor nulo")
        void deveRetornar422ComValorNulo() throws Exception {
            String body = """
                    {
                        "valor": null,
                        "dataHora": "%s"
                    }
                    """.formatted(OffsetDateTime.now().minusSeconds(1)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("deve retornar 422 com dataHora nula")
        void deveRetornar422ComDataHoraNula() throws Exception {
            String body = """
                    {
                        "valor": 100.00,
                        "dataHora": null
                    }
                    """;

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("POST /transacao - 400 Bad Request")
    class PostBadRequest {

        @Test
        @DisplayName("deve retornar 400 com JSON malformado")
        void deveRetornar400ComJsonMalformado() throws Exception {
            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalido"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 com dataHora em formato inválido")
        void deveRetornar400ComDataHoraInvalida() throws Exception {
            String body = """
                    {
                        "valor": 100.00,
                        "dataHora": "nao-e-uma-data"
                    }
                    """;

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 com body completamente vazio")
        void deveRetornar400ComBodyVazio() throws Exception {
            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 com valor como string inválida")
        void deveRetornar400ComValorString() throws Exception {
            String body = """
                    {
                        "valor": "abc",
                        "dataHora": "%s"
                    }
                    """.formatted(OffsetDateTime.now().minusSeconds(1)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            mockMvc.perform(post("/transacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---------------------------------------------------------------
    // DELETE /transacao
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /transacao")
    class DeleteTransacao {

        @Test
        @DisplayName("deve retornar 200 ao deletar todas as transações")
        void deveRetornar200AoDeletar() throws Exception {
            mockMvc.perform(delete("/transacao"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 200 mesmo quando não há transações")
        void deveRetornar200QuandoVazio() throws Exception {
            mockMvc.perform(delete("/transacao"))
                    .andExpect(status().isOk());
            // segunda chamada também deve retornar 200
            mockMvc.perform(delete("/transacao"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("após deletar, estatísticas devem retornar zeros")
        void estatisticasDevemSerZerosAposDelete() throws Exception {
            // Salva uma transação
            String body = """
                    {
                        "valor": 100.00,
                        "dataHora": "%s"
                    }
                    """.formatted(OffsetDateTime.now().minusSeconds(1)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Deleta
            mockMvc.perform(delete("/transacao"))
                    .andExpect(status().isOk());

            // Verifica que estatísticas são zero
            mockMvc.perform(get("/estatistica"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(0));
        }
    }
}
