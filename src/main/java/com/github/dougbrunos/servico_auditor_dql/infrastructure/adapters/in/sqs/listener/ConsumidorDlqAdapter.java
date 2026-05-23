package com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.in.sqs.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dougbrunos.servico_auditor_dql.application.ports.in.ProcessarTriagem;
import com.github.dougbrunos.servico_auditor_dql.core.domain.model.OcorrenciaErroBO;
import com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.in.sqs.converter.MensagemParaDominioConverter;
import com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.in.sqs.dto.MensagemRecebidaDTO;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConsumidorDlqAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ConsumidorDlqAdapter.class);
    private final ProcessarTriagem triagem;
    private final ObjectMapper jsonMapper;
    private final MensagemParaDominioConverter dominioConverter;

    public ConsumidorDlqAdapter(ProcessarTriagem triagem,
                                MensagemParaDominioConverter conversorDominio,
                                ObjectMapper jsonMapper) {
        this.triagem = triagem;
        this.dominioConverter = conversorDominio;
        this.jsonMapper = jsonMapper;
    }

    @SqsListener("${queue.order-events-dlq}")
    @Transactional
    public void processarEntradaDlq(String mensagem) {
        try {
            logger.info("Mensagem recebida da DLQ: {}", mensagem);
            MensagemRecebidaDTO dados = jsonMapper.readValue(mensagem, MensagemRecebidaDTO.class);
            OcorrenciaErroBO negocioBO = dominioConverter.converter(dados, mensagem);
            triagem.processar(negocioBO);
            logger.info("Mensagem da DLQ processada e persistida com sucesso. Cliente: {}, Severidade: {}",
                negocioBO.getCustomerId(), negocioBO.classificarNivelSeveridade());
        } catch (Exception ex) {
            logger.error("ERRO no processamento da DLQ. Detalhes: {}", ex.getMessage(), ex);
            throw new RuntimeException("Falha ao processar mensagem da DLQ", ex);
        }
    }

}