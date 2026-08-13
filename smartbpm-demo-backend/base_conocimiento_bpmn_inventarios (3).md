# Base de Conocimiento BPMN — App Móvil de Gestión de Inventarios para Tiendas de Barrio

> Documento fuente para enriquecer el RAG de un modelador de procesos con IA (tipo Bizagi Modeler + LLM). Contiene: (1) reglas generales de modelado BPMN extraídas de la clase, (2) el mapa completo de actores y pools del proyecto, y (3) cada proceso de la aplicación modelado en detalle — desde el login hasta el cierre de sesión — con actividades, decisiones nombradas, reglas de negocio y excepciones. El objetivo es que, dado un proceso en lenguaje natural, la IA pueda diagramarlo correctamente y luego optimizarlo usando estos criterios.

---

## PARTE 1 — REGLAS GENERALES DE MODELADO BPMN

Estas reglas son transversales: deben aplicarse a **cualquier** proceso que la IA modele, no solo a los de esta app. Son la "gramática" que el RAG debe usar para validar y corregir diagramas.

### 1.1 Notación mínima (4 elementos base)

| Elemento | Regla de uso |
|---|---|
| **Inicio (evento)** | Representa el disparador que arranca el flujo. Debe ser un evento observable y concreto (ej. "Cliente presiona 'Pagar'"), no una idea vaga (ej. "Inicio del proceso"). |
| **Tarea (acción)** | Se nombra siempre como **Verbo + Objeto** (ej. "Validar stock", "Registrar venta", "Enviar notificación"). Nunca solo un sustantivo ("Stock", "Notificación"). |
| **Decisión (rombo)** | Se formula como pregunta explícita, y **cada camino de salida debe tener nombre** (ej. "¿Stock suficiente?" → "Sí" / "No"). Un rombo sin caminos nombrados es un proceso ambiguo y debe rechazarse. |
| **Final (evento)** | Representa un resultado explícito y distinto por cada desenlace posible (ej. "Venta registrada", "Venta rechazada por stock insuficiente"). Un proceso puede tener varios finales, pero cada uno debe ser nombrado según su resultado, no un genérico "Fin". |

**Regla de oro:** todo camino del diagrama debe **empezar, avanzar y terminar**. No puede haber caminos huérfanos ni bucles sin condición de salida.

### 1.2 Swimlanes (pools y lanes)

- Las líneas de swimlane **separan responsabilidades, no etapas cronológicas**. Un lane = un actor/rol/sistema, no una fase del tiempo.
- **Regla de asignación:** toda actividad debe tener un responsable único y claro. Si una tarea podría estar en dos lanes, es señal de que la tarea está mal definida y debe dividirse.
- **Cambio de carril (lane a lane) = evento relevante**: normalmente indica una entrega de información, una espera, o un traspaso de responsabilidad. El RAG debe marcar automáticamente estos cruces como "puntos de integración" (posibles llamadas API, notificaciones o mensajes entre sistemas).
- **El sistema no es una persona**: un pool o lane de "Sistema" o "App" solo contiene tareas automáticas (validaciones, cálculos, envíos automáticos). Cualquier tarea que requiera juicio humano (decidir, aprobar, elegir) NO puede estar en el lane del sistema.
- Para procesos B2B con múltiples partes (cliente, proveedor, plataforma), usar **pools separados** (no solo lanes) cuando los actores pertenecen a organizaciones distintas; usar **lanes dentro de un mismo pool** cuando los actores son roles dentro de la misma organización o app.

### 1.3 Tipos de decisión (rombos) y cuándo usarlos

- **Decisión exclusiva (XOR)**: el flujo toma **un solo camino** de varios posibles. Ejemplo: "¿Cumple la validación?" → "Cumple" / "No cumple". Se usa para reglas de negocio que son mutuamente excluyentes.
- **Caminos paralelos (AND / fork-join)**: **varias acciones ocurren simultáneamente** y no son alternativas entre sí. Ejemplo: al confirmar un pedido, "Notificar al cliente" y "Preparar producto" ocurren en paralelo, no una u otra.
- **Regla crítica**: nunca usar un rombo de decisión exclusiva cuando en realidad las ramas deberían ejecutarse todas (eso es un error común que el RAG debe detectar y corregir automáticamente).
- Nombrar cada salida de un rombo evita procesos ambiguos; una salida sin nombre es un defecto de modelado que el validador debe señalar.

### 1.4 Checklist de validación de un BPM (aplicar a cada proceso generado)

Antes de dar por bueno un diagrama, verificar:

1. ✅ El alcance tiene un inicio y finales claros (todos los finales nombrados según su resultado).
2. ✅ Cada actividad tiene un responsable (lane) asignado.
3. ✅ Cada decisión tiene una pregunta explícita y salidas nombradas.
4. ✅ Las excepciones importantes (errores, casos límite, fallas de conexión, datos inválidos) están representadas, no omitidas.
5. ✅ El sistema no se confunde con una persona (ninguna tarea de "sistema" implica juicio humano).
6. ✅ No hay tareas huérfanas ni caminos sin salida.
7. ✅ El nombre de cada tarea sigue el patrón Verbo + Objeto.

### 1.5 Fórmula para enmarcar cualquier idea antes de diagramar

Antes de generar el BPM, la IA debe poder completar esta frase con la entrada en lenguaje natural del usuario:

> **"Cuando [evento], [actor] necesita [acción] para lograr [resultado]."**

Y responder tres preguntas base:
1. ¿Quién vive el problema? (actor/rol)
2. ¿Qué dispara el proceso? (evento inicial)
3. ¿Qué resultado demuestra valor? (evento final medible)

Si el usuario no da suficiente información para responder estas tres preguntas, el sistema debe pedir la aclaración antes de diagramar, en lugar de asumir.

### 1.6 Puente entre proceso (BPM) e interfaz (UI)

Regla estructural para conectar el modelo de proceso con las pantallas que la app de wireframes/mockups debe generar después:

| Del BPM... | ...se deriva en la interfaz |
|---|---|
| Actividad del usuario (ej. "Registrar producto") | Un formulario o input correspondiente |
| Punto de validación o decisión | Mensajes de estado, alertas, confirmaciones visuales |
| Elegir un camino (rama de decisión) | Controles de UI (botones, selects) y sus consecuencias visibles |

**Regla de coherencia:** si una pantalla propuesta no da soporte a ninguna actividad del BPM, sobra (eliminarla). Si el proceso exige una actividad sin pantalla asociada, falta diseñarla. El RAG debe usar esta regla para auditar que el conjunto BPM + wireframes esté completo, sin huecos ni redundancias.

### 1.7 Rol de la IA dentro del propio proceso de modelado (meta-regla)

Cuando la IA revisa/optimiza un proceso ya modelado, debe seguir esta secuencia y no saltarse pasos:
1. **Contextualizar**: identificar problema, actores y reglas del proceso.
2. **Cuestionar**: señalar excepciones no contempladas y posibles sesgos (ej. reglas que perjudican a un tipo de usuario).
3. **Comparar**: proponer alternativas de flujo (no una sola solución).
4. **Verificar**: confirmar coherencia con la experiencia real del usuario final.

Prompt crítico de referencia para la fase de optimización (no rediseñar de una vez, primero diagnosticar):

> *"Actúa como revisor. Señala 3 supuestos, 2 excepciones y 1 riesgo del proceso. No rediseñes todavía."*

---

## PARTE 2 — CONTEXTO DEL PROYECTO: SISTEMA DE INVENTARIOS PARA TIENDAS DE BARRIO

### 2.1 Problema y alcance del MVP (decisión ya tomada, con corrección del profesor)

- **Dolor central:** quiebre de stock (ventas perdidas) y stock muerto (capital inmovilizado) por gestión manual de inventario.
- **Corrección aplicada:** el MVP separa la **gestión de inventario** (prioridad, entra en el MVP) de la **generación de catálogo con visión por computador** (fase 2, no bloquea el MVP).
- **Segmento inicial:** tienda de barrio / micromercado con productos de alta rotación (abarrotes, bebidas, aseo) — patrones de demanda más predecibles.
- **Regla de reabastecimiento del MVP:** punto de reorden simple ("cuando queden X unidades, pedir Y"), configurable por producto. El modelo predictivo (IA) solo se activa cuando hay suficiente historial (mínimo 4–8 semanas de ventas).
- **Métrica de éxito (pregunta transversal del profesor):** reducción de quiebres de stock y reducción de capital inmovilizado, comparado contra la regla simple — esto es lo que debe poder demostrarse con datos, y por tanto **debe quedar modelado como evento medible dentro del proceso** (no solo como reporte).
- **Insight de Porter aplicado:** el gancho de adopción debe ser gratuito y de bajo costo de cambio (escáner básico con la cámara), con monetización posterior vía suscripción por módulos de valor (alertas automáticas) y, a futuro, comisión a proveedores por pedidos de reabastecimiento generados desde la app (modelo B2B2B). Esto implica que el proceso de "Pedido a proveedor" debe modelarse pensando en una futura integración con terceros, no solo como una nota interna.

### 2.2 Actores / Pools y Lanes del sistema completo

| Pool / Lane | Tipo | Descripción |
|---|---|---|
| **Tendero (dueño)** | Persona — Pool "Tienda" | Dueño del negocio. Configura reglas de reorden, ve reportes, administra usuarios/roles, aprueba pedidos a proveedor. |
| **Cajero / Vendedor** | Persona — Lane dentro de Pool "Tienda" | Usuario operativo diario: escanea productos, registra ventas y recepciones de mercancía. Permisos limitados (no ve reportes financieros completos). |
| **App Móvil (Cliente)** | Sistema — Lane | Interfaz que captura inputs (cámara, formularios), muestra estados y notificaciones. No toma decisiones de negocio, solo las presenta. |
| **Backend / Servidor** | Sistema — Lane | Aplica reglas de negocio, valida datos, calcula stock, dispara alertas, gestiona autenticación y roles. |
| **Servicio de IA (Predicción)** | Sistema — Lane (fase 2) | Modelo de series de tiempo que sugiere cantidad de reabastecimiento cuando hay historial suficiente. |
| **Servicio de IA (Visión)** | Sistema — Lane (fase 2, opcional) | Clasificación visual automática de producto a partir de foto (no bloquea el MVP). |
| **Proveedor / Distribuidor** | Persona/Organización — Pool separado | Externo a la tienda. Recibe pedidos de reabastecimiento generados desde la app (integración B2B2B, fase futura). |

**Regla aplicada:** Tendero, Cajero, App y Backend van en el **mismo pool** ("Sistema de Inventario — Tienda") porque pertenecen a la misma organización operativa; Proveedor va en un **pool separado** porque es una organización externa con la que solo hay intercambio de mensajes (pedido enviado / pedido confirmado).

### 2.3 Reglas de negocio globales (aplican a varios procesos)

1. **RN-01 — Un producto no puede tener stock negativo.** Ninguna venta puede completarse si deja el stock por debajo de 0.
2. **RN-02 — Todo movimiento de stock (venta o recepción) debe quedar registrado con: producto, cantidad, usuario responsable, fecha/hora.** (trazabilidad obligatoria).
3. **RN-03 — El punto de reorden por defecto es configurable por producto**, pero si el tendero no lo configura, el sistema usa un valor sugerido basado en las ventas promedio de las últimas 2 semanas.
4. **RN-04 — El modelo predictivo de IA solo se activa si el producto tiene ≥ 4 semanas de historial de ventas.** Antes de eso, se usa la regla simple de reorden (RN-03).
5. **RN-05 — Solo el rol "Tendero" puede aprobar pedidos a proveedor y ver reportes financieros completos.** El "Cajero" solo puede registrar ventas y recepciones.
6. **RN-06 — Toda alerta de stock bajo debe generarse automáticamente (no requiere que un humano la dispare) en cuanto el stock cruza el punto de reorden.**
7. **RN-07 — Si el escaneo de código de barras falla o el producto no existe en el catálogo, el sistema debe ofrecer registro manual inmediato**, sin bloquear el flujo de venta o recepción.
8. **RN-08 — Todo producto debe tener registrado un costo de compra además del precio de venta**, para poder calcular margen/ganancia real del tendero. Sin costo registrado, el sistema debe marcar el producto como "margen no calculable" en vez de asumir un valor.
9. **RN-09 — El acceso a funcionalidades premium (predicción IA, reportes avanzados, pedidos automáticos a proveedor) depende del estado de suscripción de la tienda.** Si la suscripción está vencida, el sistema degrada a las funciones gratuitas (escaneo básico y registro manual) sin bloquear el uso operativo diario (venta/recepción), para no dejar al tendero sin poder operar.
10. **RN-10 — Toda comisión cobrada a un proveedor por un pedido generado desde la app debe registrarse como transacción independiente**, nunca oculta dentro del monto que paga el tendero — el tendero no debe pagar de más por esta comisión.

---

## PARTE 3 — PROCESOS DETALLADOS (de login a logout)

Cada proceso se describe con: evento inicial, pools/lanes involucrados, secuencia de actividades, decisiones nombradas, excepciones y evento(s) final(es). Están escritos para que el RAG los use como plantillas de referencia al generar o validar diagramas equivalentes.

---

### 3.1 Proceso: Registro y Autenticación (Login / Signup)

**Evento inicial:** "Usuario abre la app por primera vez" o "Usuario abre la app y no tiene sesión activa".

**Pools/Lanes:** App Móvil, Backend.

**Secuencia:**
1. (App) Mostrar pantalla de inicio con opciones "Iniciar sesión" / "Crear cuenta".
2. **Camino A — Crear cuenta:**
   - (App) Solicitar datos de tienda y usuario (nombre, correo, contraseña, nombre del negocio).
   - (Backend) Validar formato de datos y unicidad de correo.
   - **Decisión: "¿Correo ya registrado?"**
     - Sí → (App) Mostrar mensaje de error y sugerir "Iniciar sesión" → **Final: "Registro rechazado — cuenta existente"**.
     - No → (Backend) Crear cuenta y asignar rol "Tendero" por defecto → (Backend) Enviar correo/código de verificación → **Decisión: "¿Verificación exitosa?"**
       - No (tras N intentos o expiración) → **Final: "Registro incompleto — verificación fallida"**.
       - Sí → (Backend) Activar cuenta → **Final: "Cuenta creada y activa"**.
3. **Camino B — Iniciar sesión:**
   - (App) Solicitar correo/usuario y contraseña.
   - (Backend) Validar credenciales.
   - **Decisión: "¿Credenciales válidas?"**
     - No → (App) Mostrar mensaje de error, contador de intentos → **Excepción: tras 5 intentos fallidos, bloquear temporalmente y ofrecer "Recuperar contraseña"** → **Final: "Acceso denegado"**.
     - Sí → (Backend) Generar token de sesión → (Backend) Identificar rol del usuario (Tendero/Cajero) → **Final: "Sesión iniciada"**.

**Reglas de negocio aplicadas:** RN-05 (rol determina permisos visibles desde el login).

**Excepciones a modelar:** sin conexión a internet al momento de autenticar (mostrar estado "offline", reintentar); token expirado durante el uso (forzar re-login sin perder datos no guardados).

**Nota para wireframes (puente BPM→UI):** pantalla de login, pantalla de registro, mensajes de error inline, pantalla de recuperación de contraseña, indicador de estado de conexión.

---

### 3.2 Proceso: Registro de Productos (alta en catálogo)

**Evento inicial:** "Usuario (Tendero o Cajero) selecciona 'Agregar producto'".

**Pools/Lanes:** App Móvil, Backend, Servicio de IA (Visión) — este último solo en fase 2.

**Secuencia (MVP — sin IA de visión):**
1. (App) Mostrar opción: "Escanear código de barras" o "Registrar manualmente".
2. **Camino A — Escaneo:**
   - (App) Activar cámara y capturar código de barras.
   - (Backend) Buscar código en catálogo existente.
   - **Decisión: "¿Producto ya existe en el catálogo?"**
     - Sí → (App) Mostrar producto encontrado y pedir cantidad → ir a 3.3 (Recepción de mercancía) o continuar según contexto.
     - No → (App) Mostrar formulario de alta con código pre-cargado (nombre, categoría, precio, unidad, punto de reorden) → (Backend) Guardar nuevo producto → **Final: "Producto registrado exitosamente"**.
   - **Excepción: "¿Falla el escaneo o código ilegible?"** → (App) Ofrecer registro manual inmediato sin bloquear el flujo (RN-07) → **Final: "Producto registrado manualmente"**.
3. **Camino B — Registro manual directo:**
   - (App) Formulario manual (nombre, categoría, precio, unidad, stock inicial, punto de reorden).
   - (Backend) Validar campos obligatorios.
   - **Decisión: "¿Datos válidos?"**
     - No → (App) Resaltar campos con error → regresar a formulario.
     - Sí → (Backend) Guardar producto → **Final: "Producto registrado exitosamente"**.

**Extensión fase 2 (Servicio de IA — Visión), como rama paralela opcional, no obligatoria:**
- (App) Capturar foto del producto → (Servicio IA Visión) Clasificar categoría y sugerir descripción → (App) Mostrar sugerencia como **prellenado editable**, nunca como dato final automático (el tendero siempre confirma o corrige) → (Backend) Guardar con los datos confirmados.
- **Regla de negocio (transparencia de IA):** toda sugerencia de IA debe presentarse como propuesta editable, no como decisión automática aplicada sin confirmación humana — esto es consistente con el criterio de "la IA propone, el equipo/usuario decide".

**Reglas de negocio aplicadas:** RN-07.

---

### 3.3 Proceso: Registro de Venta (salida de stock)

**Evento inicial:** "Cajero selecciona 'Nueva venta'".

**Pools/Lanes:** App Móvil (Cajero), Backend.

**Secuencia:**
1. (App) Activar escaneo o búsqueda manual de producto.
2. (App) Agregar producto(s) al carrito de venta con cantidad.
3. (Backend) Validar disponibilidad de stock para cada producto del carrito.
4. **Decisión: "¿Stock suficiente para todos los productos?"**
   - No → (App) Marcar producto(s) con stock insuficiente y bloquear la cantidad excedente (RN-01) → **Decisión: "¿Cajero ajusta cantidad o elimina producto?"**
     - Ajusta → regresar a validación de stock.
     - Cancela ítem → continuar sin ese producto.
   - Sí → continuar.
5. (App) Confirmar venta y total.
6. (Backend) Descontar stock, registrar movimiento (RN-02).
7. **Caminos paralelos (no exclusivos):**
   - (Backend) Verificar si algún producto cruzó su punto de reorden → si sí, disparar proceso 3.5 (Alertas de stock bajo) automáticamente (RN-06).
   - (App) Generar comprobante/recibo de venta.
8. **Final: "Venta registrada exitosamente"**.

**Excepciones a modelar:** venta cancelada a mitad de proceso (revertir cambios no confirmados, no debe afectar stock); pérdida de conexión durante el registro (guardar venta en cola local y sincronizar cuando vuelva la conexión, sin duplicar ni perder el movimiento).

**Reglas de negocio aplicadas:** RN-01, RN-02, RN-06.

---

### 3.4 Proceso: Recepción de Mercancía (entrada de stock / reabastecimiento manual)

**Evento inicial:** "Cajero o Tendero selecciona 'Recibir mercancía'" (llega pedido físico del proveedor).

**Pools/Lanes:** App Móvil, Backend.

**Secuencia:**
1. (App) Escanear o buscar producto recibido.
2. (App) Ingresar cantidad recibida.
3. (Backend) Validar producto existente en catálogo.
4. **Decisión: "¿Producto existe en catálogo?"**
   - No → ir a proceso 3.2 (Registro de producto) antes de continuar.
   - Sí → continuar.
5. (Backend) Sumar cantidad al stock actual, registrar movimiento (RN-02) con referencia a proveedor si aplica.
6. **Final: "Recepción registrada — stock actualizado"**.

**Reglas de negocio aplicadas:** RN-02.

---

### 3.5 Proceso: Alertas y Punto de Reorden (regla simple — MVP)

**Evento inicial:** "Stock de un producto cruza su punto de reorden" (disparado automáticamente por 3.3 o por revisión periódica del backend).

**Pools/Lanes:** Backend, App Móvil (Tendero/Cajero).

**Secuencia:**
1. (Backend) Comparar stock actual contra punto de reorden configurado (RN-03).
2. **Decisión: "¿Stock actual ≤ punto de reorden?"**
   - No → **Final: "Sin alerta — stock suficiente"** (fin del ciclo de verificación).
   - Sí → (Backend) Generar alerta automática (RN-06) → (App) Notificar al Tendero (push/notificación in-app) con producto y cantidad sugerida de pedido.
3. **Decisión: "¿Cantidad sugerida viene de regla simple o modelo predictivo?"** (según RN-04)
   - Historial < 4 semanas → (Backend) Usar cantidad sugerida = regla fija configurada por el tendero.
   - Historial ≥ 4 semanas → activar proceso 3.6 (Predicción de demanda, fase 2) para calcular la cantidad sugerida.
4. **Final: "Alerta generada — pendiente de decisión del Tendero"** (el Tendero decide si genera pedido, ver 3.7).

**Métrica asociada (para reportes, ligada a la pregunta transversal del profesor):** cada alerta debe quedar marcada como "atendida a tiempo" o "resultó en quiebre de stock", para poder calcular la métrica de éxito del proyecto (reducción de quiebres de stock).

---

### 3.6 Proceso: Predicción de Demanda (Servicio de IA — fase 2)

**Evento inicial:** "Backend solicita cantidad sugerida de reabastecimiento para un producto con historial suficiente" (disparado desde 3.5).

**Pools/Lanes:** Backend, Servicio de IA (Predicción).

**Secuencia:**
1. (Backend) Enviar historial de ventas del producto al servicio de IA.
2. (Servicio IA) Calcular proyección de demanda y cantidad sugerida.
3. **Decisión: "¿Confianza de la predicción es suficiente?"** (umbral definido, ej. varianza aceptable del modelo)
   - No → (Backend) Usar regla simple como respaldo (fallback a RN-03) → **Final: "Sugerencia por regla simple (respaldo)"**.
   - Sí → (Backend) Recibir cantidad sugerida → **Final: "Sugerencia generada por IA"**.
4. (App) Mostrar siempre la sugerencia como **propuesta editable**, con la razón/base de la sugerencia visible (transparencia), nunca aplicada automáticamente sin confirmación del Tendero.

**Regla de negocio (explicabilidad, alineada con la corrección del profesor para el caso hotelero, aplicable aquí también):** toda recomendación automática debe mostrar las variables que la generaron y dejar la decisión final a la persona; el sistema no genera el pedido por sí solo.

---

### 3.7 Proceso: Generación de Pedido a Proveedor (B2B2B — fase futura)

**Evento inicial:** "Tendero decide generar pedido a partir de una alerta o sugerencia (3.5/3.6)".

**Pools/Lanes:** App Móvil (Tendero), Backend, Proveedor (pool externo).

**Secuencia:**
1. (App) Tendero revisa productos sugeridos y cantidades.
2. (App) Tendero confirma o ajusta cantidades manualmente.
3. (Backend) Consolidar pedido y enviarlo al proveedor (mensaje entre pools — cruce de swimlane).
4. **Decisión: "¿Proveedor confirma disponibilidad?"** (evento externo, pool Proveedor)
   - No → (Backend) Notificar al Tendero → **Final: "Pedido rechazado por proveedor"**.
   - Sí → (Proveedor) Confirmar pedido y tiempo de entrega → (Backend) Registrar pedido en tránsito → **Final: "Pedido confirmado — en tránsito"**.
5. Cuando llega la mercancía → dispara proceso 3.4 (Recepción de mercancía).

**Nota de monetización (Porter):** este cruce de pool con Proveedor es el punto de integración donde a futuro se modela la comisión B2B2B; debe quedar identificado como "punto de integración externo" en el diagrama.

---

### 3.8 Proceso: Consulta de Reportes / Dashboard

**Evento inicial:** "Tendero selecciona 'Reportes'".

**Pools/Lanes:** App Móvil (Tendero — solo este rol, RN-05), Backend.

**Secuencia:**
1. (App) Solicitar rango de fechas y tipo de reporte (stock actual, quiebres de stock, capital inmovilizado, ventas, **margen de ganancia**).
2. (Backend) Calcular métricas solicitadas, incluyendo **ganancia bruta = (precio de venta − costo de compra) × unidades vendidas**, por producto y consolidada.
3. **Decisión: "¿Hay datos suficientes en el rango solicitado?"**
   - No → (App) Mostrar mensaje "Sin datos suficientes para este periodo" → **Final: "Reporte vacío"**.
   - Sí → continuar.
4. **Decisión: "¿Todos los productos vendidos tienen costo de compra registrado?"** (RN-08)
   - No → (App) Mostrar el reporte con los productos sin costo marcados como "margen no calculable", sin bloquear el resto del reporte.
   - Sí → (Backend) Calcular margen completo.
5. (Backend) Generar reporte → (App) Mostrar visualización → **Final: "Reporte generado"**.

**Reglas de negocio aplicadas:** RN-05 (solo Tendero accede a reportes financieros completos, incluido el de ganancia; Cajero puede tener una vista reducida sin datos de margen si se decide habilitarla), RN-08.

---

### 3.9 Proceso: Gestión de Usuarios y Roles

**Evento inicial:** "Tendero selecciona 'Gestionar usuarios'".

**Pools/Lanes:** App Móvil (Tendero), Backend.

**Secuencia:**
1. (App) Mostrar lista de usuarios de la tienda (cajeros).
2. **Decisión: "¿Acción a realizar?"** — caminos exclusivos:
   - "Agregar usuario" → (App) Formulario de invitación (correo, rol) → (Backend) Crear usuario con rol "Cajero" → **Final: "Usuario agregado"**.
   - "Editar permisos" → (Backend) Actualizar rol → **Final: "Permisos actualizados"**.
   - "Eliminar usuario" → **Decisión: "¿Confirmar eliminación?"** → Sí → (Backend) Revocar acceso → **Final: "Usuario eliminado"**; No → cancelar.

**Reglas de negocio aplicadas:** RN-05 (solo el Tendero puede ejecutar este proceso).

---

### 3.10 Proceso: Cierre de Sesión (Logout)

**Evento inicial:** "Usuario selecciona 'Cerrar sesión'" o "Sesión expira por inactividad".

**Pools/Lanes:** App Móvil, Backend.

**Secuencia:**
1. **Decisión: "¿Hay operaciones pendientes de sincronizar (ej. ventas en cola offline)?"**
   - Sí → (App) Advertir al usuario y ofrecer "Sincronizar antes de salir" o "Salir de todas formas" → según elección, sincronizar o continuar.
   - No → continuar directo.
2. (Backend) Invalidar token de sesión.
3. (App) Redirigir a pantalla de login.
4. **Final: "Sesión cerrada"**.

**Excepción a modelar:** cierre por expiración automática (inactividad o token vencido) debe seguir el mismo flujo de advertencia si hay datos pendientes, no debe perder información silenciosamente.

---

### 3.11 Proceso Transversal: Manejo de Excepciones Técnicas

Este proceso no se dispara por un evento de negocio, sino que debe **envolver** (aplicar como manejo transversal) a todos los procesos anteriores. El RAG debe insertarlo como capa de validación en cualquier proceso que involucre red o hardware (cámara, conexión):

| Excepción | Manejo esperado |
|---|---|
| Sin conexión a internet | Guardar operación en cola local; mostrar indicador de "modo offline"; sincronizar automáticamente al recuperar conexión, sin duplicar registros. |
| Falla de cámara / escaneo ilegible | Ofrecer alternativa de registro manual inmediato, sin bloquear el flujo (RN-07). |
| Producto duplicado al registrar | Detectar por código de barras único; alertar antes de crear duplicado y ofrecer "editar el existente" en vez de crear uno nuevo. |
| Token de sesión expirado a mitad de una operación | Guardar el progreso no confirmado localmente, forzar re-login, y restaurar el estado tras autenticar de nuevo. |
| Servicio de IA no disponible (predicción o visión) | Hacer fallback automático a la regla simple (RN-03) o al registro manual, sin bloquear al usuario ni mostrar error técnico crudo. |

---

### 3.12 Proceso: Registro de Costo y Cálculo de Margen (ganancia del Tendero)

**Evento inicial:** "Tendero o Cajero registra/edita el costo de compra de un producto" (puede ocurrir al dar de alta el producto en 3.2, al recibir mercancía en 3.4, o de forma independiente si cambia el precio del proveedor).

**Pools/Lanes:** App Móvil, Backend.

**Secuencia:**
1. (App) Mostrar campo de "costo de compra" junto al "precio de venta" en el formulario del producto.
2. (Backend) Validar que el costo sea numérico y mayor a cero.
3. **Decisión: "¿Costo de compra registrado?"**
   - No → (Backend) Marcar producto como "margen no calculable" (RN-08) → el producto puede seguir vendiéndose con normalidad, solo queda sin dato de ganancia.
   - Sí → (Backend) Calcular margen unitario = precio de venta − costo de compra → guardar.
4. **Decisión: "¿Margen unitario es negativo o cero?"** (venta que no genera ganancia o genera pérdida)
   - Sí → (App) Alertar al Tendero al guardar ("Este producto se vendería con pérdida o sin margen") — no bloquea el guardado, es una advertencia.
   - No → continuar normalmente.
5. **Final: "Costo y margen actualizados"**.

**Reglas de negocio aplicadas:** RN-08.

**Nota:** este proceso alimenta directamente el cálculo de ganancia bruta usado en 3.8 (Reportes) y es la respuesta concreta a "cuánto dinero le genera esto al tendero", que es justo la pregunta transversal del profesor.

---

### 3.13 Proceso: Suscripción y Pagos de la Tienda (ganancia para el equipo — modelo SaaS freemium)

**Evento inicial:** "Tendero selecciona 'Mejorar plan' / 'Suscribirme'" o "El sistema detecta que la suscripción está por vencer/vencida".

**Pools/Lanes:** App Móvil (Tendero), Backend, **Pasarela de Pagos** (pool externo, ej. Wompi/PayU — comodity según el análisis de Porter), Servicio de IA (queda condicionado a este proceso, ver RN-09).

**Secuencia:**
1. (App) Mostrar comparación de plan gratuito vs. plan(es) pago(s) — ej. gratis: escaneo y registro manual; pago: alertas automáticas, predicción de demanda, pedidos a proveedor, reportes de margen.
2. (App) Tendero selecciona plan y método de pago.
3. (Backend) Generar solicitud de cobro y enviarla a la Pasarela de Pagos (cruce de pool — integración externa).
4. **Decisión: "¿Pago aprobado por la pasarela?"**
   - No → (App) Mostrar motivo de rechazo (fondos, datos de tarjeta, etc.) y ofrecer reintento → **Final: "Suscripción no completada"**.
   - Sí → (Backend) Activar plan pago, registrar fecha de vigencia y próximo cobro → **Final: "Suscripción activa"**.
5. **Evento recurrente (no disparado por el usuario): "Se acerca la fecha de renovación"**
   - (Backend) Intentar cobro automático a través de la Pasarela de Pagos.
   - **Decisión: "¿Cobro automático exitoso?"**
     - No → (Backend) Marcar suscripción como vencida → aplicar RN-09 (degradar a funciones gratuitas sin bloquear operación diaria) → (App) Notificar al Tendero para actualizar método de pago.
     - Sí → (Backend) Renovar vigencia → **Final: "Suscripción renovada"**.

**Reglas de negocio aplicadas:** RN-09.

**Nota (justificación de negocio, basada en Porter):** el plan gratuito es el "gancho" para vencer la resistencia de adopción del tendero (bajo poder de negociación frente a su reticencia a pagar por software); el ingreso real del equipo se activa cuando el tendero valora lo suficiente el módulo de alertas/predicción como para pagar por él. Este proceso es el que materializa esa estrategia, no debe modelarse como un simple "checkout" genérico.

---

### 3.14 Proceso: Comisión a Proveedores por Pedido Generado (ganancia para el equipo — modelo B2B2B, fase futura)

**Evento inicial:** "Proveedor confirma un pedido generado desde la app" (continuación directa de 3.7, paso donde el Proveedor confirma disponibilidad).

**Pools/Lanes:** Backend, Proveedor (pool externo), Pasarela de Pagos (pool externo).

**Secuencia:**
1. (Backend) Al confirmarse el pedido (evento final "Pedido confirmado — en tránsito" de 3.7), calcular la comisión correspondiente según el acuerdo comercial con ese proveedor (porcentaje o monto fijo por pedido).
2. **Decisión: "¿Proveedor tiene método de cobro configurado con la plataforma?"**
   - No → (Backend) Registrar la comisión como pendiente de cobro → (Backend) Notificar al equipo/administración para gestión manual → **Final: "Comisión pendiente — cobro manual"**.
   - Sí → (Backend) Generar cobro automático de comisión a través de la Pasarela de Pagos → **Decisión: "¿Cobro exitoso?"**
     - No → reintentar según política definida → si falla persistentemente, pasar a cobro manual (mismo final que arriba).
     - Sí → (Backend) Registrar comisión cobrada → **Final: "Comisión cobrada exitosamente"**.
3. (Backend) Registrar la transacción de forma **separada** de cualquier cobro al Tendero (RN-10) — el tendero nunca ve ni paga esta comisión, es 100% entre la plataforma y el proveedor.

**Reglas de negocio aplicadas:** RN-10.

**Nota:** este proceso depende de que 3.7 (Pedido a Proveedor) ya esté integrado con proveedores reales; en el MVP puede modelarse pero implementarse como un registro contable simple, sin cobro automático todavía.

---

## PARTE 4 — RESUMEN PARA EL RAG: CRITERIOS DE OPTIMIZACIÓN AUTOMÁTICA

Cuando la IA del modelador reciba un proceso en lenguaje natural similar a los de esta app (o releated a inventarios/retail), debe aplicar estas heurísticas de optimización, en este orden:

1. **Separar MVP de fase 2**: si el proceso mezcla una funcionalidad esencial (ej. registrar stock) con una funcionalidad de IA no crítica (ej. clasificación visual), sugerir dividirlas en procesos independientes, replicando la corrección aplicada en este proyecto.
2. **Nunca automatizar decisiones de negocio sin confirmación humana** cuando involucren dinero o inventario (pedidos, cantidades, precios) — toda sugerencia de IA debe ser editable antes de ejecutarse.
3. **Todo evento de "stock bajo" o similar debe ser automático (RN-06)**, no depender de que un humano lo note manualmente — esa es la razón de ser del sistema.
4. **Cada camino de excepción (falla técnica, dato inválido, falta de conexión) debe tener un manejo explícito**, no debe asumirse un "camino feliz" único.
5. **Cada proceso debe terminar en un evento medible** cuando sea posible (ej. "venta registrada", "quiebre de stock evitado"), para poder alimentar reportes y métricas de éxito del negocio.
6. **Roles con permisos diferenciados (RN-05) deben reflejarse en los lanes**, restringiendo qué actividades puede iniciar cada actor.
7. **Separar siempre los flujos de dinero según quién paga a quién**: el dinero que el tendero gana (ventas, margen) y el dinero que el tendero paga a la plataforma (suscripción) son procesos distintos entre sí, y ambos son distintos del dinero que el proveedor paga a la plataforma (comisión, RN-10). Nunca deben mezclarse en un solo cálculo o pantalla sin distinguir el origen y destino del pago.
