# SistemaDeNotas
Aplicación en Java para gestionar cursos, estudiantes, evaluaciones, notas, promedios y estadísticas académicas.

## Cómo ejecutar

Requisitos: JDK 17 o superior.

Desde la carpeta raíz del proyecto (en bash / Git Bash):

```bash
javac -d out $(find src -name "*.java")
java -cp out app.Main
```

En CMD de Windows (no tiene `find`):

```cmd
dir /s /b src\*.java > fuentes.txt
javac -d out @fuentes.txt
java -cp out app.Main
```

Los datos se guardan en `datos/notas.txt` al elegir la opción 11 (Guardar y salir) y se cargan automáticamente al iniciar.