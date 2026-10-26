"""
Genera el diagrama EER de REDA en dos formatos a partir de una sola especificación JSON:
  - .drawio  (editable en app.diagrams.net / draw.io)
  - .png     (exportación en imagen, dibujada con matplotlib)

Por qué existe: en el equipo no hay draw.io de escritorio para exportar el PNG, y tener
una única fuente evita que el .drawio y el .png se desincronicen.

Especificación (JSON):
  tablas:    [{id, nombre, x, y, ancho, color?, columnas: ["PK id", "FK id_x", "nombre", ...]}]
  relaciones:[{desde, hacia, puntos: [[x,y], ...], etiqueta?, origen?, destino?}]
    - 'puntos' es la ruta ortogonal completa: el primero está en el borde de 'desde'
      y el último en el borde de 'hacia'. Así las líneas no se enciman.
    - origen/destino: terminación pata de gallo (por defecto ERmandOne -> ERzeroToMany).
  grupos:    [{texto, x, y, ancho, alto, color}]  (rectángulos de fondo opcionales)

Uso:  python scripts/generar_diagrama.py diagramas/especificacion_eer.json diagramas/reda_eer
"""
import json
import math
import sys
from xml.sax.saxutils import escape

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle

HEADER_H = 26
ROW_H = 18
PAD_B = 8
COLORES = {
    "catalogo": ("#dae8fc", "#6c8ebf"),
    "nucleo": ("#fff2cc", "#d6b656"),
    "legal": ("#d5e8d4", "#82b366"),
    "soporte": ("#f5f5f5", "#666666"),
}


def alto(t):
    return HEADER_H + ROW_H * len(t["columnas"]) + PAD_B


def geom(t):
    return t["x"], t["y"], t["ancho"], alto(t)


def fraccion(g, p):
    x, y, w, h = g
    fx = min(max((p[0] - x) / w, 0), 1)
    fy = min(max((p[1] - y) / h, 0), 1)
    return round(fx, 4), round(fy, 4)


# ----------------------------------------------------------------------- draw.io
def a_drawio(spec, ruta):
    celdas = ['<mxCell id="0"/>', '<mxCell id="1" parent="0"/>']
    for i, gr in enumerate(spec.get("grupos", [])):
        celdas.append(
            f'<mxCell id="g{i}" value="{escape(gr["texto"])}" style="rounded=1;whiteSpace=wrap;html=1;'
            f'fillColor={gr["color"]};strokeColor=#bbbbbb;dashed=1;verticalAlign=top;align=left;'
            f'spacingLeft=10;fontStyle=2;fontColor=#666666;arcSize=3;" vertex="1" parent="1">'
            f'<mxGeometry x="{gr["x"]}" y="{gr["y"]}" width="{gr["ancho"]}" height="{gr["alto"]}" as="geometry"/></mxCell>'
        )
    tablas = {t["id"]: t for t in spec["tablas"]}
    for t in spec["tablas"]:
        fill, stroke = COLORES[t.get("color", "catalogo")]
        x, y, w, h = geom(t)
        celdas.append(
            f'<mxCell id="{t["id"]}" value="{escape(t["nombre"])}" style="swimlane;fontStyle=1;childLayout=stackLayout;'
            f'horizontal=1;startSize={HEADER_H};horizontalStack=0;resizeParent=1;resizeParentMax=0;resizeLast=0;'
            f'collapsible=0;marginBottom=0;html=1;fillColor={fill};strokeColor={stroke};fontSize=12;" vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>'
        )
        for j, col in enumerate(t["columnas"]):
            estilo = "fontStyle=4;" if col.startswith("PK") else ""
            celdas.append(
                f'<mxCell id="{t["id"]}_c{j}" value="{escape(col)}" style="text;strokeColor=none;fillColor=none;'
                f'align=left;verticalAlign=middle;spacingLeft=6;html=1;fontSize=11;{estilo}" vertex="1" parent="{t["id"]}">'
                f'<mxGeometry y="{HEADER_H + j * ROW_H}" width="{w}" height="{ROW_H}" as="geometry"/></mxCell>'
            )
    for k, r in enumerate(spec["relaciones"]):
        pts = r["puntos"]
        ex, ey = fraccion(geom(tablas[r["desde"]]), pts[0])
        nx, ny = fraccion(geom(tablas[r["hacia"]]), pts[-1])
        medios = "".join(f'<mxPoint x="{p[0]}" y="{p[1]}"/>' for p in pts[1:-1])
        celdas.append(
            f'<mxCell id="r{k}" value="{escape(r.get("etiqueta", ""))}" style="edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;'
            f'fontSize=10;labelBackgroundColor=#ffffff;startArrow={r.get("origen", "ERmandOne")};'
            f'endArrow={r.get("destino", "ERzeroToMany")};startFill=0;endFill=0;'
            f'exitX={ex};exitY={ey};exitDx=0;exitDy=0;entryX={nx};entryY={ny};entryDx=0;entryDy=0;" '
            f'edge="1" parent="1" source="{r["desde"]}" target="{r["hacia"]}">'
            f'<mxGeometry relative="1" as="geometry"><Array as="points">{medios}</Array></mxGeometry></mxCell>'
        )
    w, h = lienzo(spec)
    xml = (
        '<mxfile host="app.diagrams.net">\n'
        f'  <diagram name="{escape(spec["titulo"])}" id="reda-eer">\n'
        f'    <mxGraphModel dx="1400" dy="900" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" '
        f'fold="1" page="1" pageScale="1" pageWidth="{w}" pageHeight="{h}" math="0" shadow="0">\n'
        '      <root>\n        ' + "\n        ".join(celdas) + "\n      </root>\n"
        "    </mxGraphModel>\n  </diagram>\n</mxfile>\n"
    )
    with open(ruta, "w", encoding="utf-8") as f:
        f.write(xml)


# ----------------------------------------------------------------------- PNG
def lienzo(spec):
    w = max(t["x"] + t["ancho"] for t in spec["tablas"]) + 40
    h = max(t["y"] + alto(t) for t in spec["tablas"]) + 40
    for gr in spec.get("grupos", []):
        w, h = max(w, gr["x"] + gr["ancho"] + 20), max(h, gr["y"] + gr["alto"] + 20)
    return int(w), int(h) + (40 if spec.get("leyenda") else 0)


def terminacion(ax, p, q, tipo):
    """Dibuja la notación pata de gallo en p (borde de la tabla); q es el punto siguiente."""
    dx, dy = q[0] - p[0], q[1] - p[1]
    d = math.hypot(dx, dy) or 1
    ux, uy, nx, ny = dx / d, dy / d, -dy / d, dx / d
    c, lw = "#333333", 1.1

    def barra(t):
        cx, cy = p[0] + ux * t, p[1] + uy * t
        ax.plot([cx - nx * 6, cx + nx * 6], [cy - ny * 6, cy + ny * 6], color=c, lw=lw, zorder=4)

    def circulo(t):
        ax.add_patch(plt.Circle((p[0] + ux * t, p[1] + uy * t), 4, fc="white", ec=c, lw=lw, zorder=5))

    def pata():
        b = (p[0] + ux * 13, p[1] + uy * 13)
        for s in (-1, 0, 1):
            ax.plot([b[0], p[0] + nx * 7 * s], [b[1], p[1] + ny * 7 * s], color=c, lw=lw, zorder=4)

    if tipo == "ERmandOne":
        barra(7), barra(12)
    elif tipo == "ERone":
        barra(9)
    elif tipo == "ERzeroToOne":
        barra(7), circulo(16)
    elif tipo == "ERmany":
        pata()
    elif tipo == "ERoneToMany":
        pata(), barra(17)
    elif tipo == "ERzeroToMany":
        pata(), circulo(19)


def a_png(spec, ruta):
    w, h = lienzo(spec)
    fig = plt.figure(figsize=(w / 100, h / 100), dpi=160)
    ax = fig.add_axes([0, 0, 1, 1])
    ax.set_xlim(0, w)
    ax.set_ylim(h, 0)
    ax.axis("off")
    fig.patch.set_facecolor("white")

    for gr in spec.get("grupos", []):
        ax.add_patch(Rectangle((gr["x"], gr["y"]), gr["ancho"], gr["alto"], fc=gr["color"],
                               ec="#bbbbbb", ls="--", lw=0.8, zorder=0))
        ax.text(gr["x"] + 10, gr["y"] + 6, gr["texto"], fontsize=8, style="italic",
                color="#666666", va="top", zorder=0)

    for r in spec["relaciones"]:
        pts = r["puntos"]
        ax.plot([p[0] for p in pts], [p[1] for p in pts], color="#333333", lw=1.0, zorder=1)
        terminacion(ax, pts[0], pts[1], r.get("origen", "ERmandOne"))
        terminacion(ax, pts[-1], pts[-2], r.get("destino", "ERzeroToMany"))
        if r.get("etiqueta"):
            # la etiqueta va en el tramo más largo de la ruta
            i = max(range(len(pts) - 1), key=lambda k: math.dist(pts[k], pts[k + 1]))
            mx, my = (pts[i][0] + pts[i + 1][0]) / 2, (pts[i][1] + pts[i + 1][1]) / 2
            ax.text(mx, my, r["etiqueta"], fontsize=6.5, ha="center", va="center", color="#222222",
                    bbox=dict(fc="white", ec="none", pad=0.8), zorder=6)

    for t in spec["tablas"]:
        fill, stroke = COLORES[t.get("color", "catalogo")]
        x, y, tw, th = geom(t)
        ax.add_patch(Rectangle((x, y), tw, th, fc="white", ec=stroke, lw=1.2, zorder=2))
        ax.add_patch(Rectangle((x, y), tw, HEADER_H, fc=fill, ec=stroke, lw=1.2, zorder=3))
        ax.text(x + tw / 2, y + HEADER_H / 2, t["nombre"], fontsize=8.5, weight="bold",
                ha="center", va="center", zorder=4)
        for j, col in enumerate(t["columnas"]):
            ax.text(x + 6, y + HEADER_H + j * ROW_H + ROW_H / 2 + 1, col, fontsize=7,
                    va="center", ha="left", zorder=4,
                    weight="bold" if col.startswith("PK") else "normal")

    if spec.get("leyenda"):
        ax.text(20, h - 20, spec["leyenda"], fontsize=7, color="#444444", va="center")
    fig.savefig(ruta, dpi=160, facecolor="white")
    plt.close(fig)


def validar(spec):
    """Comprueba que cada ruta empiece y termine en el borde de su tabla y sea ortogonal."""
    tablas = {t["id"]: t for t in spec["tablas"]}
    errores = []
    for k, r in enumerate(spec["relaciones"]):
        pts = r["puntos"]
        for (a, b) in zip(pts, pts[1:]):
            if a[0] != b[0] and a[1] != b[1]:
                errores.append(f"relación {k} ({r['desde']}->{r['hacia']}): tramo no ortogonal {a}->{b}")
        for tid, p in ((r["desde"], pts[0]), (r["hacia"], pts[-1])):
            x, y, w, h = geom(tablas[tid])
            en_borde = (p[0] in (x, x + w) and y <= p[1] <= y + h) or (p[1] in (y, y + h) and x <= p[0] <= x + w)
            if not en_borde:
                errores.append(f"relación {k} ({r['desde']}->{r['hacia']}): {p} no está en el borde de {tid} {x, y, w, h}")
    return errores


if __name__ == "__main__":
    with open(sys.argv[1], encoding="utf-8") as f:
        spec = json.load(f)
    errores = validar(spec)
    if errores:
        print("\n".join(errores))
        sys.exit(1)
    a_drawio(spec, sys.argv[2] + ".drawio")
    a_png(spec, sys.argv[2] + ".png")
    print(f"Generados {sys.argv[2]}.drawio y {sys.argv[2]}.png "
          f"({len(spec['tablas'])} tablas, {len(spec['relaciones'])} relaciones)")
