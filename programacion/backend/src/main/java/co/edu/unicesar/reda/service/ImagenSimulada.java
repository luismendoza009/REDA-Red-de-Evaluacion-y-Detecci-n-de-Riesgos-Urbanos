package co.edu.unicesar.reda.service;

import java.nio.charset.StandardCharsets;

/**
 * Las evidencias de la semilla son SIMULADAS: tienen registro en la base pero no hay archivo real.
 * Para que la demo no muestre imágenes rotas, se genera una imagen SVG que dice claramente que es
 * un ejemplo. Nunca se hace pasar por una foto real.
 */
final class ImagenSimulada {

    static final String MIME = "image/svg+xml";

    private ImagenSimulada() {
    }

    static byte[] para(String categoria, int numero) {
        String texto = escapar(categoria == null ? "Falla reportada" : categoria);
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="800" height="600" viewBox="0 0 800 600">
                  <defs>
                    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0" stop-color="#1e293b"/><stop offset="1" stop-color="#0f172a"/>
                    </linearGradient>
                  </defs>
                  <rect width="800" height="600" fill="url(#g)"/>
                  <g fill="none" stroke="#334155" stroke-width="2">
                    <path d="M0 450 L800 380"/><path d="M0 500 L800 430"/><path d="M380 600 L430 380"/>
                  </g>
                  <circle cx="400" cy="250" r="70" fill="none" stroke="#f59e0b" stroke-width="6"/>
                  <path d="M400 210 v50 M400 285 v8" stroke="#f59e0b" stroke-width="10" stroke-linecap="round"/>
                  <text x="400" y="390" text-anchor="middle" font-family="Segoe UI, Arial, sans-serif"
                        font-size="34" fill="#e2e8f0">%s</text>
                  <text x="400" y="440" text-anchor="middle" font-family="Segoe UI, Arial, sans-serif"
                        font-size="22" fill="#94a3b8">Foto de ejemplo n.º %d · datos simulados de la semilla</text>
                </svg>
                """.formatted(texto, numero);
        return svg.getBytes(StandardCharsets.UTF_8);
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
