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

INSERT INTO zonas (id_zona, nombre, tipo, ciudad, nivel_trafico, centros_educativos, centros_salud) VALUES
  (1, 'Centro', 'COMERCIAL', 'Valledupar', 5, 4, 3),
  (2, 'Loperena', 'MIXTA', 'Valledupar', 4, 2, 1),
  (3, 'Cañaguate', 'RESIDENCIAL', 'Valledupar', 3, 1, 0),
  (4, 'San Joaquín', 'RESIDENCIAL', 'Valledupar', 3, 2, 1),
  (5, 'Altagracia', 'RESIDENCIAL', 'Valledupar', 3, 1, 0),
  (6, 'Gaitán', 'RESIDENCIAL', 'Valledupar', 3, 2, 0),
  (7, 'Novalito', 'MIXTA', 'Valledupar', 4, 2, 1),
  (8, 'Los Fundadores', 'RESIDENCIAL', 'Valledupar', 2, 1, 0),
  (9, 'Pontevedra', 'RESIDENCIAL', 'Valledupar', 2, 1, 1),
  (10, 'Rosario Norte', 'RESIDENCIAL', 'Valledupar', 2, 1, 0),
  (11, 'Los Mayales', 'RESIDENCIAL', 'Valledupar', 3, 2, 1),
  (12, 'Novalito Norte', 'RESIDENCIAL', 'Valledupar', 2, 1, 0),
  (13, 'Villa Castro', 'RESIDENCIAL', 'Valledupar', 3, 2, 1),
  (14, 'Primero de Mayo', 'RESIDENCIAL', 'Valledupar', 4, 3, 1),
  (15, 'La Nevada', 'RESIDENCIAL', 'Valledupar', 3, 2, 0),
  (16, 'Mareigua', 'RESIDENCIAL', 'Valledupar', 3, 2, 1),
  (17, 'Garupal', 'RESIDENCIAL', 'Valledupar', 3, 3, 1),
  (18, 'Villa Miriam', 'RESIDENCIAL', 'Valledupar', 3, 1, 0),
  (19, 'Sicarare', 'MIXTA', 'Valledupar', 4, 2, 1),
  (20, 'Panamá', 'RESIDENCIAL', 'Valledupar', 3, 2, 0),
  (21, 'El Prado', 'RESIDENCIAL', 'Valledupar', 2, 1, 1),
  (22, 'Villa Clara', 'RESIDENCIAL', 'Valledupar', 2, 1, 0),
  (23, 'Doce de Octubre', 'RESIDENCIAL', 'Valledupar', 3, 2, 1),
  (24, 'Las Delicias', 'RESIDENCIAL', 'Valledupar', 3, 1, 0),
  (25, 'Dangond', 'RESIDENCIAL', 'Valledupar', 3, 1, 0),
  (26, 'San Fernando', 'RESIDENCIAL', 'Valledupar', 2, 2, 0),
  (27, 'Valle Meza', 'RESIDENCIAL', 'Valledupar', 2, 1, 0),
  (28, 'Los Músicos', 'RESIDENCIAL', 'Valledupar', 2, 1, 0),
  (29, 'Simón Bolívar', 'RESIDENCIAL', 'Valledupar', 3, 2, 1),
  (30, 'Hospital Rosario Pumarejo', 'INSTITUCIONAL', 'Valledupar', 5, 0, 2);

INSERT INTO entidades_publicas (id_entidad, nombre, tipo, ciudad) VALUES
  (1, 'Alcaldía de Valledupar - Secretaría de Obras Públicas', 'SECRETARIA', 'Valledupar'),
  (2, 'Alcaldía de Valledupar - Secretaría de Tránsito y Transporte', 'SECRETARIA', 'Valledupar'),
  (3, 'Alcaldía de Valledupar - Oficina de Alumbrado Público', 'SECRETARIA', 'Valledupar'),
  (4, 'Alcaldía de Valledupar - Gestión Ambiental', 'SECRETARIA', 'Valledupar'),
  (5, 'EMDUPAR S.A. E.S.P.', 'EMPRESA_SERVICIOS', 'Valledupar'),
  (6, 'Empresa prestadora del servicio de aseo', 'EMPRESA_SERVICIOS', 'Valledupar'),
  (7, 'Gobernación del Cesar - Secretaría de Infraestructura', 'GOBERNACION', 'Valledupar');

INSERT INTO versiones_indice (id_version, nombre_indice, numero_version, descripcion, vigente, fecha_creacion) VALUES
  (1, 'IRU', 1, 'Pesos iniciales por criterio del equipo; pendientes de validación con expertos (AHP).', true, '2026-02-01T08:00:00-05:00'),
  (2, 'IVC', 1, 'Pesos iniciales por criterio del equipo; pendientes de validación con abogados (AHP).', true, '2026-02-01T08:00:00-05:00');

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
  (2, 'oportunidad_plazo', 0.1, 'Tiempo restante del plazo de caducidad (por verificar con abogado); recién ocurrido = 100.');

