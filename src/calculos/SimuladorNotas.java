package calculos;

import java.time.LocalDate;
import java.util.ArrayList;
import evaluaciones.Evaluacion;
import excepciones.NotaInvalidaException;
import modelo.Curso;

// Estudiante 2 - Ayelen Choquecota - Simulador: promedio si saco X (simularNota), nota necesaria para una meta
// (notaNecesaria) y si todavia es posible aprobar (esPosibleAprobar).
//
// NOTA para Estudiante 3 (solo si hace falta) - en el menu:
//  - simularNota(...) lanza NotaInvalidaException si la nota no esta entre 0 y 20: atrapala con try/catch.
//  - Antes de mostrar la nota necesaria usa hayEvaluacionesPendientes(curso); si no hay pendientes, avisalo.
//  - Si notaNecesaria(...) da mayor que 20, mostrar que es imposible llegar a esa meta.
// Simulador: "y si saco X?" y "cuanto necesito sacar?". Nunca modifica el curso.
// Los pesos se toman tal como estan registrados en el curso.
public class SimuladorNotas {
    private static final double MAXIMO = Evaluacion.NOTA_MAXIMA;

    // Evaluacion temporal que solo existe dentro de una simulacion
    private static class Simulada extends Evaluacion {
        Simulada(String nombre, double peso, LocalDate fecha) {
            super(nombre, peso, fecha);
        }
        @Override
        public String getTipo() {
            return "Simulada";
        }
    }

    // Promedio ponderado que quedaria si la proxima evaluacion pendiente saliera con "nota".
    // Si no queda nada pendiente, devuelve el promedio actual.
    public double simularNota(Curso curso, double nota) throws NotaInvalidaException {
        Evaluacion proxima = buscarProximaPendiente(curso);
        if (proxima == null) {
            return curso.calcularPromedio();
        }
        ArrayList<Evaluacion> copia = new ArrayList<>(curso.getEvaluaciones());
        Simulada simulada = new Simulada(proxima.getNombre(), proxima.getPeso(), proxima.getFecha());
        simulada.setNota(nota); // lanza NotaInvalidaException si no esta entre 0 y 20
        copia.set(copia.indexOf(proxima), simulada);
        return new PromedioPonderado().calcular(copia);
    }

    // Nota que se necesita, en promedio, en TODAS las evaluaciones pendientes para llegar a "meta":
    //   (meta x pesoTotal - acumulado) / pesoRestante
    // - Si da mayor que 20, es imposible llegar a la meta.
    // - Si ya alcanzaste la meta, devuelve 0.
    // - Si no queda nada pendiente, devuelve 0 (usa hayEvaluacionesPendientes() para distinguirlo).
    public double notaNecesaria(Curso curso, double meta) {
        double acumulado = 0;
        double pesoRestante = 0;
        double pesoTotal = 0;
        for (Evaluacion e : curso.getEvaluaciones()) {
            pesoTotal += e.getPeso();
            if (e.isRendida()) {
                acumulado += e.getNota() * e.getPeso();
            } else {
                pesoRestante += e.getPeso();
            }
        }
        if (pesoRestante == 0) {
            return 0;
        }
        double necesaria = (meta * pesoTotal - acumulado) / pesoRestante;
        return Math.max(0, necesaria);
    }

    public boolean hayEvaluacionesPendientes(Curso curso) {
        return buscarProximaPendiente(curso) != null;
    }

    // True si aun es matematicamente posible alcanzar la nota minima aprobatoria
    public boolean esPosibleAprobar(Curso curso) {
        if (!hayEvaluacionesPendientes(curso)) {
            return curso.estaAprobado();
        }
        return notaNecesaria(curso, Curso.NOTA_MINIMA_APROBATORIA) <= MAXIMO;
    }

    // La primera evaluacion sin rendir, segun el orden de la lista
    private Evaluacion buscarProximaPendiente(Curso curso) {
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (!e.isRendida()) {
                return e;
            }
        }
        return null;
    }
}
