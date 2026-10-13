# Publicar TurnoPro con Vercel

## Lo que puede alojar Vercel

El frontend Vite es un sitio estático. El backend de este repositorio es Spring Boot/Java y la base de datos es MySQL. Vercel publica el frontend, pero sus runtimes oficiales para funciones no incluyen Java; el backend y MySQL requieren un servidor externo compatible. La página en Vercel sin esas dos piezas puede abrirse desde cualquier dispositivo, pero iniciar sesión, generar/consultar turnos y recibir llamados no funcionará.

## Preparar el despliegue del frontend

1. Sube el repositorio a un proveedor Git compatible con Vercel o impórtalo desde el panel de Vercel.
2. En el proyecto Vercel define **Root Directory** como `frontend`.
3. Configura el framework como **Vite**, el comando de build `npm run build` y el directorio de salida `dist`.
4. Agrega en **Settings → Environment Variables** `VITE_API_URL` con la URL pública del backend seguida por `/api`, y `VITE_WS_URL` con la URL pública del backend seguida por `/ws`. Usa `https://` para ambos.
5. Publica y comprueba que `/`, `/nuevo`, `/consulta`, `/admin` y `/pantalla` carguen. `frontend/vercel.json` reescribe las rutas del cliente a la SPA.

## Despliegue completo de bajo costo para demo

El repositorio incluye un `backend/Dockerfile` y `render.yaml` para el API. Render admite contenedores Java mediante Docker y entrega un subdominio público; el esquema propone su plan Free. Para MySQL, Aiven ofrece un plan Free con un nodo, 1 GB de RAM y 1 GB de almacenamiento, con apagado posible si queda inactivo. Estos planes sirven para una demo: los servicios web gratuitos de Render se duermen tras 15 minutos sin tráfico y tardan alrededor de un minuto en despertar; no equivalen a una instancia disponible continuamente. [Render: límites Free](https://render.com/docs/free) · [Aiven: MySQL Free](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier)

1. Sube el proyecto completo a GitHub/GitLab y conecta ese repositorio con Render. Crea un Blueprint usando el archivo `render.yaml`; selecciona un plan Free para `turnopro-api`.
2. Crea un servicio MySQL Free en Aiven. Anota host, puerto, usuario y contraseña privados. En Aiven habilita acceso de red para Render y usa TLS. Limita el acceso según las opciones disponibles en tu cuenta.
3. Crea la base `turnopro_db` y ejecuta `database/turnopro_db.sql` contra el servicio MySQL. El backend usa `ddl-auto=validate`, por lo que requiere las tablas antes de arrancar.
4. En Render configura `DB_URL` con la forma `jdbc:mysql://HOST:PUERTO/turnopro_db?sslMode=REQUIRED&serverTimezone=UTC`; completa `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_INITIAL_USERNAME` y `ADMIN_INITIAL_PASSWORD` en la configuración privada del servicio. Render genera `JWT_SECRET` en el Blueprint. No compartas secretos por chat ni los subas a Git.
5. Cuando el servicio pase el health check, copia la URL `https://...onrender.com`. En Vercel establece `VITE_API_URL=https://...onrender.com/api` y `VITE_WS_URL=https://...onrender.com/ws` y vuelve a desplegar el frontend.
6. Copia el origen público de Vercel (solo `https://<proyecto>.vercel.app`, sin ruta) en `CORS_ORIGINS` del backend y vuelve a desplegar el API.
7. Comprueba login, generación y consulta de turno, llamada desde `/admin` y pantalla `/pantalla` en otro dispositivo.

La instancia gratuita de Render puede tardar hasta cerca de un minuto al recibir la primera visita después de dormir. Aiven puede apagar un MySQL Free que permanezca inactivo y no ofrece SLA de disponibilidad. Para un negocio que requiera atención continua, respaldo operativo y datos de clientes, habrá que elegir capacidad de pago con disponibilidad y recuperación adecuadas.

## Requisitos para que la aplicación sea funcional

- Desplegar `backend/` en un servicio que mantenga vivo un proceso Spring Boot con Java 21 y permita WebSocket/STOMP (incluyendo SockJS).
- Alojar MySQL en una instancia pública protegida, importar `database/turnopro_db.sql` y configurar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_INITIAL_USERNAME`, `ADMIN_INITIAL_PASSWORD` y `CORS_ORIGINS` en el backend.
- Incluir el dominio de producción de Vercel en `CORS_ORIGINS`. Si se usan dominios Preview, agregarlos explícitamente o evitar habilitar orígenes comodín.
- Mantener API y base de datos accesibles desde Internet con TLS y credenciales seguras. No pongas secretos MySQL/JWT en variables `VITE_*`: Vite las inserta en el JavaScript público.

Para completar una publicación funcional se necesita la URL pública elegida para el backend/MySQL (o elegir un proveedor para alojarlos) y acceso a una cuenta Vercel autenticada. El frontend se puede publicar antes, pero quedará como interfaz sin operaciones reales.
