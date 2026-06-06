package com.erichiroshi.desafio_itau.application.port.in;

import com.erichiroshi.desafio_itau.application.input.TransacaoInput;

public interface TransacaoSavePort {

    void execute(TransacaoInput input);
}
