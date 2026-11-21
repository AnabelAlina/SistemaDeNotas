package calculos;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import evaluaciones.Evaluacion;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
// Anabel Quispe (Estudiante 2) - Evalua un curso: estado (aprobado, en riesgo, desaprobado), distribucion de notas
// por rangos y estadisticas de un ciclo o acumuladas. No imprime nada ni modifica el curso.
//
// Uso en una ventana:
//   EvaluadorCurso evaluador = new EvaluadorCurso();
//   EstadoCurso estado = evaluador.estadoDe(curso);   // null si todavia no hay notas
//   EstadisticasNotas est = evaluador.estadisticasDeCiclo(ciclo);   // ya calculadas
public class EvaluadorCurso {
    // Limites de los rangos de la distribucion
    private static final double LIMITE_1 = 10.5;
    private static final double LIMITE_2 = 14;
    private static final double LIMITE_3 = 17;
    private SimuladorNotas simulador = new SimuladorNotas();
    // Estado del curso con las reglas del grupo:
    //  - sin notas: no se calcula (devuelve null; la ventana dice "Todavia no hay notas")
    //  - APROBADO: el promedio actual es 10.5 o mas
    //  - DESAPROBADO: ya es matematicamente imposible llegar a 10.5, o el curso termino por debajo
    //  - EN_RIESGO: promedio menor que 10.5 pero todavia es posible aprobar
    public EstadoCurso estadoDe(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("El curso no puede ser nulo.");
        }
        if (!curso.tieneNotas()) {
            return null;
        }
        if (curso.calcularPromedio() >= Curso.NOTA_MINIMA_APROBATORIA) {
            return EstadoCurso.APROBADO;
        }
        // Promedio menor que 10.5: si no queda nada por rendir o ya es imposible, esta desaprobado
        if (!simulador.hayEvaluacionesPendientes(curso) || !simulador.esPosibleAprobar(curso)) {
            return EstadoCurso.DESAPROBADO;
        }
        return EstadoCurso.EN_RIESGO;
    }
    // Cuenta las notas que cuentan del curso (ya con el sustitutorio) en cuatro rangos.
    // El orden del mapa es el de los rangos, de menor a mayor.
    public LinkedHashMap<String, Integer> distribucionPorRangos(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("El curso no puede ser nulo.");
        }
        LinkedHashMap<String, Integer> rangos = new LinkedHashMap<>();
        rangos.put("0 a 10.49", 0);
        rangos.put("10.5 a 13.99", 0);
        rangos.put("14 a 16.99", 0);
        rangos.put("17 a 20", 0);
        for (double nota : notasEfectivas(curso)) {
            String clave;
            if (nota < LIMITE_1) {
                clave = "0 a 10.49";
            } else if (nota < LIMITE_2) {
                clave = "10.5 a 13.99";
            } else if (nota < LIMITE_3) {
                clave = "14 a 16.99";
            } else {
                clave = "17 a 20";
            }
            rangos.put(clave, rangos.get(clave) + 1);
        }
        return rangos;
    }
    // Notas que cuentan de UN curso (el sustitutorio se aplica dentro de su propio curso)
    public ArrayList<Double> notasEfectivas(Curso curso) {
        return new ArrayList<>(NotasEfectivas.calcular(curso.getEvaluaciones()).values());
    }
    // Notas que cuentan de todos los cursos de un ciclo, calculadas curso por curso
    // (asi el sustitutorio de un curso nunca reemplaza notas de otro)
    public ArrayList<Double> notasEfectivasDeCiclo(Ciclo ciclo) {
        if (ciclo == null) {
            throw new IllegalArgumentException("El ciclo no puede ser nulo.");
        }
        ArrayList<Double> notas = new ArrayList<>();
        for (Curso curso : ciclo.getCursos()) {
            notas.addAll(notasEfectivas(curso));
        }
        return notas;
    }
    // Notas que cuentan de todos los ciclos del estudiante, curso por curso
    public ArrayList<Double> notasEfectivasAcumuladas(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        ArrayList<Double> notas = new ArrayList<>();
        for (Ciclo ciclo : estudiante.getCiclos()) {
            notas.addAll(notasEfectivasDeCiclo(ciclo));
        }
        return notas;
    }
    // Estadisticas de un ciclo, ya calculadas (revisar hayDatos() antes de mostrar)
    public EstadisticasNotas estadisticasDeCiclo(Ciclo ciclo) {
        EstadisticasNotas estadisticas = new EstadisticasNotas();
        estadisticas.calcularDesdeNotas(notasEfectivasDeCiclo(ciclo));
        return estadisticas;
    }
    // Estadisticas acumuladas de todos los ciclos del estudiante, ya calculadas
    public EstadisticasNotas estadisticasAcumuladas(Estudiante estudiante) {
        EstadisticasNotas estadisticas = new EstadisticasNotas();
        estadisticas.calcularDesdeNotas(notasEfectivasAcumuladas(estudiante));
        return estadisticas;
    }
}