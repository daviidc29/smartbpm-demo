# Contexto del Proyecto: Sistema de Inventarios para Tiendas de Barrio

## Problema y alcance del MVP

- **Dolor central:** quiebre de stock (ventas perdidas) y stock muerto (capital inmovilizado) por gestión manual de inventario.
- **Corrección aplicada:** el MVP separa la gestión de inventario (prioridad, entra en el MVP) de la generación de catálogo con visión por computador (fase 2, no bloquea el MVP).
- **Segmento inicial:** tienda de barrio / micromercado con productos de alta rotación (abarrotes, bebidas, aseo) — patrones de demanda más predecibles.
- **Regla de reabastecimiento del MVP:** punto de reorden simple ("cuando queden X unidades, pedir Y"), configurable por producto. El modelo predictivo (IA) solo se activa cuando hay suficiente historial (mínimo 4–8 semanas de ventas).
- **Métrica de éxito:** reducción de quiebres de stock y reducción de capital inmovilizado, comparado contra la regla simple — debe quedar modelado como evento medible dentro del proceso.
- **Insight de Porter:** gancho de adopción gratuito y de bajo costo de cambio (escáner con la cámara), con monetización posterior vía suscripción por módulos de valor y comisión a proveedores por pedidos de reabastecimiento (modelo B2B2B).

## Actores / Pools y Lanes del sistema completo

| Pool / Lane | Tipo | Descripción |
|---|---|---|
| **Tendero (dueño)** | Persona — Pool "Tienda" | Dueño del negocio. Configura reglas de reorden, ve reportes, administra usuarios/roles, aprueba pedidos a proveedor. |
| **Cajero / Vendedor** | Persona — Lane dentro de Pool "Tienda" | Usuario operativo diario: escanea productos, registra ventas y recepciones de mercancía. Permisos limitados. |
| **App Móvil (Cliente)** | Sistema — Lane | Interfaz que captura inputs (cámara, formularios), muestra estados y notificaciones. No toma decisiones de negocio. |
| **Backend / Servidor** | Sistema — Lane | Aplica reglas de negocio, valida datos, calcula stock, dispara alertas, gestiona autenticación y roles. |
| **Servicio de IA (Predicción)** | Sistema — Lane (fase 2) | Modelo de series de tiempo que sugiere cantidad de reabastecimiento cuando hay historial suficiente. |
| **Servicio de IA (Visión)** | Sistema — Lane (fase 2, opcional) | Clasificación visual automática de producto a partir de foto (no bloquea el MVP). |
| **Proveedor / Distribuidor** | Persona/Organización — Pool separado | Externo a la tienda. Recibe pedidos de reabastecimiento generados desde la app. |

**Regla aplicada:** Tendero, Cajero, App y Backend van en el mismo pool ("Sistema de Inventario — Tienda") porque pertenecen a la misma organización operativa; Proveedor va en un pool separado porque es una organización externa.

## Reglas de Negocio Globales (aplican a varios procesos)

- **RN-01:** Un producto no puede tener stock negativo. Ninguna venta puede completarse si deja el stock por debajo de 0.
- **RN-02:** Todo movimiento de stock (venta o recepción) debe quedar registrado con: producto, cantidad, usuario responsable, fecha/hora (trazabilidad obligatoria).
- **RN-03:** El punto de reorden por defecto es configurable por producto, pero si el tendero no lo configura, el sistema usa un valor sugerido basado en las ventas promedio de las últimas 2 semanas.
- **RN-04:** El modelo predictivo de IA solo se activa si el producto tiene ≥ 4 semanas de historial de ventas. Antes se usa la regla simple de reorden (RN-03).
- **RN-05:** Solo el rol "Tendero" puede aprobar pedidos a proveedor y ver reportes financieros completos. El "Cajero" solo puede registrar ventas y recepciones.
- **RN-06:** Toda alerta de stock bajo debe generarse automáticamente (no requiere que un humano la dispare) en cuanto el stock cruza el punto de reorden.
- **RN-07:** Si el escaneo de código de barras falla o el producto no existe en el catálogo, el sistema debe ofrecer registro manual inmediato, sin bloquear el flujo de venta o recepción.
- **RN-08:** Todo producto debe tener registrado un costo de compra además del precio de venta, para poder calcular margen/ganancia real. Sin costo registrado, el sistema debe marcar el producto como "margen no calculable".
- **RN-09:** El acceso a funcionalidades premium depende del estado de suscripción de la tienda. Si la suscripción está vencida, el sistema degrada a funciones gratuitas sin bloquear el uso operativo diario.
- **RN-10:** Toda comisión cobrada a un proveedor por un pedido generado desde la app debe registrarse como transacción independiente, nunca oculta dentro del monto que paga el tendero.
