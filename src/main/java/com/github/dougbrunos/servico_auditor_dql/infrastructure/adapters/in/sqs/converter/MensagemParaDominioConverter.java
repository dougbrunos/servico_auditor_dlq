package com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.in.sqs.converter;

import com.github.dougbrunos.servico_auditor_dql.core.domain.model.ItemPedidoBO;
import com.github.dougbrunos.servico_auditor_dql.core.domain.model.OcorrenciaErroBO;
import com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.in.sqs.dto.MensagemItemDTO;
import com.github.dougbrunos.servico_auditor_dql.infrastructure.adapters.in.sqs.dto.MensagemRecebidaDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MensagemParaDominioConverter {
    public OcorrenciaErroBO converter(MensagemRecebidaDTO dto, String jsonBruto) {
        List<ItemPedidoBO> listaItensBO = new ArrayList<>();

        if (dto.getOrderItems() != null) {
            for (MensagemItemDTO itemDto : dto.getOrderItems()) {
                listaItensBO.add(new ItemPedidoBO(itemDto.getSku(), itemDto.getAmount()));
            }
        }

        return new OcorrenciaErroBO(
                dto.getZipCode(),
                dto.getCustomerId(),
                listaItensBO,
                dto.getOrigin(),
                dto.getOccurredAt(),
                jsonBruto
        );
    }
}