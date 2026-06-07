package com.erichiroshi.desafio_itau.infrastructure.http.docs;

import com.erichiroshi.desafio_itau.infrastructure.http.request.TransacaoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Transações", description = "Recebimento e limpeza de transações financeiras")
public interface TransacaoControllerDocs {

    @Operation(
            summary = "Registrar transação",
            description = "Recebe uma transação financeira com valor e data/hora. " +
                    "Rejeita transações no futuro, com valor negativo ou campos nulos."
    )

    @ApiResponse(responseCode = "201", description = "Transação aceita e registrada",
            content = @Content)
    @ApiResponse(responseCode = "422", description = "Transação inválida (futuro, valor negativo ou campo nulo)",
            content = @Content)
    @ApiResponse(responseCode = "400", description = "JSON malformado ou tipo de campo inválido",
            content = @Content)
    ResponseEntity<Void> saveTransacao(@RequestBody TransacaoRequest request);

    @Operation(
            summary = "Limpar todas as transações",
            description = "Remove todas as transações armazenadas em memória."
    )
    @ApiResponse(responseCode = "200", description = "Transações removidas com sucesso",
            content = @Content)
    ResponseEntity<Void> deleteAllTransacao();
}