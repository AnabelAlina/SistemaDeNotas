# Pruebas manuales de las ventanas

Se hacen a mano con `app.Main` (`java -cp out app.Main`), después de compilar y de correr las pruebas automáticas.
Para empezar sin datos viejos, borra la carpeta `datos/` antes de abrir la aplicación.

En la columna **Resultado** anota `OK` o `FALLO: qué pasó`. Los casos que fallen se explican abajo, en *Fallos encontrados*.

| N.º | Ventana | Qué se hace | Qué debe pasar | Resultado |
|---|---|---|---|---|
| 1 | Gestión académica | Registrar un estudiante con DNI `1234` (menos de 8 cifras) | Mensaje de error y no se registra |OK |
| 2 | Gestión académica | Registrar un estudiante válido (DNI de 8 cifras, CUI de 8 cifras, correo `@unsa.edu.pe`) y luego otro con el mismo CUI o el mismo DNI | El primero se registra; el repetido se rechaza con un aviso | OK |
| 3 | Gestión académica | Agregar el ciclo `2026-B` y volver a agregar `2026-B` | El primero se agrega; el repetido da error | OK |
| 4 | Gestión académica | Cargar los cursos predeterminados y repetir la carga | La primera carga agrega los 7 cursos; la segunda no duplica ninguno | OK|
| 5 | Gestión académica | Agregar un curso manual con porcentajes que suman 90 | Error de pesos; el curso no se agrega | OK|
| 6 | Registro de notas | Examen 1 con nota `25` y luego con `abc` | Error en cada caso; el examen sigue sin rendir |OK |
| 7 | Registro de notas | Examen 1 = `15` (y nada más) | El promedio abajo dice `15.00 - Vas aprobando`, en color turquesa | OK|
| 8 | Registro de notas | Sustitutorio con nota `25`, y después mirar el curso | Error de nota inválida; el curso sigue sin sustitutorio (dice "sin rendir") | OK|
| 9 | Registro de notas | Con Examen 1 = 8, Examen 2 = 12 y Examen 3 = 10 (curso 16/16/18): guardar un sustitutorio de `14` | El promedio sube de 10.00 a 11.92 (reemplaza la nota 8) | OK|
| 10 | Registro de notas | En una continua, agregar un trabajo de 60 % y otro de 50 % | El segundo se rechaza porque los trabajos pasarían de 100 %; queda solo el de 60 % |OK |
| 11 | Registro de notas | Calificar el trabajo de 60 % con `18` y quitar el trabajo (con confirmación) | La continua muestra su nota y "incompleta"; al quitarlo vuelve a "sin notas" | ok|
| 12 | Análisis de notas | Con Examen 1 = 15: mirar el estado y la leyenda | Estado "Vas aprobando" en turquesa; debajo, la leyenda con los cuatro estados |OK |
| 13 | Análisis de notas | Con una continua con un solo trabajo calificado: mirar "Necesitas…" | La línea termina en "(aprox.: faltan trabajos por calificar)" y se ve completa | OK|
| 14 | Análisis de notas | Simulador: elegir una evaluación pendiente, escribir `14,5` (con coma) y pulsar *Simular*; después `30` | Con `14,5` muestra el promedio simulado; con `30` un mensaje de error |OK |
| 15 | Análisis de notas | Exportar en CSV y luego en HTML (formato junto al botón *Exportar*) | Se crean `reporte_<CUI>.csv` (abre en Excel con tildes bien) y `reporte_<CUI>.html` (abre en el navegador con el estado en color) | OK|
| 16 | Ventana principal | Guardar datos en *Texto*, cambiar a *Objetos* y guardar, cambiar a *Texto* y pulsar *Cargar datos* | Se crean `notas.txt` y `notas.dat`; *Cargar datos* pide confirmación y deja los mismos datos; aparecen líneas en `datos/bitacora.txt` | OK|
| 17 | Ventana principal | Cerrar con la X, elegir *Guardar*; volver a abrir la aplicación | Se guardan los cambios y al abrir de nuevo todo sigue (estudiante, ciclo, notas) | OK|
| 18 | Ventana principal | Hacer un cambio, cerrar con la X y elegir *No guardar*; volver a abrir | El cambio se pierde; lo anterior sigue igual |OK |

## Fallos encontrados

(Anotar aquí cada caso que falle: número, qué se esperaba y qué pasó. Si no hubo fallos, escribir "Ninguno".)

## Datos de la prueba

- Fecha:
- Quién probó:
- Versión (rama o commit):
- Sistema operativo y versión de Java (`java -version`):
