export const examples = [
  {
    id: 'reembolso',
    label: 'Reembolso con posible auto-aprobación',
    narrative: 'El empleado registra un reembolso, el sistema valida la factura, si el monto es menor a 500 se aprueba automáticamente, si no el gerente aprueba, finanzas paga y al final el sistema notifica al empleado.'
  },
  {
    id: 'vacaciones',
    label: 'Solicitud de vacaciones',
    narrative: 'El empleado registra su solicitud de vacaciones, el jefe revisa y decide, recursos humanos registra la decisión y el sistema notifica al empleado.'
  },
  {
    id: 'compra',
    label: 'Solicitud de compra',
    narrative: 'El solicitante registra una compra, el jefe revisa, compras emite la orden y al final se notifica el resultado.'
  }
]
