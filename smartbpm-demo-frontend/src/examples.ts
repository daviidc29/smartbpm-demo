export const examples = [
  {
    id: 'registro_venta',
    label: 'Registro de Venta',
    narrative: 'El cajero activa el escaneo de producto y lo agrega al carrito. El backend valida el stock; si no es suficiente el cajero ajusta la cantidad, de lo contrario confirma la venta, descuenta el stock y genera el comprobante.'
  },
  {
    id: 'alertas_reorden',
    label: 'Alertas de Reorden',
    narrative: 'El backend detecta que un producto cruzó el punto de reorden y genera una alerta automática. El tendero recibe la notificación con la cantidad sugerida de pedido y decide si generar el pedido al proveedor.'
  },
  {
    id: 'recepcion_mercancia',
    label: 'Recepción de Mercancía',
    narrative: 'El tendero recibe mercancía, escanea el producto y registra la cantidad recibida. El sistema valida si el producto existe; si no, procede al alta en catálogo, y si sí, suma la cantidad al stock actual.'
  }
]
