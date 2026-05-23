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

## 4. Estrutura de Pastas e Justificativas

### 4.1 application/

Esta camada contém as regras e fluxos de orquestração da aplicação. É o intermediário entre o mundo externo (adapters) e o domínio (lógica pura).

#### ports/in/

Aqui ficam as interfaces (portas) que definem como o domínio pode ser acionado de fora. ProcessarTriagem.java é uma porta de entrada porque:

- Define um contrato claro: qualquer coisa que queira processar triagem deve seguir essa interface
- Não especifica COMO os dados chegam (poderia ser SQS, HTTP, arquivo, etc.)
- Permite que no futuro tenhamos múltiplos adapters chamando a mesma lógica com diferentes origens
- Facilita testes: posso criar um mock dessa interface

#### services/

ProcessarTriagemService implementa a porta ProcessarTriagem. Por que separar interface de implementação?

- Desacoplamento: o adaptador SQS não conhece ProcessarTriagemService, conhece apenas ProcessarTriagem
- Flexibilidade: posso ter múltiplas implementações da mesma porta (uma para processamento síncrono, outra para assíncrono)
- Testabilidade: testo a interface, não a classe concreta
- Inversão de Control: o framework injeta ProcessarTriagem (a interface), não a implementação

### 4.2 core/domain/

Este é o coração da aplicação. Tudo que está aqui deve ser 100% independente de tecnologia.

#### domain/model/

ItemPedidoBO.java e OcorrenciaErroBO.java são Business Objects. Por que "BO" e não "Entity" ou "Model"?

- **Desmarca claramente que é regra de negócio**: Qualquer desenvolvedor vendo "BO" sabe que contém lógica crítica
- **Separa do conceito de Entity JPA**: Uma Entity JPA é apenas um reflexo da tabela. Um BO contém comportamento
- **Facilita replicação**: O mesmo OcorrenciaErroBO pode ser persistido em diferentes tecnologias (SQL, NoSQL, XML)
- **Encapsulamento de lógica**: A método classificarNivelSeveridade() fica aqui, não em um Service genérico

A lógica de severidade (HIGH/MEDIUM/LOW) está no OcorrenciaErroBO porque:

- É regra de negócio pura, não depende de como os dados são armazenados
- Pode ser testada isoladamente sem banco de dados
- Se a regra mudar, sabemos exatamente onde procurar
- Poderia ser usada por múltiplos serviços (auditoria, analytics, etc.)

#### domain/exceptions/

RegraNegocioException.java é uma exceção customizada. Por que não usar Exception genérica?

- Claridade semântica: catch(RegraNegocioException) deixa claro que é erro de domínio, não de framework
- Diferencia problemas: um RegraNegocioException é tratado diferente de um SQLException
- Logging específico: posso logar e monitorar erros de negócio separadamente
- Contrato claro: a porta ProcessarTriagem declara que lança RegraNegocioException, então quem a chama sabe que pode falhar

### 4.3 infrastructure/

Tudo o que é específico de tecnologia fica aqui. O resto do código não sabe que AWS existe.

#### adapters/in/

O conceito "in" significa que dados ENTRAM no sistema por aqui.

##### sqs/

Por que separar SQS em seus próprios pacotes?

- Isolamento: se trocar para Kafka, crio uma nova pasta kafka/ sem tocar no código existente
- Escalabilidade: cada message broker tem complexidades próprias (FIFO, grupos de consumo, DLQs)
- Responsabilidade única: o adapter SQS apenas converte mensagens SQS para BO
- Reutilização: a mesma listener poderia ser usada por múltiplos serviços

**ConsumidorDlqAdapter.java**: Por que não apenas um listener genérico?

- Expressividade: "ConsumidorDlqAdapter" deixa claro que consome especificamente de DLQ
- Preparação para teste: posso mockar apenas o consumidor sem mockar todo o SQS
- Orquestração: este adapter sabe chamar ProcessarTriagem, mas não sabe lógica de negócio
- Tratamento de erro específico: sabe como lidar com falhas do SQS

**MensagemParaDominioConverter.java**: Por que um conversor separado?

- Separação de conceitos: converter dados é diferente de processar negócio
- Reutilização: múltiplos adapters (SQS, HTTP, gRPC) poderiam usar o mesmo conversor
- Testabilidade: testo conversão independentemente de adapters
- Manutenibilidade: se o DTO mudar, sei exatamente onde mexer

##### dto/

Por que DTOs aqui e não "Models"?

- DTOs (Data Transfer Objects) são especificamente para transferência de dados entre camadas
- Um DTO MensagemRecebidaDTO reflete a estrutura exata que vem do SQS
- BOs (OcorrenciaErroBO) refletem conceitos de negócio, não estrutura de dados
- DTOs podem ser descartáveis/efêmeros; BOs são persistentes em lógica

MensagemRecebidaDTO tem setters porque:

- Jackson (desserializador) precisa deles para criar instâncias
- DTOs são apenas recipientes de dados, não têm lógica
- BOs têm lógica e podem ter setters private/restricted

#### infrastructure/config/

JacksonConfig.java está aqui porque:

- É configuração de infraestrutura, não de domínio
- Específica da tecnologia Java/Spring que escolhi
- Se trocar de framework, essa classe seria descartada
- Centraliza customizações de serialização em um lugar conhecidoOs benefícios dessa organização