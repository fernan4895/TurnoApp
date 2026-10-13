# TurnoPro

Sistema local de turnos digitales con React/TypeScript, Spring Boot y MySQL.

## Componentes

- `frontend/`: aplicación Vite, interfaz pública y panel operativo.
- `backend/`: API Spring Boot con JPA, validación, seguridad JWT y WebSocket STOMP.
- `database/turnopro_db.sql`: esquema y datos de muestra.
- `docs/`: instalación, uso, API y arquitectura.

Consulta [INSTALACION.md](docs/INSTALACION.md) antes de iniciar. El backend requiere Java 21 y MySQL 8; configura secretos mediante variables de entorno. Las credenciales se crean con `ADMIN_INITIAL_USERNAME` y `ADMIN_INITIAL_PASSWORD` al iniciar por primera vez.

Para publicar el frontend en Vercel y conectar una API pública, consulta [DEPLOY_VERCEL.md](docs/DEPLOY_VERCEL.md). Vercel aloja el frontend; el backend Java y MySQL necesitan un servicio compatible aparte.
