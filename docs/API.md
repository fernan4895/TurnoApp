# API REST

Base local: `http://localhost:8080/api`. JSON UTF-8. Endpoints administrativos requieren `Authorization: Bearer <JWT>`.

| Método | Ruta | Acceso | Uso |
|---|---|---|---|
| POST | `/auth/login` | Público | `username`, `password`; devuelve JWT |
| POST | `/auth/logout` | Sesión cliente | Cierre local del token |
| GET | `/servicios` | Público | Servicios activos |
| POST | `/turnos` | Público | Crea turno con `name`, `serviceId`, `phone` opcional |
| GET | `/turnos/consulta/{codigo}?number=A001` | Público con código | Consulta privada del turno |
| GET | `/turnos/espera` | Admin/Supervisor/Agente | Cola de espera |
| POST | `/turnos/{id}/llamar` | Admin/Supervisor/Agente | Llama al cliente |
| POST | `/turnos/{id}/iniciar` | Admin/Supervisor/Agente | Comienza atención |
| POST | `/turnos/{id}/finalizar` | Admin/Supervisor/Agente | Finaliza atención |
| POST | `/turnos/{id}/ausente` | Admin/Supervisor/Agente | Registra ausencia |
| POST | `/turnos/{id}/cancelar` | Admin/Supervisor/Agente | Cancela turno |
| GET | `/pantalla/llamados` | Público | Llamados públicos recientes, sin datos personales |
| GET | `/reportes/resumen` | Admin/Supervisor | Conteos del día |

WebSocket STOMP: endpoint `/ws`; suscripción pública `/topic/queue` emite eventos de número/estado sin nombre ni teléfono. La pantalla pública usa además consulta periódica de respaldo. Las listas y reportes no implementan aún todos los filtros, paginación ni exportación CSV del alcance final.
