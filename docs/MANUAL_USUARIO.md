# Manual de usuario

## Cliente

1. Abre la página de inicio y elige **Generar mi turno**.
2. Selecciona el servicio, escribe tu nombre y, si quieres, un teléfono. Confirma.
3. Conserva el número y código de consulta del comprobante. En **Consultar turno**, ingresa ambos para ver estado, posición y espera estimada.
4. Imprime el comprobante desde el navegador. La impresión depende del diálogo y la impresora seleccionada.

## Personal

1. Entra a `/admin` con la cuenta configurada por variables de entorno.
2. El dashboard consulta los conteos reales de la API y la lista de espera.
3. Pulsa **Llamar** junto al turno. Las transiciones disponibles por API son llamar, iniciar, finalizar, ausente y cancelar.
4. Abre `/pantalla` en el monitor de sala; muestra solo número, servicio y ventanilla.

La consulta pública requiere número y token aleatorio de consulta. No compartas el código. En esta primera versión el panel muestra errores de API/MySQL cuando falta configurar el servidor.
