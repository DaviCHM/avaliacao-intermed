# Sistema de Avaliacoes (simulado PF Projeto de Software)

Mesmo padrao da avaliacao intermediaria: Spring Boot 4.1.1, Java 17, camadas `controller / service / repository / model / dto / exception`, Dockerfile multi-stage, deploy via Docker Hub + SSH na EC2.

```
backend/    Spring Boot (JPA, Postgres/H2, Redis cache, Auth0, Actuator)
frontend/   React + Vite + @auth0/auth0-react
.github/workflows/deploy.yml   build, push no Docker Hub, docker run na EC2
```

Auth0 (ja configurado no codigo):

| | |
|---|---|
| Domain | `dev-33x3qs157uhfckjg.us.auth0.com` |
| Audience | `https://avaliacoes-api` |
| Client ID (SPA) | `9jvYx7S3JtD3kfqjC6jlFRDvXIOzPvBR` |
| Claims no token | `https://avaliacoes-api/roles`, `https://avaliacoes-api/email` |

---

## 1. Rodar local

**Back** (H2 em memoria, sem Postgres e sem Redis):
- IntelliJ: abrir a pasta `backend`, Run Configuration → `Active profiles: dev`, rodar `DemoApplication`.
- Terminal: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

**Testes:** `cd backend && ./mvnw test`
- `AvaliacaoServiceTest` (Mockito): listar, criar com email do avaliador e data.
- `AvaliacaoControllerIntegrationTest`: 401 sem token, ADMIN cria (201), USER lista (200), USER nao cria (403), nota 11 (400), roles lidas da claim do Auth0.

**Front:**
```
cd frontend
npm install
npm run dev     # http://localhost:5173  (/api vai para localhost:8080)
```

---

## 2. GitHub: secrets (mesmos nomes da intermediaria)

Settings → Secrets and variables → Actions:

| Secret | Valor |
|---|---|
| `DOCKERHUB_USERNAME` | seu usuario do Docker Hub |
| `DOCKERHUB_TOKEN` | token do Docker Hub |
| `EC2_HOST` | IP publico da EC2 |
| `EC2_SSH_KEY` | conteudo inteiro do `.pem` |
| `DB_HOST` | **reaproveitando a EC2 da intermediaria:** o mesmo valor de antes. **EC2 nova:** `postgres` (o workflow cria o container) |
| `DB_USER` | ex. `postgres` |
| `DB_PASSWORD` | senha do Postgres |
| `DB_NAME` | `avaliacoes` |

O workflow:
1. Compila com `./mvnw` e faz push da imagem `DOCKERHUB_USERNAME/avaliacoes:latest`.
2. Na EC2: cria a rede `transacoes-net` se faltar, sobe Redis (e Postgres se `DB_HOST=postgres`), cria o banco `DB_NAME` se faltar, para o container da intermediaria (libera a porta 8080) e roda `avaliacoes`.

Testar: `http://IP:8080/actuator/health` → `{"status":"UP"}`.

**EC2 nova** (se nao for reaproveitar): Ubuntu, security group com portas 22 e 8080, e instalar Docker:
```
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
```

## 3. Vercel

1. Em `frontend/vercel.json`, trocar `IP_DA_EC2` pelo IP real. Commit e push.
2. vercel.com → Add New Project → importar o repo → **Root Directory: `frontend`** → Deploy.
3. Adicionar a URL da Vercel no Auth0: Applications → avaliacoes-front → **Allowed Callback URLs, Allowed Logout URLs, Allowed Web Origins**.

O `vercel.json` existe porque o front na Vercel e HTTPS e a EC2 e HTTP; o navegador bloqueia essa chamada (mixed content). Com o rewrite, o navegador chama `/api/...` na Vercel e ela repassa para a EC2.

---

## 4. Na hora da prova: o que muda

| O que | Onde |
|---|---|
| Campos da entidade | `model/Avaliacao.java` |
| Campos do POST e validacoes | `dto/AvaliacaoRequestDTO.java` |
| Campos da resposta | `dto/AvaliacaoResponseDTO.java` + `toResponseDTO` no service |
| Regras de negocio | `service/AvaliacaoService.java` |
| Rotas | `controller/AvaliacaoController.java` |
| Quem acessa cada rota | `config/SecurityConfig.java`, bloco `ROTAS` |
| Tela | `frontend/src/App.jsx` |

Dicas:
- Renomear entidade: Shift+F6 no IntelliJ troca todas as referencias.
- Rota com id: `@GetMapping("/{id}")` + `@PathVariable Long id`; no SecurityConfig use `"/avaliacoes/*"`.
- 404: `throw new RecursoNaoEncontradoException("Avaliacao nao encontrada com id: " + id)` (ja tratado no `GlobalExceptionHandler`).
- Metodo que altera dados: coloque `@CacheEvict(value = "avaliacoes", allEntries = true)`.
- Se pedirem padrao de projeto (como o Observer da intermediaria), o lugar e o service, igual antes.
- Nao mexer: pom.xml, Dockerfile, workflow, vercel.json, main.jsx, api.js.

## 5. Problemas comuns

| Sintoma | Causa provavel |
|---|---|
| 401 em tudo | audience errado, ou token antigo (logout e login) |
| 403 sendo ADMIN | Action fora do fluxo Post Login, ou role nao atribuida; logout/login depois de mudar role |
| "Failed to fetch" na Vercel | IP errado no `vercel.json`, porta 8080 fechada, ou container caiu (`docker logs avaliacoes`) |
| Callback URL mismatch | URL da Vercel nao cadastrada no Auth0 |
| App nao conecta no banco | `DB_HOST` diferente do nome do container do Postgres (`docker ps` na EC2) |
| Porta 8080 em uso | outro container antigo: `docker ps` e `docker stop <nome>` |
