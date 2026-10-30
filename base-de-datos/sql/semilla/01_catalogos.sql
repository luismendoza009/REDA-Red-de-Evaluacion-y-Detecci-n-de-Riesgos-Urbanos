-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO roles (id_rol, nombre) VALUES
  (1, 'CIUDADANO'),
  (2, 'OPERADOR'),
  (3, 'ABOGADO'),
  (4, 'ADMINISTRADOR'),
  (5, 'ANALISTA');

INSERT INTO estados (id_estado, nombre) VALUES
  (1, 'Pendiente'),
  (2, 'En revisión'),
  (3, 'Validado'),
  (4, 'En proceso'),
  (5, 'Solucionado'),
  (6, 'Rechazado');

INSERT INTO estados_caso (id_estado_caso, nombre) VALUES
  (1, 'Borrador'),
  (2, 'Evaluado'),
  (3, 'Remitido'),
  (4, 'Aceptado por abogado'),
  (5, 'Rechazado'),
  (6, 'Cerrado');

INSERT INTO categorias (id_categoria, nombre, descripcion) VALUES
  (1, 'Hueco en la vía', 'Huecos, hundimientos o pavimento fracturado en calzada.'),
  (2, 'Alcantarilla sin tapa u obstruida', 'Pozos de inspección destapados, rejillas rotas o sumideros tapados.'),
  (3, 'Luminaria dañada', 'Postes de alumbrado público apagados, intermitentes o con cables expuestos.'),
  (4, 'Señalización deteriorada', 'Señales de tránsito caídas, ilegibles o demarcación borrada.'),
  (5, 'Acumulación de residuos', 'Basureros satélite o residuos que obstruyen vías y drenajes.'),
  (6, 'Árbol en riesgo de caída', 'Árboles inclinados, ramas secas o raíces que levantan el andén.'),
  (7, 'Fuga de agua', 'Fugas de tubería que deterioran la vía o generan encharcamientos.'),
  (8, 'Otros', 'Andenes destruidos, barandas caídas, puentes peatonales en mal estado y similares.');

INSERT INTO zonas (id_zona, nombre, tipo, ciudad, nivel_trafico, centros_educativos, centros_salud, latitud_referencia, longitud_referencia, fuente_ubicacion) VALUES
  (1, 'Centro', 'COMERCIAL', 'Valledupar', 5, 4, 3, 10.47765, -73.24455, 'Plaza Alfonso López (OSM), consultado el 2026-10-26'),
  (2, 'Loperena', 'MIXTA', 'Valledupar', 4, 2, 1, 10.47351, -73.24883, 'Colegio Nacional Loperena (OSM), consultado el 2026-10-26'),
  (3, 'Cañaguate', 'RESIDENCIAL', 'Valledupar', 3, 1, 0, 10.4799, -73.2493, 'Manzanas Cañahuates (OSM, landuse=residential), consultado el 2026-10-26'),
  (4, 'Santa Rosa', 'RESIDENCIAL', 'Valledupar', 3, 2, 1, 10.4603, -73.2393, 'Parque Santa Rosa (OSM), consultado el 2026-10-26'),
  (5, 'Santo Domingo', 'RESIDENCIAL', 'Valledupar', 3, 1, 0, 10.46959, -73.23802, 'Colegio Santo Domingo (OSM), consultado el 2026-10-26'),
  (6, 'Doce de Octubre', 'RESIDENCIAL', 'Valledupar', 3, 2, 0, 10.4571, -73.24501, 'Plaza 12 de Octubre (OSM), consultado el 2026-10-26'),
  (7, 'Novalito', 'MIXTA', 'Valledupar', 4, 2, 1, 10.4865, -73.2537, 'Manzanas Novalito (OSM, landuse=residential), consultado el 2026-10-26'),
  (8, 'Villa Clara', 'RESIDENCIAL', 'Valledupar', 2, 1, 0, 10.46262, -73.23689, 'Barrio Villa Clara (OSM, place=neighbourhood), consultado el 2026-10-26'),
  (9, 'Candelaria Sur', 'RESIDENCIAL', 'Valledupar', 2, 1, 1, 10.46193, -73.23228, 'Candelaria Sur y Villa del Rosario (OSM, place=city_block), consultado el 2026-10-26'),
  (10, 'Los Corales', 'RESIDENCIAL', 'Valledupar', 2, 1, 0, 10.48995, -73.2704, 'Conjunto Los Corales (OSM, landuse=residential), consultado el 2026-10-26'),
  (11, 'Los Mayales', 'RESIDENCIAL', 'Valledupar', 3, 2, 1, 10.4588, -73.23053, 'Parque Los Mayales (OSM), consultado el 2026-10-26'),
  (12, 'Rosario Real', 'RESIDENCIAL', 'Valledupar', 2, 1, 0, 10.49537, -73.27702, 'Urbanización Rosario Real (OSM, landuse=residential), consultado el 2026-10-26'),
  (13, 'Villa Bolivariana', 'RESIDENCIAL', 'Valledupar', 3, 2, 1, 10.45133, -73.23832, 'Unidad Villa Bolivariana (OSM, landuse=residential), consultado el 2026-10-26'),
  (14, 'Primero de Mayo', 'RESIDENCIAL', 'Valledupar', 4, 3, 1, 10.45397, -73.25294, 'Casa de Justicia Primero de Mayo (OSM), consultado el 2026-10-26'),
  (15, 'La Nevada', 'RESIDENCIAL', 'Valledupar', 3, 2, 1, 10.47657, -73.2828, 'Escuela Mixta y Parque La Nevada (OSM), consultado el 2026-10-26'),
  (16, 'San Martín', 'RESIDENCIAL', 'Valledupar', 3, 2, 1, 10.45059, -73.24872, 'Parque San Martín (OSM), consultado el 2026-10-26'),
  (17, 'Garupal', 'RESIDENCIAL', 'Valledupar', 3, 3, 1, 10.47048, -73.26375, 'Barrio Garupal (OSM, landuse=residential), consultado el 2026-10-26'),
  (18, 'Doña Clara', 'RESIDENCIAL', 'Valledupar', 3, 1, 0, 10.45456, -73.22862, 'Urbanización Doña Clara (OSM, landuse=residential), consultado el 2026-10-26'),
  (19, 'Universidad Popular del Cesar', 'ESCOLAR', 'Valledupar', 4, 1, 0, 10.4495, -73.2624, 'UPC sede Sabanas (OSM, amenity=university), consultado el 2026-10-26'),
  (20, 'Iracal', 'RESIDENCIAL', 'Valledupar', 3, 2, 0, 10.47438, -73.26801, 'Parque Iracal (OSM), consultado el 2026-10-26'),
  (21, 'Los Cortijos', 'RESIDENCIAL', 'Valledupar', 2, 1, 1, 10.47632, -73.26188, 'Parque Los Cortijos (OSM), consultado el 2026-10-26'),
  (22, 'Don Carmelo', 'RESIDENCIAL', 'Valledupar', 2, 1, 0, 10.44097, -73.2602, 'Barrio Don Carmelo (OSM, place=neighbourhood), consultado el 2026-10-26'),
  (23, 'Nuevo Milenio', 'RESIDENCIAL', 'Valledupar', 3, 2, 1, 10.44221, -73.25171, 'Barrio Milenio (OSM, place=neighbourhood), consultado el 2026-10-26'),
  (24, 'Verona', 'RESIDENCIAL', 'Valledupar', 2, 1, 0, 10.44528, -73.26427, 'Urbanización Verona (OSM, landuse=residential), consultado el 2026-10-26'),
  (25, 'Terminal de Transportes', 'INSTITUCIONAL', 'Valledupar', 5, 0, 0, 10.44436, -73.24302, 'Terminal de Transporte de Valledupar (OSM, amenity=bus_station), consultado el 2026-10-26'),
  (26, 'Leandro Díaz', 'RESIDENCIAL', 'Valledupar', 2, 2, 0, 10.4364, -73.264, 'Urbanización Leandro Díaz (OSM, landuse=residential), consultado el 2026-10-26'),
  (27, 'Vive Alto', 'RESIDENCIAL', 'Valledupar', 2, 1, 0, 10.44038, -73.23392, 'Conjunto Vive Alto (OSM, landuse=residential), consultado el 2026-10-26'),
  (28, 'Don Alberto', 'RESIDENCIAL', 'Valledupar', 2, 1, 0, 10.4663, -73.2885, 'Urbanización Norte y Altos de Don Alberto (OSM), consultado el 2026-10-26'),
  (29, 'Las Trinitarias', 'RESIDENCIAL', 'Valledupar', 3, 2, 1, 10.49801, -73.27514, 'Las Trinitarias (OSM, landuse=residential), consultado el 2026-10-26'),
  (30, 'Hospital Rosario Pumarejo', 'INSTITUCIONAL', 'Valledupar', 5, 0, 2, 10.46933, -73.25452, 'Hospital Rosario Pumarejo de López (OSM, amenity=hospital), consultado el 2026-10-26');

INSERT INTO entidades_publicas (id_entidad, nombre, tipo, ciudad) VALUES
  (1, 'Alcaldía de Valledupar - Secretaría de Obras Públicas', 'SECRETARIA', 'Valledupar'),
  (2, 'Alcaldía de Valledupar - Secretaría de Tránsito y Transporte', 'SECRETARIA', 'Valledupar'),
  (3, 'Alcaldía de Valledupar - Oficina de Alumbrado Público', 'SECRETARIA', 'Valledupar'),
  (4, 'Alcaldía de Valledupar - Gestión Ambiental', 'SECRETARIA', 'Valledupar'),
  (5, 'EMDUPAR S.A. E.S.P.', 'EMPRESA_SERVICIOS', 'Valledupar'),
  (6, 'Empresa prestadora del servicio de aseo', 'EMPRESA_SERVICIOS', 'Valledupar'),
  (7, 'Gobernación del Cesar - Secretaría de Infraestructura', 'GOBERNACION', 'Valledupar');

INSERT INTO versiones_indice (id_version, nombre_indice, numero_version, descripcion, vigente, fecha_creacion) VALUES
  (1, 'IRU', 1, 'Pesos iniciales por criterio del equipo; pendientes de validación con expertos (AHP).', true, '2026-02-01'),
  (2, 'IVC', 1, 'Pesos iniciales por criterio del equipo; pendientes de validación con abogados (AHP).', true, '2026-02-01'),
  (3, 'IRU', 2, 'Propuesta: más peso a la gravedad evaluada por el operador. En estudio.', false, '2026-07-01'),
  (4, 'IVC', 2, 'Propuesta: más peso a los antecedentes previos, que prueban que la entidad sabía. En estudio.', false, '2026-07-11'),
  (5, 'IRU', 3, 'Propuesta: más peso a los reportes cercanos y menos al tráfico (dato simulado). En estudio.', false, '2026-09-19');

INSERT INTO parametros_indice (id_version, variable, peso, descripcion) VALUES
  (1, 'gravedad_evaluada', 0.25, 'Promedio de gravedad e impacto de la última evaluación del operador (0-100).'),
  (1, 'reportes_cercanos', 0.2, 'Reportes de la misma categoría a <= 100 m; 5 o más = 100.'),
  (1, 'antiguedad_sin_atencion', 0.2, 'Días sin solucionarse; 180 o más = 100.'),
  (1, 'trafico_zona', 0.15, 'Nivel de tráfico de la zona 1-5 llevado a 0-100. [SIMULADO]'),
  (1, 'cercania_colegios_salud', 0.1, 'Colegios + centros de salud en la zona; 5 o más = 100. [SIMULADO]'),
  (1, 'historial_incidentes_zona', 0.1, 'Incidentes en la zona en los últimos 12 meses; 5 o más = 100.'),
  (2, 'dano_documentado', 0.2, 'LESION o AMBOS = 100; DANO_MATERIAL = 60.'),
  (2, 'calidad_evidencia', 0.15, 'Fotos con hash y fecha de captura; 3 o más = 100.'),
  (2, 'antecedentes_previos', 0.2, 'Reportes de condición previos a <= 100 m; 4 o más = 100.'),
  (2, 'antiguedad_primer_antecedente', 0.15, 'Días entre el primer antecedente y el hecho; 120 o más = 100.'),
  (2, 'atencion_medica', 0.1, 'Hubo atención médica registrada = 100; si no = 0.'),
  (2, 'testigos', 0.1, 'Número de testigos; 2 o más = 100.'),
  (2, 'oportunidad_plazo', 0.1, 'Tiempo restante del plazo de caducidad (por verificar con abogado); recién ocurrido = 100.'),
  (3, 'gravedad_evaluada', 0.3, 'Igual que en la versión 1.'),
  (3, 'reportes_cercanos', 0.2, 'Igual que en la versión 1.'),
  (3, 'antiguedad_sin_atencion', 0.15, 'Igual que en la versión 1.'),
  (3, 'trafico_zona', 0.15, 'Igual que en la versión 1. [SIMULADO]'),
  (3, 'cercania_colegios_salud', 0.1, 'Igual que en la versión 1. [SIMULADO]'),
  (3, 'historial_incidentes_zona', 0.1, 'Igual que en la versión 1.'),
  (4, 'dano_documentado', 0.2, 'Igual que en la versión 1.'),
  (4, 'calidad_evidencia', 0.1, 'Igual que en la versión 1.'),
  (4, 'antecedentes_previos', 0.25, 'Igual que en la versión 1.'),
  (4, 'antiguedad_primer_antecedente', 0.15, 'Igual que en la versión 1.'),
  (4, 'atencion_medica', 0.1, 'Igual que en la versión 1.'),
  (4, 'testigos', 0.1, 'Igual que en la versión 1.'),
  (4, 'oportunidad_plazo', 0.1, 'Igual que en la versión 1.'),
  (5, 'gravedad_evaluada', 0.25, 'Igual que en la versión 1.'),
  (5, 'reportes_cercanos', 0.25, 'Igual que en la versión 1.'),
  (5, 'antiguedad_sin_atencion', 0.2, 'Igual que en la versión 1.'),
  (5, 'trafico_zona', 0.1, 'Igual que en la versión 1. [SIMULADO]'),
  (5, 'cercania_colegios_salud', 0.1, 'Igual que en la versión 1. [SIMULADO]'),
  (5, 'historial_incidentes_zona', 0.1, 'Igual que en la versión 1.');

