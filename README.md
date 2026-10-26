# REDA — Red de Evaluación y Detección de Riesgos Urbanos

Proyecto integrador de Base de Datos I y Programación 3 — Universidad Popular del Cesar, Valledupar.

REDA es un sistema de **documentación probatoria de fallas en infraestructura pública**. El ciudadano
reporta condiciones (huecos, alcantarillas, luminarias, señalización…) e incidentes (daños sufridos por
esas fallas). El sistema vincula cada incidente con los reportes previos del mismo punto
("la entidad ya sabía") y, cuando el ciudadano lo pide, varios abogados verificados se postulan y
**el ciudadano elige** con quién seguir. Al terminar, el ciudadano **califica** a su abogado.

## Integrantes
- Rafael Alfonso Rivera Restrepo
- Ivan Andres Rodriguez Rodriguez
- Luis Eliecer Mendoza Perpiñan

## Organización del repositorio

```
base-de-datos/            BASE DE DATOS I
├─ migraciones/           DDL numerado (001_ … 011_) ejecutable en orden sobre base vacía
├─ sql/                   semilla, consultas, índices, pruebas, herramientas
├─ scripts/               migrar.ps1 y generadores (semilla, diccionario, diagrama, documento)
├─ diagramas/             diagrama EER (.drawio editable + .png) y su especificación JSON
└─ documentacion/         entregas, diccionario, decisiones, datos simulados, uso de IA

programacion/             PROGRAMACIÓN 3
├─ backend/               API REST en Java 17 + Spring Boot (capas) e interfaz web en src/main/resources/static
└─ documentacion/         mapa de temas, auditorías GRASP y SOLID, decisiones, entorno
```

## Cómo correrlo (Windows, PowerShell)

Requisitos: PostgreSQL 16+, Java 17, Maven. La primera vez Maven descarga dependencias.

**1. Crear la base con los datos de demostración** (borra y recrea la base `reda`):
```powershell
cd base-de-datos
$env:PGPASSWORD = "TU_CLAVE_DE_POSTGRES"
.\scripts\migrar.ps1 -Recrear -ConSemilla
```

**2. Arrancar la aplicación:**
```powershell
cd ..\programacion\backend
$env:REDA_DB_CLAVE    = "TU_CLAVE_DE_POSTGRES"
$env:REDA_JWT_SECRETO = "una-frase-larga-cualquiera-de-al-menos-32-caracteres"
mvn spring-boot:run
```
Abrir **http://localhost:8080** (desde el celular en la misma red: `http://IP-del-PC:8080`).
Documentación de la API: http://localhost:8080/swagger-ui.html

Para no escribir las variables cada vez: copiar `programacion/backend/application-example.properties` como
`application-local.properties` (está en `.gitignore`).

**Recuperar contraseña:** no hay servidor de correo en este entorno; el enlace se escribe en la consola donde
corre `mvn spring-boot:run` (buscar «CORREO SIMULADO»).

## Cuentas de demostración

Contraseña de todas: `Reda2026*` (datos simulados, ninguna persona es real).

| Rol | Correo | Qué mostrar |
|---|---|---|
| Ciudadana (caso demo) | `lorena.cuello53@correo.test` | Incidente en el Centro con 4 reportes previos desde el 19 de mayo; sus fotos privadas |
| Ciudadano | `jose.onate33@correo.test` | Caso con 3 abogados postulados: comparar reputación y **elegir** |
| Ciudadana | `lorena.quintero18@correo.test` | Caso cerrado sin calificar: **calificar** al abogado |
| Operadora | `lorena.arzuaga2@correo.test` | Bandeja de revisión: validar el incidente demo (#137) |
| Abogado | `jorge.gutierrez6@correo.test` | Casos disponibles (resumen anónimo) y postularse |
| Abogada | `maria.rincones8@correo.test` | Mis casos: expediente completo, cerrar caso |
| Abogado sin verificar | `jesus.zuleta10@correo.test` | Cuenta pendiente de verificación |
| Administrador | `luis.fuentes1@correo.test` | Verificar abogados |

## Pruebas

```powershell
cd base-de-datos            # con PGPASSWORD definido
psql -U postgres -d reda -f sql\pruebas\verificar_semilla.sql        # 15 comprobaciones de coherencia
psql -U postgres -d reda -f sql\pruebas\probar_restricciones.sql     # 27 pruebas negativas
psql -U postgres -d reda -f sql\consultas\verificacion_consultas.sql # verificación cruzada del banco

cd ..\programacion\backend
mvn test                                  # 71 pruebas unitarias
mvn verify -Pintegracion                  # + 14 de integración contra PostgreSQL (base recién creada)
```

## Regenerar el diagrama
```powershell
cd base-de-datos
python scripts\generar_diagrama.py diagramas\especificacion_eer_v2.json diagramas\reda_eer
```
