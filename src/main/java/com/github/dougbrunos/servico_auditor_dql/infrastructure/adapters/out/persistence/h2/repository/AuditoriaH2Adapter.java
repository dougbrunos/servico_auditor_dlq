package com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.out.persistence.h2.repository;

import com.github.dougbrunos.servico_auditor_dql.application.ports.out.persistence.AuditoriaPort;
import com.github.dougbrunos.servico_auditor_dql.core.domain.model.OcorrenciaErroBO;
import com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.out.persistence.h2.entity.AuditoriaEntity;
import com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.out.persistence.h2.jpa.AuditoriaRepository;
import com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.out.persistence.h2.mapper.DominioParaEntidadeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class AuditoriaH2Adapter implements AuditoriaPort {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaH2Adapter.class);
    private final AuditoriaRepository repository;
    private final DominioParaEntidadeConverter conversorEntidade;
    private final String fila;

    public AuditoriaH2Adapter(AuditoriaRepository repository,
                              DominioParaEntidadeConverter conversorEntidade,
                              @Value("${queue.order-events-dlq}") String fila) {
        this.repository = repository;
        this.conversorEntidade = conversorEntidade;
        this.fila = fila;
    }

    @Override
    @Transactional
    public void salvar(OcorrenciaErroBO ocorrencia) {
        AuditoriaEntity tabelaLinha = conversorEntidade.converterParaLinhaTabela(ocorrencia);

        String errorId = UUID.randomUUID().toString();
        tabelaLinha.setErrorId(errorId);
        tabelaLinha.setQueueName(this.fila);
        tabelaLinha.setTimestamp(OffsetDateTime.now().toString());
        tabelaLinha.setStatus("PENDING_ANALYSIS");
        tabelaLinha.setSeverity(ocorrencia.classificarNivelSeveridade());

        repository.save(tabelaLinha);

        logger.info("Ocorrência salva. ErrorId: {}, Severidade: {}, Cliente: {}",
            errorId, tabelaLinha.getSeverity(), ocorrencia.getCustomerId());
    }
}