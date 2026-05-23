package com.github.dougbrunos.servico_auditor_dql.application.ports.out.persistence;

import com.github.dougbrunos.servico_auditor_dql.core.domain.model.OcorrenciaErroBO;

public interface AuditoriaPort {
    void salvar(OcorrenciaErroBO ocorrencia);
}