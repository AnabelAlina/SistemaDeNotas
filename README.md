# Sistema de Notas UNSA

Aplicación en Java orientada a objetos con interfaz gráfica (**Java Swing**) y un modo consola alternativo, para gestionar el rendimiento académico de estudiantes de la Universidad Nacional de San Agustín (UNSA).

Qué permite hacer:

- Registrar estudiantes (con validación de DNI, CUI y correo `@unsa.edu.pe`), ciclos y cursos (los predeterminados de 1.er año, 2.º semestre, o cursos manuales).
- Registrar notas de exámenes, evaluaciones continuas con trabajos internos (individuales o grupales) y el examen sustitutorio.
- Ver promedios, el **estado del curso**, estadísticas, un simulador de notas y la nota necesaria para llegar a una meta.
- Guardar y cargar los datos en **dos formatos** (texto y objetos), con una **bitácora** de lo que se hace.
- Exportar un reporte en **CSV** o en **HTML**.

---

## Requisitos

- **JDK 17** o superior.
- Una terminal **Git Bash**, **PowerShell** o **CMD**. Compila siempre desde la carpeta raíz del proyecto (no uses *Code Runner* en archivos sueltos).

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

### Modo consola
Es la versión original por terminal. Usa el mismo modelo y los mismos archivos de datos:

```bash
java -Dfile.encoding=UTF-8 -cp out app.MainConsola
```

---

## Ejecutar las pruebas automáticas

Desde la carpeta raíz (una vez compilado). Cada prueba termina con `TODO OK` (o con `Todas las pruebas pasaron.`) si no hay fallos:

```bash
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaModelo
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaCalculos
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaUNSA
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaGuardado
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaExportacion
java -Dfile.encoding=UTF-8 -cp out pruebas.PruebaFlujoCompleto
```

`PruebaFlujoCompleto` recorre todo el programa sin abrir ventanas (registrar, notas, sustitutorio, los cuatro estados, guardar y cargar en texto y en objetos, archivo dañado, reportes y errores). Las ventanas se prueban a mano con la lista de `docs/pruebas_manuales.md`.

---

## Guardado de datos y bitácora

Los datos se guardan en la carpeta `datos/` (no se sube a Git). Al iniciar `app.Main` se cargan automáticamente los datos en texto; se pueden guardar con **Guardar datos** o al cerrar la ventana principal.

| Formato | Archivo | Clase | Cómo escribe |
|---|---|---|---|
| Texto | `datos/notas.txt` | `RepositorioArchivo` | `PrintWriter` y `Scanner` con `FileReader` |
| Objetos | `datos/notas.dat` | `RepositorioObjetos` | `ObjectOutputStream` y `ObjectInputStream` (las clases del modelo son `Serializable`) |

- En `VentanaPrincipal`, el menú **Formato de guardado** cambia entre los dos formatos, y **Cargar datos** reemplaza lo que hay en pantalla (pide confirmación).
- La primera vez que se guarda en cada sesión sobre un archivo que ya existe, se deja antes una copia `.bak` (por ejemplo `notas.txt.bak`).
- Si al cargar el archivo de texto hay líneas dañadas, se ignoran y se muestra un aviso con las primeras 10 (nada se pierde en silencio).
- **Bitácora:** `datos/bitacora.txt` va añadiendo una línea `AAAA-MM-DD HH:MM:SS | mensaje` por cada estudiante registrado, nota, guardado y carga (`PrintWriter` sobre `FileWriter` en modo añadir).
- Ambos formatos implementan la interfaz `Repositorio`: el resto del programa no cambia al cambiar de formato.

---

## Reportes (exportación)

Se piden desde **Análisis de notas** (elige el formato junto al botón *Exportar*). El reporte sale en `datos/reporte_<CUI>.csv` o `datos/reporte_<CUI>.html`, o en la ruta que escribas. `VentanaPrincipal` también tiene un botón de reporte CSV.

| Formato | Clase | Qué trae |
|---|---|---|
| CSV | `ExportadorCSV` | Separado por `;`, UTF-8 con BOM (abre bien en Excel). Evaluaciones y trabajos de las continuas, y filas *Resumen* por curso, ciclo y acumulado |
| HTML | `ExportadorHTML` | Una tabla por curso, con el estado de cada curso en su color; se abre en el navegador |

Los dos implementan la interfaz `Exportable`.

---

## Estado del curso

`EvaluadorCurso.estadoDe(curso)` devuelve uno de cuatro estados (o nada, si el curso aún no tiene notas). Se calcula con tres valores:

- **puntos ganados:** lo que ya está asegurado en la nota final (ya cuenta el sustitutorio).
- **peso pendiente:** el porcentaje del curso que todavía falta ganar.
- **puntos posibles:** puntos ganados + peso pendiente × 20 / 100, es decir, lo máximo a lo que aún se puede llegar.

| Estado | Regla |
|---|---|
| **Aprobado** | Ya no queda nada pendiente y el promedio es 10.5 o más, o los puntos ganados ya son 10.5 o más |
| **Vas aprobando** | Tu promedio actual (de lo rendido) es 10.5 o más, pero todavía no está asegurado |
| **En riesgo** | Tu promedio actual es menor que 10.5, pero aún puedes aprobar |
| **Desaprobado** | Ya no queda nada pendiente y el promedio es menor que 10.5, o los puntos posibles son menos de 10.5 |

La nota mínima aprobatoria es 10.5. Ejemplo: con solo el Examen 1 = 15 en un curso donde ese examen vale 16 %, el estado es *Vas aprobando* (todavía falta rendir casi todo). Si hay una continua con trabajos por calificar, la nota necesaria se muestra como aproximada.

---

## Examen sustitutorio

Si hay sustitutorio rendido, reemplaza la nota más baja entre el Examen 1 y el Examen 2 (nunca el Examen 3), y solo si la mejora. Esa regla está en `NotasEfectivas` y la usan los promedios, las estadísticas y el simulador.

---

## Estructura del proyecto

- `src/modelo/`: Entidades del dominio (`Persona`, `Estudiante`, `Ciclo`, `Curso`, `Calculable`). Un estudiante tiene ciclos, un ciclo tiene cursos y un curso tiene evaluaciones.
- `src/evaluaciones/`: Jerarquía de evaluaciones (`Evaluacion` abstracta, `Examen`, `Continua` con sus `TrabajoContinua`, `ExamenSustitutorio`).
- `src/calculos/`: Promedios (`EstrategiaPromedio`, `PromedioPonderado`, `PromedioSimple`), regla del sustitutorio (`NotasEfectivas`), `EstadoCurso` y `EvaluadorCurso`, `EstadisticasNotas` y `SimuladorNotas`.
- `src/plan/`: Catálogo de cursos predeterminados UNSA (`PlanEstudios`).
- `src/persistencia/`: Interfaz `Repositorio`, con `RepositorioArchivo` (texto) y `RepositorioObjetos` (objetos), y la `Bitacora`.
- `src/exportacion/`: Interfaz `Exportable`, con `ExportadorCSV` y `ExportadorHTML`.
- `src/gui/`: Interfaz gráfica en Swing (`Estilo`, `SelectorCurso`, `VentanaPrincipal`, `VentanaAcademica`, `VentanaNotas`, `VentanaAnalisis`). Las ventanas no calculan nada: piden todo a `calculos` y a `GestorNotas`.
- `src/app/`: Puntos de entrada (`Main` gráfico y `MainConsola`), coordinador (`GestorNotas`), utilidades y menús de la consola (`Entrada`, `MenuPrincipal`, `MenuAcademico`, `MenuNotas`, `MenuAnalisis`).
- `src/excepciones/`: Excepciones propias (`NotaInvalidaException`, `PesoInvalidoException`).
- `src/pruebas/`: Pruebas automáticas sin ventanas.
- `docs/uml_dominio.md`: Diagrama de clases UML del sistema.
- `docs/pruebas_manuales.md`: Lista de pruebas manuales de las ventanas.

### Patrones y conceptos usados

- **Strategy:** `Curso` calcula su promedio con una `EstrategiaPromedio`. Se usa `PromedioPonderado`; `PromedioSimple` es la estrategia alternativa (promedio sin pesos) y se puede activar con `Curso.setEstrategia(...)`, aunque ninguna ventana lo ofrece.
- **Herencia, clase abstracta y polimorfismo:** `Evaluacion` y sus subclases (`Continua` sobrescribe `getNota`).
- **Interfaces con varias implementaciones:** `Repositorio` (texto y objetos) y `Exportable` (CSV y HTML).
- **Composición:** Estudiante → Ciclo → Curso → Evaluación → Trabajo.

---

## Equipo de desarrollo

| Integrante | Rol |
|---|---|
| **Estudiante 1:** Ayelen Choquecota | Interfaz de estados y avisos, pruebas del flujo completo, código muerto, documentación y entrega (ronda 2: PR 5 y PR 6) |
| **Estudiante 2:** Anabel Quispe | Lógica corregida (estado del curso y nota necesaria) y reportes CSV y HTML (ronda 2: PR 3 y PR 4) |
| **Estudiante 3:** Karol Mamani | Reparación y limpieza, persistencia segura, formatos de archivo (objetos), bitácora y `GestorNotas` (ronda 2: PR 1 y PR 2) |
