/** Guarda un archivo descargado de la API (PDF, Excel, CSV). */
export function guardarArchivo(blob: Blob, nombre: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = nombre
  document.body.appendChild(a)
  a.click()
  a.remove()
  window.setTimeout(() => URL.revokeObjectURL(url), 10_000)
}

/** Abre una URL firmada (S3) en otra pestaña, sin dar acceso a esta ventana. */
export function abrirEnPestana(url: string) {
  window.open(url, '_blank', 'noopener,noreferrer')
}
