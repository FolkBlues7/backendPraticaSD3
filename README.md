# 🌐 Projeto Prático IoT - Arquitetura de Borda (Edge Computing)

Este projeto demonstra a implementação de uma arquitetura de Internet das Coisas (IoT) com foco em Computação de Borda (*Edge Computing*). O sistema é modular, compreendendo um **Gateway de Borda** (Simulador de Sensores) e um **Servidor Central** (Backend em Spring Boot), que se comunicam de forma distribuída.

## 🏗️ Arquitetura do Sistema

A arquitetura do projeto foi concebida para simular um ambiente de laboratórios, utilizando múltiplos protocolos de comunicação para demonstrar a versatilidade do sistema:

*   **Laboratório 1 (CoAP):** Os sensores deste laboratório enviam dados diretamente para o Gateway de Borda utilizando o protocolo CoAP. O Gateway, por sua vez, é responsável por encaminhar esses dados para a camada de mensageria na nuvem, implementada com **RabbitMQ**.
*   **Laboratório 2 e 3 (MQTT):** Nestes laboratórios, os sensores publicam seus dados em um broker MQTT local (*Edge Mosquitto*). O Servidor Central consome os dados diretamente deste broker, demonstrando uma abordagem diferente de integração.

Esta configuração permite testar a robustez do sistema na coleta e processamento de dados provenientes de diferentes fontes e protocolos, antes de sua persistência e análise no backend.

## 💻 Tecnologias Utilizadas

O projeto emprega um conjunto de tecnologias modernas para garantir escalabilidade, eficiência e facilidade de implantação:

*   **Java & Spring Boot:** Utilizados para o desenvolvimento do Backend e da API REST, proporcionando um ambiente robusto para processamento de dados e exposição de funcionalidades.
*   **Java puro + Maven:** Empregados no desenvolvimento do Simulador de Sensores e do Edge Gateway, permitindo a criação de componentes leves e eficientes para a camada de borda.
*   **Docker & Docker Compose:** Essenciais para a conteinerização de toda a infraestrutura, facilitando a implantação e garantindo a portabilidade do ambiente de desenvolvimento para produção.
*   **PostgreSQL (v16):** Banco de dados relacional escolhido para a persistência dos dados coletados e processados pelo backend.
*   **RabbitMQ:** Atua como o sistema de mensageria principal (AMQP), garantindo a comunicação assíncrona e confiável entre o Gateway de Borda e o Backend.
*   **Eclipse Mosquitto:** Broker MQTT leve e eficiente, utilizado na camada de borda para a coleta de dados dos sensores dos Laboratórios 2 e 3.
*   **Eclipse Californium:** Framework para implementação de servidores e clientes CoAP, fundamental para a comunicação com os sensores do Laboratório 1.

## 🚀 Como Executar o Projeto (Em Duas Máquinas)

Para replicar o cenário de *Edge Computing*, o projeto foi projetado para ser executado em duas máquinas distintas (ex: Computador A e Computador B), conectadas na mesma rede local (Wi-Fi ou Ethernet).

### Passo 1: Descobrir os Endereços IP das Máquinas

Antes de iniciar a configuração, é necessário identificar os endereços IP de cada máquina na rede local. Abra o terminal em ambas as máquinas e utilize os comandos apropriados:

*   **Linux:** `ip a`
*   **Windows:** `ipconfig`

Anote os IPs para as seguintes configurações:

*   **IP do Computador A (Simulador):** Exemplo: `192.168.x.x`
*   **IP do Computador B (Backend):** Exemplo: `192.168.y.y`

### Passo 2: Configurar e Iniciar o Backend (Computador B)

No **Computador B**, que hospedará o Backend, siga os passos abaixo:

1.  **Alterar o IP do Broker MQTT:**
    Edite o arquivo `src/main/resources/application.properties` no projeto Spring Boot do Backend. Atualize a propriedade `mqtt.broker.url` para apontar para o IP do **Computador A**, onde o broker Mosquitto de borda estará rodando:
    ```properties
    mqtt.broker.url=tcp://<IP_DO_COMPUTADOR_A>:1883
    ```

2.  **Subir a Infraestrutura Central (Docker):**
    Navegue até a pasta raiz do projeto Backend no terminal e execute o comando para iniciar os serviços Docker necessários (PostgreSQL, RabbitMQ e Mosquitto central):
    ```bash
    docker-compose up -d
    ```

3.  **Iniciar a Aplicação Spring Boot:**
    Após a infraestrutura Docker estar em funcionamento, inicie a aplicação Spring Boot. Isso pode ser feito executando a classe principal `Pratica3SdApplication.java` através de uma IDE ou utilizando o Maven:
    ```bash
    mvn spring-boot:run
    ```

### Passo 3: Configurar e Iniciar o Simulador (Computador A)

No **Computador A**, que hospedará o Simulador de Sensores e o Edge Gateway, siga os passos abaixo:

1.  **Alterar o IP do Backend para o RabbitMQ:**
    Abra a classe `App.java` no projeto do Simulador. Modifique a linha de inicialização do `RabbitMqPublisher` para incluir o IP do **Computador B**, onde o RabbitMQ está rodando:
    ```java
    RabbitMqPublisher rabbitPublisher = new RabbitMqPublisher("<IP_DO_COMPUTADOR_B>", "admin", "12345");
    ```

2.  **Subir a Infraestrutura de Borda (Docker):**
    Navegue até a pasta raiz do projeto Simulador no terminal e execute o comando para iniciar o Mosquitto de borda:
    ```bash
    docker-compose up -d
    ```

3.  **Iniciar o Simulador (Edge Gateway):**
    Execute a classe principal `App.java` do Simulador através de sua IDE ou via terminal.

## 🧪 Testando a Aplicação

Com ambos os projetos em execução, você observará os logs em tempo real nos terminais:

*   **Simulador:** Exibirá mensagens indicando a geração e o envio de telemetrias e anomalias.
*   **Backend:** Mostrará o consumo de dados das filas RabbitMQ e do broker MQTT, bem como o registro desses dados no PostgreSQL.

### Endpoints da API REST

Para consultar os dados recebidos e processados pelo Backend, utilize um navegador web ou uma ferramenta como o Postman no **Computador B**. Abaixo estão alguns exemplos de endpoints disponíveis:

*   **Listar todos os dados de telemetria:**
    `GET http://localhost:8080/api/telemetria`

*   **Listar equipamentos de um laboratório específico (ex: Laboratório 1):**
    `GET http://localhost:8080/api/telemetria/laboratorios/1/dispositivos`

*   **Ver o último alerta de segurança:**
    `GET http://localhost:8080/api/telemetria/alertas/ultimo`

Este README fornece um guia completo para a configuração e execução do projeto, permitindo uma compreensão clara de sua arquitetura e funcionalidades. Para mais detalhes sobre os requisitos do backend e a estrutura completa dos endpoints, consulte o relatório de análise de backend fornecido anteriormente.
