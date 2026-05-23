package com.github.dougbrunos.servico_auditor_dql.core.domain.model;

public class ItemPedidoBO {
    private final Integer sku;
    private final Integer amount;

    public ItemPedidoBO(Integer sku, Integer amount) {
        this.sku = sku;
        this.amount = amount;
    }

    public Integer getSku() {
        return sku;
    }

    public Integer getAmount() {
        return amount;
    }
}