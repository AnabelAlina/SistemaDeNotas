package calculos;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
// Evalua un curso: estado (aprobado, vas aprobando, en riesgo, desaprobado), distribucion de notas
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
        // Margen para comparar decimales sin errores de redondeo
    private static final double TOLERANCIA = 0.0001;
    // Estado del curso. Con puntosGanados (lo ya asegurado), pesoPendiente (lo que falta) y
    // puntosPosibles = puntosGanados + pesoPendiente x 20 / 100 (lo maximo que aun se puede llegar a tener):
    //  - sin notas: null (la ventana dice "Todavia no hay notas")
    //  - APROBADO: no queda nada pendiente y el promedio es 10.5 o mas, o los puntos ganados ya son 10.5 o mas
    //  - DESAPROBADO: no queda nada pendiente y el promedio es menor que 10.5, o los puntos posibles son menos de 10.5
    //  - VAS_APROBANDO: el promedio de lo rendido es 10.5 o mas, pero todavia no esta asegurado
    //  - EN_RIESGO: en los demas casos
    public EstadoCurso estadoDe(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("El curso no puede ser nulo.");
        }
        if (!curso.tieneNotas()) {
            return null;
        }
        double minimo = Curso.NOTA_MINIMA_APROBATORIA;
        double promedio = curso.calcularPromedio();
        double ganados = simulador.puntosGanados(curso);
        double pendiente = simulador.pesoPendiente(curso);
        double posibles = ganados + pendiente * 20 / 100.0;
        boolean nadaPendiente = pendiente < TOLERANCIA;

        if ((nadaPendiente && promedio >= minimo - TOLERANCIA) || ganados >= minimo - TOLERANCIA) {
            return EstadoCurso.APROBADO;
        }
        if (nadaPendiente || posibles < minimo - TOLERANCIA) {
            return EstadoCurso.DESAPROBADO;
        }
        if (promedio >= minimo - TOLERANCIA) {
            return EstadoCurso.VAS_APROBANDO;
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