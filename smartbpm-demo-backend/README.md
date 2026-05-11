# SmartBPM Demo — Backend

The backend is the orchestration core of SmartBPM. It receives a natural-language process description, enriches it with a lightweight RAG step, builds a structured intermediate JSON, compiles it deterministically to BPMN 2.0 XML, runs multi-layer validation, applies explainable optimization heuristics, stores all artifacts, and exposes everything through a clean REST API.

---

## Getting Started

These instructions will get the backend running on your local machine for development and testing. See the [Deployment](#deployment) section for notes on deploying to a live AWS environment.

### Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java (JDK) | 21 |
| Maven | 3.9+ |
| Docker *(optional, for containerized run)* | 24+ |

> **OpenAI API key** — optional. The backend runs in deterministic fake-AI mode by default. Set `OPENAI_API_KEY` to enable real GPT-4o-mini calls.

### Installing

**1. Clone the repository**

```bash
git clone https://github.com/daviidc29/smartbpm-demo.git
cd smartbpm-demo/smartbpm-demo-backend
```

**2. Build the project**

```bash
mvn clean package -DskipTests
```

**3. Run locally (fake-AI mode, embedded H2 database)**

```bash
mvn spring-boot:run
```

The REST API starts at `http://localhost:8081`.

**4. Verify the server is up**

```bash
curl http://localhost:8081/api/processes/ping
```

Expected response: `200 OK`

**5. Generate your first process**

```bash
curl -s -X POST http://localhost:8081/api/processes/generate \
  -H "Content-Type: application/json" \
  -d '{"narrative": "An employee submits a reimbursement. The system validates the invoice. If the amount is below 500 it is auto-approved, otherwise the manager approves it. Finance pays and the employee is notified."}'
```

The response includes the generated BPMN XML, intermediate JSON, validation report, and a full audit trail — all in one call.

---

## Configuration

All settings live in `src/main/resources/application.yml`. The most relevant options:

```yaml
smartbpm:
  cors-allowed-origins: "http://localhost:5173"   # Frontend origin
  ai:
    provider: openai        # "openai" | "fake"
    model: gpt-4o-mini
    openai-base-url: https://models.inference.ai.azure.com
  storage:
    mode: filesystem        # "filesystem" | "s3"
    filesystem-root: ./data/artifacts
    s3-bucket: smartbpm-demo-artifacts
  execution:
    mode: local             # "local" | "aws"
```

**Environment variables** recognized at runtime:

| Variable | Description |
|----------|-------------|
| `OPENAI_API_KEY` | API key for OpenAI / Azure OpenAI |
| `AURORA_HOST` | PostgreSQL host (Aurora-compatible) |
| `AURORA_PORT` | PostgreSQL port (default `5432`) |
| `AURORA_DB` | Database name (default `smartbpm`) |
| `AURORA_USERNAME` | Database user |
| `AURORA_PASSWORD` | Database password |

---

## Running the Tests

```bash
mvn test
```

### End-to-end pipeline tests

These tests exercise the full generation pipeline from narrative input to BPMN XML output using the fake-AI provider and an in-memory H2 database.

```bash
mvn test -Dtest="*IntegrationTest"
```

**What they verify:**
- A narrative produces a non-empty BPMN XML string.
- The XML is well-formed and contains at least one `<startEvent>`, one `<endEvent>`, and one sequence flow.
- The process record is persisted and retrievable by ID.

### Unit tests — Compiler

```bash
mvn test -Dtest="BpmnCompilerTest"
```

Verifies that every valid intermediate JSON shape maps to the correct BPMN element type and that the layout calculator assigns non-overlapping coordinates.

### Unit tests — Validator

```bash
mvn test -Dtest="ValidationServiceTest"
```

Asserts that structural rules catch: missing start/end events, orphan nodes, invalid gateway references, and malformed XML.

### Unit tests — Optimizer

```bash
mvn test -Dtest="OptimizerServiceTest"
```

Confirms that each heuristic (redundant-review removal, XOR gateway insertion, notification repositioning) produces the expected change-log entry and a structurally valid output.

---

## API Reference

### Generate process

`POST /api/processes/generate`

```json
{
  "narrative": "The employee registers a reimbursement..."
}
```

**Response** — full `ProcessViewResponse` including BPMN XML, intermediate JSON, validation report, RAG context, and audit timeline.

### Validate current version

`POST /api/processes/{processId}/validate`

Runs the five validation layers (structural, consistency, semantic, XML schema, visual) on the current BPMN version and updates the validation report.

### Optimize current version

`POST /api/processes/{processId}/optimize`

Applies explainable heuristics and stores the optimized BPMN as a separate artifact.

### Get full trace

`GET /api/processes/{processId}`

Returns the complete process view including all intermediate artifacts, validation results, optimization change log, and audit events.

### Download artifacts

| Endpoint | Content |
|----------|---------|
| `GET /api/processes/{id}/artifacts/bpmn` | Original BPMN XML |
| `GET /api/processes/{id}/artifacts/json` | Intermediate JSON |
| `GET /api/processes/{id}/artifacts/optimized-bpmn` | Optimized BPMN XML |
| `GET /api/processes/{id}/artifacts/optimized-json` | Optimized JSON |
| `GET /api/processes/{id}/artifacts/pdf` | Exported PDF |

### Upload PDF from frontend

`POST /api/processes/{processId}/artifacts/pdf`

```json
{
  "fileName": "process-diagram.pdf",
  "base64Pdf": "<base64-encoded content>"
}
```

---

## Module Structure

```
src/main/java/com/smartbpm/demo/
├── api/                  # REST controllers and DTOs
├── application/          # Orchestration & service-client interfaces
├── config/               # Spring configuration (CORS, beans)
├── domain/               # Core domain models
├── lambda/               # AWS Lambda invocation adapters
├── persistence/          # JPA entities and repositories
└── service/
    ├── ai/               # Prompt building, fake-AI, OpenAI adapter
    ├── audit/            # Trace event recorder
    ├── compiler/         # Deterministic BPMN XML compiler + layout
    ├── optimizer/        # Optimization heuristics
    ├── rag/              # Keyword-based RAG over local documents
    ├── storage/          # Filesystem and S3 storage adapters
    └── validation/       # Multi-layer BPMN validator
```

---

## Deployment

### Docker (recommended)

```bash
docker build -t smartbpm-backend .
docker run -p 8081:8081 \
  -e OPENAI_API_KEY=<your-key> \
  -e AURORA_HOST=<rds-host> \
  -e AURORA_PORT=5432 \
  -e AURORA_DB=smartbpm \
  -e AURORA_USERNAME=<user> \
  -e AURORA_PASSWORD=<password> \
  smartbpm-backend
```

The container activates `--spring.profiles.active=aws`, which:
- Switches the datasource from H2 to Aurora PostgreSQL.
- Switches artifact storage from filesystem to Amazon S3.
- Enables Lambda delegation for AI, transformer, and validator steps.

### AWS App Runner

Push the Docker image to Amazon ECR and create an App Runner service pointing to it. Configure the environment variables listed above as App Runner environment variables. See [`../docs/SMARTBPM_AWS_INTEGRATION.md`](../docs/SMARTBPM_AWS_INTEGRATION.md) for the full deployment guide.

---

## Built With

| Technology | Role |
|-----------|------|
| [Spring Boot 3.3](https://spring.io/projects/spring-boot) | Application framework |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | ORM and repository layer |
| [H2](https://h2database.com) | Embedded database for local execution |
| [PostgreSQL](https://www.postgresql.org) | Production database (Aurora-compatible) |
| [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/) | S3 storage & Lambda invocation |
| [Apache Commons Text](https://commons.apache.org/proper/commons-text/) | Text utilities |
| [Maven](https://maven.apache.org) | Build and dependency management |
| [Docker](https://www.docker.com) | Containerization |

---

## Contributing

Please read the project-level [`CONTRIBUTING`](../README.md#contributing) guidelines before opening a pull request.

---

## Versioning

We use [SemVer](https://semver.org/) for versioning. For available versions, see the [tags on this repository](https://github.com/<your-org>/smartbpm-demo/tags).

---

## Authors

- **Camilo Andres Fernandez**
- **David Santiago Castro**
- **Juan Jose Mejia**

---

## License

This project is licensed under the MIT License — see the [LICENSE](../LICENSE) file for details.

---

## Acknowledgments

- [Spring.io](https://spring.io/) for the battle-tested framework that makes production-ready Java services approachable.
- [OMG BPMN 2.0 specification](https://www.omg.org/spec/BPMN/2.0/) for the standardized process modeling notation.
- [OpenAI](https://openai.com/) for the language model API driving process generation.
- [bpmn.io](https://bpmn.io/) for the ecosystem of open-source BPMN tools.
