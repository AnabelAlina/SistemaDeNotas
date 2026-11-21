# Sistema de Notas UNSA

Aplicación en Java orientada a objetos con interfaz gráfica (**Java Swing**) y modo consola alternativo para gestionar el rendimiento académico de estudiantes de la Universidad Nacional de San Agustín (UNSA): registro de estudiantes (con validación de DNI, CUI y correo `@unsa.edu.pe`), ciclos, cursos predeterminados (1.er año, 2.º semestre) y manuales, evaluaciones continuas con trabajos internos, exámenes teóricos, examen sustitutorio, estadísticas, simulador de notas, exportación a CSV y persistencia en archivo.

---

## Requisitos

- **JDK 17** o superior.
- Terminal **Git Bash**, **PowerShell** o **CMD** (compilar siempre desde la carpeta raíz del proyecto; no usar *Code Runner* en archivos sueltos).

---

## Cómo compilar y ejecutar

### En Git Bash / Linux / macOS
```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Dfile.encoding=UTF-8 -cp out app.Main
```

### En PowerShell (Windows, con soporte para tildes UTF-8)
```powershell
chcp 65001
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" -cp out app.Main
```

### En CMD (Windows)
```cmd
chcp 65001
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
java -Dfile.encoding=UTF-8 -cp out app.Main
```

---

## Ejecutar pruebas automáticas

Desde la carpeta raíz (una vez compilado):

```bash
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaModelo
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaCalculos
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaUNSA
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaGuardado
```

---

## Persistencia y exportación

- Persistencia (datos/notas.txt): Al iniciar app.Main, se cargan automáticamente los registros guardados. Puedes guardar en cualquier momento desde Guardar datos en VentanaPrincipal o al cerrar la ventana.

- Exportación CSV: La opción Exportar reporte CSV genera un archivo compatible con Excel (separado por ; y con codificación UTF-8 BOM) en datos/reporte_<CUI>.csv.
---

## Estructura del proyecto

- `src/modelo/`: Entidades del dominio (`Persona`, `Estudiante`, `Docente`, `Ciclo`, `Curso`, `Calculable`).
- `src/evaluaciones/`: Jerarquía de evaluaciones (`Evaluacion`, `Continua`, `TrabajoContinua`, `Examen`, `ExamenSustitutorio`).
- `src/calculos/`: Estrategias de promedio, aplicación de sustitutorio (`NotasEfectivas`), estadísticas y simulador.
- `src/plan/`: Catálogo de cursos predeterminados UNSA (`PlanEstudios`).
- `src/persistencia/`: Interfaz `Repositorio` e implementación en archivo de texto (`RepositorioArchivo`).
- `src/exportacion/`: Interfaz `Exportable` e implementación CSV (`ExportadorCSV`).
- src/gui/: Interfaz gráfica en Java Swing (Estilo, SelectorCurso, VentanaPrincipal, VentanaAcademica, VentanaNotas, VentanaAnalisis).
- `src/app/`: Punto de entrada (`Main`), coordinador (`GestorNotas`), utilidades de consola (`Entrada`) y menús (`MenuPrincipal`, `MenuAcademico`, `MenuNotas`, `MenuAnalisis`).
- `src/pruebas/`: Pruebas de verificación del modelo, cálculos y persistencia.
- docs/uml_dominio.md: Diagrama de clases UML del sistema.

---

## Equipo de desarrollo

- **Estudiante 1:** Anabel Quispe (`anabel-quispe`) — Modelo académico, validaciones y menús de registro y notas.
- **Estudiante 2:** Ayelen Choquecota (`ayelen-choquecota`) — Modelo de evaluaciones UNSA, cálculos, análisis y exportación CSV.
- **Estudiante 3:** Karol Mamani (`karol-mamani`) — Arquitectura base, coordinación (`GestorNotas`), persistencia (`RepositorioArchivo`), pruebas de guardado y documentación.