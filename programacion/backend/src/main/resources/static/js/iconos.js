// =============================================================================
// Iconos de línea (SVG en el propio código: sin librerías ni peticiones extra).
// Todos usan viewBox 24x24 y toman el color del texto (currentColor).
// =============================================================================

const TRAZOS = {
  inicio: '<path d="M3 11l9-7 9 7"/><path d="M5 10v10h5v-6h4v6h5V10"/>',
  mapa: '<path d="M9 4L3 6v14l6-2 6 2 6-2V4l-6 2-6-2z"/><path d="M9 4v14M15 6v14"/>',
  reportar: '<circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/>',
  reportes: '<rect x="5" y="3" width="14" height="18" rx="2"/><path d="M9 8h6M9 12h6M9 16h4"/>',
  casos: '<rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2M3 13h18"/>',
  buscar: '<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>',
  revision: '<path d="M4 4h16v12H4z"/><path d="M4 12h5l1 2h4l1-2h5"/><path d="M8 20h8"/>',
  verificar: '<path d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6l7-3z"/><path d="M8.5 12l2.5 2.5 4.5-5"/>',
  api: '<path d="M8 7l-5 5 5 5M16 7l5 5-5 5M13.5 4l-3 16"/>',
  cuenta: '<circle cx="12" cy="8" r="4"/><path d="M4 21c1-4 4-6 8-6s7 2 8 6"/>',
  ayuda: '<circle cx="12" cy="12" r="9"/><path d="M9.5 9.5a2.5 2.5 0 1 1 3.5 2.3c-.6.3-1 .8-1 1.5V14"/><path d="M12 17.5v.01"/>',
  salir: '<path d="M15 4h4v16h-4"/><path d="M10 8l-4 4 4 4M6 12h10"/>',
  alerta: '<path d="M12 3l10 18H2L12 3z"/><path d="M12 10v5M12 18v.01"/>',
  flecha: '<path d="M5 12h14M13 6l6 6-6 6"/>',
};

/** Devuelve el SVG del icono pedido (o un círculo si el nombre no existe). */
export function icono(nombre, clase = "icono") {
  const trazo = TRAZOS[nombre] ?? '<circle cx="12" cy="12" r="4"/>';
  return `<svg class="${clase}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
    stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${trazo}</svg>`;
}
