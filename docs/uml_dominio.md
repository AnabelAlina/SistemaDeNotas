# Diagrama de Clases UML — Sistema de Notas UNSA

```mermaid
classDiagram
    %% Paquete modelo
    class Calculable {
        <<interface>>
        +calcularPromedio() double
    }
    class Persona {
        <<abstract>>
        -String nombre
        -String apellido
        -String dni
        -String correo
        +validarDni(String) String
        +validarCorreo(String) String
        +getRol() String
    }
    class Estudiante {
        -String codigo
        -String carrera
        +validarCui(String) String
        +validarCorreoInstitucional(String) String
        +agregarCiclo(Ciclo)
        +quitarCiclo(String) boolean
        +buscarCiclo(String) Ciclo
        +getCicloActual() Ciclo
    }
    class Docente {
        -String especialidad
        +agregarCurso(Curso)
    }
    class Ciclo {
        -String nombre
        -int anio
        +agregarCurso(Curso)
        +agregarCursosPredeterminados(double) int
        +agregarCursoManual(...) Curso
        +quitarCurso(String) boolean
        +buscarCurso(String) Curso
    }
    class Curso {
        -String codigo
        -String nombre
        -int creditos
        -double metaPromedio
        +agregarEvaluacion(Evaluacion)
        +getSustitutorio() ExamenSustitutorio
        +obtenerOCrearSustitutorio() ExamenSustitutorio
        +estaAprobado() boolean
        +notaNecesaria() double
    }

    %% Paquete evaluaciones
    class Evaluacion {
        <<abstract>>
        -String nombre
        -double peso
        -double nota
        -LocalDate fecha
        -boolean rendida
        +setNota(double)
        +getNotaPonderada() double
        +getTipo() String
    }
    class Examen {
        -int numero
    }
    class Continua {
        -int numero
        +agregarTrabajo(TrabajoContinua)
        +quitarTrabajo(TrabajoContinua) boolean
        +isCompleta() boolean
    }
    class TrabajoContinua {
        -String nombre
        -double peso
        -double nota
        -boolean rendido
        -boolean esGrupal
        +setNota(double)
    }
    class ExamenSustitutorio

    %% Paquete calculos y plan
    class EstrategiaPromedio {
        <<interface>>
        +calcular(ArrayList~Evaluacion~) double
    }
    class PromedioPonderado
    class PromedioSimple
    class NotasEfectivas {
        +calcular(ArrayList~Evaluacion~) LinkedHashMap
    }
    class SimuladorNotas {
        +simularNota(Curso, double) double
        +notaNecesaria(Curso, double) double
        +esPosibleAprobar(Curso) boolean
    }
    class EstadisticasNotas {
        +calcularFrecuencias(ArrayList~Evaluacion~)
        +getModa() double
        +getMediana() double
        +getDesviacion() double
        +getTendencia() String
    }
    class EvaluadorCurso {
        +estadoDe(Curso) EstadoCurso
        +distribucionPorRangos(Curso) LinkedHashMap
    }
    class EstadoCurso {
        <<enumeration>>
        APROBADO
        EN_RIESGO
        DESAPROBADO
    }
    class PlanEstudios {
        +getCursosPrimerAnioSegundoSemestre() ArrayList
        +crearCurso(...) Curso
    }

    %% Paquete persistencia, exportacion, app y gui
    class Repositorio {
        <<interface>>
        +guardar(ArrayList~Estudiante~)
        +cargar() ArrayList~Estudiante~
    }
    class RepositorioArchivo
    class Exportable {
        <<interface>>
        +exportar(Estudiante, String)
    }
    class ExportadorCSV
    class GestorNotas {
        -Estudiante actual
        +agregarEstudiante(Estudiante)
        +asignarNota(Curso, Evaluacion, double)
        +asignarNotaTrabajo(TrabajoContinua, double)
        +guardar()
        +cargar()
    }
    class VentanaPrincipal
    class VentanaAcademica
    class VentanaNotas
    class VentanaAnalisis
    class SelectorCurso
    class Estilo

    %% Relaciones
    Persona <|-- Estudiante
    Persona <|-- Docente
    Calculable <|.. Estudiante
    Calculable <|.. Ciclo
    Calculable <|.. Curso
    Estudiante "1" *-- "0..*" Ciclo : composición
    Ciclo "1" *-- "0..*" Curso : composición
    Curso "1" *-- "6..7" Evaluacion : composición
    Docente "1" o-- "0..*" Curso : agregación

    Evaluacion <|-- Examen
    Evaluacion <|-- Continua
    Evaluacion <|-- ExamenSustitutorio
    Continua "1" *-- "0..*" TrabajoContinua : composición

    EstrategiaPromedio <|.. PromedioPonderado
    EstrategiaPromedio <|.. PromedioSimple
    Curso o-- EstrategiaPromedio : Strategy
    Curso ..> PlanEstudios : crea desde

    Repositorio <|.. RepositorioArchivo
    Exportable <|.. ExportadorCSV
    GestorNotas o-- Repositorio
    GestorNotas "1" o-- "0..*" Estudiante

    VentanaPrincipal ..> GestorNotas
    VentanaPrincipal ..> Exportable
    VentanaPrincipal ..> VentanaAcademica
    VentanaPrincipal ..> VentanaNotas
    VentanaPrincipal ..> VentanaAnalisis
    VentanaAcademica *-- SelectorCurso
    VentanaNotas *-- SelectorCurso
    VentanaAnalisis *-- SelectorCurso
    VentanaAnalisis ..> EvaluadorCurso
    EvaluadorCurso ..> EstadoCurso