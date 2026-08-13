# Reglas Generales de Modelado BPMN

Estas reglas son transversales: deben aplicarse a cualquier proceso que la IA modele. Son la "gramática" que el RAG debe usar para validar y corregir diagramas.

## 1. Notación mínima (4 elementos base)

- **Inicio (evento):** Representa el disparador que arranca el flujo. Debe ser un evento observable y concreto (ej. "Cliente presiona 'Pagar'"), no una idea vaga (ej. "Inicio del proceso").
- **Tarea (acción):** Se nombra siempre como Verbo + Objeto (ej. "Validar stock", "Registrar venta", "Enviar notificación"). Nunca solo un sustantivo ("Stock", "Notificación").
- **Decisión (rombo):** Se formula como pregunta explícita, y cada camino de salida debe tener nombre (ej. "¿Stock suficiente?" → "Sí" / "No"). Un rombo sin caminos nombrados es un proceso ambiguo y debe rechazarse.
- **Final (evento):** Representa un resultado explícito y distinto por cada desenlace posible (ej. "Venta registrada", "Venta rechazada por stock insuficiente"). Un proceso puede tener varios finales, pero cada uno debe ser nombrado según su resultado, no un genérico "Fin".

**Regla de oro:** todo camino del diagrama debe empezar, avanzar y terminar. No puede haber caminos huérfanos ni bucles sin condición de salida.

## 2. Swimlanes (pools y lanes)

- Las líneas de swimlane separan responsabilidades, no etapas cronológicas. Un lane = un actor/rol/sistema, no una fase del tiempo.
- Toda actividad debe tener un responsable único y claro. Si una tarea podría estar en dos lanes, la tarea está mal definida y debe dividirse.
- Cambio de carril (lane a lane) = evento relevante: normalmente indica una entrega de información, una espera, o un traspaso de responsabilidad. Marcar automáticamente estos cruces como "puntos de integración" (posibles llamadas API, notificaciones o mensajes entre sistemas).
- El sistema no es una persona: un pool o lane de "Sistema" o "App" solo contiene tareas automáticas (validaciones, cálculos, envíos automáticos). Cualquier tarea que requiera juicio humano (decidir, aprobar, elegir) NO puede estar en el lane del sistema.
- Para procesos B2B con múltiples partes (cliente, proveedor, plataforma), usar pools separados cuando los actores pertenecen a organizaciones distintas; usar lanes dentro de un mismo pool cuando los actores son roles dentro de la misma organización o app.

## 3. Tipos de decisión (rombos) y cuándo usarlos

- **Decisión exclusiva (XOR):** el flujo toma un solo camino de varios posibles. Ejemplo: "¿Cumple la validación?" → "Cumple" / "No cumple". Se usa para reglas de negocio mutuamente excluyentes.
- **Caminos paralelos (AND / fork-join):** varias acciones ocurren simultáneamente y no son alternativas entre sí. Ejemplo: al confirmar un pedido, "Notificar al cliente" y "Preparar producto" ocurren en paralelo.
- **Regla crítica:** nunca usar un rombo de decisión exclusiva cuando en realidad las ramas deberían ejecutarse todas (error común que debe detectarse y corregirse automáticamente).
- Nombrar cada salida de un rombo evita procesos ambiguos; una salida sin nombre es un defecto de modelado.

## 4. Checklist de validación (aplicar a cada proceso generado)

1. El alcance tiene un inicio y finales claros (todos los finales nombrados según su resultado).
2. Cada actividad tiene un responsable (lane) asignado.
3. Cada decisión tiene una pregunta explícita y salidas nombradas.
4. Las excepciones importantes (errores, casos límite, fallas de conexión, datos inválidos) están representadas, no omitidas.
5. El sistema no se confunde con una persona (ninguna tarea de "sistema" implica juicio humano).
6. No hay tareas huérfanas ni caminos sin salida.
7. El nombre de cada tarea sigue el patrón Verbo + Objeto.

## 5. Fórmula para enmarcar cualquier idea antes de diagramar

Antes de generar el BPM, completar esta frase:

> "Cuando [evento], [actor] necesita [acción] para lograr [resultado]."

Y responder tres preguntas base:
1. ¿Quién vive el problema? (actor/rol)
2. ¿Qué dispara el proceso? (evento inicial)
3. ¿Qué resultado demuestra valor? (evento final medible)

Si el usuario no da suficiente información para responder estas tres preguntas, el sistema debe pedir la aclaración antes de diagramar, en lugar de asumir.

## 6. Puente entre proceso (BPM) e interfaz (UI)

| Del BPM... | ...se deriva en la interfaz |
|---|---|
| Actividad del usuario (ej. "Registrar producto") | Un formulario o input correspondiente |
| Punto de validación o decisión | Mensajes de estado, alertas, confirmaciones visuales |
| Elegir un camino (rama de decisión) | Controles de UI (botones, selects) y sus consecuencias visibles |

Si una pantalla propuesta no da soporte a ninguna actividad del BPM, sobra. Si el proceso exige una actividad sin pantalla asociada, falta diseñarla.

## 7. Rol de la IA al revisar/optimizar un proceso (meta-regla)

Cuando la IA revisa/optimiza un proceso ya modelado, debe seguir esta secuencia:
1. **Contextualizar:** identificar problema, actores y reglas del proceso.
2. **Cuestionar:** señalar excepciones no contempladas y posibles sesgos.
3. **Comparar:** proponer alternativas de flujo (no una sola solución).
4. **Verificar:** confirmar coherencia con la experiencia real del usuario final.

Prompt de referencia para la fase de optimización:
> "Actúa como revisor. Señala 3 supuestos, 2 excepciones y 1 riesgo del proceso. No rediseñes todavía."
