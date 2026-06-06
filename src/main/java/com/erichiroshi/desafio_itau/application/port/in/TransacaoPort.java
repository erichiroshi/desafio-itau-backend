package com.erichiroshi.desafio_itau.application.port.in;

import com.erichiroshi.desafio_itau.application.input.TransacaoInput;

public interface TransacaoPort {

    void execute(TransacaoInput input);
}
