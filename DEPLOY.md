# NeoBank – Deploy Guide

## Run everything locally / on any Linux VM (Docker)
```bash
cp .env.example .env        # then edit DB_PASSWORD and JWT_SECRET
docker compose up -d --build
```
Open http://localhost (or http://<server-ip>). nginx serves the Angular app and proxies `/api/*` to Spring Boot, so no CORS setup is needed.

Check backend: `docker compose logs -f backend` — wait for "Started BankingApplication".
Swagger: http://localhost/swagger-ui.html is NOT proxied; use `docker compose exec backend wget -qO- localhost:8080/v3/api-docs | head` to test.

## Local development (no Docker)
- Backend: `cd banking && mvn spring-boot:run` (needs MySQL on localhost:3306, db `banking`, root/root — or set DB_* env vars)
- Frontend: `cd frontend/bank && npm ci && npm start` (`proxy.conf.json` forwards /api to :8080)

## Free-ish hosting options
- One VM (Oracle Cloud free tier / any VPS / EC2 t3.small): install Docker, clone repo, run the commands above.
- Split hosting (Vercel/Netlify frontend + Render/Railway backend + managed MySQL): frontend then needs an absolute API URL or a rewrite rule to the backend, and backend needs `CORS_ORIGINS=https://your-frontend-domain`.

## Env vars (backend)
DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET (>=32 chars), CORS_ORIGINS, DDL_AUTO (default update), SHOW_SQL (default false), PORT.
