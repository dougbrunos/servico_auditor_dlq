# Serviço Auditor DLQ - Justificativa Arquitetural

## 1. Visão Geral

O Serviço Auditor DLQ é uma aplicação responsável por consumir mensagens de uma fila Dead Letter Queue (DLQ) do AWS SQS, processar essas mensagens aplicando regras de negócio de classificação de severidade e persistir os registros em um banco de dados relacional para auditoria e análise.

## 2. Arquitetura Escolhida: Hexagonal

Optei pela arquitetura Hexagonal por diversos motivos fundamentais:

### 2.1 Isolamento da Lógica de Negócio

A lógica de triagem de severidade não deve estar acoplada a nenhuma tecnologia específica. Ela precisa ser independente de:
- Como a mensagem chega
- Onde os dados são armazenados
- Como são serializados ou desserializados

A arquitetura hexagonal garante isso através da definição de portas (interfaces) que definem o contrato, sem depender de implementações concretas.

### 2.2 Testabilidade

Com essa abordagem, é possível testar a lógica de negócio sem depender de:
- AWS SQS (que pode estar indisponível)
- Banco de dados real (usando mocks)
- Chamadas HTTP externas

Os testes unitários ficam mais rápidos e confiáveis porque testam apenas a lógica, não a infraestrutura.

### 2.3 Substituição de Tecnologias

Se no futuro for necessário trocar a tecnologia, apenas os adapters são modificados. O domínio permanece intacto. Isso reduz drasticamente o risco de regressão e facilita manutenção.

### 2.4 Clareza nas Responsabilidades

Cada camada tem uma responsabilidade bem definida:
- O domínio cuida da lógica de negócio
- As portas definem contratos (interfaces)
- Os adapters implementam esses contratos usando tecnologias específicas


## 3. Por que não usar arquitetura em camadas simples (MVC)?

Uma arquitetura em camadas simples teria os seguintes problemas:

- **Acoplamento**: A lógica de negócio ficaria dependente do JPA/Hibernate
- **Testabilidade**: Seria necessário mockar ou inicializar um banco de dados real para testar
- **Inflexibilidade**: Trocar de SQS para outro message broker exigiria reescrever a camada de service
- **Difícil reutilização**: A lógica de triagem não poderia ser usada por um adaptador REST sem duplicar código