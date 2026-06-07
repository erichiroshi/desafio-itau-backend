package com.erichiroshi.desafio_itau.infrastructure.http.docs;

import com.erichiroshi.desafio_itau.infrastructure.http.response.EstatisticaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Estatísticas", description = "Estatísticas das transações dos últimos 60 segundos")
public interface EstatisticaControllerDocs {

    @Operation(
            summary = "Calcular estatísticas",
            description = "Retorna count, sum, avg, min e max das transações " +
                    "ocorridas nos últimos 60 segundos. " +
                    "Retorna zeros quando não há transações no período."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Estatísticas calculadas com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EstatisticaResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "count": 3,
                              "sum": 160.0,
                              "avg": 53.33,
                              "min": 10.0,
                              "max": 100.0
                            }
                            """)
            )
    )
    ResponseEntity<EstatisticaResponse> getEstatisticaLast60Seconds();
}