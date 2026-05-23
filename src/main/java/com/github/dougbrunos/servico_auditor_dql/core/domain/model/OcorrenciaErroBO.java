package com.github.dougbrunos.servico_auditor_dql.core.domain.model;

import java.util.List;

public class OcorrenciaErroBO {
    private final String zipCode;
    private final Long customerId;
    private final List<ItemPedidoBO> orderItems;
    private final String origin;
    private final String occurredAt;
    private final String conteudoOriginalJson;

    public OcorrenciaErroBO(String zipCode, Long customerId, List<ItemPedidoBO> orderItems, String origin, String occurredAt, String conteudoOriginalJson) {
        this.zipCode = zipCode;
        this.customerId = customerId;
        this.orderItems = orderItems;
        this.origin = origin;
        this.occurredAt = occurredAt;
        this.conteudoOriginalJson = conteudoOriginalJson;
    }

    private int calcularTotalProdutos() {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0;
        }

        int somaProdutos = 0;
        for (ItemPedidoBO item : orderItems) {
            if (item.getAmount() != null) {
                somaProdutos += item.getAmount();
            }
        }
        return somaProdutos;
    }

    public String classificarNivelSeveridade() {
        int totalProdutos = calcularTotalProdutos();

        if (totalProdutos > 100) {
            return "HIGH";
        } else if (totalProdutos >= 50) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    public String getZipCode() {
        return zipCode;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public List<ItemPedidoBO> getOrderItems() {
        return orderItems;
    }

    public String getOrigin() {
        return origin;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public String getConteudoOriginalJson() {
        return conteudoOriginalJson;
    }
}