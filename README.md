# TurnoPro

Sistema web de turnos digitales. El frontend y la API Node.js se publican juntos en Vercel; MySQL se aloja en Aiven. El backend Spring Boot se conserva como opción para ejecutar el sistema localmente.

## Estructura

- `frontend/`: aplicación web React y TypeScript.
- `api/`: API Node.js para las funciones de Vercel.
- `backend/`: backend alternativo Spring Boot para uso local.
- `database/turnopro_db.sql`: esquema y datos iniciales para MySQL.
- `docs/`: instalación, operación y publicación.

Para instalar localmente consulta [docs/INSTALACION.md](docs/INSTALACION.md). Para publicar con el repositorio conectado a Vercel y MySQL en Aiven, sigue [docs/DEPLOY_VERCEL.md](docs/DEPLOY_VERCEL.md).
