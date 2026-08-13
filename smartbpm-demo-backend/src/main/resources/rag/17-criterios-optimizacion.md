# Criterios de Optimización Automática para Procesos de Inventarios/Retail

Cuando la IA reciba un proceso en lenguaje natural similar a los de esta app (o relacionado a inventarios/retail), debe aplicar estas heurísticas de optimización, en este orden:

## 1. Separar MVP de fase 2
Si el proceso mezcla una funcionalidad esencial (ej. registrar stock) con una funcionalidad de IA no crítica (ej. clasificación visual), sugerir dividirlas en procesos independientes.

## 2. No automatizar decisiones de negocio sin confirmación humana
Cuando las decisiones involucren dinero o inventario (pedidos, cantidades, precios), toda sugerencia de IA debe ser editable antes de ejecutarse. El sistema propone, el usuario decide.

## 3. Alertas automáticas obligatorias
Todo evento de "stock bajo" o similar debe ser automático (RN-06), no depender de que un humano lo note manualmente — esa es la razón de ser del sistema.

## 4. Manejo explícito de excepciones
Cada camino de excepción (falla técnica, dato inválido, falta de conexión) debe tener un manejo explícito. No asumir un "camino feliz" único.

## 5. Eventos finales medibles
Cada proceso debe terminar en un evento medible cuando sea posible (ej. "venta registrada", "quiebre de stock evitado"), para poder alimentar reportes y métricas de éxito del negocio.

## 6. Roles diferenciados en lanes
Roles con permisos diferenciados (RN-05) deben reflejarse en los lanes, restringiendo qué actividades puede iniciar cada actor. Solo el Tendero aprueba pedidos y ve reportes financieros; el Cajero solo registra ventas y recepciones.

## 7. Separar flujos de dinero
Separar siempre los flujos de dinero según quién paga a quién:
- Dinero que el tendero **gana** (ventas, margen) — proceso independiente
- Dinero que el tendero **paga** a la plataforma (suscripción) — proceso independiente
- Dinero que el proveedor **paga** a la plataforma (comisión, RN-10) — proceso independiente
Nunca deben mezclarse en un solo cálculo o pantalla sin distinguir el origen y destino del pago.

## Meta-regla de diagnóstico antes de optimizar
Antes de rediseñar un proceso, primero diagnosticar:
1. Señalar 3 supuestos del proceso
2. Identificar 2 excepciones no contempladas
3. Identificar 1 riesgo principal
Solo después de este diagnóstico, proponer las optimizaciones.
