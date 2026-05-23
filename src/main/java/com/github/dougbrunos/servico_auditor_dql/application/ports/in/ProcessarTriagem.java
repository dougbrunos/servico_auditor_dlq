package com.github.dougbrunos.servico_auditor_dql.application.ports.in;

import com.github.dougbrunos.servico_auditor_dql.core.domain.model.OcorrenciaErroBO;

public interface ProcessarTriagem {
    void processar(OcorrenciaErroBO ocorrencia);
}