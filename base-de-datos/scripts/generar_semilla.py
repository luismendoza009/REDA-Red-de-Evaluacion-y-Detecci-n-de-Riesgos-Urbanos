"""
Genera los datos semilla SIMULADOS de REDA (sql/semilla/*.sql).

- Determinista: random.seed fija -> siempre produce los mismos archivos.
- Coherente: los estados actuales coinciden con el último cambio del historial, las fechas
  avanzan en orden (hecho <= reporte <= revisión <= validación...), los vínculos de
  ANTECEDENTE se calculan con la misma regla que la consulta de negocio (misma categoría,
  reporte de CONDICION, distancia Haversine <= 100 m, fecha anterior al hecho).
- Sin datos reales de personas: nombres combinados al azar, correos en el dominio
  reservado .test, teléfonos ficticios. Los nombres de barrios y entidades sí son de
  Valledupar, pero las coordenadas, atributos de zona y atribuciones son SIMULADOS
  (ver documentacion/datos_simulados.md).

Uso:  python scripts/generar_semilla.py
"""
import hashlib
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

# (barrio, tipo, latitud, longitud, tráfico, colegios, centros de salud) — coordenadas APROXIMADAS SIMULADAS
ZONAS = [
    ("Centro", "COMERCIAL", 10.4770, -73.2445, 5, 4, 3),
    ("Loperena", "MIXTA", 10.4745, -73.2480, 4, 2, 1),
    ("Cañaguate", "RESIDENCIAL", 10.4805, -73.2420, 3, 1, 0),
    ("San Joaquín", "RESIDENCIAL", 10.4710, -73.2405, 3, 2, 1),
    ("Altagracia", "RESIDENCIAL", 10.4795, -73.2475, 3, 1, 0),
    ("Gaitán", "RESIDENCIAL", 10.4690, -73.2470, 3, 2, 0),
    ("Novalito", "MIXTA", 10.4850, -73.2500, 4, 2, 1),
    ("Los Fundadores", "RESIDENCIAL", 10.4900, -73.2560, 2, 1, 0),
    ("Pontevedra", "RESIDENCIAL", 10.4930, -73.2470, 2, 1, 1),
    ("Rosario Norte", "RESIDENCIAL", 10.4965, -73.2540, 2, 1, 0),
    ("Los Mayales", "RESIDENCIAL", 10.4985, -73.2640, 3, 2, 1),
    ("Novalito Norte", "RESIDENCIAL", 10.4890, -73.2470, 2, 1, 0),
    ("Villa Castro", "RESIDENCIAL", 10.4600, -73.2560, 3, 2, 1),
    ("Primero de Mayo", "RESIDENCIAL", 10.4580, -73.2440, 4, 3, 1),
    ("La Nevada", "RESIDENCIAL", 10.4520, -73.2600, 3, 2, 0),
    ("Mareigua", "RESIDENCIAL", 10.4540, -73.2490, 3, 2, 1),
    ("Garupal", "RESIDENCIAL", 10.4470, -73.2560, 3, 3, 1),
    ("Villa Miriam", "RESIDENCIAL", 10.4640, -73.2330, 3, 1, 0),
    ("Sicarare", "MIXTA", 10.4660, -73.2620, 4, 2, 1),
    ("Panamá", "RESIDENCIAL", 10.4635, -73.2490, 3, 2, 0),
    ("El Prado", "RESIDENCIAL", 10.4870, -73.2600, 2, 1, 1),
    ("Villa Clara", "RESIDENCIAL", 10.4560, -73.2370, 2, 1, 0),
    ("Doce de Octubre", "RESIDENCIAL", 10.4605, -73.2400, 3, 2, 1),
    ("Las Delicias", "RESIDENCIAL", 10.4500, -73.2420, 3, 1, 0),
    ("Dangond", "RESIDENCIAL", 10.4680, -73.2530, 3, 1, 0),
    ("San Fernando", "RESIDENCIAL", 10.4430, -73.2490, 2, 2, 0),
    ("Valle Meza", "RESIDENCIAL", 10.4720, -73.2310, 2, 1, 0),
    ("Los Músicos", "RESIDENCIAL", 10.4820, -73.2370, 2, 1, 0),
    ("Simón Bolívar", "RESIDENCIAL", 10.4560, -73.2530, 3, 2, 1),
    ("Hospital Rosario Pumarejo", "INSTITUCIONAL", 10.4755, -73.2520, 5, 0, 2),
]

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
        abogados.append((i + 1, uid, str(random.randint(100000, 399999)), ESPECIALIDADES[i % 3], "Valledupar", i != 4))
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
        if random.random() < 0.08:
            return [REV, RECH]
        if not atendible:  # el punto crítico: la entidad no lo arregla
            return random.choice([[REV], [REV, VAL], [REV, VAL]])
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
        clat, clon = desplazar(z[2], z[3], 250)
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
        lat, lon = desplazar(z[2], z[3], 450)
        f = hora_diurna(fecha_aleatoria(INICIO, FIN))
        nuevo_reporte(cat, zona, "CONDICION", lat, lon, f, ciclo_condicion(f))

    # 3) Incidentes aislados (sin antecedentes o con pocos): casos más débiles, para contraste.
    for _ in range(9):
        cat = random.choices(range(1, 9), weights=pesos_cat)[0]
        zona = random.randint(1, len(ZONAS))
        z = ZONAS[zona - 1]
        lat, lon = desplazar(z[2], z[3], 450)
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
            if rc[5] != "CONDICION" or rc[2] != ri[2]:
                continue
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
    casos, remisiones = [], []
    tarifa = {"LESION": 200000, "AMBOS": 200000, "DANO_MATERIAL": 120000}
    for inc in incidentes:
        iid, rid = inc[0], inc[1]
        rep = por_id[rid]
        if iid == id_incidente_demo or rep[4] != VAL:
            continue
        fuerte = ante_por_reporte.get(rid, 0) >= 2 or inc[5] != "DANO_MATERIAL"
        if not fuerte and random.random() < 0.6:
            continue
        val = max(h[5] for h in historial if h[1] == rid and h[3] == VAL)
        creacion = val + timedelta(hours=random.randint(2, 48))
        cid = len(casos) + 1
        estado = random.choices([C_EVAL, C_REM, C_ACEP, C_RECH, C_CERR], weights=[1, 2, 4, 1, 1])[0]
        cierre = None
        activos = [a for a in abogados if a[5]]
        random.shuffle(activos)
        envio = creacion + timedelta(hours=random.randint(1, 24))
        valor = tarifa[inc[5]]
        if estado == C_REM:
            remisiones.append([None, cid, activos[0][0], envio, "ENVIADA", None, None, valor])
        elif estado in (C_ACEP, C_CERR):
            if random.random() < 0.35:  # un primer abogado rechazó y se reenvió
                resp = envio + timedelta(hours=random.randint(4, 72))
                remisiones.append([None, cid, activos[1][0], envio, "RECHAZADA", resp, "Sin disponibilidad para nuevos casos.", valor])
                envio = resp + timedelta(hours=random.randint(1, 12))
            resp = envio + timedelta(hours=random.randint(4, 96))
            remisiones.append([None, cid, activos[0][0], envio, "ACEPTADA", resp, None, valor])
            if estado == C_CERR:
                cierre = resp + timedelta(days=random.randint(15, 40))
                if cierre > FIN:
                    cierre, estado = None, C_ACEP
        elif estado == C_RECH:
            for k in range(2):
                resp = envio + timedelta(hours=random.randint(4, 72))
                remisiones.append([None, cid, activos[k][0], envio, "RECHAZADA", resp,
                                   random.choice(["Evidencia insuficiente del daño.", "Plazo de caducidad comprometido."]), valor])
                envio = resp + timedelta(hours=2)
        casos.append((cid, iid, estado, random.choice(operadores), creacion, cierre,
                      "Incidente con antecedentes previos en el punto." if ante_por_reporte.get(rid, 0) else "Incidente sin antecedentes registrados."))
    remisiones.sort(key=lambda r: r[3])
    for i, r in enumerate(remisiones, 1):
        r[0] = i

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
        f.write(inserts("zonas", ["id_zona", "nombre", "tipo", "ciudad", "nivel_trafico", "centros_educativos", "centros_salud"],
                        [(i, z[0], z[1], "Valledupar", z[4], z[5], z[6]) for i, z in enumerate(ZONAS, 1)]))
        f.write(inserts("entidades_publicas", ["id_entidad", "nombre", "tipo", "ciudad"],
                        [(i, e[0], e[1], "Valledupar") for i, e in enumerate(ENTIDADES, 1)]))
        v = datetime(2026, 2, 1, 8, 0, tzinfo=TZ)
        f.write(inserts("versiones_indice", ["id_version", "nombre_indice", "numero_version", "descripcion", "vigente", "fecha_creacion"], [
            (1, "IRU", 1, "Pesos iniciales por criterio del equipo; pendientes de validación con expertos (AHP).", True, v),
            (2, "IVC", 1, "Pesos iniciales por criterio del equipo; pendientes de validación con abogados (AHP).", True, v),
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
        ]))

    with open(os.path.join(SALIDA, "02_usuarios.sql"), "w", encoding="utf-8") as f:
        f.write(cab + "-- Contraseña de todos los usuarios de prueba: Reda2026*\n\n")
        f.write(inserts("usuarios", ["id_usuario", "nombre", "correo", "password_hash", "id_rol", "fecha_registro", "activo", "fecha_consentimiento"], usuarios))
        f.write(inserts("abogados", ["id_abogado", "id_usuario", "tarjeta_profesional", "especialidad", "ciudad", "activo"], abogados))

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

    with open(os.path.join(SALIDA, "04_incidentes.sql"), "w", encoding="utf-8") as f:
        f.write(cab)
        f.write(inserts("incidentes", ["id_incidente", "id_reporte", "tipo_reporte", "id_entidad", "fecha_hora_hecho", "tipo_dano",
                                       "descripcion_dano", "hubo_atencion_medica"], incidentes))
        f.write(inserts("testigos", ["id_testigo", "id_incidente", "nombre", "contacto"], testigos))
        f.write(inserts("reportes_relacionados", ["id_reporte", "id_reporte_relacionado", "tipo_relacion", "origen", "fecha_vinculo"], relacionados))

    with open(os.path.join(SALIDA, "05_casos.sql"), "w", encoding="utf-8") as f:
        f.write(cab)
        f.write(inserts("casos", ["id_caso", "id_incidente", "id_estado_caso", "id_operador", "fecha_creacion", "fecha_cierre", "observacion"], casos))
        f.write(inserts("remisiones", ["id_remision", "id_caso", "id_abogado", "fecha_envio", "estado_remision", "fecha_respuesta",
                                       "motivo_rechazo", "valor_cobrado"], remisiones))

    with open(os.path.join(SALIDA, "99_secuencias.sql"), "w", encoding="utf-8") as f:
        f.write("-- La semilla inserta IDs explícitos; se ajustan las secuencias para que los\n"
                "-- siguientes INSERT de la aplicación no choquen con ellos.\nDO $$\nBEGIN\n")
        for t, c in [("roles", "id_rol"), ("estados", "id_estado"), ("estados_caso", "id_estado_caso"),
                     ("categorias", "id_categoria"), ("zonas", "id_zona"), ("entidades_publicas", "id_entidad"),
                     ("usuarios", "id_usuario"), ("abogados", "id_abogado"), ("reportes", "id_reporte"),
                     ("evidencias", "id_evidencia"), ("evaluaciones", "id_evaluacion"), ("historial_estados", "id_historial"),
                     ("incidentes", "id_incidente"), ("testigos", "id_testigo"), ("casos", "id_caso"),
                     ("remisiones", "id_remision"), ("versiones_indice", "id_version"), ("calculos_indice", "id_calculo")]:
            f.write(f"  PERFORM setval(pg_get_serial_sequence('{t}', '{c}'), coalesce((SELECT max({c}) FROM {t}), 0) + 1, false);\n")
        f.write("END $$;\n")

    print(f"usuarios={len(usuarios)} reportes={len(reportes)} (incidentes={len(incidentes)}) evidencias={len(evidencias)} "
          f"historial={len(historial)} evaluaciones={len(evaluaciones)} relacionados={len(relacionados)} "
          f"testigos={len(testigos)} casos={len(casos)} remisiones={len(remisiones)}")
    print(f"incidente demo: id_incidente={id_incidente_demo}")


if __name__ == "__main__":
    main()
