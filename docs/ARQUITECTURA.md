# Arquitectura

Frontend React + Vite + TypeScript ofrece inicio, solicitud/consulta de turno, pantalla de llamados y panel operativo. Axios centraliza las solicitudes y transmite JWT desde `sessionStorage`; la pantalla pública consulta cada cinco segundos como alternativa de conectividad.

Backend Spring Boot está dividido en API, dominio/repositorios, seguridad y configuración WebSocket. JPA persiste directamente en MySQL. La creación de secuencia bloquea la fila de servicio con `PESSIMISTIC_WRITE` dentro de transacción y la base aplica restricción única. Las transiciones se validan en servidor y la entidad usa `@Version` para concurrencia optimista.

El JWT usa secreto HMAC del entorno y vence. Las claves se procesan con BCrypt. La cuenta inicial se configura en variables del backend, nunca se crea con credencial predeterminada embebida. CORS limita los orígenes configurados.

El SQL reserva tablas para roles, usuarios, sucursales, servicios, ventanillas, turnos, historial y configuración. La versión inicial del API ya ejecuta persistencia de turnos/servicios y autorizaciones operativas, pero la administración completa de usuarios/servicios/ventanillas, bitácora de cambios, métricas temporales, filtros y CSV requieren ampliación.
