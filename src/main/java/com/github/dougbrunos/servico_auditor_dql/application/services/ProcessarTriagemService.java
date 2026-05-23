package com.github.dougbrunos.servico_auditor_dql.application.services;

import com.github.dougbrunos.servico_auditor_dql.application.ports.in.ProcessarTriagem;
import com.github.dougbrunos.servico_auditor_dql.application.ports.out.persistence.AuditoriaPort;
import com.github.dougbrunos.servico_auditor_dql.core.domain.model.OcorrenciaErroBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessarTriagemService implements ProcessarTriagem {

    private static final Logger logger = LoggerFactory.getLogger(ProcessarTriagemService.class);
    private final AuditoriaPort auditoriaPort;

    public ProcessarTriagemService(AuditoriaPort auditoriaPort) {
        this.auditoriaPort = auditoriaPort;
    }

    @Override
    @Transactional
    public void processar(OcorrenciaErroBO ocorrencia) {
        try {
            String severidade = ocorrencia.classificarNivelSeveridade();
            auditoriaPort.salvar(ocorrencia);
            logger.info("Triagem concluída. Cliente: {}, Severidade: {}", ocorrencia.getCustomerId(), severidade);
        } catch (Exception ex) {
            logger.error("Erro ao processar triagem: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
