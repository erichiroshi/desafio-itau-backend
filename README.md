# Desafio Itaú — API de Transações

API REST para registro de transações financeiras e cálculo de estatísticas em tempo real, desenvolvida como solução ao [Desafio de Programação Itaú Unibanco](https://youtu.be/uke3i4uOejs).

---

## Índice

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
  - [1. Clonando o repositório](#1-clonando-o-repositório)
  - [2. Executando localmente com Gradle](#2-executando-localmente-com-gradle)
  - [3. Executando com Docker](#3-executando-com-docker)
  - [4. Executando com Docker Compose](#4-executando-com-docker-compose)
- [Configurações](#configurações)
- [Endpoints da API](#endpoints-da-api)
  - [POST /transacao](#post-transacao)
  - [DELETE /transacao](#delete-transacao)
  - [GET /estatistica](#get-estatistica)
- [Documentação interativa](#documentação-interativa)
- [Testes](#testes)
- [Observabilidade](#observabilidade)

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 25 | Linguagem principal |
| Spring Boot | 4.0.6 | Framework web |
| Gradle | 9.5.1 | Build e gerenciamento de dependências |
| Lombok | — | Redução de boilerplate |
| SpringDoc OpenAPI | 3.0.3 | Documentação interativa (Swagger UI) |
| Docker | 24+ | Containerização |
| JUnit 5 + Mockito | — | Testes automatizados |

---

## Arquitetura

O projeto segue **Arquitetura Hexagonal (Ports & Adapters)**, organizando o código em três anéis concêntricos onde as dependências sempre apontam para dentro:

```
src/main/java/com/erichiroshi/desafio_itau/
│
├── domain/                         # Núcleo — zero dependências externas
│   ├── model/
│   │   ├── Transacao.java          # Entidade com validações de negócio
│   │   └── Estatistica.java        # Value object com cálculo de estatísticas
│   └── exception/
│       └── TransacaoException.java # Exceção de domínio
│
├── application/                    # Casos de uso e portas
│   ├── port/
│   │   ├── in/                     # Portas de entrada (contratos que o mundo chama)
│   │   │   ├── TransacaoSavePort.java
│   │   │   ├── TransacaoDeletePort.java
│   │   │   └── EstatisticaPort.java
│   │   └── out/                    # Portas de saída (contratos que a app precisa)
│   │       └── TransacaoRepositoryPort.java
│   ├── input/
│   │   └── TransacaoInput.java     # DTO de entrada para o domínio
│   ├── output/
│   │   └── EstatisticaOutput.java  # DTO de saída do domínio
│   ├── TransacaoSaveUseCase.java
│   ├── TransacaoDeleteAllUseCase.java
│   └── EstatisticaUseCase.java
│
├── infrastructure/                 # Adaptadores — implementam as portas
│   ├── http/                       # Adaptador de entrada (controllers)
│   │   ├── docs/                   # Interfaces de documentação Swagger
│   │   ├── request/
│   │   ├── response/
│   │   ├── TransacaoController.java
│   │   └── EstatisticaController.java
│   └── repository/                 # Adaptador de saída (armazenamento)
│       └── TransacaoRepositoryAdapter.java
│
└── shared/
    ├── config/
    │   └── OpenApiConfig.java
    └── http/exception/
        └── GlobalExceptionHandler.java
```

**Regra de dependência:** `infrastructure` → `application` → `domain`. O domínio não conhece Spring, HTTP nem repositório.

---

## Pré-requisitos

Para executar **localmente sem Docker**, você precisa de:

- **JDK 25** — recomendado via [SDKMAN](https://sdkman.io/):
  ```bash
  sdk install java 25-open
  ```

Para executar **com Docker**, você precisa apenas de:

- **Docker 24+** — [instalação oficial](https://docs.docker.com/get-docker/)
- **Docker Compose v2** — já incluso no Docker Desktop

> O Gradle Wrapper (`gradlew`) está incluído no repositório — não é necessário instalar o Gradle separadamente.

---

## Como executar

### 1. Clonando o repositório

```bash
git clone https://github.com/erichiroshi/desafio-itau.git
cd desafio-itau
```

### 2. Executando localmente com Gradle

**Build e execução:**

```bash
# Linux / macOS / # Windows (Git Bash)
./gradlew bootRun

```

A aplicação sobe em `http://localhost:8080`.

**Apenas compilar (sem executar):**

```bash
./gradlew build
```

**Gerar o JAR e executar manualmente:**

```bash
./gradlew bootJar
java -jar build/libs/desafio-itau-0.0.1-SNAPSHOT.jar
```

### 3. Executando com Docker

**Build da imagem:**

```bash
docker build -t erichiroshi/desafio-itau:1.0.0 .
```

**Executar o container:**

```bash
docker run -p 8080:8080 erichiroshi/desafio-itau:1.0.0
```

**Com janela de tempo customizada (ex: 120 segundos):**

```bash
docker run -p 8080:8080 \
  -e APP_ESTATISTICA_JANELA-SEGUNDOS=120 \
  erichiroshi/desafio-itau:1.0.0
```

### 4. Executando com Docker Compose

```bash
docker compose up
```

Para rodar em background:

```bash
docker compose up -d
```

Para parar:

```bash
docker compose down
```

---

## Configurações

A aplicação é configurável via `application.yaml`, variáveis de ambiente ou argumentos JVM.

| Propriedade | Variável de ambiente | Padrão | Descrição |
|---|---|---|---|
| `app.estatistica.janela-segundos` | `APP_ESTATISTICA_JANELA-SEGUNDOS` | `60` | Janela de tempo (em segundos) para cálculo das estatísticas |
| `server.port` | `SERVER_PORT` | `8080` | Porta HTTP da aplicação |
| `logging.level.com.erichiroshi` | `LOGGING_LEVEL_COM_ERICHIROSHI` | `DEBUG` | Nível de log da aplicação |

**Exemplos:**

```bash
# Via argumento JVM
java -jar app.jar --app.estatistica.janela-segundos=120

# Via variável de ambiente
APP_ESTATISTICA_JANELA-SEGUNDOS=120 java -jar app.jar

# Via application.yaml
app:
  estatistica:
    janela-segundos: 120
```

---

## Endpoints da API

A aplicação sobe na porta `8080` por padrão.

### POST /transacao

Registra uma nova transação financeira.

**Request body:**
```json
{
  "valor": 123.45,
  "dataHora": "2025-06-01T10:30:00.000-03:00"
}
```

| Campo | Tipo | Obrigatório | Restrições |
|---|---|---|---|
| `valor` | `number` | Sim | Maior ou igual a `0` |
| `dataHora` | `string` (ISO 8601) | Sim | Não pode ser no futuro |

**Respostas:**

| Status | Descrição |
|---|---|
| `201 Created` | Transação aceita e registrada |
| `422 Unprocessable Entity` | Transação inválida (futuro, valor negativo ou campo nulo) |
| `400 Bad Request` | JSON malformado ou tipo de campo inválido |

**Exemplo cURL:**
```bash
curl -X POST http://localhost:8080/transacao \
  -H "Content-Type: application/json" \
  -d '{"valor": 99.90, "dataHora": "2025-06-01T10:00:00.000-03:00"}'
```

---

### DELETE /transacao

Remove todas as transações armazenadas em memória.

**Respostas:**

| Status | Descrição |
|---|---|
| `200 OK` | Transações removidas com sucesso |

**Exemplo cURL:**
```bash
curl -X DELETE http://localhost:8080/transacao
```

---

### GET /estatistica

Retorna estatísticas das transações ocorridas na janela de tempo configurada (padrão: últimos 60 segundos).

**Response body:**
```json
{
  "count": 3,
  "sum": 160.0,
  "avg": 53.33,
  "min": 10.0,
  "max": 100.0
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `count` | `long` | Quantidade de transações no período |
| `sum` | `double` | Soma total dos valores |
| `avg` | `double` | Média dos valores |
| `min` | `double` | Menor valor |
| `max` | `double` | Maior valor |

> Quando não há transações no período, todos os campos retornam `0`.

**Exemplo cURL:**
```bash
curl http://localhost:8080/estatistica
```

---

## Documentação interativa

Com a aplicação rodando, acesse:

| URL | Descrição |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interface Swagger para testar os endpoints |
| `http://localhost:8080/v3/api-docs` | Especificação OpenAPI em JSON |

---

## Testes

**Executar todos os testes:**

```bash
./gradlew test
```

**Executar com relatório detalhado:**

```bash
./gradlew test --info
```

O relatório HTML fica em `build/reports/tests/test/index.html`.

**Estrutura dos testes:**

```
src/test/
├── domain/model/
│   └── TransacaoTest.java              # Unitário — regras de domínio
├── application/
│   ├── UseCasesTest.java               # Unitário — orquestração com Mockito
│   └── service/
│       └── EstatisticaServiceTest.java # Unitário — cálculo de estatísticas
├── infrastructure/
│   ├── http/
│   │   ├── TransacaoControllerTest.java    # Funcional — HTTP com MockMvc
│   │   └── EstatisticaControllerTest.java  # Funcional — HTTP com MockMvc
│   └── repository/
│       └── TransacaoRepositoryAdapterTest.java # Unitário — filtro temporal
```

---

## Observabilidade

Com o Spring Boot Actuator configurado, os seguintes endpoints estão disponíveis:

| Endpoint | Descrição |
|---|---|
| `GET /actuator/health` | Status de saúde da aplicação |
| `GET /actuator/metrics` | Lista de métricas disponíveis |
| `GET /actuator/metrics/http.server.requests?tag=uri:/estatistica` | Tempo de resposta do endpoint de estatísticas |

**Exemplo de resposta do health:**
```json
{
  "status": "UP"
}
```
