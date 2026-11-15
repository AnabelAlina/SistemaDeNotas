package evaluaciones;

import java.util.ArrayList;
import excepciones.PesoInvalidoException;

// Estudiante 2 - Ayelen Choquecota - Evaluacion continua de un curso UNSA: Continua 1, 2 o 3.
// Se compone de trabajos (TrabajoContinua) que valen un % interno; esos % suman 100.
// Hereda de Evaluacion: su "peso" es el % continua del silabo (sobre la nota final del curso)
// y su nota NO se escribe a mano: se calcula con los trabajos (polimorfismo: getNota() sobrescrito).
// Concepto: herencia, composicion con TrabajoContinua, ArrayList, sobrescritura.
//
// Regla: la continua cuenta como "rendida" apenas hay un trabajo con nota; su nota es el promedio
// ponderado de los trabajos rendidos. Cuando todos estan rendidos y suman 100%, isCompleta() es true.
//
// NOTA para Estudiante 3 (solo si hace falta) - en el menu:
//   continua.agregarTrabajo(new TrabajoContinua(nombre, pesoInterno, esGrupal)); // atrapar PesoInvalidoException
//   trabajo.setNota(nota);                                                        // atrapar NotaInvalidaException
//   Para guardar/cargar: recorrer continua.getTrabajos() (nombre, peso, esGrupal, isRendido, getNota).
//   No llamar continua.setNota(...): lanza UnsupportedOperationException (la nota sale de los trabajos).
public class Continua extends Evaluacion {
    private static final double PESO_INTERNO_TOTAL = 100.0;
    private static final double TOLERANCIA = 0.0001;

    private int numero; // 1, 2 o 3
    private ArrayList<TrabajoContinua> trabajos;

    public Continua(int numero, double peso) {
        super("Continua " + validarNumero(numero), peso, null);
        this.numero = numero;
        this.trabajos = new ArrayList<>();
    }

    private static int validarNumero(int numero) {
        if (numero < 1 || numero > 3) {
            throw new IllegalArgumentException("El numero de continua debe ser 1, 2 o 3.");
        }
        return numero;
    }

    public int getNumero() { return numero; }

    // Copia de la lista, para no modificarla desde fuera sin validar
    public ArrayList<TrabajoContinua> getTrabajos() {
        return new ArrayList<>(trabajos);
    }

    // Rechaza el trabajo si los pesos internos pasarian de 100%
    public void agregarTrabajo(TrabajoContinua trabajo) throws PesoInvalidoException {
        if (trabajo == null) {
            throw new IllegalArgumentException("El trabajo no puede ser nulo.");
        }
        if (getPesoInterno() + trabajo.getPeso() > PESO_INTERNO_TOTAL + TOLERANCIA) {
            throw new PesoInvalidoException("Los trabajos de " + getNombre() + " superan el 100%. Peso actual: "
                + getPesoInterno() + "%, intentas agregar: " + trabajo.getPeso() + "%.");
        }
        trabajos.add(trabajo);
    }

    // Suma de los % internos de los trabajos registrados
    public double getPesoInterno() {
        double total = 0;
        for (TrabajoContinua t : trabajos) {
            total += t.getPeso();
        }
        return total;
    }

    // True si los trabajos suman 100% y todos tienen nota (la continua ya es definitiva)
    public boolean isCompleta() {
        if (Math.abs(getPesoInterno() - PESO_INTERNO_TOTAL) > TOLERANCIA) {
            return false;
        }
        for (TrabajoContinua t : trabajos) {
            if (!t.isRendido()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isRendida() {
        for (TrabajoContinua t : trabajos) {
            if (t.isRendido()) {
                return true;
            }
        }
        return false;
    }

    // Promedio ponderado de los trabajos rendidos: suma(nota x peso) / suma(pesos rendidos)
    @Override
    public double getNota() {
        double suma = 0;
        double pesoRendido = 0;
        for (TrabajoContinua t : trabajos) {
            if (t.isRendido()) {
                suma += t.getNota() * t.getPeso();
                pesoRendido += t.getPeso();
            }
        }
        return pesoRendido == 0 ? 0 : suma / pesoRendido;
    }

    @Override
    public void setNota(double nota) {
        throw new UnsupportedOperationException(
            "La nota de " + getNombre() + " se calcula con sus trabajos: registra la nota de cada trabajo.");
    }

    @Override
    public String getTipo() {
        return "Continua";
    }
}
