package com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.out.persistence.h2.mapper;

import com.github.dougbrunos.servico_auditor_dql.core.domain.model.OcorrenciaErroBO;
import com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.out.persistence.h2.entity.AuditoriaEntity;
import org.springframework.stereotype.Component;

@Component
public class DominioParaEntidadeConverter {

    public AuditoriaEntity converterParaLinhaTabela(OcorrenciaErroBO bo) {
        AuditoriaEntity entidade = new AuditoriaEntity();
        entidade.setPayload(bo.getConteudoOriginalJson());
        return entidade;
    }

}