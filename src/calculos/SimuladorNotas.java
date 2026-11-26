package calculos;

import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import excepciones.NotaInvalidaException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
        return simularNota(curso, proxima, nota);
    }

    // Igual, pero la ventana elige CUAL evaluacion simular (una pendiente del curso).
    // Lanza IllegalArgumentException si no es del curso, si ya tiene nota o si es el sustitutorio
    // (para eso esta simularSustitutorio). Nunca modifica el curso.
    public double simularNota(Curso curso, Evaluacion evaluacion, double nota) throws NotaInvalidaException {
        if (curso == null || evaluacion == null) {
            throw new IllegalArgumentException("Falta el curso o la evaluacion a simular.");
        }
        int posicion = curso.getEvaluaciones().indexOf(evaluacion);
        if (posicion < 0) {
            throw new IllegalArgumentException("Esa evaluacion no es de este curso.");
        }
        if (NotasEfectivas.esOpcional(evaluacion)) {
            throw new IllegalArgumentException("El sustitutorio se simula con simularSustitutorio.");
        }
        if (evaluacion.isRendida()) {
            throw new IllegalArgumentException(evaluacion.getNombre() + " ya tiene nota: elige una pendiente.");
        }
        ArrayList<Evaluacion> copia = new ArrayList<>(curso.getEvaluaciones());
        Simulada simulada = new Simulada(evaluacion.getNombre(), evaluacion.getPeso(), evaluacion.getFecha());
        simulada.setNota(nota); // lanza NotaInvalidaException si no esta entre 0 y 20
        copia.set(posicion, simulada);
        return new PromedioPonderado().calcular(copia);
    }

    // Promedio que quedaria si se rindiera el sustitutorio con "nota" (reemplaza la mas baja del
    // Examen 1 o 2, solo si la mejora). Si el curso ya tenia sustitutorio, se simula en su lugar.
    public double simularSustitutorio(Curso curso, double nota) throws NotaInvalidaException {
        if (curso == null) {
            throw new IllegalArgumentException("El curso no puede ser nulo.");
        }
        ArrayList<Evaluacion> copia = new ArrayList<>();
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (!(e instanceof ExamenSustitutorio)) {
                copia.add(e);
            }
        }
        ExamenSustitutorio simulado = new ExamenSustitutorio();
        simulado.setNota(nota); // lanza NotaInvalidaException si no esta entre 0 y 20
        copia.add(simulado);
        return new PromedioPonderado().calcular(copia);
    }

    // Nota que se necesita en el sustitutorio para que el promedio actual llegue a "meta".
    // Con m = la nota mas baja entre Examen 1 y 2, w = su peso, P = peso ya rendido y prom = promedio actual:
    //   X = m + (meta - prom) x P / w
    // Resultado:
    //  - NaN: no aplica (no hay Examen 1 ni 2 rendido, o el sustitutorio ya se rindio)
    //  - 0: no hace falta rendirlo (con X menor o igual que m la nota no mejora)
    //  - mayor que 20: imposible llegar a la meta con el sustitutorio
    //  - otro valor: la nota que hay que sacar
    public double notaNecesariaSustitutorio(Curso curso, double meta) {
        ExamenSustitutorio sustitutorio = curso.getSustitutorio();
        if (sustitutorio != null && sustitutorio.isRendida()) {
            return Double.NaN;
        }
        double suma = 0;
        double pesoRendido = 0;
        Evaluacion masBajo = null;
        double notaMasBaja = 0;
        for (java.util.Map.Entry<Evaluacion, Double> par : NotasEfectivas.calcular(curso.getEvaluaciones()).entrySet()) {
            Evaluacion e = par.getKey();
            double nota = par.getValue();
            suma += nota * e.getPeso();
            pesoRendido += e.getPeso();
            if (e instanceof Examen && ((Examen) e).getNumero() <= 2 && (masBajo == null || nota < notaMasBaja)) {
                masBajo = e;
                notaMasBaja = nota;
            }
        }
        if (masBajo == null || pesoRendido == 0) {
            return Double.NaN;
        }
        double promedio = suma / pesoRendido;
        double necesaria = notaMasBaja + (meta - promedio) * pesoRendido / masBajo.getPeso();
        return necesaria <= notaMasBaja ? 0 : necesaria;
    }

    // Peso (en %) del curso que todavia falta ganar, sin contar el sustitutorio:
    //  - Examen sin rendir: su peso entero. Examen rendido: 0.
    //  - Continua: peso x (100 - p) / 100, con p = suma de los % internos de sus trabajos ya calificados.
    //    Una continua sin trabajos esta toda pendiente.
    public double pesoPendiente(Curso curso) {
        double pendiente = 0;
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (NotasEfectivas.esOpcional(e)) {
                continue;
            }
            pendiente += e.getPeso() - pesoGanadoDe(e);
        }
        return pendiente;
    }

    // Puntos que ya estan asegurados en la nota final: suma de (nota efectiva x peso ganado / 100).
    // Usa NotasEfectivas, asi que ya cuenta el sustitutorio.
    public double puntosGanados(Curso curso) {
        LinkedHashMap<Evaluacion, Double> efectivas = NotasEfectivas.calcular(curso.getEvaluaciones());
        double puntos = 0;
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (NotasEfectivas.esOpcional(e) || !efectivas.containsKey(e)) {
                continue;
            }
            puntos += efectivas.get(e) * pesoGanadoDe(e) / 100.0;
        }
        return puntos;
    }

    // Peso que una evaluacion ya aporto al curso (0 si no tiene nada calificado)
    private double pesoGanadoDe(Evaluacion e) {
        if (e instanceof Continua) {
            double p = 0; // suma de % internos de los trabajos calificados
            for (evaluaciones.TrabajoContinua t : ((Continua) e).getTrabajos()) {
                if (t.isRendido()) {
                    p += t.getPeso();
                }
            }
            return e.getPeso() * p / 100.0;
        }
        return e.isRendida() ? e.getPeso() : 0;
    }

    // Nota que se necesita, en promedio, en TODO lo que falta para llegar a "meta":
    //   (meta x 100 - suma de nota x peso ganado) / pesoPendiente
    // - Si da mayor que 20, es imposible llegar a la meta.
    // - Si ya alcanzaste la meta, devuelve 0.
    // - Si no queda nada pendiente, devuelve 0 (usa hayEvaluacionesPendientes() para distinguirlo).
    public double notaNecesaria(Curso curso, double meta) {
        double pendiente = pesoPendiente(curso);
        if (pendiente < 0.0001) {
            return 0;
        }
        double necesaria = (meta * 100 - puntosGanados(curso) * 100) / pendiente;
        return Math.max(0, necesaria);
    }

    // True si falta rendir algo o si alguna continua todavia esta incompleta
    public boolean hayEvaluacionesPendientes(Curso curso) {
        if (buscarProximaPendiente(curso) != null) {
            return true;
        }
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (e instanceof Continua && !((Continua) e).isCompleta()) {
                return true;
            }
        }
        return false;
    }

    // True si aun es matematicamente posible alcanzar la nota minima aprobatoria
    public boolean esPosibleAprobar(Curso curso) {
        if (!hayEvaluacionesPendientes(curso)) {
            return curso.estaAprobado();
        }
        return notaNecesaria(curso, Curso.NOTA_MINIMA_APROBATORIA) <= MAXIMO;
    }

    // La primera evaluacion sin rendir, segun el orden de la lista (ignora el sustitutorio, que es opcional)
    private Evaluacion buscarProximaPendiente(Curso curso) {
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (!e.isRendida() && !NotasEfectivas.esOpcional(e)) {
                return e;
            }
        }
        return null;
    }
}