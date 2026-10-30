# REDA — Red de Evaluación y Detección de Riesgos Urbanos

Proyecto integrador de Base de Datos I y Programación 3 — Universidad Popular del Cesar, Valledupar.

REDA es un sistema de **documentación probatoria de fallas en infraestructura pública**. El ciudadano
reporta condiciones (huecos, alcantarillas, luminarias, señalización…) e incidentes (daños sufridos por
esas fallas). El sistema vincula cada incidente con los reportes previos del mismo punto
("la entidad ya sabía") y, cuando el ciudadano lo pide, varios abogados verificados se postulan y
**el ciudadano elige** con quién seguir. Al terminar, el ciudadano **califica** a su abogado.

## Integrantes
- Rafael Alfonso Rivera Restrepo
- Ivan Andrés Rodriguez Rodriguez
- Luis Eliecer Mendoza Perpiñán
- Carlos Eduardo Camelo Montaño

## Organización del repositorio

```
base-de-datos/            BASE DE DATOS I
├─ migraciones/           DDL numerado (001_ … 014_) ejecutable en orden sobre base vacía
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

**Atajo (un solo comando):** pide la clave de PostgreSQL la primera vez, aplica las migraciones pendientes y arranca
el servidor. Con `-Recrear` reconstruye la base desde cero con los datos de demostración.
```powershell
cd programacion\backend
.\iniciar.ps1            # o: .\iniciar.ps1 -Recrear
```

Pasos manuales equivalentes:

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
| Ciudadana (caso demo) | `lorena.cuello53@correo.test` | Incidente en el Centro con 4 reportes previos desde el 19 de mayo; sus fotos privadas; subir su foto de perfil |
| Ciudadano | `oscar.castro21@correo.test` | Caso con 3 abogados postulados: comparar foto, insignia de verificado y reputación, y **elegir** |
| Ciudadano | `jose.onate33@correo.test` | Caso con 2 abogados postulados (Luis Oñate, 4,0 ★, y María Rincones) |
| Ciudadana | `ana.arzuaga37@correo.test` | Caso cerrado y ya calificado (3 ★): ver su calificación |
| Ciudadana | `lorena.quintero18@correo.test` | Caso 6 en curso con María Rincones: cuando ella lo cierre, **calificar** a la abogada |
| Operadora | `lorena.arzuaga2@correo.test` | Bandeja de revisión: validar el incidente demo (#137) |
| Abogado verificado | `jorge.gutierrez6@correo.test` | Casos disponibles (resumen anónimo) y postularse; editar su perfil y su foto |
| Abogado verificado | `luis.onate7@correo.test` | Postulado en 2 casos; calificación 4,0 de un cliente |
| Abogada verificada | `maria.rincones8@correo.test` | Mis casos: expediente completo del caso 6, **cerrar caso** (luego Lorena Quintero lo califica) |
| Abogada verificada | `rosa.zuleta9@correo.test` | Caso 2 cerrado con 5 ★ de su cliente |
| Abogado sin verificar | `jesus.zuleta10@correo.test` | Cuenta pendiente: no puede postularse, subir foto ni editar su perfil |
| Administrador | `luis.fuentes1@correo.test` | Verificar abogados (al verificar a Jesús Zuleta se le habilita el perfil) |

Las fotos de perfil no vienen en la semilla (no se usan fotos de personas): se suben en vivo desde **Mi cuenta**.

## Pruebas

```powershell
cd base-de-datos            # con PGPASSWORD definido
psql -U postgres -d reda -f sql\pruebas\verificar_semilla.sql        # 17 comprobaciones de coherencia
psql -U postgres -d reda -f sql\pruebas\probar_restricciones.sql     # 35 pruebas negativas
psql -U postgres -d reda -f sql\pruebas\demostracion_tres_casos.sql  # guion de la sustentación: relación con varias, con ninguna y rechazos
psql -U postgres -d reda -f sql\consultas\verificacion_consultas.sql # verificación cruzada del banco
.\scripts\verificar_desde_cero.ps1   # todo lo anterior sobre una base aparte creada desde cero (no toca 'reda')

cd ..\programacion\backend
mvn test                                  # 81 pruebas unitarias
mvn verify -Pintegracion                  # + 17 de integración (requiere las variables del paso 2)
```

## Regenerar el diagrama
```powershell
cd base-de-datos
python scripts\generar_diagrama.py diagramas\especificacion_eer_v2.json diagramas\reda_eer
```
