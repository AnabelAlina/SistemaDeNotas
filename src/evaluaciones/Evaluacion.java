package evaluaciones;

import java.io.Serializable;
import java.time.LocalDate;
import excepciones.NotaInvalidaException;

// Clase abstracta base de todas las evaluaciones
// (nombre, peso, nota, fecha, rendida). Concepto: clase abstracta + encapsulamiento.
// Los pesos se manejan en porcentaje (0 a 100) y las notas en escala 0 a 20.
public abstract class Evaluacion implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final double NOTA_MINIMA = 0;
    public static final double NOTA_MAXIMA = 20;

    private String nombre;
    private double peso;
    private double nota;
    private LocalDate fecha;
    private boolean rendida;

    public Evaluacion(String nombre, double peso, LocalDate fecha) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la evaluacion no puede estar vacio.");
        }
        this.nombre = nombre.trim();
        this.peso = peso;
        this.fecha = fecha;
        this.rendida = false;
    }

    public String getNombre() { return nombre; }
    public double getPeso() { return peso; }
    public double getNota() { return nota; }
    public LocalDate getFecha() { return fecha; }
    public boolean isRendida() { return rendida; }

    // Permite restaurar la fecha al cargar un archivo (puede ser null si no tiene fecha)
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    // Valida que la nota este entre 0 y 20 (lanza NotaInvalidaException)
    // y, si es valida, la guarda y marca la evaluacion como rendida.
    public void setNota(double nota) throws NotaInvalidaException {
        if (Double.isNaN(nota) || nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new NotaInvalidaException("La nota debe estar entre " + (int) NOTA_MINIMA
                    + " y " + (int) NOTA_MAXIMA + ". Recibido: " + nota);
        }
        this.nota = nota;
        this.rendida = true;
    }

    // Cuanto aporta esta evaluacion al promedio final: nota x peso / 100.
    // Si todavia no se rindio, aporta 0.
    public double getNotaPonderada() {
        if (!isRendida()) {
            return 0;
        }
        return getNota() * peso / 100.0;
    }

    // Metodo abstracto: cada subclase lo sobrescribe (polimorfismo).
    public abstract String getTipo();

    @Override
    public String toString() {
        String estado = isRendida() ? "nota " + getNota() : "pendiente";
        return getTipo() + " " + nombre + " (" + peso + "%): " + estado;
    }
}