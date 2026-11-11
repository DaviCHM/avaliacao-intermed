# Sistema de Avaliacoes (simulado PF Projeto de Software)

Mesmo padrao da avaliacao intermediaria: Spring Boot 4.1.1, Java 17, camadas `controller / service / repository / model / dto / exception`, Dockerfile multi-stage, deploy via Docker Hub + SSH na EC2.

```
backend/    Spring Boot (JPA, Postgres/H2, Redis cache, Auth0, Actuator)
frontend/   React + Vite + @auth0/auth0-react
.github/workflows/deploy.yml   build, push no Docker Hub, docker run na EC2
```

## 0. Enderecos e valores

| | |
|---|---|
| Front (Vercel) | https://avaliacao-front-neon.vercel.app |
| API (EC2) | http://3.226.252.254:8080 |
| Health | http://3.226.252.254:8080/actuator/health |
| Auth0 Domain | `dev-33x3qs157uhfckjg.us.auth0.com` |
| Auth0 Audience | `https://avaliacoes-api` |
| Auth0 Client ID (SPA) | `9jvYx7S3JtD3kfqjC6jlFRDvXIOzPvBR` |
| Claims no token | `https://avaliacoes-api/roles`, `https://avaliacoes-api/email` |
| Usuarios de teste | `admin@teste.com` (ADMIN), `user@teste.com` (USER) |
| Containers na EC2 | `avaliacoes` (app), `transacoes-db` (Postgres, banco `demo`), `redis` |

Checagem antes de comecar: health `UP` e login funcionando na URL da Vercel.

---

## 1. Rodar local (Windows / PowerShell)

**Testes do back:**
```
cd backend
.\mvnw.cmd test
```
- `AvaliacaoServiceTest` (Mockito): listar, criar com email do avaliador e data.
- `AvaliacaoControllerIntegrationTest`: 401 sem token, ADMIN cria (201), USER lista (200), USER nao cria (403), nota 11 (400), roles lidas da claim do Auth0.

**Back local** (H2 em memoria, sem Postgres e sem Redis), opcional:
```
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

**Front local:**
```
cd frontend
npm install
npm run dev     # http://localhost:5173
```
O `vite.config.js` manda `/api/...` para a **EC2** (3.226.252.254:8080). Para usar o back local, troque o `target` para `http://localhost:8080`.

---

## 2. Deploy (automatico)

Todo push na `main` que mexe em `backend/` ou no workflow dispara o GitHub Actions:
1. Compila com `./mvnw` (sem rodar testes) e faz push da imagem `DOCKERHUB_USERNAME/avaliacoes:latest`.
2. Na EC2: liga o Postgres (`DB_HOST`) e o Redis, para o container antigo e sobe `avaliacoes` com `TZ=America/Sao_Paulo`.

O deploy demora uns 3 a 5 minutos. O verde no Actions nao garante que o app subiu: confira o health 1 a 2 minutos depois.

O front na Vercel republica sozinho a cada push.

Secrets do repo (ja cadastrados, vindos da intermediaria): `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `EC2_HOST`, `EC2_SSH_KEY`, `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`.

**Acessar a EC2:**
```
ssh -i "$HOME\Downloads\projsoft26b (4).pem" ubuntu@3.226.252.254
docker ps -a
docker logs avaliacoes --tail 50
```

---

## 3. Na hora da prova: o que muda

| O que | Onde |
|---|---|
| Campos da entidade | `backend/.../model/Avaliacao.java` |
| Campos do POST e validacoes | `backend/.../dto/AvaliacaoRequestDTO.java` |
| Campos da resposta | `backend/.../dto/AvaliacaoResponseDTO.java` + `toResponseDTO` no service |
| Regras de negocio | `backend/.../service/AvaliacaoService.java` |
| Rotas | `backend/.../controller/AvaliacaoController.java` |
| Quem acessa cada rota | `backend/.../config/SecurityConfig.java`, bloco `ROTAS` |
| Testes | `backend/src/test/...` (ajuste se mudar campos ou rotas) |
| Tela (formulario e tabela) | `frontend/src/App.jsx` |

Dicas:
- Renomear classe/variavel no VS Code: clique no nome e aperte **F2** (troca todas as referencias).
- Rota com id: `@GetMapping("/{id}")` + `@PathVariable Long id`; no SecurityConfig use `"/avaliacoes/*"`.
- 404: `throw new RecursoNaoEncontradoException("Avaliacao nao encontrada com id: " + id)` (ja tratado no `GlobalExceptionHandler`).
- Metodo que altera dados (POST/PUT/DELETE): coloque `@CacheEvict(value = "avaliacoes", allEntries = true)`.
- Email de quem esta logado no back: `jwt.getClaimAsString("https://avaliacoes-api/email")` (ver controller).
- Role de quem esta logado no front: `user['https://avaliacoes-api/roles']` (ver App.jsx).
- Se pedirem padrao de projeto (como o Observer da intermediaria), o lugar e o service.
- Nao mexer: pom.xml, Dockerfile, workflow, vercel.json, vite.config.js, main.jsx, api.js, .env.

Fluxo: alterar codigo → `.\mvnw.cmd test` em `backend` → commit e push pelo VS Code → Actions verde → health `UP` → testar na Vercel.

---

## 4. Problemas comuns (os que ja aconteceram estao marcados)

| Sintoma | Causa e solucao |
|---|---|
| Health nao abre / conexao recusada | (aconteceu) Postgres parado. Na EC2: `docker start transacoes-db redis avaliacoes`, esperar 30s |
| Clicar em Login so recarrega a pagina, URL com `?error=` | (aconteceu) Auth0 → APIs → avaliacoes-api → Application Access → avaliacoes-front → Grant Access |
| Primeira requisicao demora muito depois do deploy | (aconteceu) maquina lenta ao subir a JVM; esperar e nao clicar varias vezes |
| Actions falha com `exit code 126` | (aconteceu) `git update-index --chmod=+x backend/mvnw`, commit e push |
| 401 em tudo | audience errado, ou token antigo (logout e login) |
| 403 sendo ADMIN | role nao atribuida ou Action fora do Post Login; logout/login depois de mudar role |
| "Failed to fetch" na Vercel | container caiu (`docker logs avaliacoes`) ou IP errado no `vercel.json` |
| Callback URL mismatch | URL nao cadastrada no Auth0 (Applications → avaliacoes-front → URLs) |
| Teste do service quebra depois de mudar o repository | o mock precisa bater com o metodo chamado, ex. `findAll(any(Sort.class))` |