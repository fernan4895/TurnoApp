# Instalación local

## Requisitos

- Java 21, Maven 3.9+, Node.js 20.19+ y npm.
- MySQL 8.0 (XAMPP puede proporcionar MySQL/MariaDB; MariaDB 10.6+ debería funcionar con los tipos usados).

## Windows (PowerShell)

1. Inicia MySQL desde el panel de XAMPP.
2. En phpMyAdmin importa `database/turnopro_db.sql`, o ejecuta `mysql -u root -p < database/turnopro_db.sql` desde la raíz del repositorio.
3. Copia `backend/.env.example` a un archivo local de variables o configura las variables en el entorno de IntelliJ. Define `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (mínimo 32 bytes), `CORS_ORIGINS`, `ADMIN_INITIAL_USERNAME` y `ADMIN_INITIAL_PASSWORD`. No compartas ni subas esos valores.
4. Ejecuta el backend desde la raíz: `cd backend; $env:DB_URL='jdbc:mysql://localhost:3306/turnopro_db?useSSL=false&serverTimezone=America/Mexico_City'; $env:DB_USERNAME='root'; $env:DB_PASSWORD=''; $env:JWT_SECRET='cambia-esto-por-un-secreto-aleatorio-de-32-bytes'; $env:ADMIN_INITIAL_USERNAME='admin'; $env:ADMIN_INITIAL_PASSWORD='elige-una-clave-segura'; mvn spring-boot:run`.
5. El administrador de desarrollo se obtiene del entorno al arrancar; no hay contraseña fija en el código. Cambia esos valores antes de uso compartido.
6. En otra terminal: `cd frontend; Copy-Item .env.example .env; npm install; npm run dev`.
7. Abre <http://localhost:5173>. Panel: `/admin`. La pantalla pública es `/pantalla`.

## macOS / Linux

Inicia MySQL y ejecuta `mysql -u root -p < database/turnopro_db.sql`. Exporta las variables de entorno indicadas para la terminal de backend y ejecuta `cd backend && mvn spring-boot:run`. En otra terminal ejecuta `cd frontend && cp .env.example .env && npm install && npm run dev`. Abre <http://localhost:5173>.

Frontend usa el puerto 5173 y API el 8080 (`PORT` lo modifica). Cambia `VITE_API_URL` y `VITE_WS_URL` si mueves el backend. Para impresora térmica, selecciona 58 mm u 80 mm en las preferencias del navegador/impresora e imprime desde el comprobante; el navegador controla el diálogo y la confirmación.

## Prueba manual sugerida

Genera un turno desde `/nuevo`, consulta número y código seguros desde `/consulta`, inicia sesión en `/admin`, llama un turno y observa `/pantalla`. Finaliza el servicio vía API (`POST /api/turnos/{id}/iniciar` y `/finalizar`). Las contraseñas iniciales deben establecerse por entorno; el API valida el esquema MySQL al iniciar.
