# Publicar TurnoPro con Vercel y Aiven

Este repositorio despliega la interfaz Vite y la API Node.js como un solo proyecto de Vercel. MySQL se aloja en Aiven. El backend Java/Spring se conserva para desarrollo local, pero no se despliega en Vercel.

## 1. Crear MySQL en Aiven

1. Abre [Aiven Console](https://console.aiven.io/) e inicia sesión.
2. Crea un servicio **MySQL**. Selecciona una región cercana y el plan que esté disponible en tu cuenta.
3. Espera a que el servicio esté listo. En **Overview / Connection information**, copia host, puerto, usuario y contraseña. Mantén esos datos privados.
4. Descarga el certificado CA del servicio desde la sección de conexión TLS/SSL. Se configurará como variable privada `DB_SSL_CA` en Vercel.
5. Abre **Query editor** o conecta un cliente MySQL con TLS y ejecuta `database/turnopro_db.sql`. Comprueba que existan `servicios`, `turnos` y `historial_turnos`.

## 2. Conectar el mismo repositorio en Vercel

1. Abre [Vercel New Project](https://vercel.com/new), inicia sesión y elige **Import Git Repository**.
2. Autoriza GitHub si Vercel lo solicita y selecciona `fernan4895/TurnoApp`.
3. Deja **Root Directory** en `./` (raíz del repositorio). No elijas `frontend`.
4. Vercel detecta el `vercel.json` del repositorio. Mantén el comando de build `npm run build` y el directorio de salida `frontend/dist`.
5. Antes de desplegar, agrega las variables de entorno indicadas abajo para **Production** (y Preview si también deseas que las vistas previas consulten la base real). No uses prefijo `VITE_` para secretos.
6. Pulsa **Deploy**. Los siguientes pushes a la rama `main` actualizarán el mismo proyecto.

## 3. Variables privadas en Vercel

En **Project → Settings → Environment Variables**, agrega:

| Variable | Valor |
|---|---|
| `DB_HOST` | Host de conexión mostrado por Aiven |
| `DB_PORT` | Puerto mostrado por Aiven, normalmente 3306 |
| `DB_NAME` | `turnopro_db` |
| `DB_USER` | Usuario de conexión de Aiven |
| `DB_PASSWORD` | Contraseña de conexión de Aiven |
| `DB_SSL_CA` | Contenido PEM del certificado CA que descargaste |
| `JWT_SECRET` | Cadena aleatoria larga y privada (mínimo 32 caracteres) |
| `ADMIN_INITIAL_USERNAME` | Usuario que usarás para `/admin` |
| `ADMIN_INITIAL_PASSWORD` | Contraseña privada para `/admin` |

No pongas contraseñas en archivos del repositorio, en variables `VITE_*` ni en el chat. Tras cambiar variables, crea un nuevo deployment para aplicarlas.

## 4. Verificar

Cuando Vercel muestre el dominio `https://...vercel.app`:

1. Abre `/api/health`; debe responder `{"status":"UP"}`.
2. Abre `/nuevo`, crea un turno de prueba y guarda su número y código de consulta.
3. Abre `/consulta` y verifica ese turno.
4. Entra a `/admin` con las variables `ADMIN_INITIAL_*`, llama el turno y confirma que `/pantalla` se actualiza en unos segundos.

La pantalla usa consultas periódicas; no requiere un servicio WebSocket adicional. Aiven y Vercel pueden tener límites según el plan seleccionado. Para producción con datos reales, elige disponibilidad, respaldos y capacidad adecuados.
