# Entorno de desarrollo del equipo

## Herramientas (versiones verificadas en el equipo de Luis)

| Herramienta | Versión | Para qué |
|---|---|---|
| Java (Temurin) | 17.0.20 | Compilar y ejecutar el backend |
| Maven | 3.9.16 | Dependencias, pruebas y empaquetado |
| PostgreSQL | 18.6 | Base de datos (el curso pide 16+) |
| Python | 3.14 | Generadores de semilla, diagrama y documentos de BD |
| Node / npm | 20.19 / 10.8 | Frontend (cuando se haga) |
| Git | — | Control de versiones (cada integrante con su propio usuario) |

## Oh My Posh en PowerShell

Oh My Posh muestra en el *prompt* la rama de Git, si hay cambios sin confirmar y el resultado del último
comando, lo que evita errores comunes (por ejemplo, hacer commit en `main` creyendo estar en una rama de trabajo).

Instalación (una vez por equipo, en PowerShell):

```powershell
winget install JanDeDobbeleer.OhMyPosh -s winget
oh-my-posh font install meslo           # fuente con íconos; luego elegirla en la terminal de VS Code
notepad $PROFILE                        # crear/abrir el perfil de PowerShell
```

Agregar al perfil y reiniciar la terminal:

```powershell
oh-my-posh init pwsh --config "$env:POSH_THEMES_PATH\jandedobbeleer.omp.json" | Invoke-Expression
```

En VS Code: *Settings → Terminal › Integrated: Font Family* = `MesloLGM Nerd Font`.

Nota: esto modifica la configuración personal de cada equipo, por eso no forma parte del repositorio
ni de los scripts del proyecto.

## Flujo de trabajo con Git

- `main` siempre estable; el trabajo va en ramas `feature/...`, `fix/...`, `docs/...` y se integra por Pull Request.
- Commits pequeños en español: `feat: ...`, `fix: ...`, `docs: ...`, `test: ...`, `refactor: ...`.
- Cada integrante hace commits con **su propio** usuario (`git config user.name` / `user.email`).
