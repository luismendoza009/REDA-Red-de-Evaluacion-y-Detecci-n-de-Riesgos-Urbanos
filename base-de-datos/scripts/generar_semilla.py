"""
Genera los datos semilla SIMULADOS de REDA (sql/semilla/*.sql).

- Determinista: random.seed fija -> siempre produce los mismos archivos.
- Coherente: los estados actuales coinciden con el último cambio del historial, las fechas
  avanzan en orden (hecho <= reporte <= revisión <= validación...), los vínculos de
  ANTECEDENTE se calculan con la misma regla que la consulta de negocio y la API (misma categoría,
  reporte de CONDICION no rechazado, distancia Haversine <= 100 m, fecha anterior al hecho).
- Sin datos reales de personas: nombres combinados al azar, correos en el dominio
  reservado .test, teléfonos ficticios. Los nombres de barrios y entidades son de Valledupar
  y el punto de referencia de cada barrio es real (OpenStreetMap); los reportes se reparten
  alrededor de ese punto. Los atributos de zona y las atribuciones son SIMULADOS
  (ver documentacion/datos_simulados.md).

Uso:  python scripts/generar_semilla.py
"""
import hashlib
import json
import math
import os
import random
from datetime import datetime, timedelta, timezone

random.seed(2026)
TZ = timezone(timedelta(hours=-5))  # hora de Colombia
RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SALIDA = os.path.join(RAIZ, "sql", "semilla")
# Hash BCrypt (costo 10) de la contraseña de prueba "Reda2026*". Solo para datos de prueba.
HASH = "$2a$10$Ddurwe.W/MljJhBgg5s3z..g9R20NWTvvdmVVh7epzSY9cx4u/Ti2"
RADIO_ANTECEDENTE_M = 100
INICIO = datetime(2026, 3, 1, 7, 0, tzinfo=TZ)
FIN = datetime(2026, 10, 20, 18, 0, tzinfo=TZ)


def q(v):
    """Literal SQL."""
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, (int, float)):
        return str(v)
    if isinstance(v, datetime):
        return "'" + v.isoformat(timespec="seconds") + "'"
    return "'" + str(v).replace("'", "''") + "'"


def inserts(tabla, columnas, filas):
    if not filas:
        return ""
    cab = f"INSERT INTO {tabla} ({', '.join(columnas)}) VALUES\n"
    cuerpo = ",\n".join("  (" + ", ".join(q(v) for v in f) + ")" for f in filas)
    return cab + cuerpo + ";\n\n"


def haversine_m(lat1, lon1, lat2, lon2):
    r = 6371000
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp, dl = p2 - p1, math.radians(lon2 - lon1)
    a = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def desplazar(lat, lon, metros):
    """Punto aleatorio a una distancia de hasta 'metros' del punto dado."""
    ang = random.uniform(0, 2 * math.pi)
    d = random.uniform(0, metros)
    dlat = d * math.cos(ang) / 111320
    dlon = d * math.sin(ang) / (111320 * math.cos(math.radians(lat)))
    return round(lat + dlat, 6), round(lon + dlon, 6)


def fecha_aleatoria(a, b):
    s = random.uniform(a.timestamp(), b.timestamp())
    return datetime.fromtimestamp(int(s), TZ)


def hora_diurna(d):
    return d.replace(hour=random.randint(6, 20), minute=random.randint(0, 59), second=random.randint(0, 59))


# ------------------------------------------------------------------ catálogos
ROLES = ["CIUDADANO", "OPERADOR", "ABOGADO", "ADMINISTRADOR", "ANALISTA"]
ESTADOS = ["Pendiente", "En revisión", "Validado", "En proceso", "Solucionado", "Rechazado"]
PEND, REV, VAL, PROC, SOL, RECH = range(1, 7)
ESTADOS_CASO = ["Borrador", "Evaluado", "Remitido", "Aceptado por abogado", "Rechazado", "Cerrado"]
C_BORR, C_EVAL, C_REM, C_ACEP, C_RECH, C_CERR = range(1, 7)

ENTIDADES = [
    ("Alcaldía de Valledupar - Secretaría de Obras Públicas", "SECRETARIA"),
    ("Alcaldía de Valledupar - Secretaría de Tránsito y Transporte", "SECRETARIA"),
    ("Alcaldía de Valledupar - Oficina de Alumbrado Público", "SECRETARIA"),
    ("Alcaldía de Valledupar - Gestión Ambiental", "SECRETARIA"),
    ("EMDUPAR S.A. E.S.P.", "EMPRESA_SERVICIOS"),
    ("Empresa prestadora del servicio de aseo", "EMPRESA_SERVICIOS"),
    ("Gobernación del Cesar - Secretaría de Infraestructura", "GOBERNACION"),
]

# (nombre, descripción, entidad responsable por defecto, plantillas de descripción)
CATEGORIAS = [
    ("Hueco en la vía", "Huecos, hundimientos o pavimento fracturado en calzada.", 1, [
        "Hueco profundo en la calzada que obliga a los carros a frenar en seco",
        "Hundimiento del pavimento de casi un metro de ancho",
        "Hueco tapado con escombros que se vuelve a abrir cuando llueve",
        "Pavimento fracturado con varios huecos seguidos en el carril derecho",
    ]),
    ("Alcantarilla sin tapa u obstruida", "Pozos de inspección destapados, rejillas rotas o sumideros tapados.", 5, [
        "Alcantarilla sin tapa en plena vía, solo la señalizan con una rama",
        "Rejilla del sumidero rota y hundida, un peatón puede meter el pie",
        "Pozo de inspección destapado al lado del andén",
        "Sumidero tapado; cuando llueve el agua cubre la alcantarilla abierta",
    ]),
    ("Luminaria dañada", "Postes de alumbrado público apagados, intermitentes o con cables expuestos.", 3, [
        "Poste de alumbrado apagado hace semanas, la cuadra queda a oscuras",
        "Luminaria intermitente y cable expuesto en la base del poste",
        "Tres postes seguidos sin luz en el corredor peatonal",
        "Poste inclinado con la lámpara colgando",
    ]),
    ("Señalización deteriorada", "Señales de tránsito caídas, ilegibles o demarcación borrada.", 2, [
        "Señal de PARE caída en el cruce, los carros pasan sin detenerse",
        "Paso peatonal totalmente borrado frente a la entrada",
        "Semáforo peatonal sin funcionar en la intersección",
        "Señal de reductor de velocidad tapada por la maleza",
    ]),
    ("Acumulación de residuos", "Basureros satélite o residuos que obstruyen vías y drenajes.", 6, [
        "Basurero satélite que ya ocupa medio carril",
        "Escombros y basura acumulados tapando el canal",
        "Residuos quemados en la esquina, humo constante",
        "Acumulación de basura frente al parque desde hace días",
    ]),
    ("Árbol en riesgo de caída", "Árboles inclinados, ramas secas o raíces que levantan el andén.", 4, [
        "Árbol inclinado sobre los cables, se mueve mucho con el viento",
        "Rama seca grande sobre el andén por donde pasan los niños",
        "Raíces levantaron el andén y hay un desnivel de 20 cm",
        "Árbol con el tronco podrido en la base",
    ]),
    ("Fuga de agua", "Fugas de tubería que deterioran la vía o generan encharcamientos.", 5, [
        "Fuga de agua potable que lleva días y ya erosionó el pavimento",
        "Tubo roto: sale agua a presión debajo del andén",
        "Encharcamiento permanente por fuga, la vía se está hundiendo",
    ]),
    ("Otros", "Andenes destruidos, barandas caídas, puentes peatonales en mal estado y similares.", 1, [
        "Andén destruido, los peatones tienen que bajar a la calzada",
        "Baranda del puente peatonal caída",
        "Tapa de registro eléctrico rota en el andén",
    ]),
]

# (barrio, tipo, latitud, longitud, tráfico, colegios, centros de salud, fuente de la ubicación)
# Coordenadas REALES: punto de referencia dentro del barrio tomado de OpenStreetMap (consulta del 2026-10-26),
# comprobado contra el polígono de su comuna en OSM.
# Tráfico, colegios y centros de salud siguen siendo SIMULADOS.
ZONAS = [
    ("Centro", "COMERCIAL", 10.47765, -73.24455, 5, 4, 3,
     "Plaza Alfonso López (OSM)"),
    ("Loperena", "MIXTA", 10.47351, -73.24883, 4, 2, 1,
     "Colegio Nacional Loperena (OSM)"),
    ("Cañaguate", "RESIDENCIAL", 10.47990, -73.24930, 3, 1, 0,
     "Manzanas Cañahuates (OSM, landuse=residential)"),
    ("Santa Rosa", "RESIDENCIAL", 10.46030, -73.23930, 3, 2, 1,
     "Parque Santa Rosa (OSM)"),
    ("Santo Domingo", "RESIDENCIAL", 10.46959, -73.23802, 3, 1, 0,
     "Colegio Santo Domingo (OSM)"),
    ("Doce de Octubre", "RESIDENCIAL", 10.45710, -73.24501, 3, 2, 0,
     "Plaza 12 de Octubre (OSM)"),
    ("Novalito", "MIXTA", 10.48650, -73.25370, 4, 2, 1,
     "Manzanas Novalito (OSM, landuse=residential)"),
    ("Villa Clara", "RESIDENCIAL", 10.46262, -73.23689, 2, 1, 0,
     "Barrio Villa Clara (OSM, place=neighbourhood)"),
    ("Candelaria Sur", "RESIDENCIAL", 10.46193, -73.23228, 2, 1, 1,
     "Candelaria Sur y Villa del Rosario (OSM, place=city_block)"),
    ("Los Corales", "RESIDENCIAL", 10.48995, -73.27040, 2, 1, 0,
     "Conjunto Los Corales (OSM, landuse=residential)"),
    ("Los Mayales", "RESIDENCIAL", 10.45880, -73.23053, 3, 2, 1,
     "Parque Los Mayales (OSM)"),
    ("Rosario Real", "RESIDENCIAL", 10.49537, -73.27702, 2, 1, 0,
     "Urbanización Rosario Real (OSM, landuse=residential)"),
    ("Villa Bolivariana", "RESIDENCIAL", 10.45133, -73.23832, 3, 2, 1,
     "Unidad Villa Bolivariana (OSM, landuse=residential)"),
    ("Primero de Mayo", "RESIDENCIAL", 10.45397, -73.25294, 4, 3, 1,
     "Casa de Justicia Primero de Mayo (OSM)"),
    ("La Nevada", "RESIDENCIAL", 10.47657, -73.28280, 3, 2, 1,
     "Escuela Mixta y Parque La Nevada (OSM)"),
    ("San Martín", "RESIDENCIAL", 10.45059, -73.24872, 3, 2, 1,
     "Parque San Martín (OSM)"),
    ("Garupal", "RESIDENCIAL", 10.47048, -73.26375, 3, 3, 1,
     "Barrio Garupal (OSM, landuse=residential)"),
    ("Doña Clara", "RESIDENCIAL", 10.45456, -73.22862, 3, 1, 0,
     "Urbanización Doña Clara (OSM, landuse=residential)"),
    ("Universidad Popular del Cesar", "ESCOLAR", 10.44950, -73.26240, 4, 1, 0,
     "UPC sede Sabanas (OSM, amenity=university)"),
    ("Iracal", "RESIDENCIAL", 10.47438, -73.26801, 3, 2, 0,
     "Parque Iracal (OSM)"),
    ("Los Cortijos", "RESIDENCIAL", 10.47632, -73.26188, 2, 1, 1,
     "Parque Los Cortijos (OSM)"),
    ("Don Carmelo", "RESIDENCIAL", 10.44097, -73.26020, 2, 1, 0,
     "Barrio Don Carmelo (OSM, place=neighbourhood)"),
    ("Nuevo Milenio", "RESIDENCIAL", 10.44221, -73.25171, 3, 2, 1,
     "Barrio Milenio (OSM, place=neighbourhood)"),
    ("Verona", "RESIDENCIAL", 10.44528, -73.26427, 2, 1, 0,
     "Urbanización Verona (OSM, landuse=residential)"),
    ("Terminal de Transportes", "INSTITUCIONAL", 10.44436, -73.24302, 5, 0, 0,
     "Terminal de Transporte de Valledupar (OSM, amenity=bus_station)"),
    ("Leandro Díaz", "RESIDENCIAL", 10.43640, -73.26400, 2, 2, 0,
     "Urbanización Leandro Díaz (OSM, landuse=residential)"),
    ("Vive Alto", "RESIDENCIAL", 10.44038, -73.23392, 2, 1, 0,
     "Conjunto Vive Alto (OSM, landuse=residential)"),
    ("Don Alberto", "RESIDENCIAL", 10.46630, -73.28850, 2, 1, 0,
     "Urbanización Norte y Altos de Don Alberto (OSM)"),
    ("Las Trinitarias", "RESIDENCIAL", 10.49801, -73.27514, 3, 2, 1,
     "Las Trinitarias (OSM, landuse=residential)"),
    ("Hospital Rosario Pumarejo", "INSTITUCIONAL", 10.46933, -73.25452, 5, 0, 2,
     "Hospital Rosario Pumarejo de López (OSM, amenity=hospital)"),
]

FECHA_CONSULTA_OSM = "2026-10-26"

# Dispersión alrededor del punto de referencia: corta para que el reporte quede dentro de su barrio
# (el barrio más cercano a otro está a unos 360 m).
RADIO_PUNTO_CRITICO = 80
RADIO_DISPERSO = 170

NOMBRES = ["María", "José", "Ana", "Luis", "Carmen", "Carlos", "Rosa", "Jorge", "Diana", "Andrés",
           "Paola", "Jesús", "Yuliana", "Miguel", "Sandra", "Fabián", "Lorena", "Camilo", "Daniela",
           "Rafael", "Viviana", "Óscar", "Liliana", "Iván", "Katherine", "Alberto", "Marcela", "Elkin"]
APELLIDOS = ["Gutiérrez", "Pérez", "Ospino", "Castro", "Maestre", "Araújo", "Daza", "Mejía", "Quintero",
             "Oñate", "Fuentes", "Romero", "Zuleta", "Barros", "Molina", "Cuello", "Pimienta", "Ariza",
             "Villazón", "Montero", "Arzuaga", "Rincones", "Bolaño", "Gámez"]

DANOS = {
    "LESION": [
        "Caída de un motociclista al pasar sobre la falla; fractura de muñeca y raspaduras",
        "Peatón cayó dentro de la abertura; esguince de tobillo y herida en la pierna",
        "Ciclista perdió el control y sufrió golpe en la cabeza y el hombro",
        "Adulta mayor tropezó de noche por falta de iluminación; fractura de cadera",
    ],
    "DANO_MATERIAL": [
        "Llanta y rin del vehículo destruidos al caer en la falla",
        "Daño en la suspensión delantera del carro, cotización de reparación anexada",
        "Moto con el guardabarros y la dirección dañados",
        "Rama cayó sobre el techo del vehículo estacionado",
    ],
    "AMBOS": [
        "Motociclista cayó en la falla: moto con daños en el tanque y el conductor con laceraciones",
        "Choque al esquivar la falla de noche; vehículo con daños y pasajero con contusiones",
    ],
}
ESPECIALIDADES = ["Responsabilidad del Estado", "Derecho administrativo", "Responsabilidad civil y del Estado"]
PRESENTACIONES = [
    "Litigante en reparación directa contra entidades públicas del Cesar. Atiendo en el centro de Valledupar.",
    "Abogada administrativista. Acompaño a víctimas de fallas en servicios públicos desde la primera consulta.",
    "Más de quince años en responsabilidad del Estado; casos de vías, alcantarillado y alumbrado público.",
    "Abogado joven, cercano y con disponibilidad inmediata para revisar su caso.",
    "Recién vinculado a la red; en verificación de tarjeta profesional.",
]


def direccion():
    return f"Calle {random.randint(5, 45)} con Carrera {random.randint(4, 40)}"


def main():
    os.makedirs(SALIDA, exist_ok=True)

    # ------------------------------------------------------------- usuarios
    usados, usuarios, abogados = set(), [], []

    def nuevo_usuario(rol, registro):
        while True:
            n, a1, a2 = random.choice(NOMBRES), random.choice(APELLIDOS), random.choice(APELLIDOS)
            if (n, a1, a2) not in usados and a1 != a2:
                usados.add((n, a1, a2))
                break
        sin_tilde = str.maketrans("áéíóúñÁÉÍÓÚÑ", "aeiounAEIOUN")
        uid = len(usuarios) + 1
        correo = f"{n}.{a1}{uid}".lower().translate(sin_tilde) + "@correo.test"
        usuarios.append((uid, f"{n} {a1} {a2}", correo, HASH, rol, registro, True, registro))
        return uid

    base = datetime(2026, 2, 1, 9, 0, tzinfo=TZ)
    admin = nuevo_usuario(4, base)
    operadores = [nuevo_usuario(2, base + timedelta(days=i)) for i in range(3)]
    analista = nuevo_usuario(5, base + timedelta(days=5))
    for i in range(5):
        uid = nuevo_usuario(3, base + timedelta(days=10 + i))
        # el quinto abogado queda pendiente de verificación (activo = false) para la demo del administrador
        abogados.append((i + 1, uid, str(random.randint(100000, 399999)), ESPECIALIDADES[i % 3], "Valledupar", i != 4,
                         [12, 7, 15, 4, 2][i], PRESENTACIONES[i]))
    ciudadanos = [nuevo_usuario(1, fecha_aleatoria(base, INICIO + timedelta(days=60))) for _ in range(45)]
    registro_de = {u[0]: u[5] for u in usuarios}

    # ------------------------------------------------------------- reportes
    reportes, evidencias, evaluaciones, historial = [], [], [], []
    incidentes, testigos = [], []

    def ciudadano_antes(fecha):
        candidatos = [c for c in ciudadanos if registro_de[c] < fecha]
        return random.choice(candidatos)

    def nuevo_reporte(cat, zona, tipo, lat, lon, fecha, ciclo):
        rid = len(reportes) + 1
        plantilla = random.choice(CATEGORIAS[cat - 1][3])
        desc = f"{plantilla}. {direccion()}, barrio {ZONAS[zona - 1][0]}."
        grav = None if random.random() < 0.15 else (random.randint(3, 5) if tipo == "INCIDENTE" else random.randint(1, 5))
        reportes.append([rid, ciudadano_antes(fecha), cat, zona, PEND, tipo, desc, lat, lon, fecha, grav])
        # evidencias: ~85 % de los reportes tienen al menos una foto
        n_fotos = 0 if random.random() < 0.15 else random.choice([1, 1, 2, 2, 3])
        if tipo == "INCIDENTE":
            n_fotos = max(n_fotos, 1)
        for _ in range(n_fotos):
            eid = len(evidencias) + 1
            semilla = f"evidencia-{eid}-{rid}"
            hexname = hashlib.md5(semilla.encode()).hexdigest()
            captura = fecha - timedelta(minutes=random.randint(1, 600))
            evidencias.append((eid, rid, f"evidencias/{fecha:%Y/%m}/{hexname}.jpg", "image/jpeg",
                               random.randint(250_000, 4_500_000), hashlib.sha256(semilla.encode()).hexdigest(),
                               captura if random.random() < 0.8 else None, fecha + timedelta(seconds=random.randint(5, 90))))
        aplicar_ciclo(rid, fecha, ciclo)
        return rid

    def aplicar_ciclo(rid, fecha, ciclo):
        """ciclo = lista de estados recorridos después de Pendiente."""
        actual, t = PEND, fecha
        for nuevo in ciclo:
            t = t + timedelta(hours=random.randint(6, 24 * 12))
            if t > FIN + timedelta(days=4):
                break
            op = random.choice(operadores)
            obs = {
                REV: "Se revisan foto y ubicación.",
                VAL: "Falla verificada con la evidencia aportada.",
                PROC: "Entidad notificada; reporta intervención en curso.",
                SOL: "Ciudadano confirma la reparación.",
                RECH: random.choice(["Reporte duplicado.", "La foto no corresponde a la ubicación.", "No es infraestructura pública."]),
            }[nuevo]
            historial.append([len(historial) + 1, rid, actual, nuevo, op, t, obs])
            if nuevo == VAL:
                evaluaciones.append([len(evaluaciones) + 1, rid, op, random.randint(30, 95),
                                     random.randint(20, 95), random.randint(10, 90), t])
            actual = nuevo
        reportes[rid - 1][4] = actual

    def ciclo_condicion(fecha, atendible=True):
        edad = (FIN - fecha).days
        if edad < 7:
            return random.choice([[], [REV]])
        rechazado = random.random() < 0.08
        if not atendible:  # el punto crítico: la entidad no lo arregla y el reporte es legítimo
            return random.choice([[REV], [REV, VAL], [REV, VAL]])
        if rechazado:
            return [REV, RECH]
        return random.choice([[REV], [REV, VAL], [REV, VAL, PROC], [REV, VAL, PROC, SOL], [REV, VAL, PROC, SOL]])

    # 1) Puntos críticos: varias condiciones previas en el mismo punto y luego un incidente.
    #    (zona, categoría, nº de antecedentes, fecha del primero, fecha del hecho)
    PUNTOS = [
        (1, 2, 4, datetime(2026, 5, 19, 8, 10, tzinfo=TZ), None),                    # demo: Centro, alcantarilla
        (14, 1, 3, datetime(2026, 3, 22, 7, 30, tzinfo=TZ), datetime(2026, 7, 14, 19, 40, tzinfo=TZ)),
        (7, 3, 3, datetime(2026, 4, 2, 21, 0, tzinfo=TZ), datetime(2026, 8, 3, 22, 15, tzinfo=TZ)),
        (19, 4, 2, datetime(2026, 4, 28, 12, 0, tzinfo=TZ), datetime(2026, 8, 21, 7, 5, tzinfo=TZ)),
        (16, 1, 5, datetime(2026, 3, 9, 9, 45, tzinfo=TZ), datetime(2026, 9, 2, 18, 30, tzinfo=TZ)),
        (12, 6, 2, datetime(2026, 6, 10, 15, 20, tzinfo=TZ), datetime(2026, 9, 18, 16, 0, tzinfo=TZ)),
        (25, 2, 3, datetime(2026, 4, 15, 10, 0, tzinfo=TZ), datetime(2026, 7, 30, 20, 50, tzinfo=TZ)),
        (2, 7, 2, datetime(2026, 6, 1, 6, 30, tzinfo=TZ), datetime(2026, 9, 27, 5, 45, tzinfo=TZ)),
        (30, 1, 3, datetime(2026, 3, 30, 11, 0, tzinfo=TZ), datetime(2026, 8, 11, 13, 10, tzinfo=TZ)),
    ]
    puntos_incidente = []  # (zona, cat, lat, lon, fecha_hecho, es_demo)
    for zona, cat, n, primero, hecho in PUNTOS:
        z = ZONAS[zona - 1]
        clat, clon = desplazar(z[2], z[3], RADIO_PUNTO_CRITICO)
        es_demo = hecho is None
        if es_demo:
            hecho = datetime(2026, 10, 21, 19, 20, tzinfo=TZ)
        fechas = sorted([primero] + [fecha_aleatoria(primero + timedelta(days=10), hecho - timedelta(days=5)) for _ in range(n - 1)])
        for f in fechas:
            lat, lon = desplazar(clat, clon, 35)
            nuevo_reporte(cat, zona, "CONDICION", lat, lon, hora_diurna(f) if f != primero else f,
                          ciclo_condicion(f, atendible=False))
        puntos_incidente.append((zona, cat, clat, clon, hecho, es_demo))

    # 2) Condiciones dispersas por la ciudad.
    pesos_cat = [24, 14, 14, 10, 12, 8, 10, 8]
    for _ in range(92):
        cat = random.choices(range(1, 9), weights=pesos_cat)[0]
        zona = random.randint(1, len(ZONAS))
        z = ZONAS[zona - 1]
        lat, lon = desplazar(z[2], z[3], RADIO_DISPERSO)
        f = hora_diurna(fecha_aleatoria(INICIO, FIN))
        nuevo_reporte(cat, zona, "CONDICION", lat, lon, f, ciclo_condicion(f))

    # 3) Incidentes aislados (sin antecedentes o con pocos): casos más débiles, para contraste.
    for _ in range(9):
        cat = random.choices(range(1, 9), weights=pesos_cat)[0]
        zona = random.randint(1, len(ZONAS))
        z = ZONAS[zona - 1]
        lat, lon = desplazar(z[2], z[3], RADIO_DISPERSO)
        puntos_incidente.append((zona, cat, lat, lon, fecha_aleatoria(datetime(2026, 4, 1, tzinfo=TZ), FIN - timedelta(days=3)), False))

    # Los incidentes se insertan al final, en orden cronológico del hecho.
    puntos_incidente.sort(key=lambda p: p[4])
    id_incidente_demo = None
    for zona, cat, lat, lon, hecho, es_demo in puntos_incidente:
        ilat, ilon = desplazar(lat, lon, 20)
        freporte = hecho + timedelta(hours=random.randint(2, 60)) if not es_demo else datetime(2026, 10, 22, 8, 35, tzinfo=TZ)
        ciclo = [] if es_demo else random.choice([[REV, VAL], [REV, VAL], [REV]])
        rid = nuevo_reporte(cat, zona, "INCIDENTE", ilat, ilon, freporte, ciclo)
        tipo = random.choices(["LESION", "DANO_MATERIAL", "AMBOS"], weights=[45, 35, 20])[0]
        if es_demo:
            tipo = "LESION"
        atencion = tipo != "DANO_MATERIAL" and (es_demo or random.random() < 0.75)
        desc = random.choice(DANOS[tipo])
        if es_demo:
            desc = "Peatón cayó de noche en la alcantarilla sin tapa; fractura de tibia, atendido en urgencias"
        iid = len(incidentes) + 1
        incidentes.append([iid, rid, "INCIDENTE", CATEGORIAS[cat - 1][2] if random.random() < 0.9 or es_demo else None,
                           hecho, tipo, desc, atencion])
        if es_demo:
            id_incidente_demo = iid
        for _ in range(2 if es_demo else random.choice([0, 0, 1, 1, 2])):
            n = f"{random.choice(NOMBRES)} {random.choice(APELLIDOS)}"
            if any(t[1] == iid and t[2] == n for t in testigos):
                continue
            testigos.append((len(testigos) + 1, iid, n, f"300{random.randint(1000000, 9999999)}"))

    # ------------------------------------------------------- antecedentes (misma regla que la consulta)
    relacionados = []
    por_id = {r[0]: r for r in reportes}
    for inc in incidentes:
        ri = por_id[inc[1]]
        for rc in reportes:
            if rc[5] != "CONDICION" or rc[2] != ri[2] or rc[4] == RECH:
                continue  # solo condiciones de la misma categoría y NO rechazadas
            if rc[9] >= inc[4]:
                continue
            if haversine_m(float(ri[7]), float(ri[8]), float(rc[7]), float(rc[8])) <= RADIO_ANTECEDENTE_M:
                relacionados.append((ri[0], rc[0], "ANTECEDENTE", "AUTOMATICO", ri[9] + timedelta(minutes=1)))
    # algunos SIMILAR marcados a mano por operadores (condiciones duplicadas en la misma zona y categoría)
    condiciones = [r for r in reportes if r[5] == "CONDICION"]
    for a in condiciones:
        for b in condiciones:
            if a[0] < b[0] and a[2] == b[2] and a[3] == b[3] and \
               haversine_m(float(a[7]), float(a[8]), float(b[7]), float(b[8])) <= 60 and random.random() < 0.5:
                relacionados.append((b[0], a[0], "SIMILAR", "MANUAL", b[9] + timedelta(hours=random.randint(6, 48))))

    # --------------------------------------------------------------- casos y remisiones
    ante_por_reporte = {}
    for r in relacionados:
        if r[2] == "ANTECEDENTE":
            ante_por_reporte[r[0]] = ante_por_reporte.get(r[0], 0) + 1
    casos, remisiones, postulaciones, calificaciones = [], [], [], []
    COMENTARIOS = {
        5: ["Muy claro desde la primera cita; me explicó cada paso y estuvo pendiente todo el tiempo.",
            "Excelente. Usó los reportes previos del punto y la entidad aceptó conciliar."],
        4: ["Buen acompañamiento, aunque a veces tardaba en responder los mensajes.",
            "Conoce bien estos procesos. Recomendado."],
        3: ["Cumplió, pero esperaba más comunicación durante el proceso."],
    }
    # Flujo (migración 010): el ciudadano pide abogado -> el caso queda "Remitido" (publicado a los
    # abogados) -> varios se postulan -> el ciudadano ELIGE uno -> "Aceptado por abogado" y se crea la
    # remisión ACEPTADA, que es lo que se le cobra al abogado. Reparto fijo de estados (no al azar)
    # para que la demo siempre muestre casos buscando abogado, con abogado y cerrados.
    CICLO_ESTADOS_CASO = [C_CERR, C_ACEP, C_CERR, C_REM, C_CERR, C_ACEP, C_REM, C_RECH, C_EVAL]
    tarifa = {"LESION": 200000, "AMBOS": 200000, "DANO_MATERIAL": 120000}
    MENSAJES = [
        "Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. "
        "Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.",
        "He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso "
        "sin costo inicial y reviso con usted la evidencia y los plazos.",
        "Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho "
        "a demostrar que la entidad conocía la falla. Me interesa acompañarlo.",
    ]
    for inc in incidentes:
        iid, rid = inc[0], inc[1]
        rep = por_id[rid]
        if iid == id_incidente_demo or rep[4] != VAL:
            continue
        fuerte = ante_por_reporte.get(rid, 0) >= 2 or inc[5] != "DANO_MATERIAL"
        if not fuerte and random.random() < 0.6:
            continue
        validacion = [h for h in historial if h[1] == rid and h[3] == VAL][-1]
        creacion = validacion[5] + timedelta(hours=random.randint(2, 48))
        if creacion > FIN:
            continue
        cid = len(casos) + 1
        estado = CICLO_ESTADOS_CASO[len(casos) % len(CICLO_ESTADOS_CASO)]
        cierre = None
        activos = [a for a in abogados if a[5]]
        random.shuffle(activos)
        if estado in (C_REM, C_ACEP, C_CERR):
            interesados = activos[:random.choice([2, 3, 3])]
            momento = creacion
            filas = []
            for k, a in enumerate(interesados):
                momento = momento + timedelta(hours=random.randint(3, 30))
                filas.append([None, cid, a[0], MENSAJES[k % len(MENSAJES)], "PENDIENTE", momento, None])
            # un caso reciente no alcanza a avanzar: solo ocurre lo que cabe antes del fin de la simulación
            filas = [fl for fl in filas if fl[5] <= FIN]
            if estado in (C_ACEP, C_CERR):
                eleccion = filas[-1][5] + timedelta(hours=random.randint(2, 48)) if filas else FIN + timedelta(days=1)
                if eleccion > FIN:
                    estado = C_REM if filas else C_EVAL
            if estado in (C_ACEP, C_CERR):
                elegida = random.randrange(len(filas))
                for k, fila in enumerate(filas):
                    fila[4] = "ELEGIDA" if k == elegida else "NO_ELEGIDA"
                    fila[6] = eleccion
                remisiones.append([None, cid, filas[elegida][2], eleccion, "ACEPTADA", eleccion, None, tarifa[inc[5]]])
                if estado == C_CERR:
                    cierre = eleccion + timedelta(days=random.randint(15, 40))
                    if cierre > FIN:
                        estado, cierre = C_ACEP, None
                if estado == C_CERR:
                    # el ciudadano califica al abogado unos días después del cierre
                    puntaje = random.choice([5, 5, 4, 4, 3])
                    calificaciones.append([None, cid, filas[elegida][2], puntaje,
                                           random.choice(COMENTARIOS[puntaje]) if random.random() < 2 else None,  # siempre con comentario (el sorteo se conserva solo para que la semilla siga siendo la misma que la versión verificada)
                                           min(cierre + timedelta(days=random.randint(1, 4)), FIN)])
            postulaciones.extend(filas)
        elif estado == C_RECH:
            # ningún abogado se interesó en el plazo: el caso se cierra sin abogado
            cierre = min(creacion + timedelta(days=30), FIN)
        casos.append((cid, iid, estado, validacion[4], creacion, cierre,
                      "Incidente con antecedentes previos en el punto." if ante_por_reporte.get(rid, 0) else "Incidente sin antecedentes registrados.",
                      creacion))
    # el caso cerrado más reciente queda SIN calificar, para calificarlo en vivo en la demo
    calificaciones.sort(key=lambda c: c[5])
    calificaciones = calificaciones[:-1]
    for k, c in enumerate(calificaciones, 1):
        c[0] = k
    for lista in (remisiones, postulaciones):
        lista.sort(key=lambda r: r[3] if lista is remisiones else r[5])
        for k, r in enumerate(lista, 1):
            r[0] = k

    # Desde aquí no se usa random: lo que sigue se agregó después y no debe cambiar los datos anteriores.
    # --------------------------------------------------------------- más casos cerrados y calificados
    # Para tener 5 calificaciones: se cierra el caso 2, se califica el 5 y se abre (y cierra) un caso para el
    # incidente validado del reporte 125, que no tenía. La demo de "calificar en vivo" pasa al caso 6.
    def cerrar_y_calificar(cid, dias_cierre, puntaje, comentario):
        k = next(i for i, c in enumerate(casos) if c[0] == cid)
        c = casos[k]
        rem = next(r for r in remisiones if r[1] == cid and r[4] == "ACEPTADA")
        cierre = c[5] or rem[5] + timedelta(days=dias_cierre)
        if c[5] is None:
            casos[k] = (cid, c[1], C_CERR, c[3], c[4], cierre, "Conciliación con la entidad; el caso termina.", c[7])
        calificaciones.append([len(calificaciones) + 1, cid, rem[2], puntaje, comentario, cierre + timedelta(days=2)])

    inc6 = next(i for i in incidentes if i[1] == 125)
    validacion = [h for h in historial if h[1] == 125 and h[3] == VAL][-1]
    cid = len(casos) + 1
    creacion = validacion[5] + timedelta(hours=20)
    casos.append((cid, inc6[0], C_ACEP, validacion[4], creacion, None,
                  "Incidente con antecedentes previos en el punto." if ante_por_reporte.get(125, 0) else "Incidente sin antecedentes registrados.",
                  creacion))
    eleccion = creacion + timedelta(days=2)
    for k, (id_abogado, horas, estado) in enumerate([(1, 5, "ELEGIDA"), (4, 26, "NO_ELEGIDA")]):
        postulaciones.append([len(postulaciones) + 1, cid, id_abogado, MENSAJES[k], estado, creacion + timedelta(hours=horas), eleccion])
    remisiones.append([len(remisiones) + 1, cid, 1, eleccion, "ACEPTADA", eleccion, None, tarifa[inc6[5]]])

    cerrar_y_calificar(2, 35, 5, COMENTARIOS[5][0])
    cerrar_y_calificar(5, 0, 3, COMENTARIOS[3][0])
    cerrar_y_calificar(cid, 30, 5, COMENTARIOS[5][1])

    # --------------------------------------------------------------- cálculos del IRU (versión 1, vigente)
    # Uno por reporte evaluado, en el momento de la evaluación, con la fórmula descrita en parametros_indice.
    PESOS_IRU = {"gravedad_evaluada": 0.25, "reportes_cercanos": 0.20, "antiguedad_sin_atencion": 0.20,
                 "trafico_zona": 0.15, "cercania_colegios_salud": 0.10, "historial_incidentes_zona": 0.10}
    hecho_de = {i[1]: i[4] for i in incidentes}
    calculos = []
    for ev in evaluaciones:
        rep, t = por_id[ev[1]], ev[6]
        z = ZONAS[rep[3] - 1]
        cercanos = sum(1 for o in reportes if o[0] != rep[0] and o[2] == rep[2] and o[9] <= t and
                       haversine_m(float(rep[7]), float(rep[8]), float(o[7]), float(o[8])) <= RADIO_ANTECEDENTE_M)
        inc_zona = sum(1 for o in reportes if o[5] == "INCIDENTE" and o[3] == rep[3] and
                       t - timedelta(days=365) <= hecho_de[o[0]] <= t)
        normalizado = {
            "gravedad_evaluada": (ev[3] + ev[4]) / 2,
            "reportes_cercanos": min(100, cercanos * 20),
            "antiguedad_sin_atencion": min(100, (t - rep[9]).days / 180 * 100),
            "trafico_zona": (z[4] - 1) * 25,
            "cercania_colegios_salud": min(100, (z[5] + z[6]) * 20),
            "historial_incidentes_zona": min(100, inc_zona * 20),
        }
        desglose = {v: {"valor_normalizado": round(n, 2), "peso": PESOS_IRU[v], "aporte": round(n * PESOS_IRU[v], 2)}
                    for v, n in normalizado.items()}
        valor = min(100, round(sum(d["aporte"] for d in desglose.values()), 2))
        calculos.append((len(calculos) + 1, rep[0], 1, valor, json.dumps(desglose, ensure_ascii=False),
                         t + timedelta(minutes=1)))

    # --------------------------------------------------------------- tokens de recuperación
    # Solo se guarda el SHA-256 del token. Todos están vencidos (duran 30 min); unos se usaron y otros no.
    tokens = []
    for k, (dias, usado) in enumerate([(95, True), (110, False), (130, True), (150, True), (171, False), (198, True)], 1):
        uid = ciudadanos[k * 6]
        creado = INICIO + timedelta(days=dias, hours=k)
        tokens.append((k, uid, hashlib.sha256(f"token-demo-{k}".encode()).hexdigest(), creado,
                       creado + timedelta(minutes=30), creado + timedelta(minutes=4 + k) if usado else None))

    # --------------------------------------------------------------- archivos
    cab = ("-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).\n"
           "-- Ninguna persona es real. Ver documentacion/datos_simulados.md\n\n")

    with open(os.path.join(SALIDA, "01_catalogos.sql"), "w", encoding="utf-8") as f:
        f.write(cab)
        f.write(inserts("roles", ["id_rol", "nombre"], [(i, n) for i, n in enumerate(ROLES, 1)]))
        f.write(inserts("estados", ["id_estado", "nombre"], [(i, n) for i, n in enumerate(ESTADOS, 1)]))
        f.write(inserts("estados_caso", ["id_estado_caso", "nombre"], [(i, n) for i, n in enumerate(ESTADOS_CASO, 1)]))
        f.write(inserts("categorias", ["id_categoria", "nombre", "descripcion"],
                        [(i, c[0], c[1]) for i, c in enumerate(CATEGORIAS, 1)]))
        f.write(inserts("zonas", ["id_zona", "nombre", "tipo", "ciudad", "nivel_trafico", "centros_educativos", "centros_salud",
                                  "latitud_referencia", "longitud_referencia", "fuente_ubicacion"],
                        [(i, z[0], z[1], "Valledupar", z[4], z[5], z[6], z[2], z[3], f"{z[7]}, consultado el {FECHA_CONSULTA_OSM}")
                         for i, z in enumerate(ZONAS, 1)]))
        f.write(inserts("entidades_publicas", ["id_entidad", "nombre", "tipo", "ciudad"],
                        [(i, e[0], e[1], "Valledupar") for i, e in enumerate(ENTIDADES, 1)]))
        v = datetime(2026, 2, 1, 8, 0, tzinfo=TZ)
        f.write(inserts("versiones_indice", ["id_version", "nombre_indice", "numero_version", "descripcion", "vigente", "fecha_creacion"], [
            (1, "IRU", 1, "Pesos iniciales por criterio del equipo; pendientes de validación con expertos (AHP).", True, v),
            (2, "IVC", 1, "Pesos iniciales por criterio del equipo; pendientes de validación con abogados (AHP).", True, v),
            # propuestas en estudio (no vigentes): muestran que los pesos se versionan sin perder los anteriores
            (3, "IRU", 2, "Propuesta: más peso a la gravedad evaluada por el operador. En estudio.", False, v + timedelta(days=150)),
            (4, "IVC", 2, "Propuesta: más peso a los antecedentes previos, que prueban que la entidad sabía. En estudio.", False, v + timedelta(days=160)),
            (5, "IRU", 3, "Propuesta: más peso a los reportes cercanos y menos al tráfico (dato simulado). En estudio.", False, v + timedelta(days=230)),
        ]))
        f.write(inserts("parametros_indice", ["id_version", "variable", "peso", "descripcion"], [
            (1, "gravedad_evaluada", 0.25, "Promedio de gravedad e impacto de la última evaluación del operador (0-100)."),
            (1, "reportes_cercanos", 0.20, "Reportes de la misma categoría a <= 100 m; 5 o más = 100."),
            (1, "antiguedad_sin_atencion", 0.20, "Días sin solucionarse; 180 o más = 100."),
            (1, "trafico_zona", 0.15, "Nivel de tráfico de la zona 1-5 llevado a 0-100. [SIMULADO]"),
            (1, "cercania_colegios_salud", 0.10, "Colegios + centros de salud en la zona; 5 o más = 100. [SIMULADO]"),
            (1, "historial_incidentes_zona", 0.10, "Incidentes en la zona en los últimos 12 meses; 5 o más = 100."),
            (2, "dano_documentado", 0.20, "LESION o AMBOS = 100; DANO_MATERIAL = 60."),
            (2, "calidad_evidencia", 0.15, "Fotos con hash y fecha de captura; 3 o más = 100."),
            (2, "antecedentes_previos", 0.20, "Reportes de condición previos a <= 100 m; 4 o más = 100."),
            (2, "antiguedad_primer_antecedente", 0.15, "Días entre el primer antecedente y el hecho; 120 o más = 100."),
            (2, "atencion_medica", 0.10, "Hubo atención médica registrada = 100; si no = 0."),
            (2, "testigos", 0.10, "Número de testigos; 2 o más = 100."),
            (2, "oportunidad_plazo", 0.10, "Tiempo restante del plazo de caducidad (por verificar con abogado); recién ocurrido = 100."),
            (3, "gravedad_evaluada", 0.30, "Igual que en la versión 1."),
            (3, "reportes_cercanos", 0.20, "Igual que en la versión 1."),
            (3, "antiguedad_sin_atencion", 0.15, "Igual que en la versión 1."),
            (3, "trafico_zona", 0.15, "Igual que en la versión 1. [SIMULADO]"),
            (3, "cercania_colegios_salud", 0.10, "Igual que en la versión 1. [SIMULADO]"),
            (3, "historial_incidentes_zona", 0.10, "Igual que en la versión 1."),
            (4, "dano_documentado", 0.20, "Igual que en la versión 1."),
            (4, "calidad_evidencia", 0.10, "Igual que en la versión 1."),
            (4, "antecedentes_previos", 0.25, "Igual que en la versión 1."),
            (4, "antiguedad_primer_antecedente", 0.15, "Igual que en la versión 1."),
            (4, "atencion_medica", 0.10, "Igual que en la versión 1."),
            (4, "testigos", 0.10, "Igual que en la versión 1."),
            (4, "oportunidad_plazo", 0.10, "Igual que en la versión 1."),
            (5, "gravedad_evaluada", 0.25, "Igual que en la versión 1."),
            (5, "reportes_cercanos", 0.25, "Igual que en la versión 1."),
            (5, "antiguedad_sin_atencion", 0.20, "Igual que en la versión 1."),
            (5, "trafico_zona", 0.10, "Igual que en la versión 1. [SIMULADO]"),
            (5, "cercania_colegios_salud", 0.10, "Igual que en la versión 1. [SIMULADO]"),
            (5, "historial_incidentes_zona", 0.10, "Igual que en la versión 1."),
        ]))

    with open(os.path.join(SALIDA, "02_usuarios.sql"), "w", encoding="utf-8") as f:
        f.write(cab + "-- Contraseña de todos los usuarios de prueba: Reda2026*\n\n")
        f.write(inserts("usuarios", ["id_usuario", "nombre", "correo", "password_hash", "id_rol", "fecha_registro", "activo", "fecha_consentimiento"], usuarios))
        f.write(inserts("abogados", ["id_abogado", "id_usuario", "tarjeta_profesional", "especialidad", "ciudad", "activo",
                                     "anios_experiencia", "presentacion"], abogados))
        f.write(inserts("tokens_recuperacion", ["id_token", "id_usuario", "hash_token", "fecha_creacion", "fecha_expiracion",
                                                "fecha_uso"], tokens))

    with open(os.path.join(SALIDA, "03_reportes.sql"), "w", encoding="utf-8") as f:
        f.write(cab)
        f.write(inserts("reportes", ["id_reporte", "id_usuario", "id_categoria", "id_zona", "id_estado", "tipo_reporte",
                                     "descripcion", "latitud", "longitud", "fecha_reporte", "nivel_gravedad"], reportes))
        f.write(inserts("evidencias", ["id_evidencia", "id_reporte", "ruta_imagen", "tipo_mime", "tamano_bytes", "hash_sha256",
                                       "fecha_captura", "fecha_carga"], evidencias))
        f.write(inserts("historial_estados", ["id_historial", "id_reporte", "id_estado_anterior", "id_estado_nuevo", "id_usuario",
                                              "fecha_cambio", "observacion"], historial))
        f.write(inserts("evaluaciones", ["id_evaluacion", "id_reporte", "id_usuario", "gravedad", "impacto", "frecuencia",
                                         "fecha_evaluacion"], evaluaciones))
        f.write(inserts("calculos_indice", ["id_calculo", "id_reporte", "id_version", "valor", "desglose", "fecha_calculo"],
                        calculos))

    with open(os.path.join(SALIDA, "04_incidentes.sql"), "w", encoding="utf-8") as f:
        f.write(cab)
        f.write(inserts("incidentes", ["id_incidente", "id_reporte", "tipo_reporte", "id_entidad", "fecha_hora_hecho", "tipo_dano",
                                       "descripcion_dano", "hubo_atencion_medica"], incidentes))
        f.write(inserts("testigos", ["id_testigo", "id_incidente", "nombre", "contacto"], testigos))
        f.write(inserts("reportes_relacionados", ["id_reporte", "id_reporte_relacionado", "tipo_relacion", "origen", "fecha_vinculo"], relacionados))

    with open(os.path.join(SALIDA, "05_casos.sql"), "w", encoding="utf-8") as f:
        f.write(cab)
        f.write(inserts("casos", ["id_caso", "id_incidente", "id_estado_caso", "id_operador", "fecha_creacion", "fecha_cierre",
                                  "observacion", "fecha_consentimiento"], casos))
        f.write(inserts("postulaciones", ["id_postulacion", "id_caso", "id_abogado", "mensaje", "estado", "fecha_postulacion",
                                          "fecha_resolucion"], postulaciones))
        f.write(inserts("calificaciones", ["id_calificacion", "id_caso", "id_abogado", "puntaje", "comentario", "fecha"],
                        calificaciones))
        f.write(inserts("remisiones", ["id_remision", "id_caso", "id_abogado", "fecha_envio", "estado_remision", "fecha_respuesta",
                                       "motivo_rechazo", "valor_cobrado"], remisiones))

    with open(os.path.join(SALIDA, "06_notificaciones.sql"), "w", encoding="utf-8") as f:
        f.write(cab)
        f.write("""-- Cinco avisos para autores distintos; incluye estados leídos y pendientes.
WITH candidatas AS (
    SELECT h.id_historial,
           r.id_usuario,
           h.fecha_cambio,
           row_number() OVER (PARTITION BY r.id_usuario ORDER BY h.fecha_cambio DESC, h.id_historial DESC) AS por_usuario
    FROM historial_estados h
    JOIN reportes r USING (id_reporte)
),
seleccionadas AS (
    SELECT id_historial, id_usuario, fecha_cambio
    FROM candidatas
    WHERE por_usuario = 1
    ORDER BY id_usuario
    LIMIT 5
),
numeradas AS (
    SELECT id_historial, id_usuario, fecha_cambio,
           row_number() OVER (ORDER BY id_usuario) AS numero
    FROM seleccionadas
)
INSERT INTO notificaciones (id_usuario, id_historial, fecha_creacion, fecha_lectura)
SELECT id_usuario,
       id_historial,
       fecha_cambio,
       CASE WHEN numero <= 2 THEN NULL ELSE fecha_cambio + interval '1 minute' END
FROM numeradas;
""")

    with open(os.path.join(SALIDA, "07_bloqueos_login.sql"), "w", encoding="utf-8") as f:
        f.write(cab)
        f.write("""INSERT INTO bloqueos_login (correo, intentos_fallidos, bloqueado_hasta, bloqueado_permanente) VALUES
  ('prueba.bloqueo.nuevo@reda.test', 1, NULL, false),
  ('prueba.bloqueo.primero@reda.test', 5, now() + interval '3 minutes', false),
  ('prueba.bloqueo.segundo@reda.test', 10, now() + interval '10 minutes', false),
  ('prueba.bloqueo.casi@reda.test', 14, now() + interval '10 minutes', false),
  ('prueba.bloqueo.permanente@reda.test', 15, NULL, true);
""")

    with open(os.path.join(SALIDA, "99_secuencias.sql"), "w", encoding="utf-8") as f:
        f.write("-- La semilla inserta IDs explícitos; se ajustan las secuencias para que los\n"
                "-- siguientes INSERT de la aplicación no choquen con ellos.\nDO $$\nBEGIN\n")
        for t, c in [("roles", "id_rol"), ("estados", "id_estado"), ("estados_caso", "id_estado_caso"),
                     ("categorias", "id_categoria"), ("zonas", "id_zona"), ("entidades_publicas", "id_entidad"),
                     ("usuarios", "id_usuario"), ("abogados", "id_abogado"), ("reportes", "id_reporte"),
                     ("evidencias", "id_evidencia"), ("evaluaciones", "id_evaluacion"), ("historial_estados", "id_historial"),
                     ("incidentes", "id_incidente"), ("testigos", "id_testigo"), ("casos", "id_caso"),
                     ("remisiones", "id_remision"), ("versiones_indice", "id_version"), ("calculos_indice", "id_calculo"),
                     ("postulaciones", "id_postulacion"), ("tokens_recuperacion", "id_token"),
                     ("calificaciones", "id_calificacion")]:
            f.write(f"  PERFORM setval(pg_get_serial_sequence('{t}', '{c}'), coalesce((SELECT max({c}) FROM {t}), 0) + 1, false);\n")
        f.write("END $$;\n")

    print(f"usuarios={len(usuarios)} reportes={len(reportes)} (incidentes={len(incidentes)}) evidencias={len(evidencias)} "
          f"historial={len(historial)} evaluaciones={len(evaluaciones)} relacionados={len(relacionados)} "
          f"testigos={len(testigos)} casos={len(casos)} remisiones={len(remisiones)} postulaciones={len(postulaciones)} calificaciones={len(calificaciones)}")
    print(f"incidente demo: id_incidente={id_incidente_demo}")


if __name__ == "__main__":
    main()
