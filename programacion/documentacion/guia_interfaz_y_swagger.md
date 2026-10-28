# Guía de la interfaz web y de Swagger

## 1. Cómo está armada la aplicación

```
Navegador (HTML + CSS + JavaScript)  ──HTTP/JSON──▶  API REST (Spring Boot)  ──JDBC──▶  PostgreSQL
   src/main/resources/static/                       controller → service → repository
```

- **La interfaz** es una página web (SPA) en `programacion/backend/src/main/resources/static/`. Spring Boot la
  entrega tal cual en `http://localhost:8080`, así que backend e interfaz salen del mismo puerto.
- La interfaz **no toca la base de datos**: todo lo pide a la API (`js/api.js`). El backend vuelve a verificar
  cada permiso; el menú solo oculta las opciones que no le corresponden a cada rol.
- Cada pantalla es un archivo en `js/vistas/` (una responsabilidad por archivo).

## 2. Menú por rol

Al iniciar sesión se abre **Inicio**: cifras del usuario, lo que **requiere su atención**, accesos directos
(«¿Qué quieres hacer?») y cómo funciona el proceso para su rol. El menú lateral (en celular, el botón ☰) cambia según el rol:

| Rol | Opciones del menú | Para qué |
|---|---|---|
| **Ciudadano** | Inicio · Nuevo reporte · Mis reportes · Mis casos y abogados · Mapa de reportes | Reportar fallas o incidentes, seguir su estado, buscar abogado y elegir entre los postulados |
| **Abogado** | Inicio · Casos disponibles · Mis casos · Mapa de reportes | Ver casos anónimos, postularse, consultar el expediente de los casos donde lo eligieron y cerrarlos |
| **Operador** | Inicio · Bandeja de revisión · Mapa de reportes | Validar o rechazar reportes (cada cambio queda en el historial con su nombre) |
| **Administrador** | Inicio · Bandeja de revisión · Verificar abogados · Mapa de reportes · Documentación de la API | Lo del operador + activar abogados tras revisar su tarjeta profesional + acceso a Swagger |
| Todos | Mi cuenta · Ayuda · Cerrar sesión | Perfil, foto, contraseña; preguntas frecuentes |

Por qué hay operador y administrador: el **operador** es quien filtra los reportes falsos (riesgo de producto:
incentivo a fabricar incidentes); el **administrador** controla quién entra como abogado. Separar los dos roles
aplica el principio de **privilegio mínimo**.

## 3. ¿Qué es Swagger?

**Swagger (OpenAPI)** es una página que se genera **automáticamente** a partir del código del backend y lista
todos los servicios (endpoints) de la API: la ruta, el método HTTP (GET, POST, PATCH, DELETE), qué datos recibe,
qué responde y qué errores puede devolver. En REDA la genera la librería **springdoc-openapi** leyendo los
`@RestController` y las peticiones (`peticion/`) con sus validaciones.

- Dirección: **http://localhost:8080/swagger-ui.html** (en el menú del administrador: *Documentación de la API*).
- **No es para el usuario final**: es para desarrolladores y para el profesor. Sirve para ver el «contrato» entre
  la interfaz y el backend y para **probar los endpoints a mano** sin la interfaz.

### Cómo probar un endpoint en Swagger

1. Abre `POST /api/auth/login` → **Try it out** → escribe un correo y contraseña de demostración → **Execute**.
2. Copia el `token` de la respuesta.
3. Arriba, pulsa **Authorize** y pega el token. Desde ahí las peticiones van autenticadas con JWT.
4. Prueba, por ejemplo, `GET /api/reportes/mios` (con un ciudadano) o `GET /api/revision/bandeja` (con un operador).
   Si el rol no tiene permiso, la API responde **403**: así se demuestra que la seguridad está en el backend.

### Por qué sirve en la sustentación

- Evidencia la **arquitectura en capas**: la interfaz y Swagger usan la misma API.
- Muestra las **validaciones** (Bean Validation): enviar un dato inválido devuelve 400 con el mensaje por campo.
- Muestra la **autorización por rol** (`@PreAuthorize`): el mismo endpoint responde 200 o 403 según quién llame.
